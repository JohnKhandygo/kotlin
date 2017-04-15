package test

@Target(AnnotationTarget.CLASS)
annotation class TypeClass

@Target(AnnotationTarget.CLASS)
annotation class TypeClassMember

open class A1
open class A2: A1()
open class A3: A2()
open class A4: A3()
open class A5: A4()

@TypeClass
interface TC<in T1, out T2, T3> {
    fun doStuff() {}
}

@TypeClassMember
object HighestMember : TC<A1, A1, A1>

@TypeClassMember
object MiddleMember: TC<A2, A3, A4>

@TypeClassMember
object LowestMember : TC<A5, A5, A5>

fun <T1, T2, T3, C: TC<T1, T2, T3>> doStuff() {}

fun main() {
    <caret>doStuff<A1, A1, A1>()
    <caret>doStuff<A3, A2, A4>()
    <caret>doStuff<A5, A5, A5>()
}