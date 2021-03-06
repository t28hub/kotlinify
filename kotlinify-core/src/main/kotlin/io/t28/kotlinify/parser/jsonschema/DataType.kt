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
package io.t28.kotlinify.parser.jsonschema

import io.t28.kotlinify.serialization.serializers.DataTypeSerializer
import kotlinx.serialization.Serializable

@Serializable(with = DataTypeSerializer::class)
enum class DataType(val typeName: String) {
    // Represents a boolean
    BOOLEAN("boolean"),

    // Represents an integer
    INTEGER("integer"),

    // Represents an integer or float
    NUMBER("number"),

    // Represents a string/text
    STRING("string"),

    // Represents an ordered list
    ARRAY("array"),

    // Represents a key-value map
    OBJECT("object"),

    // Represents a null
    NULL("null");

    override fun toString(): String {
        return typeName
    }

    companion object {
        fun findByTypeName(typeName: String): DataType? {
            return values().firstOrNull {
                it.typeName == typeName
            }
        }
    }
}
