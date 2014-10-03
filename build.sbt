import play.Project._

name := "esc"

version := "1.0"

playJavaSettings

libraryDependencies += javaEbean

libraryDependencies += "org.postgresql" % "postgresql" % "9.3-1100-jdbc4"