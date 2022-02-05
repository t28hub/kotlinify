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
package io.t28.json2kotlin.idea

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import io.t28.json2kotlin.idea.util.getProjectRootManager
import io.t28.json2kotlin.idea.util.ideView
import io.t28.json2kotlin.idea.util.isAvailable
import org.jetbrains.kotlin.idea.KotlinIcons

/**
 * Action class for creating a Kotlin file from JSON or JSON Schenma
 */
class NewKotlinFileAction : AnAction(KotlinIcons.FILE) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (!project.isAvailable()) {
            return
        }

        Notifications.info(
            title = Json2KotlinBundle.message("plugin.name"),
            content = "Json to Kotlin classes conversion plugin for IntelliJ IDEA",
        ).notify(project)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = isEnabled(e)
    }

    private fun isEnabled(e: AnActionEvent): Boolean {
        val project = e.project
        if (!project.isAvailable()) {
            return false
        }

        val ideView = e.ideView ?: return false
        val projectFileIndex = project.getProjectRootManager().fileIndex
        return ideView.directories.any { directory ->
            projectFileIndex.isInSource(directory.virtualFile)
        }
    }
}