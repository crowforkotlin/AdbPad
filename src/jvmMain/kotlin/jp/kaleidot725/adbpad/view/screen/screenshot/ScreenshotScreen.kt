package jp.kaleidot725.adbpad.view.screen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import jp.kaleidot725.adbpad.domain.model.command.ScreenshotCommand
import jp.kaleidot725.adbpad.view.component.screenshot.ScreenshotDropDownButton
import jp.kaleidot725.adbpad.view.component.screenshot.ScreenshotViewer
import java.io.File

@Composable
fun ScreenshotScreen(
    image1: File?,
    image2: File?,
    commands: List<ScreenshotCommand>,
    onTakeScreenshot: (ScreenshotCommand) -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        ScreenshotViewer(
            image1 = image1,
            image2 = image2,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.5f)
                .border(
                    border = BorderStroke(1.dp, Color.LightGray),
                    shape = RoundedCornerShape(4.dp)
                )
        )

        ScreenshotDropDownButton(
            commands = commands,
            onTakeScreenshot = onTakeScreenshot,
            modifier = Modifier.wrapContentSize().align(Alignment.End)
        )
    }
}

@Composable
@Preview
private fun ScreenshotScreen_Preview() {
    ScreenshotScreen(
        image1 = null,
        image2 = null,
        commands = emptyList(),
        onTakeScreenshot = {}
    )
}
