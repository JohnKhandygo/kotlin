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
interface C<T>

object A2Member : C<A2>
object B2Member : C<B2>
object A4Member : C<A4?>

fun <T, @TypeClass D: C<in T>> test(t: T) {}

fun testB1(b1: B1) = <caret>test<B1>(b1)    //nothing found
fun testA2(a2: A2) = <caret>test<A2>(a2)    //found TC<in A2> = TC<A2>
fun testA3(a3: A3) = <caret>test(a3)        //found TC<in A3> = TC<A2>
fun testA4(a5: A5) = <caret>test(a5)        //found TC<in A5> = TC<A4?>
fun testB3(b4: B4) = <caret>test<B3>(b4)    //found TC<in B3> = TC<B2>