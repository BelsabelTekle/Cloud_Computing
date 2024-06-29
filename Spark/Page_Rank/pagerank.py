from pyspark import SparkConf, SparkContext

def computeContribs(urls, rank):
    num_urls = len(urls)
    for url in urls:
        yield (url, rank / num_urls)

def parseNeighbors(urls):
    parts = urls.split()
    if len(parts) >= 2:
        return parts[0], parts[1]
    else:
        return None

if __name__ == "__main__":
    # Spark configuration
    conf = SparkConf().setAppName("PythonPageRank")
    sc = SparkContext(conf=conf)

    # Load input file
    lines = sc.textFile("gs://page_rank_bucket1/pagerank_data.txt")

    # Parse neighbors
    links = lines.map(parseNeighbors).filter(lambda x: x is not None).distinct().groupByKey().cache()

    # Initialize ranks
    ranks = links.map(lambda url_neighbors: (url_neighbors[0], 1.0))

    # Number of iterations
    iterations = 10

    for iteration in range(iterations):
        # Calculate contributions
        contribs = links.join(ranks).flatMap(lambda url_urls_rank: computeContribs(url_urls_rank[1][0], url_urls_rank[1][1]))

        # Update ranks
        ranks = contribs.reduceByKey(lambda x, y: x + y).mapValues(lambda rank: 0.15 + 0.85 * rank)

        # Collect and print the ranks for the final iteration
        output = ranks.collect()
        print(f"Iteration {iteration + 1}")
        for (link, rank) in output:
            print(f"{link} has rank: {rank}")

    # Stop the SparkContext
    sc.stop()
