package retry.netty

import java.util.concurrent.TimeUnit
import org.jboss.netty.util.{ HashedWheelTimer, Timeout, Timer, TimerTask }

case class NettyTimeout(underlying: Timeout) extends retry.Timeout {
  def cancel() = underlying.cancel()
}

case class NettyTimer(underlying: Timer = Default.underlying) extends retry.Timer {
  def apply[T](length: Long, unit: TimeUnit, todo: => T): retry.Timeout =
    NettyTimeout(underlying.newTimeout(new TimerTask {
      def run(timeout: Timeout) {
        todo
      }
    }, length, unit))
}

object Default {
  val timer = NettyTimer()
  def underlying = new HashedWheelTimer()
}
