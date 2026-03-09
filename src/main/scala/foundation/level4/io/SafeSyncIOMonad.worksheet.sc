// import cats.effect.IO
// import cats.effect.unsafe.implicits.global

final class IO[A] private (val unsafeRun: () => A):

  // Transform the result without running the effect
  def map[B](f: A => B): IO[B] =
    IO(f(unsafeRun()))

  // Chain a computation that itself produces an IO
  def flatMap[B](f: A => IO[B]): IO[B] =
    IO(f(unsafeRun()).unsafeRun())

object IO:
  // Wrap a pure value — no side effects
  def pure[A](value: A): IO[A] =
    IO(value)

  // Wrap a side-effecting block, keeping it suspended
  def apply[A](block: => A): IO[A] =
    new IO(() => block)

def deleteProduction(target: String): IO[Unit] =
  IO(println(s"💥 BOOM! Production $target Deleted!"))

def orchrestrationForDoomDay(
    dangerousThing: => IO[Unit],
    anotherDangerousThing: => IO[Unit]
): IO[String] =
  for
    _ <- IO(println("Doing something dangerous..."))
    _ <- dangerousThing
    _ <- anotherDangerousThing
    _ <- IO(println("Finished doing something dangerous."))
    state = "All clear... for now."
  yield state

val deleteProductionDatabase = deleteProduction("Database")
val deleteProductionInstance = deleteProduction("Instance")

val prepareToDestroy =
  orchrestrationForDoomDay(
    deleteProductionDatabase,
    deleteProductionInstance
  )

val destroyYourOwnProduct = prepareToDestroy

destroyYourOwnProduct
  .unsafeRun() // This is where the "bomb" actually goes off!
