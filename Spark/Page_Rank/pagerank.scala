import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.HashPartitioner

object PageRank {
  def main(args: Array[String]) {
    val sparkConf = new SparkConf().setAppName("PageRank")
    val sc = new SparkContext(sparkConf)

    val lines = sc.textFile(args(0))
    val links = lines.map { s =>
      val parts = s.split("\\s+")
      (parts(0), parts(1))
    }.distinct().groupByKey().partitionBy(new HashPartitioner(100)).persist()

    var ranks = links.mapValues(_ => 1.0)

    for (i <- 0 until args(1).toInt) {
      val contributions = links.join(ranks).flatMap {
        case (pageId, (links, rank)) =>
          links.map(dest => (dest, rank / links.size))
      }
      ranks = contributions.reduceByKey(_ + _).mapValues(0.15 + 0.85 * _)
    }

    ranks.saveAsTextFile("gs://<your-bucket>/ranks")

    sc.stop()
  }
}
