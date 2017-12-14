name := "rtplAWS"

version := "0.1"

scalaVersion := "2.11.8"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= {
  val sparkV = "2.2.0"
  Seq(
    "org.apache.spark" % "spark-core_2.11" % sparkV % "provided",
    "org.apache.spark" % "spark-sql_2.11" % sparkV % "provided",
    "org.apache.spark" % "spark-mllib_2.11" % sparkV % "provided",
    "org.apache.spark" % "spark-streaming_2.11" % sparkV % "provided",
    "org.apache.spark" % "spark-sql-kafka-0-10_2.11" % sparkV
  )
}

assemblyMergeStrategy in assembly := {
  case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
  case "log4j.properties" => MergeStrategy.discard
  case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
  case "reference.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}

mainClass in Compile := Some("example.rtpl.Stream")
