package elbauldelprogramador.tests.naivebayes

import elbauldelprogramador.naivebayes.MyNaiveBayesWrapper

import org.apache.spark.sql.SparkSession
import org.specs2.Specification
import org.specs2.specification.script.{ GWT, StandardRegexStepParsers }
import org.log4s._

class NaiveBayesSpec extends Specification
    with GWT
    with StandardRegexStepParsers {
  def is =
    s2"""
      Naive Bayes Test                                           ${noObservationSce.start}
        Given No observations at all
        When using Naive Bayes to classify observations without training examples
        Then the classifier shouldn't classify anything          ${noObservationSce.end}

                                                                 ${oneObservationSce.start}
        Given an observation for a class A with an observation 0
        When classifying an observation 23.2
        Then the classifier should classify the observation as A ${oneObservationSce.end}
    """

  val myD = groupAs("\\d+\\.\\d+").and((s: String) => s.toDouble)

  private[this] val logger = getLogger

  private[this] val spark = SparkSession.builder.
    master("local").
    appName("spark session example").
    getOrCreate()


  private[this] val noObservationSce = Scenario("NoObs").
    when(){ case _ =>

      val classifier = MyNaiveBayesWrapper(observations = 23.2)
      classifier.classify(23.2)
    }.
    andThen(){ case _ :: result :: _ =>
      result must_== None
    }

  private[this] val oneObservationSce = Scenario("oneObs").
    given(anInt).
    when(myD){ case obs :: a :: _ =>
      val classifier = MyNaiveBayesWrapper(Some("a"), obs)
      classifier.train
      classifier.classify(obs)
    }.
    andThen(){ case _ :: result :: _ =>
      result must_== Some("a")
    }
}

// spark.stop()
