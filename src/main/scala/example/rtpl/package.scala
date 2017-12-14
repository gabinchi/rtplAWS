package example

package object rtpl {
  // App level paths
  val s3rawDataPath = "s3://<fill with your own path>/covtype.data"
  val s3trainedModelPath = "s3://<fill with your own path>/rfc-model"
  val s3CheckpointingPath = "s3://<fill with your own path>/checkpoint"

  // Column names for dataset
  val colNames: Seq[String] = Seq(
    "Elevation", "Aspect", "Slope",
    "Horizontal_Distance_To_Hydrology", "Vertical_Distance_To_Hydrology",
    "Horizontal_Distance_To_Roadways",
    "Hillshade_9am", "Hillshade_Noon", "Hillshade_3pm",
    "Horizontal_Distance_To_Fire_Points"
  ) ++ (0 until 4).map(i => s"Wilderness_Area_$i") ++ (0 until 40).map(i => s"Soil_Type_$i") ++ Seq("Cover_Type")


}
