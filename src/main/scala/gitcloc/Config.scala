package gitcloc

import org.joda.time.DateTime
import java.io.File

case class Config(
  branch: String = "master",
  clocDir: File = new File("."),
  outDir: File = new File("."),
  onePerDay: Boolean = true,
  fromDate: Option[DateTime] = None,
  excludes: String = "")
