package retry

import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.concurrent.duration.Duration

object SleepFuture {
  def apply[T](d: Duration)(todo: => T)
              (implicit timer: Timer,
               executor: ExecutionContext): Future[T] = {
    val promise = Promise[T]()
    timer(d.length, d.unit, promise.complete(util.Try(todo)))
    promise.future
  }
}
