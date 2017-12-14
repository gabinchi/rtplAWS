package example.rtpl.model

import example.rtpl.{s3trainedModelPath}
import org.apache.spark.ml.PipelineModel
import org.apache.spark.sql.{DataFrame, SparkSession}

class PredictRFC(private val spark: SparkSession) {
  // Load learned model
  val modelX: PipelineModel = PipelineModel.load(s3trainedModelPath)

  // Run prediction
  def predict(data: DataFrame): DataFrame = {
    val unencData = unencodeOneHot(data, spark)
    modelX.transform(unencData.drop("Cover_Type"))
  }
}
