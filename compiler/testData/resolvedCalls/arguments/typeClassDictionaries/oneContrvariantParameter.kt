package test

open class A1
open class A2: A1()
open class A3: A2()
open class A4: A3()
open class A5: A4()

@TypeClass
interface TC<T> {
    fun doStuff(): T
}

object A1Member : TC<A1> {
    override fun doStuff(): A1 = A1()
}

object A2Member : TC<A2> {
    override fun doStuff(): A2 = A2()
}

fun <T, @TypeClass C: TC<in T>> doStuff(): T = TC.doStuff()

fun main() {
    <caret>doStuff<A1>()
    <caret>doStuff<A2>()
    <caret>doStuff<A4>()
}