import play.Project._

name := "esc"

version := "1.0"

playJavaSettings

libraryDependencies += javaEbean

libraryDependencies += "org.postgresql" % "postgresql" % "9.3-1100-jdbc4"

resolvers += "Atlassian's Maven Public Repository" at "https://maven.atlassian.com/content/groups/public"

resolvers += "Local Maven Repository" at "file://" + Path.userHome + "/.m2/repository"

libraryDependencies ++= Seq(
  javaCore,
  javaJpa,
  "com.atlassian.connect" % "ac-play-java_2.10" % "0.10.1" withSources()
)