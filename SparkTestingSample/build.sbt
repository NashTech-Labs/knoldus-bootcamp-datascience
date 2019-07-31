
name := "SparkTestingBase"

version := "1.0"

scalaVersion := "2.11.0"

val sparkVersion = "2.4.0"


resolvers ++= Seq(
  "apache-snapshots" at "http://repository.apache.org/snapshots/"
)

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-hive" % sparkVersion % "provided",
  "io.netty" % "netty-all" % "4.1.17.Final" % "provided",
  "org.apache.hadoop" % "hadoop-aws" % "2.8.2" % "provided", 
  "io.netty" % "netty-all" % "4.1.17.Final" % "provided",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.8.7" % "provided"
)

libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3" % "1.11.386" % "provided"
libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.6" % "provided"
libraryDependencies += "com.amazonaws" % "aws-java-sdk-kms" % "1.11.386" % "provided"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.8.7" % "provided"
libraryDependencies += "com.holdenkarau" %% "spark-testing-base" % "2.4.0_0.11.0" % Test

assemblyMergeStrategy in assembly := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case x => MergeStrategy.first
}
parallelExecution in Test := false

fork in Test := true
javaOptions ++= Seq("-Xms1024M", "-Xmx4096M", "-XX:MaxPermSize=4096M", "-XX:+CMSClassUnloadingEnabled")
