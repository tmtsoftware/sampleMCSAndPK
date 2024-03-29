@REM samplepkdeploy launcher script
@REM
@REM Environment:
@REM JAVA_HOME - location of a JDK home dir (optional if java on path)
@REM CFG_OPTS  - JVM options (optional)
@REM Configuration:
@REM SAMPLEPKDEPLOY_config.txt found in the SAMPLEPKDEPLOY_HOME.
@setlocal enabledelayedexpansion

@echo off


if "%SAMPLEPKDEPLOY_HOME%"=="" (
  set "APP_HOME=%~dp0\\.."

  rem Also set the old env name for backwards compatibility
  set "SAMPLEPKDEPLOY_HOME=%~dp0\\.."
) else (
  set "APP_HOME=%SAMPLEPKDEPLOY_HOME%"
)

set "APP_LIB_DIR=%APP_HOME%\lib\"

rem Detect if we were double clicked, although theoretically A user could
rem manually run cmd /c
for %%x in (!cmdcmdline!) do if %%~x==/c set DOUBLECLICKED=1

rem FIRST we load the config file of extra options.
set "CFG_FILE=%APP_HOME%\SAMPLEPKDEPLOY_config.txt"
set CFG_OPTS=
call :parse_config "%CFG_FILE%" CFG_OPTS

rem We use the value of the JAVACMD environment variable if defined
set _JAVACMD=%JAVACMD%

if "%_JAVACMD%"=="" (
  if not "%JAVA_HOME%"=="" (
    if exist "%JAVA_HOME%\bin\java.exe" set "_JAVACMD=%JAVA_HOME%\bin\java.exe"
  )
)

if "%_JAVACMD%"=="" set _JAVACMD=java

rem Detect if this java is ok to use.
for /F %%j in ('"%_JAVACMD%" -version  2^>^&1') do (
  if %%~j==java set JAVAINSTALLED=1
  if %%~j==openjdk set JAVAINSTALLED=1
)

rem BAT has no logical or, so we do it OLD SCHOOL! Oppan Redmond Style
set JAVAOK=true
if not defined JAVAINSTALLED set JAVAOK=false

if "%JAVAOK%"=="false" (
  echo.
  echo A Java JDK is not installed or can't be found.
  if not "%JAVA_HOME%"=="" (
    echo JAVA_HOME = "%JAVA_HOME%"
  )
  echo.
  echo Please go to
  echo   http://www.oracle.com/technetwork/java/javase/downloads/index.html
  echo and download a valid Java JDK and install before running samplepkdeploy.
  echo.
  echo If you think this message is in error, please check
  echo your environment variables to see if "java.exe" and "javac.exe" are
  echo available via JAVA_HOME or PATH.
  echo.
  if defined DOUBLECLICKED pause
  exit /B 1
)


rem We use the value of the JAVA_OPTS environment variable if defined, rather than the config.
set _JAVA_OPTS=%JAVA_OPTS%
if "!_JAVA_OPTS!"=="" set _JAVA_OPTS=!CFG_OPTS!

rem We keep in _JAVA_PARAMS all -J-prefixed and -D-prefixed arguments
rem "-J" is stripped, "-D" is left as is, and everything is appended to JAVA_OPTS
set _JAVA_PARAMS=
set _APP_ARGS=

