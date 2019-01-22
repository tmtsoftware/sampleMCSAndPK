package com.tmt.tcs.mcs.samplemcsDeploy
import java.io._
import java.net.InetAddress
import java.time.{Instant, LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter
import java.time.Duration
import akka.actor.{typed, ActorRefFactory, ActorSystem, Scheduler}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.actor.typed.scaladsl.adapter._
import csw.command.api.scaladsl.CommandService
import csw.command.client.CommandServiceFactory
import csw.event.api.scaladsl.EventService
import csw.event.client.EventServiceFactory
import csw.location.api.models.{AkkaLocation, ComponentId}
import csw.location.api.models.ComponentType.Assembly
import csw.location.api.models.Connection.AkkaConnection
import csw.location.client.ActorSystemFactory
import csw.location.client.scaladsl.HttpLocationServiceFactory
import csw.logging.scaladsl.LoggingSystemFactory
import csw.params.commands.CommandResponse.SubmitResponse
import csw.params.commands.{CommandName, CommandResponse, Setup}
import csw.params.core.generics.{Key, KeyType, Parameter}
import csw.params.core.models.{Id, Prefix}
import csw.params.events.{Event, SystemEvent}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

import scala.concurrent.{Await, ExecutionContext, Future}

object MCSMainApp extends App {

  private val system: ActorSystem     = ActorSystemFactory.remote("Client-App")
  implicit val mat: ActorMaterializer = ActorMaterializer()
  private val locationService         = HttpLocationServiceFactory.makeLocalClient(system, mat)
  private val host                    = InetAddress.getLocalHost.getHostName
  LoggingSystemFactory.start("MCSMainApp", "0.1", host, system)

  import system._
  implicit val timeout: Timeout                 = Timeout(300.seconds)
  implicit val scheduler: Scheduler             = system.scheduler
  implicit def actorRefFactory: ActorRefFactory = system

  private val connection = AkkaConnection(ComponentId("SamplemcsAssembly", Assembly))

  val resp1 = sendStartupCommand()

  def sendStartupCommand()(implicit ec: ExecutionContext): CommandResponse = {
    val commandService = getCommandService
    val setup          = Setup(Prefix("SampleMcsAssembly-Client"), CommandName("Startup"), None)
    val response       = Await.result(commandService.submit(setup), 10.seconds)
    println(s"Startup command response is: $response")
    response
  }

  private def getCommandService: CommandService = {
    implicit val sys: typed.ActorSystem[Nothing] = system.toTyped
    val akkaLocations: List[AkkaLocation]        = Await.result(locationService.listByPrefix("mcsAssembly"), 60.seconds)
    CommandServiceFactory.make(akkaLocations.head)(sys)
  }
}
