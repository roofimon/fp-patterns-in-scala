package foundation.level4.fastforward

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import java.nio.file.{Files, Paths}
import scala.concurrent.duration.*
import cats.effect.testkit.TestControl

/*
 * IO Monad: Chef and Fried Egg Recipe Metaphor
 * --------------------------------------------
 * - A RECIPE is like IO[A]: a description of steps to produce a value (e.g. a fried egg).
 *   It is just a description — no cooking happens when we write it.
 * - WRITING THE RECIPE = composing IO with map / flatMap / for-comprehension (pure).
 * - THE CHEF RUNS THE RECIPE = calling unsafeRunSync() (or similar); only then do
 *   side effects (heat pan, crack egg, etc.) happen.
 */

case class FriedEgg(description: String)

// Recipe steps: each returns an IO action. Nothing runs until the chef executes the recipe.
def getPan: IO[String] =
  IO.println("  1. Get a non-stick pan.").as("Pan")

// def heatPan: IO[String] =
//   IO.println("  2. Heat the pan over medium heat.").as("Pan is hot")
def heatPan: IO[String] =
  IO.println("  Heating...") *>
    IO.sleep(5.minutes) *>
    IO.pure("Hot Pan")

def crackEgg: IO[String] =
  IO.println("  3. Crack the egg into the pan.").as("Egg is in the pan")

def cook: IO[String] =
  IO.println("  4. Cook until the white is set and the yolk is to your liking.")
    .as("Egg is cooking")

def serve: IO[FriedEgg] =
  IO.println("  5. Slide onto a plate and serve.")
    .as(FriedEgg("One sunny-side-up fried egg"))

// Writing the recipe: we compose IO values. This is pure — no side effects yet.
val recipeForFriedEgg: IO[FriedEgg] =
  for
    _ <- getPan
    _ <- heatPan
    _ <- crackEgg
    _ <- cook
    egg <- serve
  yield egg

@main def runIOMonadDemo(): Unit =
  println("🔍 Starting Blueprint Inspection...")
  println("The chef is reading the recipe... (building IO is done)")
  TestControl
    .executeEmbed(heatPan)
    .flatMap { result =>
      IO.println(s"Test finished in 0.001s with result: $result")
    }
    .unsafeRunSync()
//   recipeForFriedEgg.unsafeRunSync() match {
//     case Right(res) => println(s"✅ Test Passed: Plan resulted in '$res'")
//     case Left(e) => println(s"❌ Test Failed: Unexpected error")
//   }

// println("The chef is reading the recipe... (building IO is done)")
// println("Now the chef executes the recipe:")
// val getPanIO: IO[String] = getPan
// println(
//   s"getPanIO: $getPanIO"
// )
// This is just a description, no side effects yet.
// val result = recipeForFriedEgg.unsafeRunSync()
// println(s"Result: ${result.description}")

// _ <- IO.raiseError(new Exception("No Eggs!"))

// def heatPan: IO[String] =
//   IO.println("  Heating...") *>
//     IO.sleep(5.minutes) *>
//     IO.pure("Hot Pan")
