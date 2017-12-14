package example.rtpl

import example.rtpl.model.PredictRFC
import org.apache.spark.sql.SparkSession

object Stream {

  def main (args: Array[String]) {
    val usage =
      """
        |host
      """.stripMargin
    if (args.length != 1)
      throw new java.lang.IllegalArgumentException("Missing argument")


    val topic = "model-data"
    val kbPort = 9092

    println("*** Starting to stream")

    // Create Spark Session
    val spark = SparkSession
      .builder
      .appName("Model Scoring Example")
      .config("spark.dynamicAllocation.minExecutors", 10)
      .config("spark.executor.cores", 5)
      .getOrCreate()

    // Connect to Kafka stream
    val ds = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", s"${args(0)}:$kbPort")
      .option("subscribe", topic)
      .option("startingOffsets", "earliest") // equivalent of auto.offset.reset which is not allowed here
      .option("failOnDataLoss", "false")
      .load()

    // Transformations and model scoring steps
    val rawData = ds.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")

    // ToDo: last column will include date and time. Include it in colNames and send only the ones to predict.
    // don't cast that column as double
    val splitCol = org.apache.spark.sql.functions.split(rawData.col("value"), ",")

    val parsedData = colNames.foldLeft(rawData)((df, name) =>
      df.withColumn(name, splitCol.getItem(colNames.indexOf(name)).cast("double"))
    )

    val predictRFC = new PredictRFC(spark)
    // ToDo: take out the time here
    val predictedData = predictRFC.predict(parsedData).select("key", "prediction")

    val query = predictedData
      .selectExpr("CAST(key as STRING)", "CAST(prediction as STRING) as value")
      .writeStream
      .format("kafka")
      .option("checkpointLocation", s3CheckpointingPath)
      .option("kafka.bootstrap.servers", s"${args(0)}:$kbPort")
      .option("topic", "model-prediction")
      .start()

    println("*** Done setting up streaming")
    query.awaitTermination()  // Wait until user kills application
  }
}
