package retry

import java.security.SecureRandom
import java.util.Random
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import org.scalatest.FunSpec
import scala.concurrent.duration._
import language.postfixOps

class JitterSpec extends FunSpec {
  //val rng = new SecureRandom()
  val rng = new java.util.Random()
  val rand = Jitter.randomSource(rng)
  val cap = 2000 milliseconds

  def testJitter(jitter: Jitter)(): Unit = {
    val min = rand(1, 100) milliseconds
    var sleep = rand(1, 1000) milliseconds

    for (i <- 0 until 10000) {
      val delay = sleep
      sleep = jitter(delay, min, i+1)
      assert(sleep.unit === TimeUnit.MILLISECONDS)
      assert(sleep.length >= 0)
      assert(sleep.length <= cap.length)
    }
  }

  describe("retry.Defaults.random") {
    it ("should return sane random values") {
      for (i <- 0 until 1000) {
        val result = rand(0, 10)
        assert(result >= 0)
        assert(result <= 10)
      }
    }

    it ("should handle swapped bounds") {
      for (i <- 0 until 1000) {
        val result = rand(10, 0)
        assert(result >= 0)
        assert(result <= 10)
      }
    }

    it ("should not cache random values") {
      val counter = new AtomicInteger()
      val rng = new Random() {
        override def nextInt(): Int =
          counter.addAndGet(1)
        override def nextInt(n: Int): Int =
          counter.addAndGet(1)
      }

      val rand = Jitter.randomSource(rng)
      rand(0, 100)
      rand(0, Int.MaxValue.toLong + 1L)
      assert(counter.get() === 3)
    }
  }

  describe("retry.Jitter.none") {
    it ("should perform backoff correctly") {
      testJitter(Jitter.none(cap))
    }
  }

  describe("retry.Jitter.decorrelated") {
    it ("should perform decorrelated jitter correctly") {
      testJitter(Jitter.decorrelated(cap))
    }
  }

  describe("retry.Jitter.full") {
    it ("should perform full jitter correctly") {
      testJitter(Jitter.full(cap))
    }
  }

  describe("retry.Jitter.equal") {
    it ("should perform equal jitter correctly") {
      testJitter(Jitter.equal(cap))
    }
  }

  describe("retry.Defaults.jitter") {
    it("should work") {
      assert(Defaults.jitter(100.millis, 300.millis, 3) <= Defaults.cap)
    }
  }
}
