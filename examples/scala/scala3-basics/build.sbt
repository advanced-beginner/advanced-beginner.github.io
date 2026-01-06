val scala3Version = "3.3.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala3-basics",
    version := "0.1.0",
    scalaVersion := scala3Version,

    // 테스트 의존성
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test
  )
