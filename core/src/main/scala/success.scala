package retry

import scala.util.Try

class Success[-T](val predicate: T => Boolean)

object Success {
  implicit def either[A,B]: Success[Either[A,B]] =
    new Success(_.isRight)
  implicit def option[A]: Success[Option[A]] =
    new Success(!_.isEmpty)
  implicit def tried[A]: Success[Try[A]] =
    new Success(_.isSuccess)
}
