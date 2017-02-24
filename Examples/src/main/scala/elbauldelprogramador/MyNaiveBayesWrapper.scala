package elbauldelprogramador.naivebayes

import org.log4s._
import scala.math.{ exp, pow, sqrt }

case class MyNaiveBayesWrapper(y: Option[Map[String, Vector[Double]]] = None) {

  private[this] val log = getLogger

  def classify(observation: Double): Option[String] = y match {
    case Some(m) if m.find(_._2.size <= 1).isDefined =>
      None
    case Some(m) =>

      val labels = m.values.flatten
      val closest = labels.minBy(v => math.abs(observation - v))

      var highestProb = 0.0
      var bestClass = m.keys.head

      val probObservationGivenClass = m map { case (k, v) => (k, probability(v, observation)) }
      probObservationGivenClass map {
        case (k, v) =>
          val otherLabels = m.keySet.diff(Set(k)).toSeq
          val candidateProb = probability(k, otherLabels, probObservationGivenClass)
          val candidateClass = k

          if (candidateProb > highestProb){
            highestProb = candidateProb
            bestClass = candidateClass
            log.warn(s"BestClass: $bestClass")
          }
          bestClass
      }

      Some(bestClass)

    case _ => None
  }

  private[this] def probability(trained: Seq[Double], obs: Double): Double = {
    val mv = breeze.stats.meanAndVariance(trained)

    1 / sqrt(2 * Math.PI * mv.variance) * exp(-0.5 * pow(obs - mv.mean, 2) / mv.variance)
  }

  private[this] def probability(label: String, otherLabels: Seq[String],
    pOfObsGivenLabel: Map[String, Double]): Double = {
    val pObs = pOfObsGivenLabel(label)

    pObs / (pObs + pOfObsGivenLabel.filter(_ != label).values.sum)
  }
}
