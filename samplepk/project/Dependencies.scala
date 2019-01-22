import sbt._

object Dependencies {

  val SamplepkAssembly = Seq(
    CSW.`csw-framework`,
    CSW.`csw-testkit` % Test,
    Libs.`scalatest` % Test,
    Libs.`junit` % Test,
    Libs.`junit-interface` % Test
  )


  val SamplepkDeploy = Seq(
    CSW.`csw-framework`,
    CSW.`csw-testkit` % Test
  )
}
