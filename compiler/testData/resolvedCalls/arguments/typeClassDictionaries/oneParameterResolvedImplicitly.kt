package test

@Target(AnnotationTarget.CLASS)
annotation class TypeClass

@Target(AnnotationTarget.CLASS)
annotation class TypeClassMember

@TypeClass
interface TC<T> {
    fun doStuff(t: T)
}

@TypeClassMember
object TCMember : TC<Double> {
    override fun doStuff(t: Double) {}
}

fun <T> doStuff(dictionary: TC<T>, t: T) = dictionary.doStuff(t)

fun main() {
    <caret>doStuff<Double>(0.0)
}