import scala.annotation.tailrec

// 1. The Type Class (ส่วนอินเตอร์เฟซ)
// F[_] คือ Higher-Kinded Type ซึ่งเปรียบเสมือน "กล่อง" เช่น List, Vector หรือ Option
trait MyTransformable[F[_]]:
  def transform[A, B](fa: F[A])(f: A => B): F[B]
  def keepOnly[A](fa: F[A])(p: A => Boolean): F[A]

// 2. Type Class Instances (ส่วนการนำไปใช้งานจริงสำหรับแต่ละประเภทข้อมูล)

// การทำให้ List ใช้งาน MyTransformable ได้ (ใช้ Tail Recursion เพื่อความปลอดภัยของ Stack)
given MyTransformable[List] with {
  def transform[A, B](list: List[A])(f: A => B): List[B] =
    @tailrec
    def loop(remaining: List[A], acc: List[B]): List[B] =
      if remaining.isEmpty then
        acc.reverse // กลับลิสต์เพราะเราใช้การ prepend (::) เพื่อความเร็ว
      else loop(remaining.tail, f(remaining.head) :: acc)
    loop(list, Nil)

  def keepOnly[A](list: List[A])(p: A => Boolean): List[A] =
    @tailrec
    def loop(remaining: List[A], acc: List[A]): List[A] =
      if remaining.isEmpty then acc.reverse
      else
        val nextAcc = if p(remaining.head) then remaining.head :: acc else acc
        loop(remaining.tail, nextAcc)
    loop(list, Nil)

}

// การทำให้ Vector ใช้งาน MyTransformable ได้ (ใช้การวนลูปผ่าน Index)
given MyTransformable[Vector] with {

  def transform[A, B](vec: Vector[A])(f: A => B): Vector[B] =
    @tailrec
    def loop(idx: Int, acc: Vector[B]): Vector[B] =
      if idx >= vec.size then acc
      else loop(idx + 1, acc :+ f(vec(idx)))
    loop(0, Vector.empty)

  def keepOnly[A](vec: Vector[A])(p: A => Boolean): Vector[A] =
    @tailrec
    def loop(idx: Int, acc: Vector[A]): Vector[A] =
      if idx >= vec.size then acc
      else
        val nextAcc = if p(vec(idx)) then acc :+ vec(idx) else acc
        loop(idx + 1, nextAcc)
    loop(0, Vector.empty)

}

// 3. The Usage (Extension methods เพื่อให้เรียกใช้งานแบบ . จุด ได้)
extension [F[_], A](container: F[A])(using t: MyTransformable[F]) {
  def myTransform[B](f: A => B): F[B] = t.transform(container)(f)
  def myKeepOnly(p: A => Boolean): F[A] = t.keepOnly(container)(p)
}

// 4. ส่วนทดสอบโปรแกรม
val nums = List(1, 2, 3, 4, 5)
val vec = Vector(10, 20, 30, 40, 50)

// ใช้งานกับ List
val doubledList = nums.myTransform(_ * 2)
val filteredList = nums.myKeepOnly(_ > 3)

// ใช้งานกับ Vector โดยใช้ Syntax เดียวกันเป๊ะ!
val doubledVec = vec.myTransform(_ * 2)
val filteredVec = vec.myKeepOnly(_ > 25)

println(s"List Transformed: $doubledList")
println(s"List Filtered:    $filteredList")
println(s"Vector Transformed: $doubledVec")
println(s"Vector Filtered:    $filteredVec")
