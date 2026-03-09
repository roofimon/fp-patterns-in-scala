package level4.advance

case class FriedEgg(description: String)

sealed trait KitchenError extends RuntimeException
case class TemporaryKitchenError(message: String)
    extends RuntimeException(message)
    with KitchenError
