package gitcloc

import akka.actor._
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.format.PeriodFormatterBuilder
import scala.collection.mutable.ListBuffer
import sys.process._

case class CommitClocs(clocs: List[Cloc])

/**
 * Actor that manages scatter / gather of individual CLOC
 * calculations per Git ref
 */
class ClocAggregator(config: Config) extends Actor with ActorLogging {

  var clocResults = List[CommitClocs]()
  var completedResults = 0
  var failedResults = 0
  def totalResponses = completedResults + failedResults

  val outDir = config.outDir
  val startTime = DateTime.now

  lazy val revs: List[String] = config.fromDate match {
    case Some(from) =>
      val allCommits =
        (Seq("git", "rev-list", "--no-merges", "--after", from.toString("yyyy-MM-dd"), config.branch).!!).split("\n").toList

      if (config.onePerDay) {
        val byDate = allCommits groupBy { commit =>
          val dateStr = Seq("git", "show", "-s", "--format=%aI", commit).!!

          new DateTime(dateStr.take(10))
        }
        (byDate map (_._2.head)).toList
      } else {
        allCommits
      }
    case _          => (Seq("git", "rev-list", "--no-merges", config.branch).!!).split("\n").toList
  }

  override def preStart = {
    if (!outDir.exists()) outDir.mkdir()
    log.info(s"Will calculate CLOC for ${revs.size} Git revisions")
    var cursor: Int = 0
    revs foreach { rev =>
      log.info(s"Spawning RevClocker [$cursor of ${revs.size}]")
      val child = context.actorOf(RevClocker.props(config, rev), s"rev-$cursor")
      context.watch(child)
      cursor += 1
    }
  }

  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy

  def completeIfDone() = if (completedResults == revs.size) {
    log.info("All clocs have been gathered or stopped, will write to output file")
    writeOutputFile()
    log.info(s"Processing complete.\n\tProcessing time: ${TimeUtils.printDuration(startTime, DateTime.now)}")
    log.info(s"Results:\n\tTotal revisions considered: $completedResults\n\tTotal revisions failed due to error: $failedResults")
    context.stop(self)
    context.system.shutdown()
  }

  def receive = {
    case c: CommitClocs =>
      clocResults = clocResults :+ c
      completedResults += 1
      context.stop(context.unwatch(sender))
      completeIfDone()
    case Terminated(child) =>
      failedResults += 1
      completeIfDone()
  }

  def writeOutputFile() = {

    def withWriter[A](fileName: String)(f: BufferedWriter => A) = {
      val file = new File(fileName)
      if (file.exists())
        file.delete()
      file.createNewFile()
      val writer = new FileWriter(fileName, true)
      val bufferedWriter = new BufferedWriter(writer)
      try {
        f(bufferedWriter)
        println("CLOC history successfully written to " + file.getAbsolutePath())
      } finally {
        bufferedWriter.close
        writer.close
      }
    }

    withWriter(config.outDir + "/cloc-history.csv") { bufferedWriter =>
      bufferedWriter.write("time,files,language,blank,comment,code\n")
      clocResults.flatMap(_.clocs) foreach { cloc =>
        bufferedWriter.write(cloc.toCsv + "\n")
      }
    }

  }

}

object ClocAggregator {
  def props(config: Config) = Props(classOf[ClocAggregator], config)
}
