package gitcloc

object Main extends App {

  import scala.sys.process._
  import java.io.{ File, FileWriter, BufferedWriter }
  import scala.io.Source
  import org.joda.time.DateTime
  import akka.actor._

  type CommitClocs = List[Cloc]

  val parser = new scopt.OptionParser[Config]("git-cloc-history") {
    head("git-cloc", "0.2")

    opt[String]('b', "branch") action { (v: String, c: Config) =>
      c.copy(branch = v)
    } text ("git branch for which to generate cloc history")

    opt[File]('o', "outdir") action { (v: File, c: Config) =>
      c.copy(outDir = v)
    } text ("directory to create and put the cloc history files")

    opt[Boolean]("oneperday") action { (v, c) =>
      c.copy(onePerDay = v)
    } text ("only output one cloc per 'commit day' instead of one per commit. Default = true")

    opt[String]('f', "fromdate") action { (v, c) =>
      c.copy(fromDate = Some(new DateTime(v)))
    } text ("only generate cloc for commits after this date")

    opt[String]('e', "excludes") action { (v, c) =>
      c.copy(excludes = v)
    } text ("exclude files and directories matching one of the comma separated names from cloc results")

  }

  parser.parse(args, Config()) map { config =>

    case class Revision(rev: String)

    def requireSuccess(commands: String*)(errorMsg: String) {
      if (commands.! != 0) {
        sys.error(s"[ERROR] $errorMsg")
        sys.exit(1)
      }
    }

    def requireCommands(commands: String*) = commands foreach { command => requireSuccess("command", "-v", command)(s"'$command' not found in user's path and is required") }

    requireCommands("git", "cloc")
    requireSuccess("git", "status")("This command must be executed from within a 'git' repository")

    val system = ActorSystem("git-cloc-history")

    // Create an aggregator actor and kick it off. The rest is up to it.
    system.actorOf(ClocAggregator.props(config), "clocAggregator")

  } getOrElse {
    // args are bad, usage message will have been displayed
  }

}
