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

import com.google.common.truth.FailureMetadata
import com.google.common.truth.StringSubject
import com.google.common.truth.Subject
import com.google.common.truth.Truth

/**
 * [Truth] subject implementation for [PropertyNode].
 */
class PropertyNodeSubject(metadata: FailureMetadata, private val actual: PropertyNode) : Subject(metadata, actual) {
    fun hasName(expectedName: String) {
        return name().isEqualTo(expectedName)
    }

    fun hasOriginalName(expectedName: String) {
        return originalName().isEqualTo(expectedName)
    }

    fun value(): Subject {
        return check("value").that(actual.value)
    }

    fun name(): StringSubject {
        return check("name").that(actual.name)
    }

    fun originalName(): StringSubject {
        return check("originalName").that(actual.originalName)
    }

    companion object {
        fun factory() = Factory<PropertyNodeSubject, PropertyNode> { metadata, actual ->
            PropertyNodeSubject(metadata, actual)
        }
    }
}