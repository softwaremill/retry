package retry

import org.scalatest.FunSpec 
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

class RetrySpec extends FunSpec {
  case class FailsUntil(times: Int) {
    @volatile
    private var tried: Int = 0
    def apply(): Future[Option[Boolean]] = {
      val promise = Promise[Option[Boolean]]()      
      val truth = if (times == tried) Some(true)
      else {
        tried += 1
        None
      }
      promise.complete(Try(truth))
      promise.future
    }
  }

  describe("retry.Directly") {
    it ("should retry a future for a specified number of times") {
      val tries = FailsUntil(2)
      assert(Await.result(retry.Directly(2)({ () => tries.apply() }), 10.millis) === Some(true))
    }

    it ("should fail when expected") {
      val tries = FailsUntil(2)
      assert(Await.result(retry.Directly(1)({ () => tries.apply() }), 10.millis) === None)
    }
  }
}
