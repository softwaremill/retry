package retry

import odelay.{ Delay, Timer }
import scala.concurrent.{ Future, ExecutionContext }
import scala.concurrent.duration.{ Duration, FiniteDuration }
import java.util.concurrent.TimeUnit

object Directly extends CountingPolicy {

  /** Retry immediately after failure forever */
  def forever[T](
    delay: FiniteDuration = Defaults.delay)
    (promise: () => Future[T])
    (implicit success: Success[T],
     executor: ExecutionContext): Future[T] =
     retry(promise, promise)

  /** Retry immediately after failure for a max number of times */
  def apply[T](
    max: Int = 3)
    (promise: () => Future[T])
    (implicit success: Success[T],
     executor: ExecutionContext): Future[T] =
     countdown(max, promise, Directly(_)(promise))
}


object Pause extends CountingPolicy {

  /** Retry with a pause between attempts forever */
  def forever[T](
    delay: FiniteDuration = Defaults.delay)
    (promise: () => Future[T])
    (implicit success: Success[T],
     timer: Timer,
     executor: ExecutionContext): Future[T] =
     retry(promise, { () =>
       Delay(delay)(Pause.forever(delay)(promise)).future.flatMap(identity)
     })

  /** Retry with a pause between attempts for a max number of times */
  def apply[T](
    max: Int = 4,
    delay: FiniteDuration = Defaults.delay)
    (promise: () => Future[T])
    (implicit success: Success[T],
     timer: Timer,
     executor: ExecutionContext): Future[T] =
     countdown(
       max,
       promise,
       c => Delay(delay) {
         Pause(c, delay)(promise)
       }.future.flatMap(identity))

}


object Backoff extends CountingPolicy {

  /** Retry with exponential backoff forever */
  def forever[T](
    delay: FiniteDuration = Defaults.delay,
    base: Int = 2)
    (promise: () => Future[T])
    (implicit success: Success[T],
     timer: Timer,
     executor: ExecutionContext): Future[T] =
     retry(promise, { () =>
       Delay(delay) {
         Backoff.forever(Duration(delay.length * base, delay.unit), base)(promise)
       }.future.flatMap(identity)
     })

  /** Retry with exponential backoff for a max number of times */
  def apply[T](
    max: Int = 8,
    delay: FiniteDuration = Defaults.delay,
    base: Int = 2)
    (promise: () => Future[T])
    (implicit success: Success[T],
     timer: Timer,
     executor: ExecutionContext): Future[T] =
     countdown(
       max,
       promise,
       count => Delay(delay) {
         Backoff(count, Duration(delay.length * base, delay.unit), base)(promise)
       }.future.flatMap(identity))
}

/** A retry policy in which the a failure determines the way a future should be retried.
 *  {{{
 *  retry.When {
 *    case FailedRequest(retryAt) => retry.Pausing(delay = retryAt)_
 *  } {
 *    issueRequest
 *  }
 *  }}}
 *  If the result is not defined for the failure dispatcher the future will not
 *  be retried.
 */
object When extends Policy {
  type Promised[T] = () => Future[T]
  type Depends[T] = PartialFunction[T, Promised[T] => Future[T]]
  def apply[T](
    depends: Depends[T])
    (promise: () => Future[T])
    (implicit success: Success[T],
     executor: ExecutionContext): Future[T] = {
    val fut = promise()
    fut.flatMap { res =>
      if (success.predicate(res) || !depends.isDefinedAt(res)) fut
      else depends(res)(promise)
    }
  }
}

/** Retry policy that incorporate a count */
trait CountingPolicy extends Policy {
  protected def countdown[T](
    max: Int,
    promise: () => Future[T],
    orElse: Int => Future[T])
    (implicit success: Success[T],
     executor: ExecutionContext): Future[T] = {
      // consider this successful if our predicate says so _or_
      // we've reached the end out our countdown
      val countedSuccess = success.or(max < 1)
      retry(promise, () => orElse(max - 1))(countedSuccess, executor)
    }
}

trait Policy {
  protected def retry[T](
    promise: () => Future[T],
    orElse: () => Future[T])
    (implicit success: Success[T],
     executor: ExecutionContext): Future[T] = {
      val fut = promise()
      fut.flatMap { res =>
        if (success.predicate(res)) fut
        else orElse()
      }
    }
}

