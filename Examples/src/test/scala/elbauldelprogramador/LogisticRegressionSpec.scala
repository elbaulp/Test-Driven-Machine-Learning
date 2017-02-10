package elbauldelprogramador.tests.logistic

import org.apache.spark.ml.classification.BinaryLogisticRegressionSummary
import org.specs2.Specification
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.RFormula
import org.apache.spark.sql.SparkSession
import org.specs2.specification.script.{ GWT, StandardRegexStepParsers }
import org.log4s._

class LogisticRegressionSpec extends Specification
  with GWT
  with StandardRegexStepParsers {
  def is =
    s2"""
      LinearRegression Test                          ${logisticTest.start}
        Given the DataSet 'generated_logistic_data.csv'
        When training a Logistic regression model
        Then AUC score should be above random (>60%) ${logisticTest.end}
    """

  private[this] val logger = getLogger

  private[this] val spark = SparkSession.builder.
    master("local").
    appName("spark session example").
    getOrCreate()

  val df = spark.read.format("csv").
    option("header", "true").
    option("inferSchema", "true").
    load(getClass.getResource("/generated_logistic_data.csv").getPath).
    select("y", "variable_e")

  private[this] val logisticTest =
    Scenario("Scenario1").
      when() {
        case _ =>
          // Create the formula
          val formula = new RFormula().
            setFormula("y ~ .").
            setFeaturesCol("features").
            setLabelCol("label")
          val train = formula.fit(df).transform(df)
          train.show()

          // Fit the model
          val lr = new LogisticRegression()
          val lrModel = lr.fit(train)
          val trainingSummary = lrModel.summary
          val binarySummary = trainingSummary.asInstanceOf[BinaryLogisticRegressionSummary]
          val roc = binarySummary.roc
          roc.show()
          logger.debug(s"areaUnderROC: ${binarySummary.areaUnderROC}")

          binarySummary.areaUnderROC
      }.
      andThen(anInt) { case expected :: result :: _ => result must be>=(expected/100.0) }
}
