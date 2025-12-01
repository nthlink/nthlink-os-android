package com.nthlink.android.client.utils

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nthlink.android.client.App
import com.nthlink.android.client.R
import com.nthlink.android.client.storage.sql.AppDatabase
import com.nthlink.android.client.ui.MainActivity
import com.nthlink.android.core.utils.NO_RESOURCE

fun Fragment.showProgressDialog(): ProgressDialog = ProgressDialog.show(
    requireContext(),
    null,
    getString(R.string.word_loading),
    true,
    false
)

fun Fragment.showMessageDialog(
    @StringRes messageId: Int,
    @StringRes positiveText: Int = R.string.ok,
    onPositive: (() -> Unit)? = null,
    @StringRes negativeText: Int = R.string.cancel,
    onNegative: (() -> Unit)? = null
) {
    showMessageDialog(
        requireContext(),
        messageId,
        positiveText,
        onPositive,
        negativeText,
        onNegative
    )
}

fun Fragment.vibrate() {
    val vibrator = requireContext().getSystemService(Vibrator::class.java)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
        val attributes = VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH)
        vibrator.vibrate(effect, attributes)
    } else {
        val effect = VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(effect)
    }
}

fun Fragment.getDb(): AppDatabase = (requireActivity().application as App).db

fun Fragment.getMainActivity() = (requireActivity() as MainActivity)

fun Fragment.getRoot() = getMainActivity().root

fun Fragment.getColor(@ColorRes resId: Int) = ContextCompat.getColor(requireContext(), resId)

fun Fragment.showMaterialAlertDialog(
    overrideThemeResId: Int = NO_RESOURCE,
    setBuilder: MaterialAlertDialogBuilder.() -> Unit
): AlertDialog = showMaterialAlertDialog(requireContext(), overrideThemeResId, setBuilder)

fun Fragment.copyToClipboard(label: String, text: String) {
    copyToClipboard(requireContext(), label, text)
}

fun Fragment.openWebPage(url: String) {
    val intent = getLoadWebUrlIntent(url)
    if (intent.resolveActivity(requireContext().packageManager) != null) {
        startActivity(intent)
    }
}

fun Fragment.shareText(text: String, type: String = SHARE_TYPE_TEXT) {
    val intent = getSendTextIntent(text, type)
    val shareIntent = Intent.createChooser(intent, null)
    startActivity(shareIntent)
}