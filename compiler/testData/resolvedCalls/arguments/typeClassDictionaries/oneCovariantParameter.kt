package test

@Target(AnnotationTarget.CLASS)
annotation class TypeClass

open class A1
open class A2: A1()
open class A3: A2()
open class A4: A3()
open class A5: A4()

@TypeClass
interface TC<out T> {
    fun doStuff(): T
}

object A4Member : TC<A4> {
    override fun doStuff(): A4 = A4()
}

object A5Member : TC<A5> {
    override fun doStuff(): A5 = A5()
}

fun <T> doStuff(dictionary: TC<T>): T = dictionary.doStuff()

fun main() {
    <caret>doStuff<A2>()
    <caret>doStuff<A3>()
    <caret>doStuff<A5>()
}