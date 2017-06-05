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
interface C<T1, in T2, T3>

object M1 : C<A1, B1, B5>
object M2 : C<A4, B3, A2>
object M3 : C<B3, B4, A5>

fun <T1, T2, T3, @TypeClass D: C<in T1, T2, in T3>> test(t1: T1, t2: T2, t3: T3) {}

fun test1(b1: B1, b5: B5, a2: A2)  = <caret>test<B1, B5, A2> (b1, b5, a2) //nothing found
fun test2(a1: A1, a3: A3, a5: A5) = <caret>test<A1, A3, A5>(a1, a3, a5) //found nothing
fun test3(b2: B2, b3: B3, a4: A4?) = <caret>test(b2, b3, a4) //found  nothing
fun test4(a3: A3, b2: B2, b5: B5)  = <caret>test(a3, b2, b5) //found  M1
fun test5(a5: A5, b5: B5, a5: A5) = <caret>test<A4, B5, A5>(a5, b5, a5) //found  M2
