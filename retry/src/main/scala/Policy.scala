package retry

import odelay.{Delay, Timer}

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.control.NonFatal

// This case class and its implicit conversions allow us to accept both
// `() => Future[T]` and `Future[T]`-by-name as Policy.apply arguments.
// Note that these two types are the same after erasure.
case class PromiseWrapper[T](
    promise: () => Future[T]
)

object PromiseWrapper {
  implicit def fromFuture[T](promise: () => Future[T]): PromiseWrapper[T] =
    PromiseWrapper(promise)
  implicit def toFuture[T](pw: PromiseWrapper[T]): () => Future[T] = pw.promise
}

object Directly {

  /** Retry immediately after failure forever */
  def forever: Policy =
    new Policy {
      def apply[T](promise: PromiseWrapper[T])(
          implicit success: Success[T],
          executor: ExecutionContext): Future[T] = {
        def run(): Future[T] = {
          retry(promise, run, { _: Future[T] =>
            run()
          })
        }
        run
      }
    }

  /** Retry immediately after failure for a max number of times */
  def apply(max: Int = 3): Policy =
    new CountingPolicy {
      def apply[T](promise: PromiseWrapper[T])(
          implicit success: Success[T],
          executor: ExecutionContext): Future[T] = {
        def run(max: Int): Future[T] = countdown(max, promise, run)
        run(max)
      }
    }
}

object Pause {

  /** Retry with a pause between attempts forever */
  def forever(delay: FiniteDuration = Defaults.delay)(
      implicit timer: Timer): Policy =
    new Policy { self =>
      def apply[T](promise: PromiseWrapper[T])(
          implicit success: Success[T],
          executor: ExecutionContext): Future[T] = {
        def run(): Future[T] = {
          val nextRun: () => Future[T] = () =>
            Delay(delay)(run()).future.flatMap(identity)
          retry(promise, nextRun, { _: Future[T] =>
            nextRun()
          })
        }
        run()
      }
    }

  /** Retry with a pause between attempts for a max number of times */
  def apply(max: Int = 4, delay: FiniteDuration = Defaults.delay)(
      implicit timer: Timer): Policy =
    new CountingPolicy {
      def apply[T](promise: PromiseWrapper[T])(
          implicit success: Success[T],
          executor: ExecutionContext): Future[T] = {
        def run(max: Int): Future[T] =
          countdown(max,
                    promise,
                    c => Delay(delay)(run(c)).future.flatMap(identity))
        run(max)
      }
    }
}

/** A retry policy which will back off using a configurable policy which
  *  incorporates random jitter. This has the advantage of reducing contention
  *  if you have threaded clients using the same service.
  *
  *  {{{
  *  val policy = retry.JitterBackoff()
  *  val future = policy(issueRequest)
  *  }}}
  *
  *  The following pre-made jitter algorithms are available for you to use:
  *
  *  - [[retry.Jitter.none]]
  *  - [[retry.Jitter.full]]
  *  - [[retry.Jitter.equal]]
  *  - [[retry.Jitter.decorrelated]]
  *
  *  You can choose one like this:
  *  {{{
  *  implicit val jitter = retry.Jitter.full(cap = 5.minutes)
  *  val policy = retry.JitterBackoff(1 second)
  *  val future = policy(issueRequest)
  *  }}}
  *
  *  If a jitter policy isn't in scope, it will use [[retry.Jitter.full]] by
  *  default which tends to cause clients slightly less work at the cost of
  *  slightly more time.
  *
  *  For more information about the algorithms, see the following article:
  *
  *  [[https://www.awsarchitectureblog.com/2015/03/backoff.html]]
  */
object JitterBackoff {

  /** Retry with exponential backoff + jitter forever */
  def forever(delay: FiniteDuration = Defaults.delay)(
      implicit timer: Timer,
      jitter: Jitter = Defaults.jitter): Policy =
    new Policy {
      def apply[T](promise: PromiseWrapper[T])(
          implicit success: Success[T],
          executor: ExecutionContext): Future[T] = {
        def run(attempt: Int, sleep: FiniteDuration): Future[T] = {
          val nextRun = () =>
            Delay(delay) {
              run(attempt + 1, jitter(delay, sleep, attempt))
            }.future.flatMap(identity)
          retry(promise, nextRun, { _: Future[T] =>
            nextRun()
          })
        }
        run(1, delay)
      }
    }

