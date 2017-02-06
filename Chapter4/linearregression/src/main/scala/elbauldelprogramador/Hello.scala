package elbauldelprogramador

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.mllib.linalg.VectorUDT
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.types.StructType

object SimpleApp {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder.
      master("local")
      .appName("spark session example")
      .getOrCreate()

    val df = spark.read.format("csv").
      option("header", "true").
      load(this.getClass.getResource("/generated_data.csv").getPath)

    df.printSchema()

    val schema = new StructType()
     .add("features", new VectorUDT())

    val toDouble = udf[Double, String]( _.toDouble)
    val df2 = df.withColumn("dependent_var", toDouble(df("dependent_var")))

    val assembler = new VectorAssembler().
      setInputCols(Array("dependent_var")).
      setOutputCol("features")

    val out = assembler.transform(df2)

    df2.printSchema()
    out.printSchema()


    val lr = new LinearRegression()
      .setMaxIter(10)
      .setRegParam(0.3)
      .setElasticNetParam(0.8)

    // Fit the model
    val lrModel = lr.fit(out)

    // // Print the coefficients and intercept for linear regression
    // println(s"Coefficients: ${lrModel.coefficients} Intercept: ${lrModel.intercept}")

    // // Summarize the model over the training set and print out some metrics
    // val trainingSummary = lrModel.summary
    // println(s"numIterations: ${trainingSummary.totalIterations}")
    // println(s"objectiveHistory: [${trainingSummary.objectiveHistory.mkString(",")}]")
    // trainingSummary.residuals.show()
    // println(s"RMSE: ${trainingSummary.rootMeanSquaredError}")
    // println(s"r2: ${trainingSummary.r2}")

//    df.show()
//    sc.stop()
    spark.close()
  }
}
