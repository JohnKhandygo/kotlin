package test

open class A1
open class A2: A1()
open class A3: A2()
open class A4: A3()
open class A5: A4()

@TypeClass
interface TC<T1, in T2, T3> {
    fun doStuff() {}
}

object HighestMember : TC<A1, A1, A1>

object MiddleMember: TC<A2, A3, A4>

object LowestMember : TC<A5, A5, A5>

fun <T1, T2, T3, @TypeClass C : TC<in T1, T2, in T3>> doStuff() {}

fun main() {
    <caret>doStuff<A1, A1, A1>()
    <caret>doStuff<A5, A4, A4>()
    <caret>doStuff<A1, A4, A1>()
}