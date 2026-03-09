import scala.util.Random

val attemptStep1 =
  println("🔋 Step 1: Charging system....")
  if Random.nextBoolean() then
    println("✅ Charge Successful!")
    "System Charged"
  else
    println("❌ ERROR: Power Surge!")
    throw RuntimeException("Charging Failed")

val step2 =
  println("🚀 Step 2: Launching rocket!")
  "Orbit Reached"

val maxRetries = 10
var step1Result = ""
var success = false

for attempt <- 1 to maxRetries if !success do
  try
    step1Result = attemptStep1
    success = true
  catch
    case e: RuntimeException =>
      println(s"Retry $attempt/$maxRetries failed: ${e.getMessage}")

if !success then println("💀 All retries exhausted. Aborting.")
