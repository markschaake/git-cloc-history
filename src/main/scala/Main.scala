object Main extends App {

  import scala.sys.process._
  import java.io.{ File, FileWriter, BufferedWriter }
  import scala.io.Source
  import org.joda.time.DateTime

  case class Config(
    branch: String = "master",
    clocDir: String = ".",
    outDir: String = ".",
    onePerDay: Boolean = true,
    fromDate: Option[DateTime] = None)

  case class Cloc(date: DateTime, files: Int, language: String, blank: Int, comment: Int, code: Int) {
    def toCsv = date.toString("yyyy-MM-dd") + "," + files + "," + language + "," + blank + "," + comment + "," + code
  }

  type CommitClocs = List[Cloc]

  object Cloc {
    val CsvPattern = """(\d+),([^,]+),(\d+),(\d+),(\d+)""".r

    def apply(date: DateTime, str: String): Cloc = str match {
      case CsvPattern(files, language, blank, comment, code) =>
        new Cloc(date, files.toInt, language, blank.toInt, comment.toInt, code.toInt)
      case _ => throw new Exception(s"invalid csv string: [$str]")
    }
  }

  val parser = new scopt.immutable.OptionParser[Config]("git-cloc-history", "0.1") {
    def options = Seq(
      opt("b", "branch", "git branch for which to generate cloc history") { (v: String, c: Config) => c.copy(branch = v) },
      opt("d", "clocdir", "directory within git branch to generate cloc history") { (v: String, c: Config) => c.copy(clocDir = v) },
      opt("o", "outdir", "directory to create and put the cloc history files") { (v: String, c: Config) => c.copy(outDir = v) },
      booleanOpt("oneperday", "only output one cloc per 'commit day' instead of one per commit. Default = true") { (v: Boolean, c: Config) => c.copy(onePerDay = v) },
      opt("f", "fromdate", "only generate cloc for commits after this date") { (v: String, c: Config) => c.copy(fromDate = Some(new DateTime(v))) })
  }

  parser.parse(args, Config()) map { config =>

    def requireSuccess(commands: String*)(errorMsg: String) {
      if (commands.! != 0) {
        sys.error(s"[ERROR] $errorMsg")
        Seq("git", "checkout", "-f", config.branch).!
        sys.exit(1)
      }
    }

    def requireCommands(commands: String*) = commands foreach { command => requireSuccess("command", "-v", command)(s"'$command' not found in user's path and is required") }
    def gitCheckout(rev: String) = requireSuccess("git", "checkout", "-f", "-q", rev)(s"Could not checkout revision $rev")

    def generateClocsForRev(): CommitClocs = {
      val cdate = ((Seq("git", "log", "-1", "--format=%ct")).!!).trim.toLong
      val date = new DateTime(cdate * 1000)
      val lines = Seq("cloc", "--csv", "--quiet", "--progress-rate=0", config.clocDir).!!.split("\n").toList
      lines.drop(2).map(csv => Cloc(date, csv))
    }

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
        gitCheckout(config.branch)
      }
    }

    requireCommands("git", "cloc")
    requireSuccess("git", "status")("This command must be executed from within a 'git' repository")

    val outDir = new File(config.outDir)
    if (!outDir.exists()) outDir.mkdir()

    val revs = config.fromDate match {
      case Some(from) => (Seq("git", "rev-list", "--no-merges", "--after", from.toString("yyyy-MM-dd"), config.branch).!!).split("\n")
      case _ => (Seq("git", "rev-list", "--no-merges", config.branch).!!).split("\n")
    }

    val clocs = revs.zipWithIndex map {
      case (rev, i) =>
        println(s"Generating CLOC for rev ${i + 1} of ${revs.size} [$rev]")
        gitCheckout(rev)
        generateClocsForRev()
    }

    val clocsToOutput: List[Cloc] =
      if (config.onePerDay) {
        clocs
          .filter(_.size > 0)
          .map(commitClocs => commitClocs.head.date -> commitClocs)
          .groupBy(_._1.toString("yyyy-MM-dd"))
          .map(entry => entry._1 -> entry._2.sortBy(_._1.getMillis).last._2)
          .toList
          .sortBy(_._1)
          .map(_._2)
          .flatten
          .toList
      } else {
        clocs.flatten.toList
      }

    withWriter(config.outDir + "/cloc-history.csv") { bufferedWriter =>
      bufferedWriter.write("time,files,language,blank,comment,code\n")
      clocsToOutput foreach { cloc => bufferedWriter.write(cloc.toCsv + "\n") }
    }

  } getOrElse {
    // args are bad, usage message will have been displayed
  }

}
