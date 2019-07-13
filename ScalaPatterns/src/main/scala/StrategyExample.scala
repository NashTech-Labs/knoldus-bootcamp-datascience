object StrategyExample {

  def main(args: Array[String]): Unit = {
    val func: Double => Double=scala.math.pow(_, 3)
    val c=new CalcExpression(func)

    println(c.calc(3.0))
  }

}

class CalcExpression(func: Double => Double) {
  def calc(x: Double): Double = { func(x) }
}
