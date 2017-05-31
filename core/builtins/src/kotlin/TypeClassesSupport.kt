package kotlin

import kotlin.internal.TypeClassDictionary

fun <D> dictionaryOf(@TypeClassDictionary d: D): D = d