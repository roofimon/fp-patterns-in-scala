package level4.advance

import cats.effect.{IO, Resource}
import scala.concurrent.duration.*

object Demos:

  def resourceSafetyDemo: IO[Unit] =
    val panResource = Resource.make(
      IO.println("  📦 Acquire pan resource") *> IO.pure("Steel Pan")
    )(_ => IO.println("  🧹 Release pan resource"))

    panResource
      .use(_ =>
        IO.println("  ⏳ Using pan (this will timeout)...") *> IO.sleep(
          2.seconds
        )
      )
      .timeoutTo(500.millis, IO.println("  ⏱️ Timed out while using resource"))

  def cancellationDemo: IO[Unit] =
    for
      fiber <- (IO.println("  👨‍🍳 Long cooking started") *> IO.sleep(
        5.seconds
      ) *> IO.println(
        "  🍽️ Long cooking finished"
      )).onCancel(IO.println("  🛑 Cooking was cancelled safely")).start
      _ <- IO.sleep(400.millis)
      _ <- IO.println("  ❌ User canceled the operation")
      _ <- fiber.cancel
      _ <- fiber.join
    yield ()

  def raceDemo: IO[Unit] =
    IO.race(
      IO.sleep(700.millis) *> IO.pure("Kitchen A"),
      IO.sleep(300.millis) *> IO.pure("Kitchen B")
    ).flatMap {
      case Left(winner)  => IO.println(s"  🏁 Fastest source: $winner")
      case Right(winner) => IO.println(s"  🏁 Fastest source: $winner")
    }
