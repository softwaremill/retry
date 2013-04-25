package retry.netty

import java.util.concurrent.TimeUnit
import org.jboss.netty.util.{ Timeout, Timer, TimerTask }

case class NettyTimer(underlying: Timer) extends retry.Timer {
  def apply[T](length: Long, unit: TimeUnit, todo: => T) {
    underlying.newTimeout(new TimerTask {
      def run(timeout: Timeout) {
        todo
      }
    }, length, unit)
  }
}