set "APP_CLASSPATH=%APP_LIB_DIR%\com.tmt.tcs.samplepkdeploy-0.0.1.jar;%APP_LIB_DIR%\com.tmt.tcs.samplepkassembly-0.0.1.jar;%APP_LIB_DIR%\org.apache.commons.commons-lang3-3.6.jar;%APP_LIB_DIR%\com.thesamet.scalapb.lenses_2.12-0.8.2.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-actor-typed_2.12-2.5.18.jar;%APP_LIB_DIR%\org.antlr.antlr-runtime-3.4.jar;%APP_LIB_DIR%\org.ow2.asm.asm-util-5.0.3.jar;%APP_LIB_DIR%\org.scala-lang.scala-reflect-2.12.7.jar;%APP_LIB_DIR%\com.google.guava.guava-22.0.jar;%APP_LIB_DIR%\com.typesafe.play.play_2.12-2.6.12.jar;%APP_LIB_DIR%\org.lmdbjava.lmdbjava-0.6.1.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-remote_2.12-2.5.18.jar;%APP_LIB_DIR%\com.typesafe.ssl-config-core_2.12-0.3.6.jar;%APP_LIB_DIR%\org.tmatesoft.svnkit.svnkit-1.9.3.jar;%APP_LIB_DIR%\javax.mail.mailapi-1.4.3.jar;%APP_LIB_DIR%\com.typesafe.play.play-json_2.12-2.6.10.jar;%APP_LIB_DIR%\com.github.tmtsoftware.csw.csw-network-utils_2.12-0.6.0.jar;%APP_LIB_DIR%\com.jcraft.jsch.agentproxy.pageant-0.0.7.jar;%APP_LIB_DIR%\de.heikoseeberger.akka-http-play-json_2.12-1.22.0.jar;%APP_LIB_DIR%\io.netty.netty-codec-4.1.24.Final.jar;%APP_LIB_DIR%\io.aeron.aeron-driver-1.11.2.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-http_2.12-10.1.5.jar;%APP_LIB_DIR%\com.github.tmtsoftware.csw.csw-command-client_2.12-0.6.0.jar;%APP_LIB_DIR%\com.github.tmtsoftware.csw.csw-event-client_2.12-0.6.0.jar;%APP_LIB_DIR%\com.github.jnr.jffi-1.2.16-native.jar;%APP_LIB_DIR%\com.github.fge.btf-1.2.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-cluster_2.12-2.5.18.jar;%APP_LIB_DIR%\com.beachape.enumeratum-play-json_2.12-1.5.14.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-actor_2.12-2.5.18.jar;%APP_LIB_DIR%\com.lihaoyi.fastparse-utils_2.12-1.0.0.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-slf4j_2.12-2.5.18.jar;%APP_LIB_DIR%\io.jsonwebtoken.jjwt-0.7.0.jar;%APP_LIB_DIR%\com.github.tmtsoftware.csw.csw-config-server_2.12-0.6.0.jar;%APP_LIB_DIR%\com.lihaoyi.sourcecode_2.12-0.1.4.jar;%APP_LIB_DIR%\com.github.tmtsoftware.csw.csw-config-client_2.12-0.6.0.jar;%APP_LIB_DIR%\com.github.fge.uri-template-0.9.jar;%APP_LIB_DIR%\com.thesamet.scalapb.scalapb-runtime_2.12-0.8.2.jar;%APP_LIB_DIR%\org.mozilla.rhino-1.7R4.jar;%APP_LIB_DIR%\io.netty.netty-resolver-4.1.24.Final.jar;%APP_LIB_DIR%\org.lz4.lz4-java-1.4.jar;%APP_LIB_DIR%\com.typesafe.play.play-streams_2.12-2.6.12.jar;%APP_LIB_DIR%\com.github.fge.jackson-coreutils-1.8.jar;%APP_LIB_DIR%\com.github.tmtsoftware.csw.csw-command-api_2.12-0.6.0.jar;%APP_LIB_DIR%\com.jcraft.jsch.agentproxy.svnkit-trilead-ssh2-0.0.7.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-actor-testkit-typed_2.12-2.5.18.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-testkit_2.12-2.5.18.jar;%APP_LIB_DIR%\com.github.tmtsoftware.csw.csw-params_2.12-0.6.0.jar;%APP_LIB_DIR%\com.github.tmtsoftware.csw.csw-alarm-api_2.12-0.6.0.jar;%APP_LIB_DIR%\com.google.j2objc.j2objc-annotations-1.1.jar;%APP_LIB_DIR%\com.jcraft.jsch.agentproxy.core-0.0.7.jar;%APP_LIB_DIR%\org.slf4j.jul-to-slf4j-1.7.25.jar;%APP_LIB_DIR%\commons-codec.commons-codec-1.10.jar;%APP_LIB_DIR%\com.github.jnr.jnr-x86asm-1.0.2.jar;%APP_LIB_DIR%\com.fasterxml.jackson.core.jackson-core-2.8.11.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-persistence_2.12-2.5.6.jar;%APP_LIB_DIR%\io.netty.netty-common-4.1.24.Final.jar;%APP_LIB_DIR%\org.reactivestreams.reactive-streams-1.0.2.jar;%APP_LIB_DIR%\com.twitter.chill-akka_2.12-0.9.3.jar;%APP_LIB_DIR%\ch.qos.logback.logback-classic-1.2.3.jar;%APP_LIB_DIR%\org.scala-lang.scala-library-2.12.7.jar;%APP_LIB_DIR%\io.netty.netty-handler-4.1.24.Final.jar;%APP_LIB_DIR%\com.google.errorprone.error_prone_annotations-2.0.18.jar;%APP_LIB_DIR%\com.github.tmtsoftware.csw.csw-location-api_2.12-0.6.0.jar;%APP_LIB_DIR%\com.github.fge.msg-simple-1.1.jar;%APP_LIB_DIR%\com.typesafe.play.play-functional_2.12-2.6.10.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-distributed-data_2.12-2.5.18.jar;%APP_LIB_DIR%\com.twitter.chill_2.12-0.9.3.jar;%APP_LIB_DIR%\com.typesafe.config-1.3.3.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-stream-kafka_2.12-0.22.jar;%APP_LIB_DIR%\com.esotericsoftware.kryo-shaded-4.0.2.jar;%APP_LIB_DIR%\io.netty.netty-transport-4.1.24.Final.jar;%APP_LIB_DIR%\org.scala-lang.modules.scala-java8-compat_2.12-0.9.0.jar;%APP_LIB_DIR%\net.java.dev.jna.platform-3.4.0.jar;%APP_LIB_DIR%\com.jcraft.jsch.agentproxy.sshagent-0.0.7.jar;%APP_LIB_DIR%\org.scala-lang.modules.scala-parser-combinators_2.12-1.1.1.jar;%APP_LIB_DIR%\com.github.tmtsoftware.csw.csw-framework_2.12-0.6.0.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-stream_2.12-2.5.18.jar;%APP_LIB_DIR%\io.netty.netty-3.10.6.Final.jar;%APP_LIB_DIR%\com.github.tmtsoftware.csw.csw-logging_2.12-0.6.0.jar;%APP_LIB_DIR%\net.sf.jopt-simple.jopt-simple-4.6.jar;%APP_LIB_DIR%\com.jcraft.jsch.agentproxy.usocket-jna-0.0.7.jar;%APP_LIB_DIR%\com.github.jnr.jnr-constants-0.9.9.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-http-core_2.12-10.1.5.jar;%APP_LIB_DIR%\com.beachape.enumeratum-macros_2.12-1.5.9.jar;%APP_LIB_DIR%\de.regnis.q.sequence.sequence-library-1.0.3.jar;%APP_LIB_DIR%\com.github.tmtsoftware.csw.csw-location-client_2.12-0.6.0.jar;%APP_LIB_DIR%\com.github.tmtsoftware.csw.csw-logging-macros_2.12-0.6.0.jar;%APP_LIB_DIR%\org.objenesis.objenesis-2.5.1.jar;%APP_LIB_DIR%\org.ow2.asm.asm-5.0.3.jar;%APP_LIB_DIR%\com.github.fge.json-schema-validator-2.2.6.jar;%APP_LIB_DIR%\io.lettuce.lettuce-core-5.0.5.RELEASE.jar;%APP_LIB_DIR%\org.apache.kafka.kafka-clients-1.0.1.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-cluster-tools_2.12-2.5.18.jar;%APP_LIB_DIR%\com.persist.persist-json_2.12-1.2.1.jar;%APP_LIB_DIR%\org.ow2.asm.asm-analysis-5.0.3.jar;%APP_LIB_DIR%\com.google.code.findbugs.jsr305-3.0.0.jar;%APP_LIB_DIR%\com.github.jnr.jnr-ffi-2.1.7.jar;%APP_LIB_DIR%\com.fasterxml.jackson.datatype.jackson-datatype-jsr310-2.8.11.jar;%APP_LIB_DIR%\org.scala-lang.modules.scala-xml_2.12-1.0.6.jar;%APP_LIB_DIR%\com.github.tmtsoftware.csw.csw-alarm-client_2.12-0.6.0.jar;%APP_LIB_DIR%\net.java.dev.jna.jna-platform-4.1.0.jar;%APP_LIB_DIR%\org.codehaus.mojo.animal-sniffer-annotations-1.14.jar;%APP_LIB_DIR%\io.netty.netty-buffer-4.1.24.Final.jar;%APP_LIB_DIR%\org.slf4j.jcl-over-slf4j-1.7.25.jar;%APP_LIB_DIR%\net.java.dev.jna.jna-4.1.0.jar;%APP_LIB_DIR%\com.fasterxml.jackson.core.jackson-annotations-2.8.11.jar;%APP_LIB_DIR%\com.chuusai.shapeless_2.12-2.3.3.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-parsing_2.12-10.1.5.jar;%APP_LIB_DIR%\io.projectreactor.reactor-core-3.1.6.RELEASE.jar;%APP_LIB_DIR%\com.trilead.trilead-ssh2-1.0.0-build221.jar;%APP_LIB_DIR%\com.twitter.chill-java-0.9.3.jar;%APP_LIB_DIR%\org.scala-lang.modules.scala-async_2.12-0.9.7.jar;%APP_LIB_DIR%\com.github.tmtsoftware.csw.csw-config-api_2.12-0.6.0.jar;%APP_LIB_DIR%\com.typesafe.play.play-netty-utils-2.6.12.jar;%APP_LIB_DIR%\org.julienrf.play-json-derived-codecs_2.12-4.0.1.jar;%APP_LIB_DIR%\com.github.jnr.jffi-1.2.16.jar;%APP_LIB_DIR%\javax.transaction.jta-1.1.jar;%APP_LIB_DIR%\com.beachape.enumeratum-play_2.12-1.5.14.jar;%APP_LIB_DIR%\org.agrona.agrona-0.9.25.jar;%APP_LIB_DIR%\javax.inject.javax.inject-1.jar;%APP_LIB_DIR%\org.slf4j.slf4j-api-1.7.25.jar;%APP_LIB_DIR%\joda-time.joda-time-2.10.1.jar;%APP_LIB_DIR%\org.typelevel.macro-compat_2.12-1.1.1.jar;%APP_LIB_DIR%\com.typesafe.play.build-link-2.6.12.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-protobuf_2.12-2.5.18.jar;%APP_LIB_DIR%\com.github.tmtsoftware.csw.csw-location-server_2.12-0.6.0.jar;%APP_LIB_DIR%\com.github.fge.json-schema-core-1.2.5.jar;%APP_LIB_DIR%\com.googlecode.libphonenumber.libphonenumber-6.2.jar;%APP_LIB_DIR%\ch.qos.logback.logback-core-1.2.3.jar;%APP_LIB_DIR%\javax.activation.activation-1.1.jar;%APP_LIB_DIR%\com.beachape.enumeratum_2.12-1.5.13.jar;%APP_LIB_DIR%\com.typesafe.play.twirl-api_2.12-1.3.14.jar;%APP_LIB_DIR%\com.jcraft.jsch.agentproxy.connector-factory-0.0.7.jar;%APP_LIB_DIR%\com.github.tmtsoftware.csw.csw-commons_2.12-0.6.0.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-cluster-typed_2.12-2.5.18.jar;%APP_LIB_DIR%\com.jcraft.jsch.agentproxy.usocket-nc-0.0.7.jar;%APP_LIB_DIR%\com.google.protobuf.protobuf-java-3.6.1.jar;%APP_LIB_DIR%\com.github.scopt.scopt_2.12-3.7.0.jar;%APP_LIB_DIR%\com.lightbend.akka.akka-management-cluster-http_2.12-0.6.jar;%APP_LIB_DIR%\com.typesafe.play.play-exceptions-2.6.12.jar;%APP_LIB_DIR%\io.aeron.aeron-client-1.11.2.jar;%APP_LIB_DIR%\org.xerial.snappy.snappy-java-1.1.4.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-cluster-sharding_2.12-2.5.6.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-http-spray-json_2.12-10.0.10.jar;%APP_LIB_DIR%\com.github.tmtsoftware.csw.csw-event-api_2.12-0.6.0.jar;%APP_LIB_DIR%\com.github.tmtsoftware.csw.romaine_2.12-0.6.0.jar;%APP_LIB_DIR%\com.esotericsoftware.minlog-1.3.0.jar;%APP_LIB_DIR%\com.lihaoyi.fastparse_2.12-1.0.0.jar;%APP_LIB_DIR%\com.fasterxml.jackson.datatype.jackson-datatype-jdk8-2.8.11.jar;%APP_LIB_DIR%\org.ow2.asm.asm-tree-5.0.3.jar;%APP_LIB_DIR%\com.fasterxml.jackson.core.jackson-databind-2.8.11.1.jar;%APP_LIB_DIR%\org.ow2.asm.asm-commons-5.0.3.jar;%APP_LIB_DIR%\org.tmatesoft.sqljet.sqljet-1.1.11.jar;%APP_LIB_DIR%\io.spray.spray-json_2.12-1.3.3.jar"
set "APP_MAIN_CLASS=com.tmt.tcs.pk.PkContainerCmdApp"
set "SCRIPT_CONF_FILE=%APP_HOME%\conf\application.ini"

