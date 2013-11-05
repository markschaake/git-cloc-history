package gitcloc

import org.joda.time.DateTime

case class Cloc(date: DateTime, files: Int, language: String, blank: Int, comment: Int, code: Int) {
  def toCsv = date.toString("yyyy-MM-dd") + "," + files + "," + language + "," + blank + "," + comment + "," + code
}

object Cloc {
  val CsvPattern = """(\d+),([^,]+),(\d+),(\d+),(\d+)""".r

  def apply(date: DateTime, str: String): Cloc = str match {
    case CsvPattern(files, language, blank, comment, code) =>
      new Cloc(date, files.toInt, language, blank.toInt, comment.toInt, code.toInt)
    case _ => throw new Exception(s"invalid csv string: [$str]")
  }
}
