package example.rtpl.model

import org.apache.spark.ml.classification.{RandomForestClassificationModel, RandomForestClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{VectorAssembler, VectorIndexer}
import org.apache.spark.ml.tuning.{ParamGridBuilder, TrainValidationSplit}
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.sql.{DataFrame, SparkSession}
import example.rtpl.s3trainedModelPath

import scala.util.Random

class TrainRFC(private val spark: SparkSession) {

  def evaluateForest(trainData: DataFrame, testData: DataFrame): Unit = {
    // Reverse dummy transformation
    val unencTrainData = unencodeOneHot(trainData, spark)
    val unencTestData = unencodeOneHot(testData, spark)

    // Put features into vector column (required format for Spark ML models)
    val inputCols = unencTrainData.columns.filter(_ != "Cover_Type")
    val assembler = new VectorAssembler().
      setInputCols(inputCols).
      setOutputCol("featureVector")

    // Create indices for categorical variables
    val indexer = new VectorIndexer().
      setMaxCategories(40).
      setInputCol("featureVector").
      setOutputCol("indexedVector")

    // Setup Random Forest Classifier
    val classifier = new RandomForestClassifier().
      setSeed(Random.nextLong()).
      setLabelCol("Cover_Type").
      setFeaturesCol("indexedVector").
      setPredictionCol("prediction").
      setImpurity("entropy").
      setMaxDepth(20).
      setMaxBins(300)

    // Assemble Pipeline
    val pipeline = new Pipeline().setStages(Array(assembler, indexer, classifier))

    // Setup grid for Cross Validation
    val paramGrid = new ParamGridBuilder().
      addGrid(classifier.minInfoGain, Seq(0.0, 0.05)).
      addGrid(classifier.numTrees, Seq(1, 10)).
      build()

    // Setup model evaluator
    val multiclassEval = new MulticlassClassificationEvaluator().
      setLabelCol("Cover_Type").
      setPredictionCol("prediction").
      setMetricName("accuracy")

    // Setup Cross Validation
    val validator = new TrainValidationSplit().
      setSeed(Random.nextLong()).
      setEstimator(pipeline).
      setEvaluator(multiclassEval).
      setEstimatorParamMaps(paramGrid).
      setTrainRatio(0.9)

    // Fit pipeline and model
    val validatorModel = validator.fit(unencTrainData)

    // Get pipeline and best model in a single object and save them. This is what will be loaded
    // later by the inference process
    val bestModel = validatorModel.bestModel.asInstanceOf[PipelineModel]
    bestModel.write.overwrite().save(s3trainedModelPath)

    // *************************************************************************************
    // Pipeline and Model have been fitted at this point. We are just evaluating the model
    // *************************************************************************************

    // Get fitted model from pipeline
    val forestModel = bestModel.stages.last.asInstanceOf[RandomForestClassificationModel]

    // Print feature importances
    println(forestModel.extractParamMap)
    println(forestModel.getNumTrees)
    forestModel.featureImportances.toArray.zip(inputCols).
      sorted.reverse.foreach(println)

    // Get test set performance
    val testAccuracy = multiclassEval.evaluate(bestModel.transform(unencTestData))
    println(testAccuracy)

    // Print predictions
    bestModel.transform(unencTestData.drop("Cover_Type")).select("prediction").show()
  }
}

