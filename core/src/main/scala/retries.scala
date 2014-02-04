package retry

import scala.concurrent.{ Future, ExecutionContext }
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

/** Retry immediately after failure */
object Directly extends CountingRetry {
  def apply[T](max: Int = 3)(promise: () => Future[T])
              (implicit success: Success[T],
               executor: ExecutionContext): Future[T] = {
    retry(max, promise, success, Directly(_)(promise))
  }
}

/** Retry with a pause between attempts */
object Pause extends CountingRetry {
  def apply[T](max: Int = 4,
               delay: Duration = Duration(500, TimeUnit.MILLISECONDS))
              (promise: () => Future[T])
              (implicit success: Success[T],
               timer: Timer,
               executor: ExecutionContext): Future[T] = {
    retry(max,
          promise,
          success,
          c => SleepFuture(delay) {
            Pause(c, delay)(promise)
          }.flatMap(identity))
   }
}

/** Retry with exponential backoff */
object Backoff extends CountingRetry {
  def apply[T](max: Int = 8,
               delay: Duration = Duration(500, TimeUnit.MILLISECONDS),
               base: Int = 2)
              (promise: () => Future[T])
              (implicit success: Success[T],
               timer: Timer,
               executor: ExecutionContext): Future[T] = {
    retry(max,
          promise,
          success,
          count => SleepFuture(delay) {
            Backoff(count, 
                    Duration(delay.length * base, delay.unit),
                    base)(promise)
          }.flatMap(identity))
  }
}

trait CountingRetry {
  /** Applies the given function and will retry up to `max` times,
      until a successful result is produced. */
  protected def retry[T](max: Int,
                         promise: () => Future[T],
                         success: Success[T],
                         orElse: Int => Future[T]
                       )(implicit executor: ExecutionContext): Future[T] = {
    val fut = promise()
    fut.flatMap { res =>
      if (max < 1 || success.predicate(res)) fut
      else orElse(max - 1)    
    } recoverWith {
      case e: Throwable => if (max < 1) fut else orElse(max - 1)
    }    
  }
}

