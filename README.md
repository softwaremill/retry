# retry

[![Build Status](https://travis-ci.org/softwaremill/retry.png?branch=master)](https://travis-ci.org/softwaremill/retry)

don't give up

## install

With sbt, add the following to your project's build.sbt

```scala
libraryDependencies += "com.softwaremill.retry" %% "retry" % "0.3.4"
```
## usage

Applications fail. Network connections drop. Connections timeout. Bad things happen.

Failure to address this will cause other bad things to happen. Effort is the measurement of how hard you try.

You can give your application perseverance with retry.

Retry provides interfaces for common retry strategies that operate on Scala [Futures][fut].

Basic usage requires three things

- an implicit execution context for executing futures 
- a definition of [Success](#defining-success) encode what "success" means for the type of your future
- a block of code that results in a Scala [Future][fut].

Depending on your strategy for retrying a future you may also need an [odelay.Timer][timer] for asynchronously scheduling followup attempts

Retry provides a set of defaults that provide `retry.Success` definitions for [Option][option], [Either][either], [Try][try], and a partial function (defined with Success.definedAt(partialFunction)) out of the box.

```scala
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

retry.Backoff().apply(() => Future {
  // something that can "fail"
})
```

### Defining success

Retry needs to know what _success_ means in the context of your Future in order to know when to retry an operation.

It does this through a generic `Success[-T](pred: T => Boolean)` type class, where `T` matches the type your [Future][fut] will resolve to.

Retry looks for this definition within implicit scope of the retry.

You may wish define an application-specific definition of what "success" means for your future. You can do so by specifying the following in scope of the retry.

```scala
implicit val perfectTen = Success[Int](_ == 10)
```

If your future completes with anything other than 10, it will be considered a failure and will be retried. Here's to you, tiger mom!

Success values may also be composed with `and` and `or` semantics

```scala
// will be considered a success when the preconditions of both successA and successB are met
val successC = successA.and(successB)

// will be considered a success when the predconditions of either successC or successD are met
val successE = successC.or(successD)
```

### Sleep schedules

Rather than blocking a thread, retry attempts are scheduled using Timers. Your application may run within a platform that provides its own way for scheduling tasks. If an [odelay.jdk.JdkTimer](https://github.com/softwaremill/odelay#jdktimer) isn't what you're looking for, you may wish to use the `odelay.Timer` for netty, [odelay.netty.Timer](https://github.com/softwaremill/odelay#netty3timers) in the `odelay-netty` module or an [odelay.twitter.TwitterTimer](https://github.com/softwaremill/odelay#twittertimers) available in the `odelay-twitter` module.

See the [odelay docs][odelay] for defining your own timer. If none of these aren't what you're looking for, please open a pull request!

### According to Policy

Retry logic is implemented in modules whose behavior vary but all produce a common interface: a `retry.Policy`.

```scala
trait Policy {
  def apply[T](promise: () => Future[T])
     (implicit success: Success[T],
      executor: ExecutionContext): Future[T]
}
```          

#### Directly

The `retry.Directly` module defines interfaces for retrying a future directly
after a failed attempt.

```scala
// retry 4 times
val future = retry.Directly(4) { () =>
  attempt
}
```

#### Pause

The `retry.Pause` module defines interfaces for retrying a future with a configurable pause in between attempts

```scala
// retry 3 times pausing 30 seconds in between attempts
val future = retry.Pause(3, 30.seconds).apply { () =>
  attempt
}
```

#### Backoff

The `retry.Backoff` modules defines interfaces for retrying a future with a configureable pause and exponential
backoff factor.


```scala
// retry 4 times with a delay of 1 second which will be multipled
// by 2 on every attempt
val future = retry.Backoff(4, 1.second).apply { () =>
  attempt
}
```

#### When

All of the retry strategies above assume you are representing failure in your Future's result type. In cases where the
result of your future is "exceptional". You can use the When module which takes a PartialFunction of Any to Policy.

```scala
val policy = retry.When {
  case NonFatal(e) => retry.Pause(3, 1.second)
}

policy(execptionalAttempt)
```

Note, The domain of the PartialFunction passed to When may cover both the exception thrown _or_ the successful result of the future.

#### Suggested library usage

Since all retry modules now produce a generic interface, a `retry.Policy`, if you wish to write clients of services you may wish to make define
a Success for the type of that service and capture an configurable reference to a Policy so that clients may swap policies based on use case.

```scala
case class Client(retryPolicy: retry.Policy = retry.Directly()) {
  def request = retryPolicy(mkRequest)
}

val defaultClient = Client()

val customClient = defaultClient.copy(
  retryPolicy = retry.Backoff()
)
```

## Credits

Originally created by [Doug Tangren](https://github.com/softprops), maintained by [SoftwareMill](https://softwaremill.com).

[timer]: https://github.com/softwaremill/odelay#timers
[odelay]: https://github.com/softwaremill/odelay#readme
[fut]: http://www.scala-lang.org/api/current/index.html#scala.concurrent.Future
[either]: http://www.scala-lang.org/api/current/index.html#scala.util.Either
[option]: http://www.scala-lang.org/api/current/index.html#scala.Option
[try]: http://www.scala-lang.org/api/current/index.html#scala.util.Try
