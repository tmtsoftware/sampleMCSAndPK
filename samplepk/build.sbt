lazy val aggregatedProjects: Seq[ProjectReference] = Seq(
  `samplepkAssembly`,
  `samplepkDeploy`
)

lazy val `samplepk` = project
  .in(file("."))
  .aggregate(aggregatedProjects: _*)

lazy val `samplepkAssembly` = project
  .settings(
    libraryDependencies ++= Dependencies.SamplepkAssembly
  )

lazy val `samplepkDeploy` = project
  .dependsOn(
    `samplepkAssembly`
  )
  .enablePlugins(JavaAppPackaging, CswBuildInfo)
  .settings(
    libraryDependencies ++= Dependencies.SamplepkDeploy
  )
