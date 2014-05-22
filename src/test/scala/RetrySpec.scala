package retry

import org.scalatest.{ FunSpec, BeforeAndAfterAll }
import retry.Defaults.timer
import scala.annotation.tailrec
import scala.concurrent.{ Future, Await }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class RetrySpec extends FunSpec with BeforeAndAfterAll {

  override def afterAll() {
    timer.stop()
  }

  def forwardCountingFutureStream(value: Int = 0): Stream[Future[Int]] =
    Future(value) #:: forwardCountingFutureStream(value + 1)

  def backwardCountingFutureStream(value: Int): Stream[Future[Int]] =
    if (value < 0) Stream.empty
    else Future(value) #:: backwardCountingFutureStream(value - 1)

  def time[T](f: => T): Duration = {
    val before = System.currentTimeMillis
    f
    Duration(System.currentTimeMillis - before, MILLISECONDS)
  }

  describe("retry.Directly") {
    it ("should retry a future for a specified number of times") {
      implicit val success = Success[Int](_ == 3)
      val tries = forwardCountingFutureStream().iterator
      val result = Await.result(retry.Directly(3)(tries.next),
                                1.millis)
      assert(success.predicate(result) === true)
    }

    it ("should fail when expected") {
      val success = implicitly[Success[Option[Int]]]
      val tries = Future(None: Option[Int])
      val result = Await.result(retry.Directly(2)({ () => tries }),
                                1.millis)
      assert(success.predicate(result) === false)
    }
  }

  describe("retry.Pause") {
    it ("should pause in between retries") {
      implicit val success = Success[Int](_ == 3)
      val tries = forwardCountingFutureStream().iterator
      val policy = retry.Pause(3, 30.millis)
      val took = time {
        val result = Await.result(policy(tries.next),
                                  90.seconds + 20.millis)
        assert(success.predicate(result) == true)
      }
      assert(took >= 90.millis === true,
             "took less time than expected: %s" format took)
      assert(took <= 110.millis === true,
             "took more time than expected: %s" format took)
    }
  }

  describe("retry.Backoff") {
    it ("should pause with multiplier between retries") {
      implicit val success = Success[Int](_ == 2)
      val tries = forwardCountingFutureStream().iterator
      val policy = retry.Backoff(2, 30.millis)
      val took = time {
        val result = Await.result(policy(tries.next),
                                  90.millis + 20.millis)
        assert(success.predicate(result) === true, "predicate failed")
      }
      assert(took >= 90.millis === true,
             "took less time than expected: %s" format took)
      assert(took <= 110.millis === true,
             "took more time than expected: %s" format took)
    }
  }

  describe("retry.When") {
    it ("should retry conditionally when a condition is met") {
      implicit val success = Success[Int](_ == 2)
      val tries = forwardCountingFutureStream().iterator
      val policy = retry.When {
        // this is very constrived but should serve as an example
        // of matching then dispatching a retry depending on
        // the value of the future when completed
        case n if n == 0 => retry.When {
          case n if n == 1 => retry.Pause(delay = 2.seconds)
        }
      }
      val future = policy(tries.next)
      val result = Await.result(future, 2.seconds)
      assert(success.predicate(result) === true)
    }

    it ("should retry but only when condition is met") {
      implicit val success = Success[Int](_ == 2)
      val tries = forwardCountingFutureStream().iterator
      val policy = retry.When {
        // this cond will never be met because
        // a cond for n == 0 is not defined
        case n if n == 1 => retry.Directly()
      }

      val future = policy(tries.next)
      val result = Await.result(future, 1.millis)
      assert(success.predicate(result) === false)
    }
  }
}
