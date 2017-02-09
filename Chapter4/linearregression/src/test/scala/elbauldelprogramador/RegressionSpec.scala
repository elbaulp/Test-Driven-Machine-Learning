package elbauldelprogramador.tests

import org.specs2.Specification
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.feature.RFormula
import org.apache.spark.sql.SparkSession
import org.specs2.specification.script.{ GWT, StandardRegexStepParsers }
import org.log4s._

class RegressionSpec extends Specification
  with GWT
  with StandardRegexStepParsers {
  def is =
    s2"""
      LinearRegression Test                                                        ${vanillaModelSce.start}
        Given the DataSet 'generated_data.csv'
        When training a linear regression model
        Then Prob(F-statistic) should be small enough to reject the null hypotesis
        And model should explain 95% of the variation in the sampled data or more  ${vanillaModelSce.end}
    """

  val adjR2 = (r2: Double, n: Long, k: Int) => 1 - ((1 - r2) * (n - 1)) / (n - k - 1)

  private[this] val logger = getLogger

  private[this] val spark = SparkSession.builder.
    master("local").
    appName("spark session example").
    getOrCreate()

  private[this] val vanillaModelSce =
    Scenario("Scenario1").
      when() {
        case _ =>

          val df = spark.read.format("csv").
            option("header", "true").
            option("inferSchema", "true").
            load(getClass.getResource("/generated_data.csv").getPath)

          val df1 = df.select("dependent_var", "ind_var_a", "ind_var_b", "ind_var_c", "ind_var_e")
          val formula = new RFormula().
            setFormula("dependent_var ~ ind_var_a + ind_var_b + ind_var_c + ind_var_e + ind_var_b:ind_var_c").
            setFeaturesCol("features").
            setLabelCol("label")
          val train = formula.fit(df1).transform(df1)
          train.show()
          // Fit the model
          val lr = new LinearRegression()
          val model = lr.fit(train)
          val summ = model.summary
          val r2adj = adjR2(summ.r2, train.count(), df1.columns.size - 1)
          logger.debug(s"""
            Coefficients: ${model.coefficients}
            Intercept: ${model.intercept}
            RootMeanSquareError: ${summ.rootMeanSquaredError}
            MeanSquared Error ${summ.meanSquaredError}
            DevianceResudials: ${summ.devianceResiduals.foreach(v => print(v + " "))}
            ExplainedVariance: $summ.explainedVariance
            r2: ${summ.r2}
            adjR2: $r2adj
            pValues: ${summ.pValues.foreach(println)}
          """)
          (summ.pValues(0), r2adj)
      }.
      andThen() { case _ :: result :: _ => result._1 must be_<=(.05) }.
      andThen(anInt) { case expected :: result :: _ => result._2 must be>=(expected/100.0) }
}
