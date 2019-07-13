name := "OpenCVSpark"

version := "0.1"

scalaVersion := "2.11.12"

unmanagedJars in Compile += file("lib/opencv-2413.jar")

libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.0"
