package level4.advance

import cats.effect.IO
import scala.concurrent.duration.*

object RetrySupport:

  def retryWithBackoff[A](
      task: IO[A],
      maxAttempts: Int,
      delay: FiniteDuration,
      shouldRetry: Throwable => Boolean
  ): IO[A] =
    task.handleErrorWith { err =>
      if maxAttempts > 1 && shouldRetry(err) then
        IO.println(
          s"  🔁 Attempt failed (${err.getClass.getSimpleName}: ${err.getMessage}). Retries left: ${maxAttempts - 1}"
        ) *>
          IO.sleep(delay) *>
          retryWithBackoff(task, maxAttempts - 1, delay * 2, shouldRetry)
      else IO.raiseError(err)
    }
