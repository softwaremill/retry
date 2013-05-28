package retry.netty

import java.util.concurrent.TimeUnit
import org.jboss.netty.util.{ Timeout, Timer, TimerTask }

case class NettyTimeout(underlying: Timeout) extends retry.Timeout {
  def cancel() = underlying.cancel()
}

case class NettyTimer(underlying: Timer) extends retry.Timer {
  def apply[T](length: Long, unit: TimeUnit, todo: => T): retry.Timeout =
    NettyTimeout(underlying.newTimeout(new TimerTask {
      def run(timeout: Timeout) {
        todo
      }
    }, length, unit))
}
