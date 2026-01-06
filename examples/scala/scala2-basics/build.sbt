val scala2Version = "2.13.12"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala2-basics",
    version := "0.1.0",
    scalaVersion := scala2Version,

    // 테스트 의존성
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.17" % Test
  )
