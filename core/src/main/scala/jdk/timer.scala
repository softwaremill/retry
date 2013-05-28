package retry.jdk

import java.util.concurrent.{
  RejectedExecutionHandler, ScheduledExecutorService,
  ScheduledFuture, ScheduledThreadPoolExecutor, TimeUnit,
  ThreadFactory }

case class JdkTimeout[T](underlying: ScheduledFuture[T], interrupts: Boolean = false) extends retry.Timeout {
  def cancel() = underlying.cancel(interrupts)
}

class JdkTimer(underlying: ScheduledExecutorService, interruptOnCancel: Boolean) extends retry.Timer {
  /** customizing constructor */
  def this(poolSize: Int = Default.poolSize,
           threads: ThreadFactory = Default.threadFactory,
           handler: Option[RejectedExecutionHandler] = Default.rejectionHandler,
           interruptOnCancel: Boolean = Default.interruptOnCancel) =
    this(handler.map( rejections => new ScheduledThreadPoolExecutor(poolSize, threads, rejections))
           .getOrElse(new ScheduledThreadPoolExecutor(poolSize, threads)),
         interruptOnCancel)

  def apply[T](length: Long, unit: TimeUnit, todo: => T): retry.Timeout =
    JdkTimeout(underlying.schedule(new Runnable {
      def run = todo
    }, length, unit))
}

/** defaults for jdk timers */
object Default {
  def timer = new JdkTimer()    
  val poolSize = 2
  val threadFactory: ThreadFactory = new ThreadFactory {
    def newThread(runnable: Runnable) = {
      new Thread(runnable) {
        setDaemon(true)
      }
    }
  }
  val rejectionHandler: Option[RejectedExecutionHandler] = None
  val interruptOnCancel = true
}