rem if configuration files exist, prepend their contents to the script arguments so it can be processed by this runner
call :parse_config "%SCRIPT_CONF_FILE%" SCRIPT_CONF_ARGS

call :process_args %SCRIPT_CONF_ARGS% %%*

set _JAVA_OPTS=!_JAVA_OPTS! !_JAVA_PARAMS!

if defined CUSTOM_MAIN_CLASS (
    set MAIN_CLASS=!CUSTOM_MAIN_CLASS!
) else (
    set MAIN_CLASS=!APP_MAIN_CLASS!
)

rem Call the application and pass all arguments unchanged.
"%_JAVACMD%" !_JAVA_OPTS! !SAMPLEPKDEPLOY_OPTS! -cp "%APP_CLASSPATH%" %MAIN_CLASS% !_APP_ARGS!

@endlocal

exit /B %ERRORLEVEL%


rem Loads a configuration file full of default command line options for this script.
rem First argument is the path to the config file.
rem Second argument is the name of the environment variable to write to.
:parse_config
  set _PARSE_FILE=%~1
  set _PARSE_OUT=
  if exist "%_PARSE_FILE%" (
    FOR /F "tokens=* eol=# usebackq delims=" %%i IN ("%_PARSE_FILE%") DO (
      set _PARSE_OUT=!_PARSE_OUT! %%i
    )
  )
  set %2=!_PARSE_OUT!
