package test

@TypeClass
interface TC<T> {
    fun doStuff(t: T)
}

object TCMember : TC<Double> {
    override fun doStuff(t: Double) {}
}

fun <T, @TypeClass C: TC<T>> doStuff(t: T) = TC.doStuff(t)

fun main() {
    <caret>doStuff<Double>(0.0)
}