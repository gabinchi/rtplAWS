package example.rtpl.model

import org.apache.spark.sql.SparkSession

import scala.util.Random
import example.rtpl.{s3rawDataPath, colNames}

object modelTrain {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().getOrCreate()
    spark.sparkContext.setLogLevel("WARN")
    import spark.implicits._

    // Load data
    val dataWithoutHeader = spark.read
      .option("inferSchema", true)
      .option("header", false)
      .csv(s3rawDataPath)
      .sample(false, 0.2, Random.nextLong())

    // Add column names and cast Cover_Type
    val data = dataWithoutHeader.toDF(colNames: _*).withColumn("Cover_Type", $"Cover_Type".cast("double"))

    // Split train/test
    val Array(trainData, testData) = data.randomSplit(Array(0.9, 0.1))
    trainData.cache()
    testData.cache()

    // Train Model
    val runDF = new TrainRFC(spark)
    runDF.evaluateForest(trainData, testData)
  }

}