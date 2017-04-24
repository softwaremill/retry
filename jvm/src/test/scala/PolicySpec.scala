package retry

import java.util.Random

import org.scalatest.{ AsyncFunSpec, BeforeAndAfterAll }
import odelay.Timer
import scala.concurrent.{ Future }
import scala.concurrent.duration._
import java.util.concurrent.atomic.{ AtomicBoolean, AtomicInteger, AtomicLong }

class PolicySpecJVM extends PolicySpec {
}
