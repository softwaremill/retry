# retry

[![Build Status](https://travis-ci.org/softprops/retry.png?branch=master)](https://travis-ci.org/softprops/retry)

don't give up

## install

With sbt, add the following to your project's build.sbt

    libraryDependencies += "me.lessis" %% "retry-core" % "0.1.0"

## usage

Applications can fail a runtime. We know this. Network connections drop. Connections timeout. Bad things happen.

Don't let bad things cause other bad things to happen. Give applications a second chance to retry.

Retry provides interfaces for common retry strategies that operate on `scala.util.Future`s.

Basic usage requires four things 

- an implicit execution context for executing futures 
- a definition of `retry.Success[-T](pred: T => Boolean)` to encode what "success" means for the type of your future
- a `retry.Timer` for asynchronously scheduling a followup attempt
- a block of code that results in a `scala.concurrent.Future`

Retry provides a set of defaults that uses `scala.concurrent.ExecutionContext.Implicits.global` as an execution context, `retry.Success` definitions for `Option`, `Either`, and `scala.util.Try`, and a `retry.jdk.JdkTimer` as a `retry.Timer` out of the box.

```scala
import scala.concurrent._
import retry.Defaults._
retry.Backoff()(Future {
  // something that can fail
})
```


### Defining success.

Retry needs to know what failure or success means in order to know when to retry. It does this through a typed `Success[-T](pred: T => Boolean)` type.
Where `T` matches the type your `scala.util.Future[T]` is defined with. Retry looks for this implicitly in within the scope of retrying.
If you are using the `retry.Defaults` you will already have in scope a definition for success for `Either`, `scala.util.Try`, and `Option` types.
If you wish to define an application-specific definition of what "success" means for your future,
you may do so by specifying the following in scope of the retry.

```scala
implicit val success = new Success[Int](_ > 10)
```

If your future completes with an int less than or equal to 10. It is then considered a failure and will be retried.

### Sleep schedules

Your application may run within a platform that provides its own way for scheduling tasks to happen in the future. If `retry.jdk.JdkTimer` isn't what you're looking for, you may wish to use the `retry.Timer` for netty, `retry.netty.Timer` in the `retry-netty` module or a `retry.twitter.Timer` available in the `retry-twitter` module. If these also aren't what you're looking for, you can define your own in the scope of the retry.

```scala
class YourTimer extends retry.Timer {
  def apply[T](len: Long, unit: TimeUnit, todo: => T) = new retry.Timeout {
    def cancel() {
      // return something which may be cancelled
    }
  }
}

implicit val timer = new YourTimer 
```

Doug Tangren (softprops) 2013
