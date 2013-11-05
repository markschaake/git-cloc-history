package gitcloc

object TimeUtils {

  import org.joda.time.DateTime
  import org.joda.time.Period
  import org.joda.time.Duration
  import org.joda.time.format.PeriodFormatterBuilder

  val formatter = new PeriodFormatterBuilder()
    .printZeroAlways()
    .appendMinutes()
    .appendSuffix(" minutes ")
    .appendSeconds()
    .appendSuffix(" seconds ")
    .appendMillis()
    .appendSuffix(" milliseconds")
    .toFormatter

  def formatPeriod(period: Period) = formatter.print(period)

  def printDuration(start: DateTime, end: DateTime) = formatPeriod {
    new Period(new Duration(start, end))
  }

}