  /** Retry with exponential backoff + jitter for a max number of times */
  def apply(max: Int = 8, delay: FiniteDuration = Defaults.delay)(
      implicit timer: Timer,
      jitter: Jitter = Defaults.jitter): Policy =
    new CountingPolicy {
      override def apply[T](promise: PromiseWrapper[T])(
          implicit success: Success[T],
          executor: ExecutionContext): Future[T] = {
        def run(attempt: Int, max: Int, sleep: FiniteDuration): Future[T] =
          countdown(max,
                    promise,
                    count =>
                      Delay(sleep) {
                        run(attempt + 1, count, jitter(delay, sleep, attempt))
                      }.future.flatMap(identity))
        run(1, max, delay)
      }
    }
}

object Backoff {

  /** Retry with exponential backoff forever */
  def forever(delay: FiniteDuration = Defaults.delay, base: Int = 2)(
      implicit timer: Timer): Policy =
    new Policy {
      def apply[T](promise: PromiseWrapper[T])(
          implicit success: Success[T],
          executor: ExecutionContext): Future[T] = {
        def run(delay: FiniteDuration): Future[T] = {
          val nextRun = () =>
            Delay(delay) {
              run(Duration(delay.length * base, delay.unit))
            }.future.flatMap(identity)
          retry(promise, nextRun, { _: Future[T] =>
            nextRun()
          })
        }
        run(delay)
      }
    }

  /** Retry with exponential backoff for a max number of times */
  def apply(max: Int = 8,
            delay: FiniteDuration = Defaults.delay,
            base: Int = 2)(implicit timer: Timer): Policy =
    new CountingPolicy {
      def apply[T](promise: PromiseWrapper[T])(
          implicit success: Success[T],
          executor: ExecutionContext): Future[T] = {
        def run(max: Int, delay: FiniteDuration): Future[T] =
          countdown(max,
                    promise,
                    count =>
                      Delay(delay) {
                        run(count, Duration(delay.length * base, delay.unit))
                      }.future.flatMap(identity))
        run(max, delay)
      }
    }
}

/** A retry policy in which the a failure determines the way a future should be retried.
  *  The partial function provided may define the domain of both the success OR exceptional
  *  failure of a future fails explicitly.
  *
  *  {{{
  *  val policy = retry.When {
  *    case RetryAfter(retryAt) => retry.Pause(delay = retryAt)
  *  }
  *  val future = policy(issueRequest)
  *  }}}
  *
  *  If the result is not defined for the depends block, the future will not
  *  be retried.
  */
object When {
  type Depends = PartialFunction[Any, Policy]
  def apply(depends: Depends): Policy =
    new Policy {
      def apply[T](promise: PromiseWrapper[T])(
          implicit success: Success[T],
          executor: ExecutionContext): Future[T] = {
        val fut = promise()
        fut
          .flatMap { res =>
            if (success.predicate(res) || !depends.isDefinedAt(res)) fut
            else depends(res)(promise)
          }
          .recoverWith {
            case NonFatal(e) =>
              if (depends.isDefinedAt(e)) depends(e)(promise) else fut
          }
      }
    }
}

/** Retry policy that incorporates a count */
trait CountingPolicy extends Policy {
  protected def countdown[T](max: Int,
                             promise: () => Future[T],
                             orElse: Int => Future[T])(
      implicit success: Success[T],
      executor: ExecutionContext): Future[T] = {
    // consider this successful if our predicate says so _or_
    // we've reached the end out our countdown
    val countedSuccess = success.or(max < 1)
    retry(promise, () => orElse(max - 1), { f: Future[T] =>
      if (max < 1) f else orElse(max - 1)
    })(countedSuccess, executor)
  }
}

/** A Policy defines an interface for applying a future with retry semantics
  *  specific to implementations
  */
trait Policy {

  def apply[T](pw: PromiseWrapper[T])(implicit success: Success[T],
                                      executor: ExecutionContext): Future[T]

  def apply[T](promise: => Future[T])(implicit success: Success[T],
                                      executor: ExecutionContext): Future[T] =
    apply { () =>
      promise
    }

  protected def retry[T](promise: () => Future[T],
                         orElse: () => Future[T],
                         recovery: Future[T] => Future[T] = identity(
                           _: Future[T]))(
      implicit success: Success[T],
      executor: ExecutionContext): Future[T] = {
    val fut = promise()
    fut
      .flatMap { res =>
        if (success.predicate(res)) fut
        else orElse()
      }
      .recoverWith {
        case NonFatal(_) => recovery(fut)
      }
  }
}
