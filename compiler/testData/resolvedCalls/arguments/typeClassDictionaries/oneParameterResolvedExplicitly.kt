package test

@Target(AnnotationTarget.CLASS)
annotation class TypeClass

@TypeClass
interface TC<T> {
    fun doStuff(t: T)
}

object TCMember : TC<Double> {
    override fun doStuff(t: Double) {}
}

fun <T> doStuff(dictionary: TC<T>, t: T) = dictionary.doStuff(t)

fun main() {
    val d: Double = 0.0
    <caret>doStuff(d)
}