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
interface TC<T> {
    fun doStuff(): T
}

@TypeClassMember
object A1Member : TC<A1> {
    override fun doStuff(): A1 = A1()
}

@TypeClassMember
object A2Member : TC<A2> {
    override fun doStuff(): A2 = A2()
}

fun <T> doStuff(dictionary: TC<in T>): T = dictionary.doStuff()

fun main() {
    <caret>doStuff<A1>()
    <caret>doStuff<A2>()
    <caret>doStuff<A4>()
}