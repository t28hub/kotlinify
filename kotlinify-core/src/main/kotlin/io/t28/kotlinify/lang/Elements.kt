/*
 * Copyright (c) 2022 Tatsuya Maki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.t28.kotlinify.lang

import io.t28.kotlinify.lang.impl.AnnotationValueImpl
import kotlinx.collections.immutable.toImmutableList
import kotlin.reflect.KClass

/**
 * Whether the [AnnotatedElement] has given [Annotation].
 */
inline fun <reified T : Annotation> AnnotatedElement.hasAnnotation(): Boolean {
    return hasAnnotation(T::class)
}

/**
 * Whether the [AnnotatedElement] has given [Annotation].
 */
fun <T : Annotation> AnnotatedElement.hasAnnotation(annotationClass: KClass<T>): Boolean {
    val found = findAnnotations { annotation ->
        annotation.type == annotationClass
    }
    return found.isNotEmpty()
}


/**
 * Finds annotations by using the [filter].
 *
 * @param filter The filter function.
 */
fun AnnotatedElement.findAnnotations(filter: (AnnotationValue) -> Boolean): List<AnnotationValue> {
    return annotations.filter(filter).toImmutableList()
}

/**
 * Creates [AnnotationValue].
 *
 * @param members The members of [Annotation].
 */
inline fun <reified T : Annotation> annotation(vararg members: String): AnnotationValue {
    return annotation(T::class, members.toList())
}

/**
 * Creates [AnnotationValue] from [KClass].
 *
 * @param type The annotation class.
 * @param members The members of [Annotation].
 */
fun <T : Annotation> annotation(type: KClass<T>, members: List<String>): AnnotationValue {
    return AnnotationValueImpl(type = type, members = members.toImmutableList())
}

/**
 * Returns whether the [TypeElement] has properties.
 */
fun TypeElement.hasProperties(): Boolean {
    return properties.isNotEmpty()
}
