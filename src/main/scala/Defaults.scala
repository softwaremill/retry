package retry

import odelay.Timer
import scala.concurrent.{ ExecutionContext,  ExecutionContextExecutor }
import scala.concurrent.duration.{ Duration, FiniteDuration }
import java.util.concurrent.TimeUnit

object Defaults {
  implicit def executor: ExecutionContextExecutor =
    ExecutionContext.Implicits.global
  implicit val timer: Timer = odelay.Default.timer
  val delay: FiniteDuration = Duration(500, TimeUnit.MILLISECONDS)
}

