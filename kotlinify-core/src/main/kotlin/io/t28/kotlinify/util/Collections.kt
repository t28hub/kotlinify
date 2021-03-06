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
package io.t28.kotlinify.util

private const val FIRST_INDEX = 0

/**
 * Return the first element, or [defaultValue] if the list is empty.
 */
fun <T> List<T>.firstOrElse(defaultValue: T): T {
    return if (isEmpty()) defaultValue else this[FIRST_INDEX]
}

/**
 * Insert an element at first index.
 *
 * @param element The element to be inserted.
 */
fun <E> MutableList<E>.addFirst(element: E) {
    add(FIRST_INDEX, element)
}
