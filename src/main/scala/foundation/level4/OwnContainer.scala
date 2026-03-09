package foundation.level4

// Implement Basic IO Monad
// IO is a wrapper around a side-effecting computation that is:
//   - Lazy: the computation is not run until .unsafeRun() is called
//   - Composable: map/flatMap let you build pipelines without executing them

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

@main def ioDemo(): Unit =
  val readName: IO[String] =
    IO:
      print("Enter your name: ")
      scala.io.StdIn.readLine()

  val greet: IO[Unit] =
    readName.flatMap: name =>
      IO(println(s"Hello, $name!"))

  // for-comprehension desugars to flatMap/map — idiomatic Scala 3
  val pipeline: IO[Unit] =
    for
      _ <- IO(println("--- IO Monad Demo ---"))
      _ <- greet
      _ <- IO(println("Done."))
    yield ()

  // Nothing has run yet — execute the whole pipeline here
  pipeline.unsafeRun()
