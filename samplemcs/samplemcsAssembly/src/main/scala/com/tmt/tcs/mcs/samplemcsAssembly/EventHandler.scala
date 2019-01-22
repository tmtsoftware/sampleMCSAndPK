package com.tmt.tcs.mcs.samplemcsAssembly

import java.io._
import java.util.Calendar
import java.time.format.DateTimeFormatter
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.util.Timeout
import csw.command.api.scaladsl.CommandService
import csw.event.api.scaladsl.EventService
import csw.logging.scaladsl.LoggerFactory
import csw.params.commands.ControlCommand
import csw.params.core.models.Prefix
import csw.params.events.{Event, EventKey, EventName, SystemEvent}
import scala.collection.mutable.ListBuffer
import com.tmt.tcs.mcs.samplemcsAssembly.EventMessage.StartEventSubscription
import java.time.{Duration, Instant, LocalDateTime, ZoneId}
import csw.params.core.generics.{Key, KeyType}

sealed trait EventMessage

object EventMessage {

  case class StartEventSubscription() extends EventMessage

}

object EventHandler {
  def createObject(eventService: EventService, loggerFactory: LoggerFactory): Behavior[EventMessage] =
    Behaviors.setup(
      ctx => EventHandler(ctx: ActorContext[EventMessage], eventService: EventService, loggerFactory: LoggerFactory)
    )
}

case class DemandPosHolder(pkPublishTime: Instant, assemblyRecTime: Instant)

case class EventHandler(ctx: ActorContext[EventMessage], eventService: EventService, loggerFactory: LoggerFactory)
    extends AbstractBehavior[EventMessage] {
  private val log                              = loggerFactory.getLogger
  private val PositionDemandKey: Set[EventKey] = Set(EventKey(Prefix("tcs.pk"), EventName("mcsdemandpositions")))
  private val TimeStampKey: Key[Instant]       = KeyType.TimestampKey.make("timeStamp")
  private val eventSubscriber                  = eventService.defaultSubscriber
  private var demandCounter: Int               = 0
  private var fileWritten: Boolean             = false
  private val demandBuffer                     = new ListBuffer[DemandPosHolder]()
  private val formatter: DateTimeFormatter     = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
  private val zoneFormat: String               = "UTC"

  override def onMessage(msg: EventMessage): Behavior[EventMessage] = {
    msg match {
      case _: StartEventSubscription => subscribeEventMsg()
    }
  }

  private def subscribeEventMsg(): Behavior[EventMessage] = {
    eventSubscriber.subscribeCallback(PositionDemandKey, event => processEvent(event))
    EventHandler.createObject(eventService, loggerFactory)
  }

  private def processEvent(systemEvent: Event): Unit = {
    systemEvent match {
      case event: SystemEvent =>
        val assemblyRecTime: Instant = Instant.now()
        val pkPublishTime: Instant   = event.get(TimeStampKey).get.head
        demandCounter = demandCounter + 1
        demandBuffer += DemandPosHolder(pkPublishTime, assemblyRecTime)
        if (demandCounter == 100000 && !fileWritten) {
          writeEventDemandDataToFile
        }

      case _ => log.error(s"Unable to map received position demands  to systemEvent: $systemEvent")
    }
  }

  private def writeEventDemandDataToFile = {
    val logFilePath: String       = System.getenv("LogFiles")
    val demandPosLogFile: File    = new File(logFilePath + "/PosDemEventSimpleLog_" + System.currentTimeMillis() + ".txt")
    val isDemFileCreated: Boolean = demandPosLogFile.createNewFile()
    log.error(s"Pos demand log file created ?: $isDemFileCreated")
    val printStream: PrintStream = new PrintStream(new FileOutputStream(demandPosLogFile), true)
    printStream.println("PK publish timeStamp(t0),Assembly receive timeStamp(t1),PK to Assembly time(t1-t0)")
    val demandPosList = demandBuffer.toList
    demandPosList.foreach(dp => {
      val pkToAssembly: Double = Duration.between(dp.pkPublishTime, dp.assemblyRecTime).toNanos.toDouble / 1000000
      val str: String          = s"${getDate(dp.pkPublishTime).trim},${getDate(dp.assemblyRecTime).trim},${pkToAssembly.toString.trim}"
      printStream.println(str)
    })
    log.error(s"Successfully written data to file: ${demandPosLogFile.getAbsolutePath}")
    printStream.flush()
    printStream.close()
    fileWritten = true
  }

  private def getDate(instant: Instant): String = LocalDateTime.ofInstant(instant, ZoneId.of(zoneFormat)).format(formatter)

}
