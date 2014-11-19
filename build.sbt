import play.Project._

name := "esc"

version := "1.0"

scalaVersion := "2.10.3"

playJavaSettings

resolvers += "Atlassian's Maven Public Repository" at "https://maven.atlassian.com/content/groups/public"

resolvers += "Local Maven Repository" at "file://" + Path.userHome + "/.m2/repository"

libraryDependencies ++= Seq(
  javaCore,
  javaJpa,
  "com.atlassian.connect" % "ac-play-java_2.10" % "0.10.1" withSources(),
  "mysql" % "mysql-connector-java" % "5.1.19",
  "org.hibernate" % "hibernate-entitymanager" % "4.1.2.Final"
)