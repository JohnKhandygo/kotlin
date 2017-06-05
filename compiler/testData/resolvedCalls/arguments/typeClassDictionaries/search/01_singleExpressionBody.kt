package test

@TypeClass
interface C<T>

fun <T, @TypeClass D: C<T>>     invariant() {}
fun <T, @TypeClass D: C<out T>> covariant() {}
fun <T, @TypeClass D: C<in T>>  contravariant() {}

fun <T, @TypeClass D1: C<T>,
        @TypeClass D2: C<out T>,
        @TypeClass D3: C<in T>> test1() = <caret>invariant<T>()
fun <T, @TypeClass D1: C<T>,
        @TypeClass D2: C<out T>,
        @TypeClass D3: C<in T>> test2() = <caret>covariant<T>()
fun <T, @TypeClass D1: C<T>,
        @TypeClass D2: C<out T>,
        @TypeClass D3: C<in T>> test3() = <caret>contravariant<T>()

fun <T, @TypeClass D: C<T>> test4() = <caret>covariant<T>()
fun <T, @TypeClass D: C<T>> test5() = <caret>contravariant<T>()

fun <T, @TypeClass D: C<out T>> test6() = <caret>invariant<T>()
fun <T, @TypeClass D: C<out T>> test7() = <caret>contravariant<T>()

fun <T, @TypeClass D: C<in T>> test8() = <caret>invariant<T>()
fun <T, @TypeClass D: C<in T>> test9() = <caret>covariant<T>()