val appName = "tps-payments-backend"

val scalaV = "3.3.7"
scalaVersion := scalaV
val majorVer = 2
majorVersion := majorVer

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(commonSettings *)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    majorVersion    := majorVer,
    scalaVersion    := scalaV,
    libraryDependencies ++= AppDependencies.microserviceDependencies,
    routesGenerator := InjectedRoutesGenerator
  )
  .settings(WartRemoverSettings.wartRemoverSettingsPlay)
  .dependsOn(corJourney, corJourneyTestData)
  .aggregate(corJourney, corJourneyTestData)
  .settings(PlayKeys.playDefaultPort := 9125)
  .settings(commands ++= SbtCommands.commands)
  .settings(
    routesImport ++= Seq(
      "tps.model._",
      "tps.journey.model._",
      "tps.utils._"
    )
  )
  .settings(
    commands += Command.command("runTestOnly") { state =>
      state.globalLogging.full.info("running play using 'testOnlyDoNotUseInAppConf' routes...")
      s"""set javaOptions += "-Dplay.http.router=testOnlyDoNotUseInAppConf.Routes"""" ::
        "run" ::
        s"""set javaOptions -= "-Dplay.http.router=testOnlyDoNotUseInAppConf.Routes"""" ::
        state
    }
  )

lazy val corJourney = Project(appName + "-cor-journey", file("cor-journey"))
  .settings(commonSettings *)
  .settings(
    scalaVersion := scalaV,
    majorVersion := majorVer,
    libraryDependencies ++= List(
      "uk.gov.hmrc"       %% "bootstrap-common-play-30" % AppDependencies.bootstrapVersion % Provided,
      "com.beachape"      %% "enumeratum-play"          % AppDependencies.enumeratumPlayVersion,
      "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"       % AppDependencies.hmrcMongoVersion // for java Instant Json Formats
    )
  )

/** Collection Of Routines - test data
  */
lazy val corJourneyTestData = Project(appName + "-cor-journey-test-data", file("cor-journey-test-data"))
  .settings(commonSettings *)
  .settings(
    scalaVersion := scalaV,
    majorVersion := majorVer,
    libraryDependencies ++= List(
      "uk.gov.hmrc"       %% "bootstrap-common-play-30" % AppDependencies.bootstrapVersion % Provided,
      "org.playframework" %% "play"                     % play.core.PlayVersion.current    % Provided,
      "org.playframework" %% "play-test"                % play.core.PlayVersion.current    % Provided
    )
  )
  .dependsOn(corJourney)
  .aggregate(corJourney)

lazy val commonSettings: Seq[Def.SettingsDefinition] = Seq(
  majorVersion                  := majorVer,
  Compile / doc / scalacOptions := Seq(), // this will allow to have warnings in `doc` task
  Test / doc / scalacOptions    := Seq(), // this will allow to have warnings in `doc` task
  Compile / scalacOptions -= "utf8",
  scalacOptions ++= scalaCompilerOptions,
  scalafmtOnCompile             := true,
  scalacOptions ++= {
    if (StrictBuilding.strictBuilding.value) strictScalaCompilerOptions else Nil
  }
)
  .++(ScoverageSettings())
  .++(WartRemoverSettings.wartRemoverSettings)
  .++(SbtUpdatesSettings.sbtUpdatesSettings)

lazy val scalaCompilerOptions: Seq[String] = Seq(
  "-language:implicitConversions",
  "-language:reflectiveCalls",
  "-language:strictEquality",
  "-Wconf:src=routes/.*:s"
)

lazy val strictScalaCompilerOptions: Seq[String] = Seq(
  "-Xfatal-warnings",
  "-Wunused:implicits",
  "-Wunused:imports",
  "-Wunused:locals",
  "-Wunused:params",
  "-Wunused:patvars",
  "-Wunused:privates",
  "-deprecation",
  "-feature",
  "-unchecked"
)

lazy val strictBuilding: SettingKey[Boolean] =
  StrictBuilding.strictBuilding // defining here so it can be set before running sbt like `sbt 'set Global / strictBuilding := true' ...`
StrictBuilding.strictBuildingSetting
