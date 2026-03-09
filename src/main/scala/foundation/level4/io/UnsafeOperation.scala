package foundation.level4.io
import scala.util.Try

def deleteProduction(target: String): Try[Unit] =
  Try(println(s"💥 BOOM! Production $target Deleted!"))

def orchrestrationForDoomDay(
    dangerousThing: => Try[Unit],
    anotherDangerousThing: => Try[Unit]
): Try[Unit] =
  for
    _ <- Try(println("Doing something dangerous..."))
    _ <- dangerousThing
    _ <- anotherDangerousThing
    _ <- Try(println("Finished doing something dangerous."))
  yield ()

val deleteProductionDatabase = deleteProduction("Database")
val deleteProductionInstance = deleteProduction("Instance")

@main def runAndThenBoom(): Unit =
  println("Step 1: Doing something dangerous...")
  val prepareToDestroy =
    orchrestrationForDoomDay(
      deleteProductionDatabase,
      deleteProductionInstance
    )
  val destroyYourOwnProduct = prepareToDestroy
