package io.github.pitonite.exch_cx.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

fun isPermissionGranted(context: Context, permission: String): Boolean =
    ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

