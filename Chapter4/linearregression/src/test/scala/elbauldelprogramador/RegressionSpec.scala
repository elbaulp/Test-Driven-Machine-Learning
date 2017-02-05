import org.specs2.Specification
import org.specs2.specification.script.{GWT, StandardRegexStepParsers}

import org.saddle._
import org.saddle.io._

class RegressionSpec extends Specification
    with GWT
    with StandardRegexStepParsers {
  def is =
    s2"""
     Retrieving values
      Given a json response from previous request
      When extracting key: packet_count
      Then a field look up should return: true
     Query Controller at fixed intervals
      Given a 50 milliseconds interval
      When querying the controller for 10 seconds
      Then the size should be 200
    """
}
