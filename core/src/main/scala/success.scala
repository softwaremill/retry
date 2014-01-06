package retry

import scala.annotation.implicitNotFound
import scala.util.Try

@implicitNotFound("Cannot find an implicit retry.Success for the given type of Future, either require one yourself or import retry.Success._")
class Success[-T](val predicate: T => Boolean)

object Success {
  implicit def either[A,B]: Success[Either[A,B]] =
    new Success(_.isRight)
  implicit def option[A]: Success[Option[A]] =
    new Success(!_.isEmpty)
  implicit def tried[A]: Success[Try[A]] =
    new Success(_.isSuccess)
  def definedAt[A,B](pf: PartialFunction[A,B]) =
    new Success(pf.isDefinedAt)
}
