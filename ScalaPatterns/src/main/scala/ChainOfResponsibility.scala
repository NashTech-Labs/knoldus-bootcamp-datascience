object ChainOfResponsibility {

  def main(args: Array[String]): Unit = {
    val logger1=new Logger("Default")
    val logger2=new Logger("ERROR")

    val errorLogger=new ErrorLogger()
    val warningLogger=new WarningLogger()

    errorLogger.successor=Some(warningLogger)
    warningLogger.successor=Some(new DefaultLogger())
    errorLogger.doLog(logger1)
    errorLogger.doLog(logger2)
  }
}

class Logger(val logType: String) { }

abstract class LoggerBase {
  var successor=None:Option[LoggerBase]

  def doLog(logger: Logger): Unit
}

class DefaultLogger() extends LoggerBase {
  def doLog(logger: Logger): Unit = { println("Default") }
}

class ErrorLogger() extends LoggerBase {
  def doLog(logger: Logger): Unit = {
    if (logger.logType=="ERROR") {
      println("ERROR")
    }
    else {
      successor.getOrElse(new DefaultLogger()).doLog(logger)
    }
  }
}

class WarningLogger() extends LoggerBase {
  def doLog(logger: Logger): Unit = {
    if (logger.logType=="WARNING") {
      println("WARNING")
    }
    else {
      successor.getOrElse(new DefaultLogger()).doLog(logger)
    }
  }
}