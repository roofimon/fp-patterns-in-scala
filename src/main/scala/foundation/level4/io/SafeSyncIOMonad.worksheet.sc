// import cats.effect.IO
// import cats.effect.unsafe.implicits.global

final class BluePrint[A] private (val unsafeRun: () => A):

  // Transform the result without running the effect
  def map[B](f: A => B): BluePrint[B] =
    BluePrint(f(unsafeRun()))

  // Chain a computation that itself produces an IO
  def flatMap[B](f: A => BluePrint[B]): BluePrint[B] =
    BluePrint(f(unsafeRun()).unsafeRun())

object BluePrint:
  // Wrap a pure value — no side effects
  def pure[A](value: A): BluePrint[A] =
    BluePrint(value)

  // Wrap a side-effecting block, keeping it suspended
  def apply[A](block: => A): BluePrint[A] =
    new BluePrint(() => block)

def deleteProduction(target: String): BluePrint[Unit] =
  BluePrint(println(s"💥 BOOM! Production $target Deleted!"))

def orchrestrationForDoomDay(
    dangerousThing: => BluePrint[Unit],
    anotherDangerousThing: => BluePrint[Unit]
): BluePrint[String] =
  for
    _ <- BluePrint(println("Doing something dangerous..."))
    _ <- dangerousThing
    _ <- anotherDangerousThing
    _ <- BluePrint(println("Finished doing something dangerous."))
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

// destroyYourOwnProduct
//   .unsafeRun() // This is where the "bomb" actually goes off!
