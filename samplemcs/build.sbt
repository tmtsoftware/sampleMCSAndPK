lazy val aggregatedProjects: Seq[ProjectReference] = Seq(
  `samplemcsAssembly`,
  `samplemcsHCD`,
  `samplemcsDeploy`
)

lazy val `samplemcs` = project
  .in(file("."))
  .aggregate(aggregatedProjects: _*)

lazy val `samplemcsAssembly` = project
  .settings(
    libraryDependencies ++= Dependencies.SamplemcsAssembly
  )

lazy val `samplemcsHCD` = project
  .settings(
    libraryDependencies ++= Dependencies.SamplemcsHcd
  )

lazy val `samplemcsDeploy` = project
  .dependsOn(
    `samplemcsAssembly`,
    `samplemcsHCD`
  )
  .enablePlugins(JavaAppPackaging, CswBuildInfo)
  .settings(
    libraryDependencies ++= Dependencies.SamplemcsDeploy
  )
