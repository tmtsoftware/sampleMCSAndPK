package com.tmt.tcs.mcs.samplemcsAssembly

import csw.location.api.models.Connection.AkkaConnection
import csw.location.api.models.{ComponentId, ComponentType}
import csw.testkit.scaladsl.CSWService.{AlarmServer, EventServer}
import csw.testkit.scaladsl.ScalaTestFrameworkTestKit
import org.scalatest.FunSuiteLike

import scala.concurrent.Await
import scala.concurrent.duration._


class SamplemcsAssemblyTest extends ScalaTestFrameworkTestKit(AlarmServer, EventServer) with FunSuiteLike {

  import frameworkTestKit.frameworkWiring._

  override def beforeAll(): Unit = {
    super.beforeAll()
    // uncomment if you want one Assembly run for all tests
    spawnStandalone(com.typesafe.config.ConfigFactory.load("SamplemcsAssemblyStandalone.conf"))
  }

  test("Assembly should be locatable using Location Service") {
    val connection = AkkaConnection(ComponentId("SamplemcsAssembly", ComponentType.Assembly))
    val akkaLocation = Await.result(locationService.resolve(connection, 10.seconds), 10.seconds).get

    akkaLocation.connection shouldBe connection
  }
}