# retry

[![Build Status](https://travis-ci.org/softprops/retry.png?branch=master)](https://travis-ci.org/softprops/retry)

don't give up

## install

With sbt, add the following to your project's build.sbt

    libraryDependencies += "me.lessis" %% "retry-core" % "0.2.0"

## usage

Applications can fail a runtime. We know this. Network connections drop. Connections timeout. Bad things happen.

Don't let bad things cause other bad things to happen. Give applications a second chance to retry.

Retry provides interfaces for common retry strategies that operate on `scala.util.Future`s.

Basic usage requires four things 

- an implicit execution context for executing futures 
- a definition of `retry.Success[-T](pred: T => Boolean)` to encode what "success" means for the type of your future
- an [odelay.Timer][timer] for asynchronously scheduling a followup attempt
- a block of code that results in a `scala.concurrent.Future`

Retry provides a set of defaults that uses `scala.concurrent.ExecutionContext.Implicits.global` as an execution context, `retry.Success` definitions for `Option`, `Either`, `scala.util.Try`, and an partial function (defined with Success.definedAt(partialFunction)), and an `odelay.jdk.JdkTimer` for use as an `odelay.Timer` out of the box.

```scala
import scala.concurrent._
import retry.Defaults._
retry.Backoff()(Future {
  // something that can "fail"
})
```

### Defining success.

Retry needs to know what _failure_ or _success_ means in order to know when to retry an operation. It does this through a generic `Success[-T](pred: T => Boolean)` type class, where `T` matches the type your `scala.util.Future[T]`. Retry looks for this implicitly in within the scope of the retry.
If you are using the `retry.Defaults` you will already have in scope a definition for success for `Either`, `scala.util.Try`, and `Option` types.
If you wish to define an application-specific definition of what "success" means for your future, you may do so by specifying the following in scope of the retry.

```scala
implicit val perfectTen = new Success[Int](_ == 10)
```

If your future completes with anything other than 10, it will be considered a failure and will be retried. Here's to you, tiger mom!

### Sleep schedules

Your application may run within a platform that provides its own way for scheduling tasks. If an `odelay.jdk.JdkTimer` isn't what you're looking for, you may wish to use the `odelay.Timer` for netty, `odelay.netty.Timer` in the `odelay-netty` module or an `odelay.twitter.TwitterTimer` available in the `odelay-twitter` module. See the [odelay docs][odelay] for defining your own timer. If you come up with a generic one, let open a pull request!

Doug Tangren (softprops) 2013-2014

[timer]: https://github.com/softprops/odelay#timers
[odelay]: https://github.com/softprops/odelay#readme
