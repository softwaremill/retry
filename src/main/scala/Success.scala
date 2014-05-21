package retry

import scala.annotation.implicitNotFound
import scala.util.Try

@implicitNotFound("Cannot find an implicit retry.Success for the given type of Future, either require one yourself or import retry.Success._")
class Success[-T](val predicate: T => Boolean) {
  def or[TT <: T](that: Success[TT]): Success[TT] = new Success[TT](v => predicate(v) || that.predicate(v))
  def or[TT <: T](that: => Boolean): Success[TT] = or(new Success[TT](_ => that))
  def and[TT <: T](that: Success[TT]): Success[TT] = new Success[TT](v => predicate(v) && that.predicate(v))
  def and[TT <: T](that: => Boolean): Success[TT] = and(new Success[TT](_ => that))
}

object Success {
  implicit def either[A,B]: Success[Either[A,B]] =
    new Success(_.isRight)
  implicit def option[A]: Success[Option[A]] =
    new Success(!_.isEmpty)
  implicit def tried[A]: Success[Try[A]] =
    new Success(_.isSuccess)
}
