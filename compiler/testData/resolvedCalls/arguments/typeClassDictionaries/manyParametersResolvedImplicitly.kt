package test

@TypeClass
interface TC<T1, T2> {
    fun doStuff(t1: T1, t2: T2, t3: Map<T1, T2>)
}

object TCMember : TC<Double, List<Any?>> {
    override fun doStuff(t1: Double, t2: List<Any?>, t3: Map<Double, List<Any?>>) {}
}

fun <T1, T2, @TypeClass C: TC<T1, T2>> doStuff(t1: T1, t2: T2, t3: Map<T1, T2>) = TC.doStuff(t1, t2, t3)

fun main() {
    <caret>doStuff<Double, List<Any?>>(0.0, emptyList(), emptyMap())
}