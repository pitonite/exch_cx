package io.github.pitonite.exch_cx.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.model.SnackbarMessage
import io.github.pitonite.exch_cx.model.UserMessage
import io.github.pitonite.exch_cx.ui.components.SnackbarManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// from
// https://github.com/yveskalume/compose-qrpainter/blob/master/qrpainter/src/main/java/com/yveskalume/compose/qrpainter/rememberQrBitmapPainter.kt
// which is based on:
// https://dev.to/devniiaddy/qr-code-with-jetpack-compose-47e

/**
 * Creates a [BitmapPainter] that draws a QR code for the given [content]. The [size] parameter
 * defines the size of the QR code in dp. The [padding] parameter defines the padding of the QR code
 * in dp.
 */
@Composable
fun rememberQrBitmapPainter(content: String, size: Dp = 150.dp, padding: Dp = 0.dp): BitmapPainter {

  check(content.isNotEmpty()) { "Content must not be empty" }
  check(size >= 0.dp) { "Size must be positive" }
  check(padding >= 0.dp) { "Padding must be positive" }

  val density = LocalDensity.current
  val sizePx = with(density) { size.roundToPx() }
  val paddingPx = with(density) { padding.roundToPx() }

  val bitmapState = remember { mutableStateOf<Bitmap?>(null) }

  // Use dependency on 'content' to re-trigger the effect when content changes
  LaunchedEffect(content) {
    val bitmap = generateQrBitmap(content, sizePx, paddingPx)
    bitmapState.value = bitmap
  }

  val bitmap = bitmapState.value ?: createDefaultBitmap(sizePx)

  return remember(bitmap) { BitmapPainter(bitmap.asImageBitmap()) }
}

/**
 * Generates a QR code bitmap for the given [content]. The [sizePx] parameter defines the size of
 * the QR code in pixels. The [paddingPx] parameter defines the padding of the QR code in pixels.
 * Returns null if the QR code could not be generated. This function is suspendable and should be
 * called from a coroutine is thread-safe.
 */
private suspend fun generateQrBitmap(content: String, sizePx: Int, paddingPx: Int): Bitmap? =
    withContext(Dispatchers.IO) {
      val qrCodeWriter = QRCodeWriter()

      // Set the QR code margin to the given padding
      val encodeHints =
          mutableMapOf<EncodeHintType, Any?>().apply { this[EncodeHintType.MARGIN] = paddingPx }

      try {
        val bitmapMatrix =
            qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, encodeHints)

        val matrixWidth = bitmapMatrix.width
        val matrixHeight = bitmapMatrix.height

        val colors =
            IntArray(matrixWidth * matrixHeight) { index ->
              val x = index % matrixWidth
              val y = index / matrixWidth
              val shouldColorPixel = bitmapMatrix.get(x, y)
              if (shouldColorPixel) Color.BLACK else Color.WHITE
            }

        Bitmap.createBitmap(colors, matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
      } catch (ex: WriterException) {
        null
      }
    }

/**
 * Creates a default bitmap with the given [sizePx]. The bitmap is transparent. This is used as a
 * fallback if the QR code could not be generated. The bitmap is created on the UI thread.
 */
private fun createDefaultBitmap(sizePx: Int): Bitmap {
  return Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888).apply {
    eraseColor(Color.TRANSPARENT)
  }
}

private fun hasCameraHardware(context: Context): Boolean {
  return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
}

@Composable
fun rememberQrCodeScanner(onScanFinished: (String?) -> Unit): () -> Unit {
  val context = LocalContext.current

  val scannerLauncher =
      rememberLauncherForActivityResult(ScanContract()) { result ->
        onScanFinished(result.contents)
      }

  val launchScannerWithOptions = remember { {
    val options = ScanOptions()
    options.setBeepEnabled(false)
    options.setPrompt(context.getString(R.string.scan_your_qrcode))
    options.setOrientationLocked(false)
    options.setBarcodeImageEnabled(false)
    options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
    scannerLauncher.launch(options)
  } }

  val permLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
          launchScannerWithOptions()
        } else {
          SnackbarManager.showMessage(
              SnackbarMessage.from(
                  UserMessage.from(
                      R.string.permission_camera_denied),
                  duration = SnackbarDuration.Short,
                  withDismissAction = true,
              ))
        }
      }

  val requestLaunch = remember {
    {
      if (hasCameraHardware(context)) {
        if (ContextCompat.checkSelfPermission(
              context,
              Manifest.permission.CAMERA,
          ) == PackageManager.PERMISSION_GRANTED) {
          launchScannerWithOptions()
        } else {
          permLauncher.launch(Manifest.permission.CAMERA)
        }
      } else {
        SnackbarManager.showMessage(
            SnackbarMessage.from(
                UserMessage.from(
                    R.string.hardware_missing_camera),
                duration = SnackbarDuration.Short,
                withDismissAction = true,
            ))
      }

    }
  }

  return requestLaunch
}
