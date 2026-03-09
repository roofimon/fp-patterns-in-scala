import scala.concurrent.{Future, Await}
import scala.concurrent.duration.*
import scala.concurrent.ExecutionContext.Implicits.global

final class IO[A] private (val unsafeRun: () => A):
  // Transform the result without running the effect
  def map[B](f: A => B): IO[B] =
    IO(f(unsafeRun()))

  // Chain a computation that itself produces an IO
  def flatMap[B](f: A => IO[B]): IO[B] =
    IO(f(unsafeRun()).unsafeRun())

  // Run two IOs in parallel and return both results
  def zip[B](other: IO[B]): IO[(A, B)] =
    IO {
      // In a real library like Cats Effect, this would use a Fiber/Thread pool
      // But conceptually, it's just "Run both, then return both results"
      val resultA = Future { this.unsafeRun() }
      val resultB = Future { other.unsafeRun() }
      (Await.result(resultA, 2.second), Await.result(resultB, 2.second))
    }

object IO:
  // Wrap a pure value — no side effects
  def pure[A](value: A): IO[A] =
    IO(value)

  // Wrap a side-effecting block, keeping it suspended
  def apply[A](block: => A): IO[A] =
    new IO(() => block)

// Cross-cutting: timing combinator — wrap any IO to get (result, elapsedMs)
def timed[A](ioa: IO[A]): IO[(A, Long)] =
  IO {
    val start = System.currentTimeMillis()
    val a = ioa.unsafeRun()
    val elapsed = System.currentTimeMillis() - start
    (a, elapsed)
  }

def deleteProduction(target: String): IO[Unit] =
  IO {
    Thread.sleep(1000) // Simulate 1 second delay
    println(s"💥 BOOM! Production $target Deleted!")
  }

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

// New version using .zip() to run operations in PARALLEL
def orchrestrateUsingZip(
    dangerousThing: => IO[Unit],
    anotherDangerousThing: => IO[Unit]
): IO[String] =
  for
    _ <- IO(println("Doing something dangerous IN PARALLEL..."))
    _ <- dangerousThing.zip(anotherDangerousThing) // Both run at the same time!
    _ <- IO(println("Finished doing something dangerous."))
    state = "All clear... for now (but faster!)."
  yield state

val deleteProductionDatabase = deleteProduction("Database")
val deleteProductionInstance = deleteProduction("Instance")

// ========================================
// COMPARISON: Sequential vs Parallel (timing applied at call site via timed)
// ========================================

println("\n=== SEQUENTIAL (original) ===")
val (sequentialResult, seqMs) = timed(
  orchrestrationForDoomDay(
    deleteProductionDatabase,
    deleteProductionInstance
  )
).unsafeRun()
println(s"Result: $sequentialResult")
println(s"Total time including orchestration: ${seqMs}ms")

println("\n=== PARALLEL (using zip) ===")
val (parallelResult, parMs) = timed(
  orchrestrateUsingZip(
    deleteProductionDatabase,
    deleteProductionInstance
  )
).unsafeRun()
println(s"Result: $parallelResult")
println(s"Total time including orchestration: ${parMs}ms")

println(
  s"\n🚀 Speedup: ~${seqMs.toDouble / parMs}x faster with .zip()"
)
