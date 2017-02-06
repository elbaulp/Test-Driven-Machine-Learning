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
      LinearRegression Test                    ${vanillaModelSce.start}
        Given the DataSet 'generated_data.csv'
        When training a linear regression model
        Then show the summary of the model  ${vanillaModelSce.end}
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
          val df1 = df.select("dependent_var", "ind_var_d")
          val formula = new RFormula().
            setFormula("dependent_var ~ ind_var_d").
            setFeaturesCol("features").
            setLabelCol("label")
          val train = formula.fit(df1).transform(df1)
          train.show()
          // Fit the model
          val lr = new LinearRegression()
          val model = lr.fit(train)
          println(s"Coefficients: ${model.coefficients} Intercept: ${model.intercept}")
      }.
      andThen() { case _ => 4 must_== 4 }
}
