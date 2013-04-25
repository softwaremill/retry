package retry.twitter

import java.util.concurrent.TimeUnit
import com.twitter.util.{ Duration, Timer }

case class TwitterTimer(underlying: Timer) extends retry.Timer {
  def apply[T](length: Long, unit: TimeUnit, todo: => T) {
    underlying.doLater(Duration.fromTimeUnit(length,unit))(todo)
  }
}
