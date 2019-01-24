package controllers

import javax.inject._
import akka.actor.ActorSystem
import play.api.mvc._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}

@Singleton
class AsyncController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  def message = Action.async {
    getFutureMessage(0.second).map { msg => Ok(msg.toString) }
  }

  private def getFutureMessage(delayTime: FiniteDuration): Future[Boolean] = {
    val promise: Promise[Boolean] = Promise[Boolean]()
    actorSystem.scheduler.scheduleOnce(delayTime) {
      promise.success(true)
    }(actorSystem.dispatcher) // run scheduled tasks using the actor system's dispatcher
    val tempFuture = promise.future

    tempFuture.onComplete(t=> {
      require(t.isSuccess)
      println(t.getOrElse("Error"))
    })
    tempFuture
  }

}
