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

package org.jetbrains.kotlin.types.checker

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.resolve.calls.inference.wrapWithCapturingSubstitution
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.typesApproximation.approximateCapturedTypes
import java.util.*

private class SubtypePathNode(val type: KotlinType, val previous: SubtypePathNode?, val level: Int)

fun findCorrespondingSupertype(
        subtype: KotlinType, supertype: KotlinType,
        typeCheckingProcedureCallbacks: TypeCheckingProcedureCallbacks
): KotlinType? {
    val queue = ArrayDeque<SubtypePathNode>()
    queue.add(SubtypePathNode(subtype, null, 0))

    val supertypeConstructor = supertype.constructor

    while (!queue.isEmpty()) {
        val lastPathNode = queue.poll()
        val currentSubtype = lastPathNode.type
        val constructor = currentSubtype.constructor

        if (typeCheckingProcedureCallbacks.assertEqualTypeConstructors(constructor, supertypeConstructor)) {
            var substituted = currentSubtype
            var isAnyMarkedNullable = currentSubtype.isMarkedNullable

            var currentPathNode = lastPathNode.previous

            while (currentPathNode != null) {
                val currentType = currentPathNode.type
                if (currentType.arguments.any { it.projectionKind != Variance.INVARIANT }) {
                    substituted = TypeConstructorSubstitution.create(currentType)
                                        .wrapWithCapturingSubstitution().buildSubstitutor()
                                        .safeSubstitute(substituted, Variance.INVARIANT)
                                        .approximate()
                }
                else {
                    substituted = TypeConstructorSubstitution.create(currentType)
                                        .buildSubstitutor()
                                        .safeSubstitute(substituted, Variance.INVARIANT)
                }

                isAnyMarkedNullable = isAnyMarkedNullable || currentType.isMarkedNullable

                currentPathNode = currentPathNode.previous
            }

            val substitutedConstructor = substituted.constructor
            if (!typeCheckingProcedureCallbacks.assertEqualTypeConstructors(substitutedConstructor, supertypeConstructor)) {
                throw AssertionError("Type constructors should be equals!\n" +
                                     "substitutedSuperType: ${substitutedConstructor.debugInfo()}, \n\n" +
                                     "supertype: ${supertypeConstructor.debugInfo()} \n" +
                                     typeCheckingProcedureCallbacks.assertEqualTypeConstructors(substitutedConstructor, supertypeConstructor))
            }

            return TypeUtils.makeNullableAsSpecified(substituted, isAnyMarkedNullable)
        }

        for (immediateSupertype in constructor.supertypes) {
            queue.add(SubtypePathNode(immediateSupertype, lastPathNode, lastPathNode.level + 1))
        }
    }

    return null
}

private fun KotlinType.approximate() = approximateCapturedTypes(this).upper

private fun TypeConstructor.debugInfo() = buildString {
    operator fun String.unaryPlus() = appendln(this)

    + "type: ${this@debugInfo}"
    + "hashCode: ${this@debugInfo.hashCode()}"
    + "javaClass: ${this@debugInfo.javaClass.canonicalName}"
    var declarationDescriptor: DeclarationDescriptor? = declarationDescriptor
    while (declarationDescriptor != null) {

        + "fqName: ${DescriptorRenderer.FQ_NAMES_IN_TYPES.render(declarationDescriptor)}"
        + "javaClass: ${declarationDescriptor.javaClass.canonicalName}"

        declarationDescriptor = declarationDescriptor.containingDeclaration
    }
}

fun filterSuperTypesAndOrder(
        subtype: KotlinType,
        supertypes: Set<KotlinType>
): List<Set<KotlinType>> {
    val queue = ArrayDeque<SubtypePathNode>()
    queue.add(SubtypePathNode(subtype, null, 0))

    val typesHierarchy = mutableListOf<MutableSet<KotlinType>>()
    var lastResultLevel = -1
    while (!queue.isEmpty()) {
        val lastPathNode = queue.poll()
        val currentSubtype = lastPathNode.type
        val constructor = currentSubtype.constructor

        for (supertype in supertypes) {
            val supertypeConstructor = supertype.constructor
            if (constructor == supertypeConstructor) {
                val (substituted, shouldBeMarkedAsNullable) = scanPathUpAndSubstitute(lastPathNode)

                val substitutedConstructor = substituted.constructor
                if (substitutedConstructor != supertypeConstructor) {
                    throw AssertionError("Type constructors should be equals!\n" +
                                         "substitutedSuperType: ${substitutedConstructor.debugInfo()}, \n\n" +
                                         "supertype: ${supertypeConstructor.debugInfo()} \n")
                }

                val resultType = TypeUtils.makeNullableAsSpecified(supertype, shouldBeMarkedAsNullable)
                if (lastPathNode.level > lastResultLevel) {
                    typesHierarchy.add(mutableSetOf(resultType))
                    lastResultLevel = lastPathNode.level
                }
                if (lastPathNode.level == lastResultLevel) {
                    typesHierarchy.last().add(resultType)
                }
            }
        }

        for (immediateSupertype in constructor.supertypes) {
            queue.add(SubtypePathNode(immediateSupertype, lastPathNode, lastPathNode.level + 1))
        }
    }

    return typesHierarchy
}

