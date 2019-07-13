

trait Command {
  def execute()
}

class ProductHistory {
  private var history: List[Command] = Nil

  def storeAndExecute(cmd: Command): Unit = {
    cmd.execute()
    this.history :+ cmd
  }
}

class Product(x: Double) {
  val num=x

  def printProduct(y: Double): Unit = {
    println(y*num)
  }
}

class ProductCommand(product: Product, val y: Double) extends Command {
  def execute()=product.printProduct(y)
}

object CommandExample {
  def main(args: Array[String]): Unit = {
    val cmd=new ProductCommand(new Product(3.0), 2.0)
    val productHistory=new ProductHistory()
    productHistory.storeAndExecute(cmd)
  }
}