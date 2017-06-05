package test

@TypeClass
interface C<T>

fun <T, @TypeClass D: C<T>>     invariant() {}
fun <T, @TypeClass D: C<out T>> covariant() {}
fun <T, @TypeClass D: C<in T>>  contravariant() {}

fun <T> withAnonymous(lambda: (Unit) -> Unit) {}

fun <T, @TypeClass D1: C<T>,
        @TypeClass D2: C<out T>,
        @TypeClass D3: C<in T>> test1() {
    withAnonymous<T> {
        withAnonymous<T> {
            <caret>invariant<T>()
        }
    }
    withAnonymous<T> {
        withAnonymous<T> {
            <caret>covariant<T>()
        }
    }
    withAnonymous<T> {
        withAnonymous<T> {
            <caret>contravariant<T>()
        }
    }
}

fun <T, @TypeClass D: C<T>> test2() {
    withAnonymous<T> {
        withAnonymous<T> {
            <caret>covariant<T>()
        }
    }
    withAnonymous<T> {
        withAnonymous<T> {
            <caret>contravariant<T>()
        }
    }
}

fun <T, @TypeClass D: C<out T>> test3() {
    withAnonymous<T> {
        withAnonymous<T> {
            <caret>invariant<T>()
        }
    }
    withAnonymous<T> {
        withAnonymous<T> {
            <caret>contravariant<T>()
        }
    }
}

fun <T, @TypeClass D: C<in T>> test4() {
    withAnonymous<T> {
        withAnonymous<T> {
            <caret>invariant<T>()
        }
    }
    withAnonymous<T> {
        withAnonymous<T> {
            <caret>covariant<T>()
        }
    }
}