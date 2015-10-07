package subscript.twitter.util

import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner

import scala.concurrent.{ExecutionContext, Future, Await}
import scala.concurrent.duration._
import scala.util.{Success, Failure}

/**
 * Created by anatolii on 11/29/14.
 */
@RunWith(classOf[JUnitRunner])
class InterruptableFutureSpec extends FlatSpec with Matchers {

  "InterruptableFuture" should "successfully execute a task when not interrupted" in new FutureTools with FutureToolsValues {
    val f   = InterruptableFuture {operation}.execute()
    result(f) shouldBe atomicOperationValue
  }

  it should "cancel the previous execution when a new one starts" in new FutureTools with FutureToolsValues {
    val intFut = InterruptableFuture {operation}
    val f1     = intFut.execute()
    val f2     = intFut.execute()

    ready(f1)
    ready(f2)

    f1.value.get shouldBe a [Failure[_]]
    f2.value.get shouldBe a [Success[_]]
    f2.value.get shouldBe Success(atomicOperationValue)
  }

  it should "start an execution immediately" in new FutureTools with FutureToolsValues {
    val intFut = InterruptableFuture {atomicOperation}
    Await.ready(intFut.execute(), timeout + timeMeasureError)
  }

  it should "start an execution immediately even if the previous executions are still alive (technically)" in new FutureTools with FutureToolsValues {
    val intFut = InterruptableFuture {longRunningOperation}
    for (i <- 1 to maxOperations) intFut.execute()
    Await.ready(intFut.execute(), operationDuration.milliseconds + timeMeasureError)
  }


  trait FutureToolsValues {
    val timeout: Duration    = 3 seconds
    val operationDuration    = 100.toLong
    val atomicOperationValue = "result"
    val timeMeasureError     = 10 milliseconds
    val maxOperations        = 100
  }

  trait FutureTools {
    val timeout             : Duration
    val operationDuration   : Long
    val atomicOperationValue: String

    def result[T](f: Future[T]): T    = Await.result(f, timeout)
    def ready [T](f: Future[T]): Unit = Await.ready (f, timeout)

    def longRunningOperation = Thread sleep operationDuration
    def atomicOperation      = atomicOperationValue
    def operation            = {longRunningOperation ; atomicOperation}
  }

}
