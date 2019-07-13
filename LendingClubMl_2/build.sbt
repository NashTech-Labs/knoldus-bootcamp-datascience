name := "LendingClubMl"

version := "0.1"

scalaVersion := "2.11.12"

val sparkVersion="2.4.0"

resolvers += "fafad" at "http://repository.opendatagroup.com/maven"

//unmanagedJars in Compile += "/home/jouko/dev/software/jpmml-sparkml/target/jpmml-sparkml-1.5-SNAPSHOT.jar"

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test

libraryDependencies += "com.typesafe" % "config" % "1.3.2"

libraryDependencies += "log4j" % "log4j" % "1.2.17"

libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion % "provided"
libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion % "provided"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided"
libraryDependencies += "org.apache.spark" %% "spark-hive" % sparkVersion % "provided"

libraryDependencies += "com.ibm" %% "aardpfark" % "0.1.0-SNAPSHOT"

libraryDependencies += "com.opendatagroup" % "hadrian" % "0.8.3"

libraryDependencies += "ml.combust.mleap" %% "mleap-spark" % "0.13.0"

updateOptions := updateOptions.value.withLatestSnapshots(false)

