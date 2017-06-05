package test

interface A1
interface A2: A1
interface A3: A2
interface A4: A3
interface A5: A4

open class B1
open class B2: B1()
open class B3: B2(), A3
open class B4: B3()
class B5: B4()

@TypeClass
interface C<T1, T2, T3> {
    fun test(t1: T1, t2: T2, t3: T3) {}
}

object AMember : C<A1, A3, A5>
object BMember : C<B2, B3, B4?>
object CMember : C<B5, A2?, B3>

fun test1(b1: B1, b5: B5, a2: A2)  = C.<caret>test<B1, B5, A2> (b1, b5, a2) //nothing found
fun test2(a1: A1?, a3: A3, a5: A5) = C.<caret>test<A1?, A3, A5>(a1, a3, a5) //nothing found
fun test3(b2: B2, b3: B3, b4: B4?) = C.<caret>test(b2, b3, b4) //found BMember
fun test4(b5: B5, a3: A3, b5: B5) = C.<caret>test<B5, A2?, B3>(b5, a3, b5) //found CMember
fun test4(b3: B3, a2: A2, a1: A1) = C.<caret>test(b3, a2, a1) //nothing found