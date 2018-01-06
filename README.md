# rtplAWS

The purpose of this project was to test the deployment of a Spark ML model with Spark Streaming and Apache Kafka in AWS. This project has two components. The first component is the [simulatorAWS](https://github.com/gabinchi/simulatorAWS/). This is the second component which essentially does the following:
1.  Using Spark Streaming, consume records from a Kafka topic where the strings represent an instance to be predicted.
2.  Parse and transform the records.
3.  Make the prediction using an already trained Random Forest classifier from Spark ML.
4.  Publish the records back to Kafka.

This project also includes the code to train the model which closely resembles the example in Chapter 4 of

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Ryza, Sandy, et al. Advanced Analytics with Spark. O'Reilly, 2017.
