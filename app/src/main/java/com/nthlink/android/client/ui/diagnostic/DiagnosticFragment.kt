package com.nthlink.android.client.ui.diagnostic

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nthlink.android.client.R
import com.nthlink.android.client.databinding.FragmentDiagnosticBinding
import com.nthlink.android.client.ui.common.BindingFragment
import com.nthlink.android.client.utils.copyToClipboard
import com.nthlink.android.client.utils.getRoot
import com.nthlink.android.client.utils.showMessageDialog
import com.nthlink.android.client.utils.showProgressDialog
import com.nthlink.android.core.Root
import com.nthlink.android.core.Root.DiagnosticResult
import com.nthlink.android.core.Root.Status.CONNECTED
import kotlinx.coroutines.launch

class DiagnosticFragment : BindingFragment<FragmentDiagnosticBinding>() {
    private lateinit var root: Root
    private var loadingDialog: AlertDialog? = null

    override fun bindView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDiagnosticBinding {
        return FragmentDiagnosticBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        root = getRoot()

        binding.copyReportId.isVisible = false
        binding.start.setOnClickListener {
            binding.start.isEnabled = false

            // show alert dialog before starting diagnostics if VPN is connected
            if (root.status == CONNECTED) {
                showMessageDialog(
                    messageId = R.string.diagnostic_disconnect_alert,
                    onPositive = { startDiagnostics() },
                    onNegative = {}
                )
            } else startDiagnostics()
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    root.diagnosticResultFlow.collect { result ->
                        loadingDialog?.dismiss()
                        loadingDialog = null

                        when (result) {
                            is DiagnosticResult.ErrNoInternet -> {
                                showMessageDialog(messageId = R.string.error_no_internet)
                            }

                            is DiagnosticResult.Ok -> {
                                setupResult(result)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startDiagnostics() {
        loadingDialog = showProgressDialog()
        root.startDiagnostics()
    }

    private fun setupResult(result: DiagnosticResult.Ok) {
        val message = """
            ${getString(R.string.feedback_submit_success_message)}
                                    
            Report ID: ${result.reportId}
            """.trimIndent()

        binding.result.text = message
        binding.copyReportId.isVisible = true
        binding.copyReportId.setOnClickListener {
            copyToClipboard("Report ID", result.reportId)
            Toast.makeText(requireContext(), R.string.copied, Toast.LENGTH_LONG).show()
        }
    }
}