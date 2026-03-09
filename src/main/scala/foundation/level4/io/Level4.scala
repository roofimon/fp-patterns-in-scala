package foundation.level4.io

import cats.effect.IO
import cats.effect.unsafe.implicits.global

val deleteProductionDatabase: IO[Unit] =
  IO.println("💥 BOOM! Production Database Deleted!")

val deleteProductionInstance: IO[Unit] =
  IO.println("💥 BOOM! Production Instance Deleted!")

def doSomethingDangerous(
    dangerousThing: IO[Unit],
    anotherDangerousThing: IO[Unit]
): IO[Unit] =
  for {
    _ <- IO.println("Doing something dangerous...")
    _ <- dangerousThing
    _ <- anotherDangerousThing
    _ <- IO.println("Finished doing something dangerous.")
  } yield ()

@main def runLaziness(): Unit =
  println("Step 1: Creating the bomb...")
  val destroyYourOwnProduct = doSomethingDangerous(
    deleteProductionDatabase,
    deleteProductionInstance
  )
