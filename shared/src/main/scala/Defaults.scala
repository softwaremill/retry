package retry

import scala.concurrent.duration.{ Duration, FiniteDuration }
import java.util.concurrent.{ ThreadLocalRandom, TimeUnit }

object Defaults {
  val delay: FiniteDuration = Duration(500, TimeUnit.MILLISECONDS)
  val cap: FiniteDuration = Duration(1, TimeUnit.MINUTES)
  val random: Jitter.RandomSource =
    Jitter.randomSource(ThreadLocalRandom.current())
  val jitter = Jitter.full()
}

