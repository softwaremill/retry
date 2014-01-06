package retry

import odelay.{ Delay, Timer }
import scala.concurrent.{ Future, ExecutionContext }
import scala.concurrent.duration.{ Duration, FiniteDuration }
import java.util.concurrent.TimeUnit

/** Retry immediately after failure */
object Directly extends Policy {
  def apply[T](
    max: Int = 3)
    (promise: () => Future[T])
    (implicit success: Success[T],
     executor: ExecutionContext): Future[T] =
       retry(max, promise, success, Directly(_)(promise))
}

/** Retry with a pause between attempts */
object Pause extends Policy {
  def apply[T](
    max: Int = 4,
    delay: FiniteDuration = Defaults.delay)
    (promise: () => Future[T])
    (implicit success: Success[T],
     timer: Timer,
     executor: ExecutionContext): Future[T] =
    retry(max,
          promise,
          success,
          c => Delay(delay)(Pause(c, delay)(promise)).future.flatMap(identity))

}

/** Retry with exponential backoff */
object Backoff extends Policy {
  def apply[T](
    max: Int = 8,
    delay: FiniteDuration = Defaults.delay,
    base: Int = 2)
    (promise: () => Future[T])
    (implicit success: Success[T],
     timer: Timer,
     executor: ExecutionContext): Future[T] =
    retry(max,
          promise,
          success,
          count => Delay(delay) {
            Backoff(count, 
                    Duration(delay.length * base, delay.unit),
                    base)(promise)
          }.future.flatMap(identity))
}

trait Policy {
  /** Applies the given function and will retry up to `max` times,
      until a successful result is produced. */
  protected def retry[T](
    max: Int,
    promise: () => Future[T],
    success: Success[T],
    orElse: Int => Future[T])
    (implicit executor: ExecutionContext) = {
    val fut = promise()
    fut.flatMap { res =>
      if (max < 1 || success.predicate(res)) fut
      else orElse(max - 1)
    }
  }
}

