package gitcloc

import akka.actor._
import java.io.File
import org.joda.time.DateTime
import java.nio.file.{ Path, Files, Paths }
import scala.sys.process._

/**
 * Actor whose only responsibility is to generate Cloc entries
 * for one revision
 */
class RevClocker(config: Config) extends Actor with ActorLogging {

  type CommitClocs = List[Cloc]

  def receive = {
    case RevClocker.GenerateRev(rev) =>
      val clocDir = Files.createTempDirectory("gitcloc")
      val gitRevisionZip = Paths.get(clocDir.toString, "rev.zip")
      sender ! generateClocsForCurrentRev(rev, gitRevisionZip, config)
      deleteFileOrDirectory(clocDir.toFile)
      context.stop(self)
  }

  def deleteFileOrDirectory(dirOrFile: File): Unit = {
    if (dirOrFile.isDirectory)
      dirOrFile.listFiles foreach { f => deleteFileOrDirectory(f) }
    dirOrFile.delete()
  }

  def generateClocsForCurrentRev(rev: String, gitRevisionZip: Path, config: Config): CommitClocs = {
    // Checkout the rev into a temporary work tree:
    val start = DateTime.now
    log.info(s"Generating archive of rev $rev into work tree: ${gitRevisionZip}")
    if (Seq("git", "archive", rev, "--format", "zip", "--output", gitRevisionZip.toString).! != 0)
      throw new Exception("Failed to checkout revision into ")

    val cdate = ((Seq("git", "log", "-1", "--format=%ct", rev)).!!).trim.toLong
    val date = new DateTime(cdate * 1000)
    val lines = Seq("cloc", "--csv", "--quiet", "--progress-rate=0", s"--exclude-dir=${config.excludes}", gitRevisionZip.toString).!!.split("\n").toList
    log.info(s"Finished processing CLOC for $rev\n\tTotal time: ${TimeUtils.printDuration(start, DateTime.now)}")
    lines.drop(2).map(csv => Cloc(date, csv))
  }

}

object RevClocker {
  case class GenerateRev(rev: String)

  def props(config: Config) = Props(classOf[RevClocker], config).withDispatcher("cloc-dispatcher")
}
