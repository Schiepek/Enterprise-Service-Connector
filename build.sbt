import play.Project._

name := "esc"

version := "1.0"

scalaVersion := "2.10.3"

playJavaSettings

//libraryDependencies += javaEbean

libraryDependencies += "org.postgresql" % "postgresql" % "9.3-1100-jdbc4"

resolvers += "Atlassian's Maven Public Repository" at "https://maven.atlassian.com/content/groups/public"

resolvers += "Local Maven Repository" at "file://" + Path.userHome + "/.m2/repository"

libraryDependencies ++= Seq(
  javaCore,
  javaJpa,
//  "org.hibernate" % "hibernate-core" % "4.2.3.Final",
//  "org.hibernate" % "hibernate-entitymanager" % "4.2.3.Final"
  "com.atlassian.connect" % "ac-play-java_2.10" % "0.10.1" withSources()
)