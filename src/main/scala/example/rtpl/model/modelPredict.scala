package example.rtpl.model

import org.apache.spark.sql.SparkSession

import scala.util.Random
import example.rtpl.{s3rawDataPath, colNames}


object modelPredict {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().getOrCreate()
    import spark.implicits._

    val dataWithoutHeader = spark.read
      .option("inferSchema", value = true)
      .option("header", false)
      .csv(s3rawDataPath)
      .sample(false, 0.05, Random.nextInt())

    val data = dataWithoutHeader.toDF(colNames: _*).withColumn("Cover_Type", $"Cover_Type".cast("double"))

    val predictRDC = new PredictRFC(spark)
    predictRDC.predict(data)

  }

}


