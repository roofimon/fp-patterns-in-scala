package level3.adt

// Each RSS source is one batch job. Job and JobResult ADTs; runJob/runBatch reuse pipeline from same package.

object Level3ADT:

  case class Job(id: String, url: String, outputPath: String)

  sealed trait JobResult
  case class Success(jobId: String) extends JobResult
  case class Failed(jobId: String, reason: String) extends JobResult

  def runJob(job: Job): JobResult =
    processFeed(fetchData, parseRSS, formatter)(job) match
      case Some(_) => Success(job.id)
      case None    => Failed(job.id, "fetch or parse failed")

  def runBatch(jobs: List[Job]): List[JobResult] =
    jobs.map(runJob)

  val feedUrls = List(
    "https://feeds.bbci.co.uk/news/world/rss.xml" -> "world_news.txt",
    "https://feeds.bbci.co.uk/news/technology/rss.xml" -> "tech_news.txt",
    "https://feeds.bbci.co.uk/news/business/rss.xml" -> "business_news.txt"
  )

  def run(): Unit =
    val batch: List[Job] = feedUrls.map { case (url, outputPath) =>
      val id = url.split("/").lastOption.getOrElse("unknown")
      Job(id, url, outputPath)
    }
    val results = runBatch(batch)
    val succeeded = results.collect { case Success(id) => id }
    val failed = results.collect { case Failed(id, reason) => (id, reason) }
    println(
      s"Batch: ${batch.size} jobs, ${succeeded.size} succeeded (${succeeded.mkString(", ")}), ${failed.size} failed."
    )

@main def runLevel3ADT(): Unit = Level3ADT.run()
