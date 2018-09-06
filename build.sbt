inThisBuild(List(
  organization := "com.pepegar",
  homepage := Some(url("https://github.com/pepegar/wisdel")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "pepegar",
      "Pepe Garcia",
      "pepe@pepegar.com",
      url("https://pepegar.com")
    )
  )
))

lazy val core = project.in(file("."))
    .settings(commonSettings)
    .settings(
      name := "wisdel"
    )

lazy val V = new {
  val cats = "1.3.1"
  val kittens = "1.1.1"
  val catsEffect = "1.0.0"
  val mouse = "0.18"
  val shapeless = "2.3.3"
  val fs2 = "0.10.5"
  val circe = "0.9.3"
  val droste = "0.4.0"
  val specs2 = "4.3.4"
  val discipline = "0.10.0"
}


lazy val contributors = Seq(
  "pepegar" -> "Pepe Garcia"
)

// check for library updates whenever the project is [re]load
onLoad in Global := { s =>
  "dependencyUpdates" :: s
}

// General Settings
lazy val commonSettings = Seq(
  organization := "com.pepegar",

  scalaVersion := "2.12.6",
  crossScalaVersions := Seq(scalaVersion.value, "2.11.12"),
  scalafmtOnCompile in ThisBuild := true,

  addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.7" cross CrossVersion.binary),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4"),
  libraryDependencies ++= Seq(
    "org.typelevel"               %% "cats-core"                  % V.cats,

    "org.typelevel"               %% "kittens"                    % V.kittens,
    "org.typelevel"               %% "alleycats-core"             % V.cats,
    "org.typelevel"               %% "mouse"                      % V.mouse,

    "org.typelevel"               %% "cats-effect"                % V.catsEffect,

    "com.chuusai"                 %% "shapeless"                  % V.shapeless,

    "co.fs2"                      %% "fs2-core"                   % V.fs2,
    "co.fs2"                      %% "fs2-io"                     % V.fs2,

    "io.circe"                    %% "circe-core"                 % V.circe,
    "io.circe"                    %% "circe-generic"              % V.circe,
    "io.circe"                    %% "circe-parser"               % V.circe,

    "org.specs2"                  %% "specs2-core"                % V.specs2       % Test,
    "org.specs2"                  %% "specs2-scalacheck"          % V.specs2       % Test,
    "org.typelevel"               %% "discipline"                 % V.discipline   % Test,
  )
)

lazy val mimaSettings = {
  import sbtrelease.Version

  def semverBinCompatVersions(major: Int, minor: Int, patch: Int): Set[(Int, Int, Int)] = {
    val majorVersions: List[Int] = List(major)
    val minorVersions : List[Int] = 
      if (major >= 1) Range(0, minor).inclusive.toList
      else List(minor)
    def patchVersions(currentMinVersion: Int): List[Int] = 
      if (minor == 0 && patch == 0) List.empty[Int]
      else if (currentMinVersion != minor) List(0)
      else Range(0, patch - 1).inclusive.toList

    val versions = for {
      maj <- majorVersions
      min <- minorVersions
      pat <- patchVersions(min)
    } yield (maj, min, pat)
    versions.toSet
  }

  def mimaVersions(version: String): Set[String] = {
    Version(version) match {
      case Some(Version(major, Seq(minor, patch), _)) =>
        semverBinCompatVersions(major.toInt, minor.toInt, patch.toInt)
          .map{case (maj, min, pat) => maj.toString + "." + min.toString + "." + pat.toString}
      case _ =>
        Set.empty[String]
    }
  }
  // Safety Net For Exclusions
  lazy val excludedVersions: Set[String] = Set()

  // Safety Net for Inclusions
  lazy val extraVersions: Set[String] = Set()

  Seq(
    mimaFailOnProblem := mimaVersions(version.value).toList.headOption.isDefined,
    mimaPreviousArtifacts := (mimaVersions(version.value) ++ extraVersions)
      .filterNot(excludedVersions.contains(_))
      .map{v => 
        val moduleN = moduleName.value + "_" + scalaBinaryVersion.value.toString
        organization.value % moduleN % v
      },
    mimaBinaryIssueFilters ++= {
      import com.typesafe.tools.mima.core._
      import com.typesafe.tools.mima.core.ProblemFilters._
      Seq()
    }
  )
}

lazy val skipOnPublishSettings = Seq(
  skip in publish := true,
  publish := (()),
  publishLocal := (()),
  publishArtifact := false,
  publishTo := None
)
