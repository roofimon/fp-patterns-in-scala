package foundation.level3.adt

// The core data
case class Product(id: String, name: String, price: Int)

// THE EVENTS (Orange Stickers: What happened in the past)
enum VendingEvent derives CanEqual:
  case ProductSelected(item: Product)
  case MoneyInserted(amount: Int)
  case DispenseFailed(reason: String)
  case TransactionCanceled
  case ProductDropped(item: Product)

// THE STATE (Green Stickers: Where are we now?)
enum MachineState derives CanEqual:
  case Idle
  case AwaitingPayment(item: Product, balance: Int)
  case Processing(item: Product)
  case OutOfOrder(msg: String)

object VendingLogic {

  def update(state: MachineState, event: VendingEvent): MachineState =
    (state, event) match {

      // Transition 1: From Idle to Awaiting Payment
      case (MachineState.Idle, VendingEvent.ProductSelected(p)) =>
        MachineState.AwaitingPayment(p, 0)

      // Transition 2: Adding money while awaiting payment
      case (
            MachineState.AwaitingPayment(p, current),
            VendingEvent.MoneyInserted(amt)
          ) =>
        val newBalance = current + amt
        if (newBalance >= p.price) MachineState.Processing(p)
        else MachineState.AwaitingPayment(p, newBalance)

      // Transition 3: Handling a cancelation
      case (
            MachineState.AwaitingPayment(_, _),
            VendingEvent.TransactionCanceled
          ) =>
        MachineState.Idle

      // Transition 4: Success!
      case (MachineState.Processing(p), VendingEvent.ProductDropped(_)) =>
        MachineState.Idle

      // Transition 5: Error handling
      case (_, VendingEvent.DispenseFailed(reason)) =>
        MachineState.OutOfOrder(reason)

      // Fallback: If an event makes no sense in the current state, ignore it
      case (s, _) => s
    }
}

@main def runVendingMachine(): Unit = {
  val coke = Product("c1", "Coke", 20)

  // A stream of Orange Stickers (Events)
  val history = List(
    VendingEvent.ProductSelected(coke),
    VendingEvent.MoneyInserted(10),
    VendingEvent.MoneyInserted(15), // Total 25, more than price
    VendingEvent.ProductDropped(coke)
  )

  // We start at Idle and "fold" the events into the state
  val finalState =
    history.foldLeft[MachineState](MachineState.Idle)(VendingLogic.update)

  println(s"Final Machine State: $finalState")
}
