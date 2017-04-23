package test

@TypeClass
interface TC<T> {
    fun doStuff(t: T)
}

object TCMember : TC<Double> {
    override fun doStuff(t: Double) {}
}

fun <T, C: TC<T>> doStuff(t: T) = TC.doStuff(t)

fun main() {
    val d: Double = 0.0
    <caret>doStuff(d)
}