package test

@TypeClass
interface TC<T> {
    fun doStuff(t: T)

    companion object {
        fun <T> foo(t: T) {}
    }
}

object TCMember : TC<Double> {
    override fun doStuff(t: Double) {}
}

fun <T, @TypeClass C : TC<T>> doStuff(t: T) = TC.<caret>doStuff(t)

fun main() {
    val d: Double = 0.0
    <caret>doStuff(d)
}