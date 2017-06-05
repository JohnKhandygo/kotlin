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

object A3Member : C<A3>
object B2Member : C<B2>
fun <T, @TypeClass D: C<T>> test(t: T) {}

fun testB1(b1: B1) = <caret>test<B1>(b1)    //nothing found
fun testA2(a1: A1) = <caret>test<A1>(a1)    //nothing found
fun testA3(a3: A3) = <caret>test(a3)        //found C<A3> = C<A3>
fun testB3(b5: B5) = <caret>test<B3>(b5)    //nothing found

