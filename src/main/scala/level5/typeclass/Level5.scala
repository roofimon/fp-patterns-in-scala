package level5.typeclass

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import scala.concurrent.duration.*

/*
 * Level5: IO recipe from Level4 timeout + Retryable type class
 * - Retryable[F]: type class for "retry this effect N times"
 * - given Retryable[IO]: instance with handleErrorWith + recursive retry
 * - extension .retry(n): use at call site as getPan.timeout(10.second).retry(3)
 */

trait Retryable[F[_]]:
  def retry[A](fa: F[A], maxAttempts: Int): F[A]

given Retryable[IO] with
  def retry[A](fa: IO[A], maxAttempts: Int): IO[A] =
    fa.handleErrorWith { err =>
      if maxAttempts > 1 then
        IO.println(
          s"  🔁 getPan failed: ${err.getMessage}. Retries left: ${maxAttempts - 1}"
        ) *>
          IO.sleep(100.millis) *>
          retry(fa, maxAttempts - 1)
      else IO.raiseError(err)
    }

extension [F[_], A](fa: F[A])(using R: Retryable[F])
  def retry(n: Int): F[A] = R.retry(fa, n)

case class FriedEgg(description: String)

def heatPan: IO[String] =
  IO.println("  2. Heat the pan over medium heat.").as("Pan is hot")

def crackEgg: IO[String] =
  IO.println("  3. Crack the egg into the pan.").as("Egg is in the pan")

def cook: IO[String] =
  IO.println("  4. Cook until the white is set and the yolk is to your liking.")
    .as("Egg is cooking")

def serve: IO[Option[FriedEgg]] =
  IO.println("  5. Slide onto a plate and serve.")
    .as(Some(FriedEgg("One sunny-side-up fried egg")))

def getPan: IO[String] =
  IO.println("  Getting pan...") *>
    IO.sleep(1.minutes) *>
    IO.pure("Hot Pan")

val recipeForFriedEgg: IO[Option[FriedEgg]] =
  for
    _ <- getPan
      .timeout(10.second)
      .retry(3)
      .handleErrorWith(_ =>
        IO.println("  ⏱️ getPan failed after 3 attempts, using backup pan.")
          .as("Backup Pan")
      )
    _ <- heatPan
    _ <- crackEgg
    _ <- cook
    egg <- serve
  yield egg

@main def runLevel5(): Unit =
  println("🔍 Starting Blueprint Inspection...")
  println("The chef is reading the recipe... (building IO is done)")
  recipeForFriedEgg.unsafeRunSync() match
    case Some(res) => println(s"✅ Test Passed: Plan resulted in '$res'")
    case None      => println(s"❌ Test Failed: Unexpected error")
