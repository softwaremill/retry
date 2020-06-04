package retry

import java.util.Random

import scala.concurrent.duration._
import language.postfixOps

trait Jitter {

  def apply(
    start: FiniteDuration,
    last: FiniteDuration,
    attempt: Int): FiniteDuration =
      next(last, convert(start, last.unit), attempt)

  protected def next(
    start: FiniteDuration,
    last: FiniteDuration,
    attempt: Int): FiniteDuration

  protected def convert(
    dur: Duration,
    unit: TimeUnit): FiniteDuration =
      FiniteDuration(dur.toUnit(unit).toLong, unit)

  protected def capped(
    input: FiniteDuration,
    cap: Duration)
    (op: Long => Long): FiniteDuration = {
      if (!cap.isFinite) {
        Duration(op(input.length), input.unit)
      } else {
        val ceil = convert(cap, input.unit)
        val value = op(input.length)
        if (value >= ceil.length) ceil
        else Duration(value, input.unit)
      }
    }

  protected def pow(
    start: Long,
    base: Long,
    attempt: Long): Long = {
      val temp = start * math.pow(base, attempt)
      if (temp < 0.0 || temp > Long.MaxValue.toDouble) Long.MaxValue
      else temp.toLong
    }

  protected def cappedPow(
    start: FiniteDuration,
    cap: Duration,
    base: Long,
    attempt: Long): FiniteDuration =
      capped(start, cap) { len =>
        pow(len, base, attempt) }
}

/** The algorithms here were inspired by this article:
  * https://www.awsarchitectureblog.com/2015/03/backoff.html */
object Jitter {

  /** Given a lower and upper bound (inclusive) generate a random
    * number within those bounds */
  type RandomSource = (Long, Long) => Long

  /** Create a RandomSource from an instance of java.util.Random
    * Please be mindful of the call-by-name semantics */
  def randomSource(random: => Random): RandomSource =
    { (l, u) =>
      val (_l, _u) = if (l < u) (l, u) else (u, l)
      nextLong(random, (_u - _l) + 1) + _l
    }

  /** Simple exponential backoff + cap */
  def none(
    cap: Duration = Defaults.cap,
    base: Int = 2): Jitter =
      new Jitter {
        protected def next(
          start: FiniteDuration,
          last: FiniteDuration,
          attempt: Int): FiniteDuration =
            cappedPow(start, cap, base, attempt)

        override def toString = s"retry.Jitter.none($cap, $base)"
      }

  /** Normal exponential backoff + cap + random jitter */
  def full(
    cap: Duration = Defaults.cap,
    random: RandomSource = Defaults.random,
    base: Int = 2): Jitter =
      new Jitter {
        protected def next(
          start: FiniteDuration,
          last: FiniteDuration,
          attempt: Int): FiniteDuration = {
            val temp = cappedPow(start, cap, base, attempt)
            Duration(random(0, temp.length), temp.unit)
          }

        override def toString = s"retry.Jitter.full($cap, $random, $base)"
      }

  /** Always keep some of the backoff and jitter by a smaller amount (prevents very short sleeps) */
  def equal(
    cap: Duration = Defaults.cap,
    random: RandomSource = Defaults.random,
    base: Int = 2): Jitter =
      new Jitter {
        protected def next(
          start: FiniteDuration,
          last: FiniteDuration,
          attempt: Int): FiniteDuration = {
            val temp = cappedPow(start, cap, base, attempt)
            Duration(
              temp.length / 2 + random(0, temp.length / 2),
              temp.unit)
          }

        override def toString = s"retry.Jitter.equal($cap, $random, $base)"
      }

  /** similar to full, but we also increase the maximum jitter startd on the last random value. */
  def decorrelated(
    cap: Duration = Defaults.cap,
    random: RandomSource = Defaults.random,
    base: Int = 3): Jitter =
      new Jitter {
        protected def next(
          start: FiniteDuration,
          last: FiniteDuration,
          attempt: Int): FiniteDuration =
            capped(start, cap) { len =>
              random(len, last.length * base) }

        override def toString = s"retry.Jitter.decorrelated($cap, $random, $base)"
      }

  private def nextLong(
    random: Random,
    n: Long): Long = {
      if (n <= 0L) throw new IllegalArgumentException()

      // for small n use nextInt and cast
      if (n <= Int.MaxValue) random.nextInt(n.toInt).toLong
      else {
        // for large n use nextInt for both high and low ints
        val highLimit = (n >> 32).toInt
        val high = random.nextInt(highLimit).toLong << 32
        val low = random.nextInt().toLong & 0xffffffffL
        high | low
      }
    }
}
