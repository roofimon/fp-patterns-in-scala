package level4.world

import cats.effect.{IO, IOApp}
import cats.syntax.all.* // Provides .parMapN and .as
import scala.concurrent.duration.*
import cats.effect.unsafe.implicits.global

case class FriedEgg(description: String)
case class Coffee(kind: String)
case class FullBreakfast(egg: FriedEgg, coffee: Coffee)

//object ConcurrentBreakfast extends IOApp.Simple {

// --- 1. THE EGG PATH ---
def getPan: IO[Unit] = IO.println("🍳 [Egg] Getting pan...")
def heatPan: IO[Unit] =
  IO.println("🔥 [Egg] Heating pan...") *> IO.sleep(2.seconds)
def cookEgg: IO[Unit] =
  IO.println("🍳 [Egg] Frying egg...") *> IO.sleep(1.second)
def serveEgg: IO[FriedEgg] = IO.pure(FriedEgg("Sunny-side-up"))

val eggRecipe: IO[FriedEgg] = for {
  _ <- getPan
  _ <- heatPan
  _ <- cookEgg
} yield FriedEgg("Sunny-side-up")

// --- 2. THE COFFEE PATH ---
def grindBeans: IO[Unit] =
  IO.println("☕ [Coffee] Grinding beans...") *> IO.sleep(1.second)
def brewCoffee: IO[Unit] =
  IO.println("☕ [Coffee] Brewing...") *> IO.sleep(2.seconds)

val coffeeRecipe: IO[Coffee] = for {
  _ <- grindBeans
  _ <- brewCoffee
} yield Coffee("Black Arabica")

// --- 3. THE CONCURRENT ORCHESTRATION ---
@main def runIOMonadCon(): Unit =
  println("🔍 Starting Blueprint Inspection...")

  // THE MAGIC: parMapN runs these two IOs on separate Fibers
  val mayBeFood = (eggRecipe, coffeeRecipe)
    .parMapN { (egg, coffee) =>
      FullBreakfast(egg, coffee)
    }

  mayBeFood
    .flatMap { breakfast =>
      IO.println(
        s"🍽️ Breakfast Ready: ${breakfast.egg.description} & ${breakfast.coffee.kind}"
      )
    }
    .unsafeRunSync()

//}
