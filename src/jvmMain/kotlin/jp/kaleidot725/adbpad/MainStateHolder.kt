package jp.kaleidot725.adbpad

import com.malinskiy.adam.request.device.Device
import jp.kaleidot725.adbpad.model.data.Command
import jp.kaleidot725.adbpad.model.data.Dialog
import jp.kaleidot725.adbpad.model.data.Menu
import jp.kaleidot725.adbpad.model.usecase.AddInputTextUseCase
import jp.kaleidot725.adbpad.model.usecase.DeleteInputTextUseCase
import jp.kaleidot725.adbpad.model.usecase.ExecuteCommandUseCase
import jp.kaleidot725.adbpad.model.usecase.ExecuteInputTextCommandUseCase
import jp.kaleidot725.adbpad.model.usecase.GetCommandListUseCase
import jp.kaleidot725.adbpad.model.usecase.GetDevicesFlowUseCase
import jp.kaleidot725.adbpad.model.usecase.GetInputTextUseCase
import jp.kaleidot725.adbpad.model.usecase.GetMenuListUseCase
import jp.kaleidot725.adbpad.model.usecase.StartAdbUseCase
import jp.kaleidot725.adbpad.model.usecase.TakeScreenshotUseCase
import jp.kaleidot725.adbpad.model.usecase.TakeThemeScreenshotUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class MainStateHolder(
    val getMenuListUseCase: GetMenuListUseCase = GetMenuListUseCase(),
    val startAdbUseCase: StartAdbUseCase = StartAdbUseCase(),
    val getAndroidDevicesFlowUseCase: GetDevicesFlowUseCase = GetDevicesFlowUseCase(),
    val getCommandListUseCase: GetCommandListUseCase = GetCommandListUseCase(),
    val getInputTextUseCase: GetInputTextUseCase = GetInputTextUseCase(),
    val executeCommandUseCase: ExecuteCommandUseCase = ExecuteCommandUseCase(),
    val executeInputTextCommandUseCase: ExecuteInputTextCommandUseCase = ExecuteInputTextCommandUseCase(),
    val addInputTextUseCase: AddInputTextUseCase = AddInputTextUseCase(),
    val deleteInputTextUseCase: DeleteInputTextUseCase = DeleteInputTextUseCase(),
    val takeScreenshotUseCase: TakeScreenshotUseCase = TakeScreenshotUseCase(),
    val takeThemeScreenshotUseCase: TakeThemeScreenshotUseCase = TakeThemeScreenshotUseCase()
) {
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + Dispatchers.IO)

    private val menus: MutableStateFlow<List<Menu>> = MutableStateFlow(emptyList())
    private val selectedMenu: MutableStateFlow<Menu?> = MutableStateFlow(null)
    private val commands: MutableStateFlow<List<Command>> = MutableStateFlow(emptyList())
    private val devices: MutableStateFlow<List<Device>> = MutableStateFlow(emptyList())
    private val selectedDevice: MutableStateFlow<Device?> = MutableStateFlow(null)
    private val inputTexts: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    private val userInputText: MutableStateFlow<String> = MutableStateFlow("")
    private val previewImage1: MutableStateFlow<File?> = MutableStateFlow(null)
    private val previewImage2: MutableStateFlow<File?> = MutableStateFlow(null)
    private val dialog: MutableStateFlow<Dialog?> = MutableStateFlow(null)

    val state: StateFlow<MainState> = combine(
        menus,
        selectedMenu,
        commands,
        devices,
        selectedDevice,
        inputTexts,
        userInputText,
        previewImage1,
        previewImage2,
        dialog
    ) {
        MainState(
            menus = it[0] as List<Menu>,
            selectedMenu = it[1] as Menu?,
            commands = it[2] as List<Command>,
            devices = it[3] as List<Device>,
            selectedDevice = it[4] as Device?,
            inputTexts = it[5] as List<String>,
            userInputText = it[6] as String,
            imageFile1 = it[7] as File?,
            imageFile2 = it[8] as File?,
            dialog = it[9] as Dialog?
        )
    }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(), MainState())

    fun setup() {
        menus.value = getMenuListUseCase()
        selectedMenu.value = menus.value.firstOrNull()
        commands.value = getCommandListUseCase()

        coroutineScope.launch {
            inputTexts.value = getInputTextUseCase()
        }

        coroutineScope.launch {
            startAdbUseCase()
            getAndroidDevicesFlowUseCase(coroutineScope).collect {
                devices.value = it
                val hasNotDevice = !it.contains(selectedDevice.value)
                if (hasNotDevice) selectedDevice.value = it.firstOrNull()
            }
        }
    }

    fun selectDevice(device: Device) {
        selectedDevice.value = device
    }

    fun selectMenu(menu: Menu) {
        selectedMenu.value = menu
    }

    fun executeCommand(command: Command) {
        coroutineScope.launch {
            val serial = state.value.selectedDevice?.serial
            executeCommandUseCase(serial, command)
        }
    }

    fun inputText(text: String) {
        coroutineScope.launch {
            val serial = state.value.selectedDevice?.serial
            executeInputTextCommandUseCase(serial, text)
        }
    }

    fun updateInputText(text: String) {
        val isLettersOrDigits = text.none {
            it !in 'A'..'Z' && it !in 'a'..'z' && it !in '0'..'9'
        }
        if (isLettersOrDigits) this.userInputText.value = text
    }

    fun saveInputText(text: String) {
        coroutineScope.launch {
            addInputTextUseCase(text)
            inputTexts.value = getInputTextUseCase()
        }
    }

    fun deleteInputText(text: String) {
        coroutineScope.launch {
            deleteInputTextUseCase(text)
            inputTexts.value = getInputTextUseCase()
        }
    }

    fun takeScreenShot() {
        coroutineScope.launch {
            val serial = state.value.selectedDevice?.serial
            val filePath = takeScreenshotUseCase(serial) ?: return@launch
            previewImage1.value = File(filePath)
            previewImage2.value = null
        }
    }

    fun takeThemeScreenShot() {
        coroutineScope.launch {
            val serial = state.value.selectedDevice?.serial
            val filePathPair = takeThemeScreenshotUseCase(serial) ?: return@launch
            previewImage1.value = File(filePathPair.first)
            previewImage2.value = File(filePathPair.second)
        }
    }

    fun showSettingDialog() {
        dialog.value = Dialog.Setting
    }

    fun closeDialog() {
        dialog.value = null
    }

    fun dispose() {
        coroutineScope.cancel()
    }
}
