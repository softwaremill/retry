package retry

import org.scalatest.FunSpec 
import scala.annotation.tailrec
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class RetrySpec extends FunSpec {

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
      implicit val success = new Success[Int](_ == 3)
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
      import retry.Defaults.timer
      implicit val success = new Success[Int](_ == 3)
      val tries = forwardCountingFutureStream().iterator
      val took = time {
        val result = Await.result(retry.Pause(3, 30.millis)(tries.next),
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
      import retry.Defaults.timer
      implicit val success = new Success[Int](_ == 2)
      val tries = forwardCountingFutureStream().iterator
      val took = time {
        val result = Await.result(retry.Backoff(2, 30.millis)(tries.next),
                                  90.millis + 20.millis)
        assert(success.predicate(result) === true, "predicate failed")
      }
      assert(took >= 90.millis === true,
             "took less time than expected: %s" format took)
      assert(took <= 110.millis === true,
             "took more time than expected: %s" format took)
    }
  }

  describe("retry.CountingRetry") {
    it ("should retry if an exception was thrown") {
      import retry.Defaults.timer
      implicit val success = new Success[Int](_ == 2)
      def fut = () => future { 1 / 0 }
      val took = time {      
        val result = try {
          Await.result(retry.Pause(3, 30.millis)(fut), 
                                  90.millis + 20.millis)
        } catch {
          case e: Throwable => e
        }
      }
      assert(took >= 90.millis === true,
             "took less time than expected: %s" format took)
    }
  }
}
