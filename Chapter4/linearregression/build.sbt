import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.elbauldelprogramador",
      scalaVersion := "2.11.8",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "TDD-LinearRegression",
    libraryDependencies ++= Seq(
      "org.log4s" %% "log4s" % "1.3.4",
      "ch.qos.logback" % "logback-classic" % "1.1.8",
      "org.specs2" %% "specs2-core" % "3.8.8" % "test",
      "org.specs2" %% "specs2-gwt" % "3.8.8" % "test",
      "org.apache.spark" %% "spark-mllib" % "2.1.0"
    ),
    resolvers ++= Seq(
      "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
      "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"
    )
  )
