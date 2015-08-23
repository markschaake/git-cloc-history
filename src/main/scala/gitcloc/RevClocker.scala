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
class RevClocker(config: Config, rev: String) extends Actor with ActorLogging {

  var clocDir: Path = _

  override def preStart = {
    clocDir = Files.createTempDirectory("gitcloc")
    val gitRevisionZip = Paths.get(clocDir.toString, "rev.zip")
    context.parent ! generateClocsForCurrentRev(gitRevisionZip, config)
  }

  override def postStop = {
    deleteFileOrDirectory(clocDir.toFile)
  }

  def receive = Actor.emptyBehavior

  def deleteFileOrDirectory(dirOrFile: File): Unit = {
    if (dirOrFile.isDirectory)
      dirOrFile.listFiles foreach { f => deleteFileOrDirectory(f) }
    dirOrFile.delete()
  }

  def generateClocsForCurrentRev(gitRevisionZip: Path, config: Config): CommitClocs = {
    // Checkout the rev into a temporary work tree:
    val start = DateTime.now
    log.info(s"Generating archive of rev $rev into work tree: ${gitRevisionZip}")
    if (Seq("git", "archive", rev, "--format", "zip", "--output", gitRevisionZip.toString).! != 0)
      throw new Exception(s"Failed to checkout revision ${rev} into ${gitRevisionZip.toString}")

    val cdate = ((Seq("git", "log", "-1", "--format=%ct", rev)).!!).trim.toLong
    val date = new DateTime(cdate * 1000)
    val lines = config.excludes match {
      case Some(excludes) =>
        Seq("cloc", "--csv", "--quiet", "--progress-rate=0", s"--exclude-dir=${excludes}", gitRevisionZip.toString).!!.split("\n").toList
      case None =>
        Seq("cloc", "--csv", "--quiet", "--progress-rate=0", gitRevisionZip.toString).!!.split("\n").toList
    }
    log.info(s"Finished processing CLOC for $rev\n\tTotal time: ${TimeUtils.printDuration(start, DateTime.now)}")
    CommitClocs(lines.drop(2).map(csv => Cloc(date, csv)))
  }

}

object RevClocker {
  def props(config: Config, rev: String) = Props(classOf[RevClocker], config, rev)
}
