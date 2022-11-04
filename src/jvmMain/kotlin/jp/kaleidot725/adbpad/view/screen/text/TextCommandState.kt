package jp.kaleidot725.adbpad.view.screen.text

import jp.kaleidot725.adbpad.domain.model.Device
import jp.kaleidot725.adbpad.domain.model.command.TextCommand

data class TextCommandState(
    val commands: List<TextCommand> = emptyList(),
    val userInputText: String = "",
    val isSendingUserInputText: Boolean = false,
    val selectedDevice: Device? = null
) {
    val canSendCommand: Boolean get() = selectedDevice != null
    val canSendInputText: Boolean get() = selectedDevice != null && userInputText.isNotEmpty()
    val canSaveInputText: Boolean get() = userInputText.isNotEmpty()
}
