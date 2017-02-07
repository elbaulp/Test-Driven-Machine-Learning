import org.specs2.Specification
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.feature.RFormula
import org.apache.spark.sql.SparkSession
import org.specs2.specification.script.{ GWT, StandardRegexStepParsers }

class RegressionSpec extends Specification
  with GWT
  with StandardRegexStepParsers {
  def is =
    s2"""
      LinearRegression Test                                                        ${vanillaModelSce.start}
        Given the DataSet 'generated_data.csv'
        When training a linear regression model
        Then Prob(F-statistic) should be small enough to reject the null hypotesis ${vanillaModelSce.end}
    """

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
          val df1 = df.select("dependent_var", "ind_var_a")
          val formula = new RFormula().
            setFormula("dependent_var ~ ind_var_a").
            setFeaturesCol("features").
            setLabelCol("label")
          val train = formula.fit(df1).transform(df1)
          train.show()
          // Fit the model
          val lr = new LinearRegression()
          val model = lr.fit(train)
          val summ = model.summary
          println(s"Coefficients: ${model.coefficients} Intercept: ${model.intercept}")
          summ.pValues(0)
      }.
      andThen() { case _ :: result :: _ => result must be_<=(.05) }
}
