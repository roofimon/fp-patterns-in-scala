package level4.advance

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.effect.testkit.TestControl
@main def runAdvancedIOMonadDemo(): Unit =
  val liveProgram =
    for
      _ <- IO.println("\n=== Level4 Advance: Resilience Patterns ===")
      egg <- Recipe.recipeForFriedEgg
      _ <- IO.println(s"  🍽️ Result: ${egg.description}")
      _ <- IO.println("\n=== Resource Safety Demo ===")
      _ <- Demos.resourceSafetyDemo
      _ <- IO.println("\n=== Cancellation Demo ===")
      _ <- Demos.cancellationDemo
      _ <- IO.println("\n=== Race Demo ===")
      _ <- Demos.raceDemo
    yield ()

  liveProgram.unsafeRunSync()

@main def runAdvancedFastForwardDemo(): Unit =
  TestControl
    .executeEmbed(Recipe.recipeForFriedEgg)
    .flatMap(egg => IO.println(s"⚡ Fast-forward result: ${egg.description}"))
    .unsafeRunSync()
