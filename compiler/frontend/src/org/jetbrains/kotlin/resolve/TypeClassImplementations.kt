/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.resolve

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.checker.filterEqualTypes
import org.jetbrains.kotlin.types.checker.filterSubTypesAndOrder
import org.jetbrains.kotlin.types.checker.filterSuperTypesAndOrder
import org.jetbrains.kotlin.types.typeUtil.replaceAnnotations

class TypeClassImplementations private constructor(
        private val trace: BindingTrace,
        private val typeClassDescriptor: ClassDescriptor,
        private val implementations: MutableMap<List<KotlinType>, ClassDescriptor> = mutableMapOf()
) {

    //EK: TODO check nullability below!
    private val expressionFactory: KtPsiFactory = KtPsiFactory(DescriptorToSourceUtils.descriptorToDeclaration(typeClassDescriptor)!!)

    private val typeParameters: List<TypeParameterDescriptor> = typeClassDescriptor.typeConstructor.parameters

    fun addMapping(member: List<KotlinType>, implementationDescriptor: ClassDescriptor): Boolean {
        if (implementations.containsKey(member)) {
            return false
        }
        implementations.put(member, implementationDescriptor)
        return true
    }

    fun getImplementationInstanceExpression(member: List<TypeProjection>): KtExpression {
        val implementationDescriptor = getImplementationDescriptor(member)
        if (implementationDescriptor == null) {
            //EK: TODO warnings
            return expressionFactory.createExpression("null")
        }
        if (DescriptorUtils.isObject(implementationDescriptor)) {
            return expressionFactory.createExpression(DescriptorUtils.getFqName(implementationDescriptor).asString())
        }
        //EK: TODO warnings
        throw RuntimeException("Don't know how create instance of ${implementationDescriptor}")
    }

    private fun getImplementationDescriptor(member: List<TypeProjection>): ClassDescriptor? {
        //EK: TODO hashing here is incorrect...
        val zeroApproximationSolution = implementations.keys.map { CompositeType(it) }.toSet()
        val nearestSolution = findNearestSolution(member, listOf(zeroApproximationSolution), 0)
        if (nearestSolution.isEmpty()) {
            //EK: TODO warnings
            throw RuntimeException("Found no solutions for type class resolution.")
        }
        if (nearestSolution.size > 1) {
            //EK: TODO warnings
            throw RuntimeException("Found more than one solution for type class resolution.")
        }
        val theOnlySolution = nearestSolution.iterator().next()
        //EK: TODO warnings and return null instead
        return implementations[theOnlySolution.types] ?:
               throw RuntimeException(
                       "No known implementations for typeclass ${typeClassDescriptor.name} and member " + member)

    }

    private fun findNearestSolution(
            typeProjections: List<TypeProjection>,
            orderedSolutions: List<Set<CompositeType>>,
            i: Int
    ): Set<CompositeType> {
        for (solution in orderedSolutions) {
            solution.forEach { it.setRepresentativeType(i) }
            val thisLevelOrderedSolutions = findBestSolutionsForProjection(solution, typeProjections[i], typeParameters[i])
            if (thisLevelOrderedSolutions.isEmpty()) {
                continue
            }
            if (i == typeParameters.size - 1) {
                return thisLevelOrderedSolutions[0]
            }
            val bestFittingTypes = findNearestSolution(typeProjections, thisLevelOrderedSolutions, i + 1)
            if (!bestFittingTypes.isEmpty()) {
                return bestFittingTypes
            }
        }
        return emptySet()
    }

    private fun findBestSolutionsForProjection(
            bestFittingSoFarTypes: Set<CompositeType>,
            projection: TypeProjection,
            typeParameterDescriptor: TypeParameterDescriptor
    ): List<Set<CompositeType>> {
        val declaredVariance = typeParameterDescriptor.variance
        val usedVariance = projection.projectionKind
        val resolutionVariance = getResolutionVarianve(declaredVariance, usedVariance)
        return when (resolutionVariance) {
            Variance.IN_VARIANCE -> filterSuperTypesAndOrder(projection.type, bestFittingSoFarTypes)
            Variance.INVARIANT -> {
                val equalTypesIfAny = filterEqualTypes(projection.type, bestFittingSoFarTypes)
                listOf(equalTypesIfAny)
            }
            Variance.OUT_VARIANCE -> filterSubTypesAndOrder(bestFittingSoFarTypes, projection.type)
        }
    }

    private fun getResolutionVarianve(declaredVariance: Variance, usedVariance: Variance): Variance {
        when (usedVariance) {
            Variance.IN_VARIANCE -> {
                if (declaredVariance.allowsInPosition) {
                    return usedVariance
                }
                //EK: TODO warnings
                throw RuntimeException("Cannot use variance $usedVariance when declared is $declaredVariance.")
            }
            Variance.INVARIANT -> return declaredVariance
            Variance.OUT_VARIANCE -> {
                if (declaredVariance.allowsOutPosition) {
                    return usedVariance
                }
                //EK: TODO warnings
                throw RuntimeException("Cannot use variance $usedVariance when declared is $declaredVariance.")
            }
        }
    }

    private class CompositeType(
            val types: List<KotlinType>,
            private var index: Int = -1
    ) : SimpleType() {

        private val DEFAULT_TYPE: KotlinType = ErrorUtils.createErrorType("There is more then one type available in composite type.")

        private fun <T> performForRepresentativeOrDefault(action: (KotlinType) -> T): T {
            if (isValidRepresentativeType()) return action(types[index])
            return action(DEFAULT_TYPE)
        }

        private fun mapRepresentativeIfAny(action: (KotlinType) -> KotlinType): CompositeType {
            val mapped = types.withIndex().map { (i, v) -> if (i == index) action(v) else v }
            return CompositeType(mapped, index)
        }

        private fun isValidRepresentativeType() = index > -1 && index < types.size

        fun setRepresentativeType(index: Int) {
            this.index = index
        }

        override val annotations: Annotations
            get() = performForRepresentativeOrDefault(KotlinType::annotations)
        override val constructor: TypeConstructor
            get() = performForRepresentativeOrDefault(KotlinType::constructor)
        override val arguments: List<TypeProjection>
            get() = performForRepresentativeOrDefault(KotlinType::arguments)
        override val isMarkedNullable: Boolean
            get() = performForRepresentativeOrDefault(KotlinType::isMarkedNullable)
        override val memberScope: MemberScope
            get() = performForRepresentativeOrDefault(KotlinType::memberScope)
        //EK: TODO does this misconception (non error type with error components) leads to smth bad?
        override val isError: Boolean
            get() = isValidRepresentativeType()

        override fun replaceAnnotations(newAnnotations: Annotations): SimpleType {
            return mapRepresentativeIfAny({ representativeType -> representativeType.replaceAnnotations(newAnnotations).unwrap() })
        }

        override fun makeNullableAsSpecified(newNullability: Boolean): SimpleType {
            return mapRepresentativeIfAny({ representativeType -> representativeType.unwrap().makeNullableAsSpecified(newNullability)})
        }
    }

    companion object {
        fun forDescriptor(
                trace: BindingTrace,
                classDescriptor: ClassDescriptor
        ): TypeClassImplementations {
            if (!DescriptorUtils.isTypeClass(classDescriptor)) {
                throw RuntimeException("Cannot create for class descriptor")
            }
            return TypeClassImplementations(trace, classDescriptor)
        }
    }
}