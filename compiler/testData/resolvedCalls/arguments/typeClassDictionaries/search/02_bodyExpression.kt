package test

@TypeClass
interface C<T>

fun <T, @TypeClass D: C<T>>     invariant() {}
fun <T, @TypeClass D: C<out T>> covariant() {}
fun <T, @TypeClass D: C<in T>>  contravariant() {}

fun <T, @TypeClass D1: C<T>,
        @TypeClass D2: C<out T>,
        @TypeClass D3: C<in T>> test1() {
    <caret>invariant<T>()
    <caret>covariant<T>()
    <caret>contravariant<T>()
}

fun <T, @TypeClass D: C<T>> test2() {
    <caret>covariant<T>()
    <caret>contravariant<T>()
}

fun <T, @TypeClass D: C<out T>> test3() {
    <caret>invariant<T>()
    <caret>contravariant<T>()
}

fun <T, @TypeClass D: C<in T>> test4() {
    <caret>invariant<T>()
    <caret>covariant<T>()
}