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

                                                                 ${oneObsTwoClassSce.start}
        Given the observation 23.2
        Given another observation 73.2
        When Classifying
        Then should not be classify if there is only one obs in any class ${oneObsTwoClassSce.end}

                                                                 ${multipleObsTwoClassSce.start}
        [Multiple obs, two classes] Given the observation 23.2
        Given another observation 2.0
        When Classifying
        Then 23.2 should classify as A class and 2.0 as B       ${multipleObsTwoClassSce.end}
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
      val classifier = MyNaiveBayesWrapper()
      classifier.classify(23.2)
    }.
    andThen(){ case _ :: result :: _ =>
      result must_== None
    }

  private[this] val oneObservationSce = Scenario("oneObs").
    given(anInt).
    when(myDouble){ case obs :: a :: _ =>
      val train = Map("a" -> Vector(obs))
      val classifier = MyNaiveBayesWrapper(Some(train))
      classifier.classify(obs)
    }.
    andThen(){ case _ :: result :: _ =>
      result must_== None
    }

  private[this] val oneObsTwoClassSce = Scenario("twoObs").
    given(myDouble).
    given(myDouble).
    when() {case _ :: obs1 :: obs2 :: _ =>
      val train = Map[String, Vector[Double]]("a class" -> Vector(.0),
        "b class" -> Vector(100.0))
      val classifier = MyNaiveBayesWrapper(Some(train))
      val classification = classifier.classify(obs1)
      val classification2 = classifier.classify(obs2)

      (classification, classification2)
    }.
    andThen(){ case _ :: r :: _ =>
      (r._1 must_== None) and (r._2 must_== None)
    }

  private[this] val multipleObsTwoClassSce = Scenario("Multiple for Two class").
    given(myDouble).
    given(myDouble).
    when() { case _ :: obs1 :: obs2 :: _ =>
      val train = Map("a class" -> Vector(.0, 1.0, .5),
        "b class" -> Vector(50.0, 15.0, 100.0))

      val classifier = MyNaiveBayesWrapper(Some(train))
      val classification1 = classifier.classify(obs1)
      val classification2 = classifier.classify(obs2)

      (classification1, classification2)
    }.
    andThen(){ case _ :: r :: _ =>
      (r._1.get must_== "a class") and (r._2.get must_== "b class")
    }
}

// spark.stop()
