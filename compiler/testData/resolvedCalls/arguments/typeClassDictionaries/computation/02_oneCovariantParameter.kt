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
interface C<out T>

object A2Member : C<A2>
object A4Member : C<A4>
object B3Member : C<B3>
fun <T, @TypeClass D: C<T>> test(t: T) {}

fun testB1(b1: B1) = <caret>test<B1>(b1)    //found C<out B1> = C<B3>
fun testA2(a2: A2) = <caret>test<A2>(a2)    //found C<out A2> = C<A2>
fun testA3(a3: A3) = <caret>test(a3)        //nothing found
fun testA4(a5: A5) = <caret>test(a5)        //nothing found
fun testB2(b3: B3) = <caret>test<B2>(b3)    //found C<out B2> = C<B3>