
val scalaTestVersion    = "3.2.8"
val scalaMockVersion    = "5.1.0"

val commonSettings = Seq(
  version := "1.0",

  scalaVersion := "2.13.5",

  scalacOptions := Seq(
    "-encoding",
    "utf8",
    "-feature",
    "-unchecked",
    "-deprecation",
    "-target:jvm-1.8",
    "-language:_"
  ),

  libraryDependencies += "org.typelevel" %% "cats-effect" % "3.1.0",
  libraryDependencies += "org.scalamock" %% "scalamock" % scalaMockVersion % Test,
  libraryDependencies += "org.scalatest" %% "scalatest" % scalaTestVersion % Test
)

lazy val root = project.in(file(".")).settings(commonSettings).aggregate(
  demo,
  workshop
)

lazy val demo = project.in(file("./demo")).settings(commonSettings)
lazy val workshop = project.in(file("./workshop")).settings(commonSettings)