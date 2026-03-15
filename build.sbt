val scala3Version = "3.5.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "very-basic",
    version := "ฯ0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    Compile / unmanagedSourceDirectories += (Compile / sourceDirectory).value / "foundations",
    libraryDependencies += "org.typelevel" %% "cats-core" % "2.10.0",
    libraryDependencies += "org.typelevel" %% "cats-effect" % "3.5.4",
    libraryDependencies += "com.github.cb372" %% "cats-retry" % "4.0.0",
    libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.2.0",
    libraryDependencies += "io.circe" %% "circe-core" % "0.14.6",
    libraryDependencies += "io.circe" %% "circe-parser" % "0.14.6",
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test,
    libraryDependencies += "org.typelevel" %% "cats-effect-testkit" % "3.6.3",
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-language:strictEquality",
      "-indent"
    )
  )
