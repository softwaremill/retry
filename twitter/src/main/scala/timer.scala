package retry.twitter

import java.util.concurrent.TimeUnit
import com.twitter.util.{ Duration, Timer, TimerTask }

case class TwitterTimeout(underlying: TimerTask) extends retry.Timeout {
  def cancel() = underlying.cancel()
}

case class TwitterTimer(underlying: Timer) extends retry.Timer {
  def apply[T](length: Long, unit: TimeUnit, todo: => T): retry.Timeout =
    TwitterTimeout(underlying.schedule(Duration.fromTimeUnit(length, unit))(todo))
}