fun filterSubTypesAndOrder(
        subtypes: Set<KotlinType>,
        supertype: KotlinType
): List<Set<KotlinType>> {
    val queue = ArrayDeque<SubtypePathNode>()
    for (subtype in subtypes) {
        queue.add(SubtypePathNode(subtype, null, 0))
    }

    val typesHierarchy = mutableListOf<MutableSet<KotlinType>>()
    var lastResultLevel = -1
    while (!queue.isEmpty()) {
        val lastPathNode = queue.poll()
        val currentSubtype = lastPathNode.type
        val constructor = currentSubtype.constructor

        val supertypeConstructor = supertype.constructor
        if (constructor == supertypeConstructor) {
            val (substituted, shouldBeMarkedAsNullable) = scanPathUpAndSubstitute(lastPathNode)

            val substitutedConstructor = substituted.constructor
            if (substitutedConstructor != supertypeConstructor) {
                throw AssertionError("Type constructors should be equals!\n" +
                                     "substitutedSuperType: ${substitutedConstructor.debugInfo()}, \n\n" +
                                     "supertype: ${supertypeConstructor.debugInfo()} \n")
            }

            var initialSubtype = lastPathNode.type
            var currentPathNode = lastPathNode.previous
            while (currentPathNode != null) {
                initialSubtype = currentPathNode.type
                currentPathNode = currentPathNode.previous
            }
            //EK: TODO investigate nullability on this
            val resultType = TypeUtils.makeNullableAsSpecified(initialSubtype, shouldBeMarkedAsNullable)
            if (lastPathNode.level > lastResultLevel) {
                typesHierarchy.add(mutableSetOf(resultType))
                lastResultLevel = lastPathNode.level
            }
            if (lastPathNode.level == lastResultLevel) {
                typesHierarchy.last().add(resultType)
            }
        }

        for (immediateSupertype in constructor.supertypes) {
            queue.add(SubtypePathNode(immediateSupertype, lastPathNode, lastPathNode.level + 1))
        }
    }

    return typesHierarchy
}

fun filterEqualTypes(
        type: KotlinType,
        toChooseFrom: Set<KotlinType>
) : Set<KotlinType> {
    val equalTypes = mutableSetOf<KotlinType>()
    val constructor = type.constructor
    for (type in toChooseFrom) {
        val supertypeConstructor = type.constructor
        if (constructor == supertypeConstructor) {
            equalTypes.add(type)
        }
    }
    return equalTypes
}

private fun scanPathUpAndSubstitute(lastPathNode: SubtypePathNode): Pair<KotlinType, Boolean> {
    var substituted = lastPathNode.type
    var isAnyMarkedNullable = substituted.isMarkedNullable

    var currentPathNode = lastPathNode.previous

    while (currentPathNode != null) {
        val currentType = currentPathNode.type
        if (currentType.arguments.any { it.projectionKind != Variance.INVARIANT }) {
            substituted = TypeConstructorSubstitution.create(currentType)
                    .wrapWithCapturingSubstitution().buildSubstitutor()
                    .safeSubstitute(substituted, Variance.INVARIANT)
                    .approximate()
        }
        else {
            substituted = TypeConstructorSubstitution.create(currentType)
                    .buildSubstitutor()
                    .safeSubstitute(substituted, Variance.INVARIANT)
        }

        isAnyMarkedNullable = isAnyMarkedNullable || currentType.isMarkedNullable

        currentPathNode = currentPathNode.previous
    }
    return Pair(substituted, isAnyMarkedNullable)
}