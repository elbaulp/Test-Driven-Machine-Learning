package elbauldelprogramador

import org.apache.spark.SparkConf
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.mllib.linalg.{ DenseVector, VectorUDT, Vectors }
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{DoubleType, StructType}

object SimpleApp {
  def main(args: Array[String]): Unit = {

    val logFile = "/home/hkr/Desarrollo/scala/TDD/Chapter4/linearregression/build.sbt" // Should be some file on your system

    val conf = new SparkConf().
      setAppName("Simple Application").
      setMaster("local[2]")

//    val sc = new SparkContext(conf)


    val sparkSession = SparkSession.builder.
      master("local")
      .appName("spark session example")
      .getOrCreate()

    import org.apache.spark.sql.functions._

    val toInt    = udf[Int, String]( _.toInt)
    val toDouble = udf[Double, String]( _.toDouble)

    // val df = sparkSession.read.option("header","true").
    //   csv(this.getClass.getResource("/generated_data.csv").getPath).
    //   withColumnRenamed("dependent_var", "features")

//    df.printSchema()

    val df5 = sparkSession.read.format("csv").
      option("header", "true").
      load(this.getClass.getResource("/generated_data.csv").getPath)


    df5.printSchema()
//    out.printSchema()

    val schema = new StructType()
     .add("features", new VectorUDT())

    val df2 = df5.withColumn("dependent_var", toDouble(df5("dependent_var")))

    val assembler = new VectorAssembler().
      setInputCols(Array("dependent_var")).
      setOutputCol("features")

    val out = assembler.transform(df2)

    df2.printSchema()
    out.printSchema()


    // val vectorHead = udf{ x:DenseVector => x(0) }
    // val df6 = df5.withColumn("features", Vectors.dense(df5.col("features")))

    //df6.printSchema()

    //df2.printSchema()

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

    df5.show()
//    sc.stop()
    sparkSession.close()
  }
}
