package jp.kaleidot725.adbpad.ui.screen.screenshot.component

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.dp
import jp.kaleidot725.adbpad.domain.model.UserColor
import jp.kaleidot725.adbpad.domain.model.language.Language
import jp.kaleidot725.adbpad.domain.model.screenshot.Screenshot

@Composable
fun ScreenshotViewer(
    screenshot: Screenshot,
    isCapturing: Boolean,
    onCopyScreenshot: () -> Unit,
    onDeleteScreenshot: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        Column {
            ScreenshotHeader(
                name = screenshot.file?.name ?: "",
                enabled = screenshot.file != null,
                onCopy = onCopyScreenshot,
                onDelete = onDeleteScreenshot,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(Modifier.height(1.dp).fillMaxWidth().border(BorderStroke(1.dp, UserColor.getSplitterColor())))

            if (isCapturing) {
                Box(
                    modifier =
                        Modifier
                            .weight(1.0f)
                            .fillMaxWidth(),
                ) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            } else {
                val file = screenshot.file
                val bitmap = rememberUpdatedState(file?.let { loadImageBitmap(it.inputStream()) })
                val bitmapValue = bitmap.value
                if (bitmapValue != null) {
                    Image(
                        bitmap = bitmapValue,
                        contentDescription = "preview image",
                        modifier = Modifier.weight(1.0f).align(Alignment.CenterHorizontally),
                    )
                } else {
                    Box(
                        modifier =
                            Modifier
                                .weight(1.0f)
                                .fillMaxWidth(),
                    ) {
                        Text(
                            text = Language.notFoundScreenshot,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ScreenshotViewer_Preview() {
    ScreenshotViewer(
        screenshot = Screenshot(null),
        isCapturing = false,
        onCopyScreenshot = {},
        onDeleteScreenshot = {},
        modifier = Modifier.fillMaxSize(),
    )
}
