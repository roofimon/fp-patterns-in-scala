package level4.advance

import cats.effect.{IO, Ref}
import scala.concurrent.duration.*
import java.util.concurrent.TimeoutException

object Recipe:

  def primaryPan(attempts: Ref[IO, Int]): IO[String] =
    for
      n <- attempts.updateAndGet(_ + 1)
      _ <- IO.println(s"  🥘 [Primary] pan attempt #$n")
      _ <- IO.sleep(2.seconds)
      _ <- IO.raiseError[String](
        new TimeoutException("primary pan supplier too slow")
      )
    yield "Primary Pan"

  def getPanResilient: IO[String] =
    for
      attempts <- Ref.of[IO, Int](0)
      pan <- RetrySupport
        .retryWithBackoff(
          task = primaryPan(attempts).timeout(1.second),
          maxAttempts = 3,
          delay = 100.millis,
          shouldRetry = {
            case _: TimeoutException      => true
            case _: TemporaryKitchenError => true
            case _                        => false
          }
        )
        .handleErrorWith(_ =>
          IO.println("  🛟 Switching to backup pan after 3 failed attempts.") *>
            IO.pure("Backup Pan")
        )
    yield pan

  def heatPan(pan: String): IO[String] =
    IO.println(s"  🔥 Heating $pan...") *>
      IO.sleep(300.millis) *>
      IO.pure("Pan is hot")

  def crackEgg: IO[String] =
    IO.println("  🥚 Cracking egg...").as("Egg is in the pan")

  def cook: IO[String] =
    IO.println("  🍳 Cooking...") *>
      IO.sleep(300.millis) *>
      IO.pure("Egg is cooking")

  def serve: IO[FriedEgg] =
    IO.println("  ✅ Serve.").as(FriedEgg("One sunny-side-up fried egg"))

  val recipeForFriedEgg: IO[FriedEgg] =
    for
      pan <- getPanResilient
      _ <- heatPan(pan)
      _ <- crackEgg
      _ <- cook
      egg <- serve
    yield egg
