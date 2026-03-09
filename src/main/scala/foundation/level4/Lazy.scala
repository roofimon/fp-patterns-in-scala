package foundation.level4

//Demonstrate lazy evaluation
//In Scala, you can use the `lazy` keyword to define a lazy value. A lazy value is not computed until it is accessed for the first time. This can be useful for improving performance and avoiding unnecessary computations.
@main def lazyDemo(): Unit =
  lazy val expensiveComputation: Int =
    println("Performing expensive computation...")
    // Simulate an expensive computation
    Thread.sleep(2000)
    42

  println("Before accessing the lazy value.")
  println(s"The result of the expensive computation is: $expensiveComputation")
  println("After accessing the lazy value.")
