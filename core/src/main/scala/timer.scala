package retry

import java.util.concurrent.TimeUnit

trait Timer {
  def apply[T](length: Long, unit: TimeUnit, todo: => T): Unit
}
