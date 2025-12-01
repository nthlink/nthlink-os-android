package com.nthlink.android.client.ui.web

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.nthlink.android.client.R
import com.nthlink.android.client.databinding.FragmentWebBinding
import com.nthlink.android.client.ui.common.BindingFragment
import com.nthlink.android.client.utils.copyToClipboard
import com.nthlink.android.client.utils.openWebPage
import com.nthlink.android.client.utils.removeAllCookies
import com.nthlink.android.client.utils.shareText

class WebFragment : BindingFragment<FragmentWebBinding>(), MenuProvider,
    CustomWebChromeClient.Callback {

    private val args: WebFragmentArgs by navArgs()

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): FragmentWebBinding {
        return FragmentWebBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().addMenuProvider(this, viewLifecycleOwner)

        with(binding.webView) {
            settings.userAgentString = CUSTOM_USER_AGENT

            webViewClient = WebViewClient()
            webChromeClient = CustomWebChromeClient(this@WebFragment)
        }

        removeAllCookies {
            if (isBindingNotNull()) binding.webView.loadUrl(args.url, customExtraHeaders)
        }
    }

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        binding.progressBar.progress = newProgress
        binding.progressNum.text = getString(R.string.percentage, newProgress)
    }

    override fun onStartLoading(view: WebView) {
        binding.progressBar.isVisible = true
        binding.progressNum.isVisible = true
    }

    override fun onLoading(view: WebView) {}

    override fun onFinishLoading(view: WebView) {
        binding.progressBar.isVisible = false
        binding.progressNum.isVisible = false
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_web, menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.option_web_item_1 -> {
                copyToClipboard(getString(R.string.app_name), args.url)
                Toast.makeText(requireContext(), R.string.word_copied_link, Toast.LENGTH_LONG)
                    .show()
                true
            }

            R.id.option_web_item_2 -> {
                openWebPage(args.url)
                true
            }

            R.id.option_web_item_3 -> {
                shareText(args.url)
                true
            }

            else -> false
        }
    }

    override fun onDestroyView() {
        binding.webView.webChromeClient = null
        binding.webView.destroy()
        super.onDestroyView()
    }
}