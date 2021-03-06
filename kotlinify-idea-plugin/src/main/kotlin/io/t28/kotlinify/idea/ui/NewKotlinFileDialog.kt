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
package io.t28.kotlinify.idea.ui

import com.intellij.json.JsonFileType
import com.intellij.json.JsonLanguage
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.InputValidatorEx
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.text.Strings
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.COLUMNS_SHORT
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.LabelPosition
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.dsl.gridLayout.VerticalAlign
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.ui.layout.toBinding
import io.t28.kotlinify.idea.command.ReformatCommand
import io.t28.kotlinify.idea.message
import io.t28.kotlinify.idea.util.runReadAction
import io.t28.kotlinify.idea.util.runWriteAction
import java.awt.event.ActionEvent
import javax.swing.Action
import javax.swing.JComponent

@Suppress("UnstableApiUsage", "TooManyFunctions")
class NewKotlinFileDialog(
    private val project: Project,
    private val nameValidator: InputValidatorEx = ClassNameValidator(project),
    initialState: InputState = InputState.empty()
) : DialogWrapper(project, true) {
    private val state: InputState

    private var inputText: String
        get() = runReadAction {
            textEditor.document.text
        }
        set(value) = runWriteAction(project) {
            with(textEditor.document) {
                replaceString(0, textLength, value)
            }
        }

    private lateinit var textEditor: Editor

    init {
        state = initialState.copy()
        title = message("action.new.file.dialog.title")
        isResizable = true
        setOKButtonText(message("action.new.file.dialog.button.ok"))
        setCancelButtonText(message("action.new.file.dialog.button.cancel"))

        init()
    }

    override fun createCenterPanel(): JComponent {
        textEditor = createTextEditor()

        return panel {
            nameInputRow()
            formatInputRow()
            textInputRow()
        }.apply {
            withMinimumWidth(MIN_DIALOG_WIDTH)
            withMinimumHeight(MIN_DIALOG_HEIGHT)
        }
    }

    override fun createLeftSideActions(): Array<Action> {
        return arrayOf(
            object : DialogWrapperAction(message("action.new.file.dialog.button.format")) {
                override fun doAction(e: ActionEvent) {
                    performReformatAction()
                }
            }
        )
    }

    override fun doOKAction() {
        val errors = doValidateAll()
        if (errors.isNotEmpty()) {
            return
        }
        super.doOKAction()
    }

    override fun dispose() {
        if (!textEditor.isDisposed) {
            EditorFactory.getInstance().releaseEditor(textEditor)
        }
        super.dispose()
    }

    fun getCurrentState(): InputState {
        return state.copy()
    }

    private fun Panel.nameInputRow() = row(message("action.new.file.dialog.name.title")) {
        textField().apply {
            columns(COLUMNS_SHORT)
            bindText(state::name)

            validationOnApply(::validateClassName)

            focused()
        }
    }.layout(RowLayout.PARENT_GRID)

    private lateinit var formatComboBox: Cell<ComboBox<String>>

    private fun Panel.formatInputRow() = row(message("action.new.file.dialog.format.title")) {
        val items = Format.values().map { type ->
            type.displayName()
        }.toTypedArray()

        formatComboBox = comboBox(items).apply {
            bindItem(
                getter = { state.format.displayName() },
                setter = { name ->
                    state.format = name?.let {
                        Format.findByDisplayName(it)
                    } ?: Format.JSON
                }
            )
        }
    }.layout(RowLayout.PARENT_GRID)

    private fun Panel.textInputRow() = row {
        cell(textEditor.component).apply {
            label(message("action.new.file.dialog.text.title"), LabelPosition.TOP)

            horizontalAlign(HorizontalAlign.FILL)
            verticalAlign(VerticalAlign.FILL)
            resizableColumn()

            validationOnApply(::validateInputText)

            bind(
                componentGet = { inputText },
                componentSet = { _, value -> inputText = value },
                binding = state::content.toBinding()
            )
        }
    }.resizableRow()

    private fun createTextEditor(): Editor {
        val factory = EditorFactory.getInstance()
        val document = factory.createDocument(Strings.EMPTY_CHAR_SEQUENCE)
        return factory.createEditor(document, project, JsonFileType.INSTANCE, false).apply {
            // Register a shortcut for 'Reformat Code'
            val shortcutSet = KeymapUtil.getActiveKeymapShortcuts(IdeActions.ACTION_EDITOR_REFORMAT)
            val action = object : AnAction() {
                override fun actionPerformed(e: AnActionEvent) {
                    performReformatAction()
                }
            }
            action.registerCustomShortcutSet(shortcutSet, component)
        }.initialize()
    }

    private fun validateClassName(builder: ValidationInfoBuilder, textField: JBTextField): ValidationInfo? {
        val className = textField.text.trim()
        return nameValidator.getErrorText(className)?.let { message ->
            builder.error(message)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun validateInputText(builder: ValidationInfoBuilder, component: JComponent): ValidationInfo? {
        val currentFormat = Format.findByDisplayName(formatComboBox.component.item)
        val validator = currentFormat.inputValidator()
        return validator.getErrorText(inputText)?.let { message ->
            builder.error(message)
        }
    }

    private fun performReformatAction() {
        try {
            val reformatted = ReformatCommand(project, inputText, JsonLanguage.INSTANCE).execute()
            inputText = reformatted
        } catch (_: Throwable) {
            // TODO: Report an error
        }
    }

    companion object {
        private const val MIN_DIALOG_WIDTH = 600
        private const val MIN_DIALOG_HEIGHT = 450

        private fun Editor.initialize(): Editor {
            if (this is EditorEx) {
                isEmbeddedIntoDialogWrapper = true
            }

            with(settings) {
                isWhitespacesShown = false
                isIndentGuidesShown = true
                isLineMarkerAreaShown = false
                isLineNumbersShown = true
                isFoldingOutlineShown = false
                isRightMarginShown = false
                isVirtualSpace = false
                additionalColumnsCount = 1
                additionalLinesCount = 0
            }

            with(colorsScheme) {
                setColor(EditorColors.CARET_ROW_COLOR, defaultBackground)
            }
            return this
        }
    }

    data class InputState(
        var name: String,
        var format: Format,
        var content: String
    ) {
        companion object {
            fun empty(): InputState {
                return InputState(name = "", format = Format.JSON, content = "")
            }
        }
    }
}
