package retry

import scala.concurrent.{ ExecutionContext,  ExecutionContextExecutor }

object Defaults {
  implicit def executor: ExecutionContextExecutor =
    ExecutionContext.Implicits.global
  implicit val timer: Timer = jdk.Default.timer
}

