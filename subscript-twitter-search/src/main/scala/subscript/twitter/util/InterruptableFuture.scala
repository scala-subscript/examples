package subscript.twitter.util

import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, Promise, Future}
//import scala.concurrent.ExecutionContext.Implicits.global

class CancelException extends Exception {override def toString() = "CancelException"}

/**
 * Emulates the "/.." functionality.
 * Usage:
 * 1. Wrap the task to be done in the interruptable future: task = InterruptableFuture(task)
 * 2. When you call task.execute(), previous instance of this task's execution is cancelled and the new one is started.
 */
abstract class InterruptableFuture[T] {
  private[this] var cancel: () => Unit = () => ()

  def task: T

  def execute(): Future[T] = {
    cancel()
    val (future, newCancel) = InterruptableFuture.cancellable {task}
    cancel = newCancel
    future
  }
}

object InterruptableFuture {
  object Implicits {
    implicit val executionContext = ExecutionContext fromExecutorService Executors.newCachedThreadPool()
  }
  import Implicits.executionContext

  def cancellable[T](task: => T): (Future[T], () => Unit) = {
    val req: Future[T]      = Future(task)
    val cancellationTrigger = Promise[T]()

    val compound: Future[T] = Future firstCompletedOf Seq(req, cancellationTrigger.future)
    val cancel: () => Unit  = () => cancellationTrigger failure new CancelException()

    (compound, cancel)
  }

  def apply[T](_task: => T) = new InterruptableFuture[T] {
    def task: T = _task
  }
}