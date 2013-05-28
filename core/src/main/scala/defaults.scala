package retry

import scala.concurrent.ExecutionContext

object Defaults {
  implicit def executor = ExecutionContext.Implicits.global
  implicit val timer: Timer = jdk.Default.timer
}