exit /B 0


:add_java
  set _JAVA_PARAMS=!_JAVA_PARAMS! %*
exit /B 0


:add_app
  set _APP_ARGS=!_APP_ARGS! %*
exit /B 0


rem Processes incoming arguments and places them in appropriate global variables
:process_args
  :param_loop
  call set _PARAM1=%%1
  set "_TEST_PARAM=%~1"

  if ["!_PARAM1!"]==[""] goto param_afterloop


  rem ignore arguments that do not start with '-'
  if "%_TEST_PARAM:~0,1%"=="-" goto param_java_check
  set _APP_ARGS=!_APP_ARGS! !_PARAM1!
  shift
  goto param_loop

  :param_java_check
  if "!_TEST_PARAM:~0,2!"=="-J" (
    rem strip -J prefix
    set _JAVA_PARAMS=!_JAVA_PARAMS! !_TEST_PARAM:~2!
    shift
    goto param_loop
  )

  if "!_TEST_PARAM:~0,2!"=="-D" (
    rem test if this was double-quoted property "-Dprop=42"
    for /F "delims== tokens=1,*" %%G in ("!_TEST_PARAM!") DO (
      if not ["%%H"] == [""] (
        set _JAVA_PARAMS=!_JAVA_PARAMS! !_PARAM1!
      ) else if [%2] neq [] (
        rem it was a normal property: -Dprop=42 or -Drop="42"
        call set _PARAM1=%%1=%%2
        set _JAVA_PARAMS=!_JAVA_PARAMS! !_PARAM1!
        shift
      )
    )
  ) else (
    if "!_TEST_PARAM!"=="-main" (
      call set CUSTOM_MAIN_CLASS=%%2
      shift
    ) else (
      set _APP_ARGS=!_APP_ARGS! !_PARAM1!
    )
  )
  shift
  goto param_loop
  :param_afterloop

exit /B 0
