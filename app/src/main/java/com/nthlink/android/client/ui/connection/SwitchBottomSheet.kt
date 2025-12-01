package com.nthlink.android.client.ui.connection

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.nthlink.android.client.databinding.BottomSheetSwitchBinding


class SwitchBottomSheet(
    private var _binding: BottomSheetSwitchBinding?,
    private var onExpanded: (() -> Unit)?
) : BottomSheetCallback() {
    private val binding get() = _binding!!

    private val behavior = BottomSheetBehavior.from(binding.root).apply {
        addBottomSheetCallback(this@SwitchBottomSheet)
    }

    var isDraggable: Boolean
        set(value) {
            behavior.isDraggable = value
        }
        get() = behavior.isDraggable


    init {
        binding.dragHandle.post {
            _binding?.dragHandle?.let { behavior.peekHeight = it.height }
        }
    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        if (newState == STATE_EXPANDED) onExpanded?.invoke()
    }

    override fun onSlide(bottomSheet: View, slideOffset: Float) {}

    fun collapse() {
        behavior.state = STATE_COLLAPSED
    }

    fun expand() {
        behavior.state = STATE_EXPANDED
    }

    fun toggle() {
        behavior.state = if (behavior.state == STATE_COLLAPSED) STATE_EXPANDED else STATE_COLLAPSED
    }

    fun onDestroyView() {
        onExpanded = null
        behavior.removeBottomSheetCallback(this)
    }
}