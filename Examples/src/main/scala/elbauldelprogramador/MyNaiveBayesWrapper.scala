package elbauldelprogramador.naivebayes

case class MyNaiveBayesWrapper(classification:Option[String] = None, observations:Double) {
  def train: Option[String] = classification
  def classify(observation:Double): Option[String] = classification
}
