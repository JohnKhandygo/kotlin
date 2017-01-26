// WITH_RUNTIME
// WITH_COROUTINES

// FILE: stuff.kt
package stuff

import kotlin.coroutines.experimental.intrinsics.*

object Host {
    suspend fun suspendHere(): String = suspendCoroutineOrReturn { x ->
        x.resume("OK")
        SUSPENDED_MARKER
    }
}


// FILE: test.kt
import kotlin.coroutines.experimental.*
import kotlin.coroutines.experimental.intrinsics.*
import stuff.Host.suspendHere

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

fun box(): String {
    var result = ""

    builder {
        result = suspendHere()
    }

    return result
}
