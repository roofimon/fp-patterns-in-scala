import scala.util.Try

def deleteProduction(target: String): Try[Unit] =
  Try(println(s"💥 BOOM! Production $target Deleted!"))

def orchrestrationForDoomDay(
    dangerousThing: => Try[Unit],
    anotherDangerousThing: => Try[Unit]
): Try[String] =
  for
    _ <- Try(println("Doing something dangerous..."))
    _ <- dangerousThing
    _ <- anotherDangerousThing
    _ <- Try(println("Finished doing something dangerous."))
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
println(destroyYourOwnProduct)

val x = println("Hello")
val y = x
y
