package com.tmt.tcs.mcs.samplemcsAssembly

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import csw.framework.scaladsl.ComponentHandlers
import akka.actor.typed.scaladsl.adapter.UntypedActorSystemOps

import scala.concurrent.duration._
import akka.util.Timeout
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import csw.command.api.CurrentStateSubscription
import csw.command.api.scaladsl.CommandService
import csw.command.client.CommandServiceFactory
import csw.command.client.messages.TopLevelActorMessage
import csw.config.api.scaladsl.ConfigClientService
import csw.config.client.scaladsl.ConfigClientFactory
import csw.framework.models.CswContext
import csw.location.api.models.{AkkaLocation, LocationRemoved, LocationUpdated, TrackingEvent}
import csw.params.commands.CommandIssue.{UnsupportedCommandInStateIssue, UnsupportedCommandIssue, WrongNumberOfParametersIssue}
import csw.params.commands.CommandResponse._
import csw.params.commands.{CommandResponse, ControlCommand}
import csw.params.core.generics.Parameter

import com.tmt.tcs.mcs.samplemcsAssembly.EventMessage.StartEventSubscription

/**
 * Domain specific logic should be written in below handlers.
 * This handlers gets invoked when component receives messages/commands from other component/entity.
 * For example, if one component sends Submit(Setup(args)) command to SamplemcsHcd,
 * This will be first validated in the supervisor and then forwarded to Component TLA which first invokes validateCommand hook
 * and if validation is successful, then onSubmit hook gets invoked.
 * You can find more information on this here : https://tmtsoftware.github.io/csw/commons/framework.html
 */
class SamplemcsAssemblyHandlers(ctx: ActorContext[TopLevelActorMessage], cswCtx: CswContext)
    extends ComponentHandlers(ctx, cswCtx) {

  import cswCtx._
  implicit val ec: ExecutionContextExecutor = ctx.executionContext
  private val log                           = loggerFactory.getLogger
  var hcdLocation: Option[CommandService]   = None

  val eventHandler: ActorRef[EventMessage] =
    ctx.spawn(EventHandler.createObject(eventService, loggerFactory), name = "EventHandlerActor")

  override def initialize(): Future[Unit] = {
    log.info("Initializing SampleMCS assembly...")
    Future.unit
  }

  override def onLocationTrackingEvent(trackingEvent: TrackingEvent): Unit = {

    trackingEvent match {
      case LocationUpdated(location) =>
        hcdLocation = Some(CommandServiceFactory.make(location.asInstanceOf[AkkaLocation])(ctx.system))
      case LocationRemoved(_) =>
        hcdLocation = None
    }
  }

  override def validateCommand(controlCommand: ControlCommand): ValidateCommandResponse = {
    controlCommand.commandName.name match {
      case "Startup" => Accepted(controlCommand.runId)
      case x         => Invalid(controlCommand.runId, UnsupportedCommandIssue(s"Command $x is not supported"))
    }
  }

  override def onSubmit(controlCommand: ControlCommand): SubmitResponse = {
    controlCommand.commandName.name match {
      case "Startup" =>
        eventHandler ! StartEventSubscription()
        CommandResponse.Completed(controlCommand.runId)
    }
  }

  override def onOneway(controlCommand: ControlCommand): Unit = {}

  override def onShutdown(): Future[Unit] = { Future.unit }

  override def onGoOffline(): Unit = {}

  override def onGoOnline(): Unit = {}

}
