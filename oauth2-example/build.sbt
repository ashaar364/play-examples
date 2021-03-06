name := "oauth2-example"

version := "1.0.0-SNAPSHOT"

lazy val root = project in file(".") enablePlugins(PlayJava)

libraryDependencies ++= Seq(
  "me.tfeng.play-plugins" % "oauth2-plugin" % "0.4.0",
  javaWs % "test",
  "me.tfeng.play-plugins" % "spring-test" % "0.4.0" % "test"
)
