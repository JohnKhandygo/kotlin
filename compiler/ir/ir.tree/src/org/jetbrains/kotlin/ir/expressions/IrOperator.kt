/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.ir.expressions

enum class IrOperator {
    INVOKE, INVOKE_EXTENSION,
    PREFIX_INCR, PREFIX_DECR, POSTFIX_INCR, POSTFIX_DECR,
    UMINUS,
    EXCL,
    EXCLEXCL,
    LT, GT, LTEQ, GTEQ,
    EQEQ, EQEQEQ, EXCLEQ, EXCLEQEQ,
    IN, NOT_IN,
    ANDAND, OROR,
    RANGE,
    PLUS, MINUS, MUL, DIV, MOD,
    EQ,
    PLUSEQ, MINUSEQ, MULEQ, DIVEQ, MODEQ;
}

private val CAO_START = IrOperator.PLUSEQ
private val CAO_END = IrOperator.MODEQ
private val DUAL_START = IrOperator.PLUS
private val DUAL_END = IrOperator.MOD
private val INCR_DECR_START = IrOperator.PREFIX_INCR
private val INCR_DECR_END = IrOperator.POSTFIX_DECR

fun IrOperator.isIncrementOrDecrement(): Boolean =
        this in INCR_DECR_START..INCR_DECR_END

fun IrOperator.isCompoundAssignment(): Boolean =
        this in CAO_START .. CAO_END

fun IrOperator.isAssignmentOrCompoundAssignment(): Boolean =
        this == IrOperator.EQ || isCompoundAssignment()

fun IrOperator.hasCompoundAssignmentDual(): Boolean =
        this in DUAL_START .. DUAL_END

fun IrOperator.toDualOperator(): IrOperator =
        when {
            isCompoundAssignment() ->
                IrOperator.values()[this.ordinal - CAO_START.ordinal + DUAL_START.ordinal]
            hasCompoundAssignmentDual() ->
                IrOperator.values()[this.ordinal - DUAL_START.ordinal + CAO_START.ordinal]
            else ->
                throw UnsupportedOperationException("Operator $this is not a compound assignment")
        }
