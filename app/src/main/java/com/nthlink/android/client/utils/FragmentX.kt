package com.nthlink.android.client.utils

import android.app.ProgressDialog
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.fragment.app.Fragment
import com.nthlink.android.client.R

fun Fragment.showProgressDialog(): ProgressDialog = ProgressDialog.show(
    requireContext(),
    null,
    getString(R.string.word_loading),
    true,
    false
)

fun Fragment.getColor(res: Int): Int = resources.getColor(res, requireActivity().theme)

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