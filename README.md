# retry

don't give up

## usage

Applications can fail a runtime. We know this. Network connections drop. Connections timeout. Bad things happen.

Don't let bad things cause other bad things to happen. Give applications a second chance to retry.

Retry provides interfaces for common retry strategies that operate on `scala.util.Future`s.

Basic usage requires three things 

# an implicit execution context for executing futures 
# a `retry.Timer` for asynchronously scheduling a followup attempt
# a block of code that results in a `scala.util.Future`

Retry provides a set of defaults that uses `scala.concurrent.ExecutionContext.Implicits.global` as an execution context and `retry.jdk.JdkTimer` as a `retry.Timer` out of the box.

```scala
import scala.concurrent._
import retry.Defaults._
retry.Backoff()(Future {
  // something that can fail
})
```

