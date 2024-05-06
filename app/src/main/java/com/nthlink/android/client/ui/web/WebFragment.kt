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
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.nthlink.android.client.R
import com.nthlink.android.client.databinding.FragmentWebBinding
import com.nthlink.android.client.utils.removeAllCookies
import tw.hankli.brookray.core.extension.copyToClipboard
import tw.hankli.brookray.core.extension.openWebPage
import tw.hankli.brookray.core.extension.shareText
import tw.hankli.brookray.core.extension.showToast

class WebFragment : Fragment(), MenuProvider, WebChrome.Callback {
    private var _binding: FragmentWebBinding? = null
    private val binding get() = _binding!!

    private val args: WebFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWebBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().addMenuProvider(this, viewLifecycleOwner)

        with(binding.webView) {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            webChromeClient = WebChrome(this@WebFragment)
        }

        removeAllCookies {
            if (_binding != null) binding.webView.loadUrl(args.url)
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
                showToast(R.string.word_coped_link)
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
        super.onDestroyView()
        binding.webView.webChromeClient = null
        binding.webView.destroy()
        _binding = null
    }
}