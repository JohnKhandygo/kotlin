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
interface C<in T1, T2, out T3> {
    fun test(t1: T1, t2: T2, t3: T3) {}
}

object M1 : C<A1, B1, B5>
object M2 : C<A4, B3, A2>
object M3 : C<B3, B4, A5>

fun test1(a4: A4, b1: B1, b1: B1)  = C.<caret>test<A4, B1, B1> (a4, b1, b1) //found M1
fun test2(a5: A5, b3: B3, a1: A1) = C.<caret>test<A5, B3, A1>(a5, b3, a1) //found M2
fun test3(b4: B4, b4: B4, a1: A1?) = C.<caret>test(b4, b4, a1) //found M3
fun test4(b3: B3, a5: A5, b4: B4?)  = C.<caret>test(b3, a5, b4) //found  nothing
fun test5(b3: B3, b4: B4, a5: A5) = C.<caret>test<B3, B5, A5>(b3, b4, a5) //found  nothing
