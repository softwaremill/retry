package retry

import scala.concurrent.duration.{ Duration, FiniteDuration }
import java.util.concurrent.TimeUnit

object Defaults {
  val delay: FiniteDuration = Duration(500, TimeUnit.MILLISECONDS)
}

