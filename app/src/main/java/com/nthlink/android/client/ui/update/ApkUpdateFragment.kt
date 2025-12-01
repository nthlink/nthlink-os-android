package com.nthlink.android.client.ui.update

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.nthlink.android.client.R
import com.nthlink.android.client.databinding.FragmentApkUpdateBinding
import com.nthlink.android.client.ui.common.BindingFragment
import com.nthlink.android.client.updates.ApkDownloadManager
import com.nthlink.android.client.updates.DownloadState
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ApkUpdateFragment : BindingFragment<FragmentApkUpdateBinding>() {
    private val args: ApkUpdateFragmentArgs by navArgs()
    private val viewModel by viewModel<ApkUpdateViewModel>()

    private lateinit var version: String
    private lateinit var downloadUrl: String

    override fun bindView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentApkUpdateBinding {
        return FragmentApkUpdateBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch arguments
        version = args.version.ifEmpty {
            throw IllegalArgumentException("Version is empty")
        }
        downloadUrl = args.url.ifEmpty {
            throw IllegalArgumentException("Download URL is empty")
        }

        // Observe download progress
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.downloadState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    private fun updateUI(state: DownloadState) {
        when (state) {
            DownloadState.Idle -> if (apkFileExists()) uiViewDownloads() else uiReadForDownload()
            is DownloadState.Downloading -> uiDownloading(state.progress)
            DownloadState.Cancelled -> uiReadForDownload()
            DownloadState.Completed -> uiViewDownloads()
            is DownloadState.Failed -> uiReadForDownload(state.error)
        }
    }

    private fun apkFileExists(): Boolean {
        return ApkDownloadManager.getApkFile(version).exists()
    }

    private fun uiReadForDownload(error: String? = null) {
        binding.updateDescription.isVisible = true
        binding.updateDescription.setText(R.string.update_has_new_apk)

        binding.updateButton.isVisible = true
        binding.updateButton.text = getString(R.string.download_update)
        binding.updateButton.setOnClickListener { viewModel.startDownload(version, downloadUrl) }

        binding.progressBar.isVisible = false
        binding.progressText.isVisible = false

        binding.errorText.text = error
    }

    private fun uiDownloading(progress: Int) {
        binding.updateDescription.isVisible = true
        binding.updateDescription.text = getString(R.string.downloading_update)

        binding.updateButton.isVisible = true
        binding.updateButton.text = getString(R.string.cancel)
        binding.updateButton.setOnClickListener { viewModel.cancelDownload() }

        binding.progressBar.isVisible = true
        binding.progressText.isVisible = true

        binding.errorText.text = null

        if (progress < 0) return

        binding.progressBar.progress = progress
        binding.progressText.text = getString(R.string.percentage, progress)
    }

    private fun uiViewDownloads() {
        binding.updateDescription.isVisible = true
        binding.updateDescription.text = getString(R.string.update_download_complete)

        binding.updateButton.isVisible = true
        binding.updateButton.text = getString(R.string.open_files_app)
        binding.updateButton.setOnClickListener { ApkDownloadManager.viewDownloads(requireContext()) }

        binding.progressBar.isVisible = false
        binding.progressText.isVisible = false

        binding.errorText.text = null
    }
}
