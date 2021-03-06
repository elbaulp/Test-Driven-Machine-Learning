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

                                                                 ${twoObservationsSce.start}
        Given the observation 23.2
        Given another observation 73.2
        When Classifying
        Then 23.2 should classify as A class and 72.3 as B       ${twoObservationsSce.end}
    """

  val myDouble = groupAs("\\d+\\.\\d+").and((s: String) => s.toDouble)
  val myTwoDoubles = groupAs("\\d+\\.\\d+").and((s: String, ss: String) => (s.toDouble, ss.toDouble))

  private[this] val logger = getLogger

  private[this] val spark = SparkSession.builder.
    master("local").
    appName("spark session example").
    getOrCreate()


  private[this] val noObservationSce = Scenario("NoObs").
    when(){ case _ =>
      val classifier = MyNaiveBayesWrapper(observations = Vector(23.2))
      classifier.classify(23.2)
    }.
    andThen(){ case _ :: result :: _ =>
      result must_== None
    }

  private[this] val oneObservationSce = Scenario("oneObs").
    given(anInt).
    when(myDouble){ case obs :: a :: _ =>
      val classifier = MyNaiveBayesWrapper(Some(Vector("a")), Vector(obs))
      classifier.classify(obs)
    }.
    andThen(){ case _ :: result :: _ =>
      result must_== Some("a")
    }

  private[this] val twoObservationsSce = Scenario("twoObs").
    given(myDouble).
    given(myDouble).
    when() {case _ :: obs1 :: obs2 :: _ =>
      val classifier = MyNaiveBayesWrapper(Some(Vector("a class", "b class")),
        observations = Vector(0.0, 100.0))
      val classification = classifier.classify(obs1)
      val classification2 = classifier.classify(obs2)

      (classification, classification2)
    }.
    andThen(){ case _ :: r :: _ =>
      (r._1.get must_== "b class") and (r._2.get must_== "a class")
    }
}

// spark.stop()
