package retry

import org.scalatest.FunSpec
import scala.util.{ Try, Success => TrySuccess }

class SuccessSpec extends FunSpec {
  describe("retry.Success.either") {
    val either = implicitly[Success[Either[String, String]]]
    it ("should be successful on a Right") {
      assert(either.predicate(Right("")) === true)
    }

    it ("should be a failure on a Left") {
      assert(either.predicate(Left("")) === false)
    }
  }

  describe("retry.Success.option") {
    val option = implicitly[Success[Option[String]]]
    it ("should be successful on Some(_)") {
      assert(option.predicate(Some("")) == true)
    }

    it ("should be a failure on None") {
      assert(option.predicate(None) == false)
    }
  }

  describe("retry.Success.tried") {
    val tried = implicitly[Success[Try[String]]]
    it ("should be successful on Success(_)") {
      assert(tried.predicate(Try("")) == true)
    }

    it ("should be failure on Failure(_)") {
      assert(tried.predicate(Try({ throw new RuntimeException("")})) === false)
    }
  }

  describe("retry.Success combinators") {
    val a = Success[Int](_ > 1)
    val b = Success[Int](_ < 3)
    it ("should support and") {
      assert(a.and(b).predicate(2) === true)
      assert(a.and(false).predicate(2) === false)
    }
    it ("should support or") {
      assert(a.or(b).predicate(4) === true)
      assert(a.or(true).predicate(0) === true)
    }
  }
}
