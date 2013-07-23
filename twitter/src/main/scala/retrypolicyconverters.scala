package retry

import com.twitter.util.{Future => TwitterFuture, Return, Throw}
import com.twitter.zk.{RetryPolicy => TwitterRetryPolicy}
import com.twitter.bijection.twitter_util.UtilBijections.twitter2ScalaFuture
import com.twitter.bijection.Conversion.asMethod
import scala.concurrent.{Future => ScalaFuture, ExecutionContext}
import scala.concurrent.ExecutionContext.Implicits.global

object RetryPolicyConverters {
  val defaultContext = global

  def retryPolicyToRetrier(policy: TwitterRetryPolicy): ScalaRetryPolicy = new ScalaRetryPolicy(policy, defaultContext)
}

class ScalaRetryPolicy(policy: TwitterRetryPolicy, context: ExecutionContext) {
  implicit val ctxt = context

  def apply[A](op: => ScalaFuture[A]): ScalaFuture[A] = policy(op.as[TwitterFuture[A]]).as[ScalaFuture[A]]
}
