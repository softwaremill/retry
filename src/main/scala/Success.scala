package retry

import scala.annotation.implicitNotFound
import scala.util.Try

@implicitNotFound("Cannot find an implicit retry.Success for the given type of Future, either require one yourself or import retry.Success._")
class Success[-T](val predicate: T => Boolean) {
  def or[TT <: T](that: Success[TT]): Success[TT] = Success[TT](v => predicate(v) || that.predicate(v))
  def or[TT <: T](that: => Boolean): Success[TT] = or(Success[TT](_ => that))
  def and[TT <: T](that: Success[TT]): Success[TT] = Success[TT](v => predicate(v) && that.predicate(v))
  def and[TT <: T](that: => Boolean): Success[TT] = and(Success[TT](_ => that))
}

object Success {
  implicit def either[A,B]: Success[Either[A,B]] =
    Success(_.isRight)
  implicit def option[A]: Success[Option[A]] =
    Success(!_.isEmpty)
  implicit def tried[A]: Success[Try[A]] =
    Success(_.isSuccess)

  def apply[T](pred: T => Boolean) = new Success(pred)
}
