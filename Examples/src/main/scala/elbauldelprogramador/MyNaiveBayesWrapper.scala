package elbauldelprogramador.naivebayes

import scala.language.postfixOps

import org.log4s._

case class MyNaiveBayesWrapper(y:Option[Seq[String]] = None,
  observations: Seq[Double] = Seq.empty) {

  private[this] val log = getLogger

  private[this] val model = y match {
    case Some(x) => x zip observations toMap
    case None => Map.empty[String, Double]
  }

  def classify(observation:Double): Option[String] = model match {
    case label if model.nonEmpty =>
      val closest = label.values.minBy(v => math.abs(observation - v))
      log.warn(s"$model")
      log.warn(s"CLOSEST: $closest")
      log.warn(s"find: ${model.find(_._2 == closest)}")
      Some(model.find(_._2 == closest).get._1)
    case _ => None
  }
}
