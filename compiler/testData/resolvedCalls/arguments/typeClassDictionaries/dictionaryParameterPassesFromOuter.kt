package test

@Target(AnnotationTarget.CLASS)
annotation class TypeClass

@Target(AnnotationTarget.CLASS)
annotation class TypeClassMember

@TypeClass
interface TC<T> {
    fun doStuff(t: T)

    companion object {
        fun <T> doStuff(d: TC<T>, t: T) = d.doStuff(t)
    }
}

@TypeClassMember
object TCMember : TC<Double> {
    override fun doStuff(t: Double) {}
}

fun <T, C : TC<T>> doStuff(t: T) = TC.<caret>doStuff(t)

fun main() {
    val d: Double = 0.0
    <caret>doStuff(d)
}