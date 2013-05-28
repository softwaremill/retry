package retry

import java.util.concurrent.TimeUnit

/** Represents a timeout emitted by the invocation of a timer operation */
trait Timeout {
  def cancel(): Unit
}

/** interface for executing a block of code at a given time */
trait Timer {
  def apply[T](length: Long, unit: TimeUnit, todo: => T): Timeout
}
