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
interface C<out T1, out T2, out T3> {
    fun test(t1: T1, t2: T2, t3: T3) {}
}

object M1 : C<A1, B1, B5>
object M2 : C<A4, B3, A2>
object M3 : C<B3, B4, A5>

fun test1(b1: B1, b5: B5, a2: A2)  = C.<caret>test<B1, B5, A2> (b1, b5, a2) //nothing found
fun test2(a1: A1?, a3: A3, a5: A5) = C.<caret>test<A1?, A3, A5>(a1, a3, a5) //found M3
fun test3(b2: B2, b3: B3, a4: A4?) = C.<caret>test(b2, b3, a4) //found  M3
fun test4(a3: A3, b2: B2, a1: A1)  = C.<caret>test(a3, b2, a1) //found  M2
fun test5(a4: A4?, b5: B5, a5: A5) = C.<caret>test<A3, B4, A5>(a4, b5, a5) //found  M3
