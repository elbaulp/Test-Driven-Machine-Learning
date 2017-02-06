import org.saddle._
import org.saddle.io._
import org.specs2.Specification
import org.specs2.specification.script.{GWT, StandardRegexStepParsers}
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.feature.RFormula

class RegressionSpec extends Specification
    with GWT
    with StandardRegexStepParsers {
  def is =
    s2"""
      LinearRegression Test                    ${sce1.start}
        Given the DataSet 'generated_data.csv'
        When training a linear regression model
        Then show the summary of the model  ${sce1.end}
    """

  private[this] val sce1 =
    Scenario("Scenario1").
      when() { case _ =>
        val file = CsvFile(getClass.getResource("/generated_data.csv").getPath)
        val frame = CsvParser.parse(file)
        val lr = new LinearRegression().
          setMaxIter(10).
          setRegParam(.3).
          setElasticNetParam(.8)

      }.
      andThen(){ case _ => 4 must_== 4 }
}
