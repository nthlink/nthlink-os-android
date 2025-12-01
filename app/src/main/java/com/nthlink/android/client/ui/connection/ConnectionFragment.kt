package com.nthlink.android.client.ui.connection

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.nthlink.android.client.App.Companion.TAG
import com.nthlink.android.client.R
import com.nthlink.android.client.databinding.FragmentConnectionBinding
import com.nthlink.android.client.storage.datastore.CommonDataStore
import com.nthlink.android.client.storage.sql.NewsAnalyzer
import com.nthlink.android.client.ui.common.BindingFragment
import com.nthlink.android.client.utils.MarginItemDecoration
import com.nthlink.android.client.utils.getColor
import com.nthlink.android.client.utils.getDb
import com.nthlink.android.client.utils.getRoot
import com.nthlink.android.client.utils.installFromGooglePlay
import com.nthlink.android.client.utils.removeAllCookies
import com.nthlink.android.client.utils.requireRatingApp
import com.nthlink.android.client.utils.showMaterialAlertDialog
import com.nthlink.android.client.utils.vibrate
import com.nthlink.android.core.Root
import com.nthlink.android.core.Root.Error
import com.nthlink.android.core.Root.Status
import com.nthlink.android.core.utils.NO_RESOURCE
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class ConnectionFragment : BindingFragment<FragmentConnectionBinding>(), MenuProvider {
    private val newsAdapter = NewsAdapter()
    private val dataStore: CommonDataStore by inject()

    private var landingPage: MenuItem? = null
    private var hasLandingPageShown: Boolean = false

    private lateinit var root: Root
    private lateinit var newsAnalyzer: NewsAnalyzer
    private lateinit var switch: SwitchBottomSheet

    private var vpnFlowsJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        newsAnalyzer = NewsAnalyzer(getDb().clickedNewsDao(), lifecycleScope)

        // restore hasLandingPageShown
        lifecycleScope.launch(IO) {
            hasLandingPageShown = dataStore.readHasLandingPageShown()
        }
    }

    override fun bindView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentConnectionBinding {
        return FragmentConnectionBinding.inflate(inflater, container, false).also {
            switch = SwitchBottomSheet(it.switchBottomSheet, ::onSwitchExpanded)
        }
    }

    private fun onSwitchExpanded() {
        if (root.status == Status.CONNECTED && binding.newsList.isVisible) {
            removeAllCookies {
                newsAdapter.submitList(newsAnalyzer.getPinnedAndRecommendedNews())
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        root = getRoot()

        binding.switchBottomSheet.dragHandle.setOnClickListener {
            if (root.status == Status.CONNECTED) switch.toggle()
        }

        binding.switchBottomSheet.toggle.setOnClickListener {
            toggle()
            vibrate()
        }

        newsAdapter.onNewsItemClick = ::onNewsItemClick

        with(binding.newsList) {
            setHasFixedSize(false)
            adapter = newsAdapter
            addItemDecoration(MarginItemDecoration(18))
        }

        requireActivity().addMenuProvider(this, viewLifecycleOwner)

        vpnFlowsJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    root.statusFlow.collect { updateUI(it) }
                }

                launch {
                    root.errorFlow.collect { showError(it) }
                }
            }
        }
    }

    private fun toggle() {
        if (root.status == Status.DISCONNECTED) {
            root.connect()
        } else {
            root.disconnect()
        }
    }

    private fun updateUI(status: Status) {
        with(binding) {
            // connection background
            val res = if (status == Status.CONNECTED) R.color.white else R.color.eggshell_white
            connectionPage.setBackgroundColor(getColor(res))

            // logo and title
            logo.isVisible = status != Status.CONNECTED
            logoTitle.isVisible = logo.isVisible

            // news list
            newsList.isVisible = status == Status.CONNECTED

            // landing page icon
            landingPage?.isVisible = status == Status.CONNECTED

            // switch draggable
            switch.isDraggable = status == Status.CONNECTED

            when (status) {
                Status.DISCONNECTED -> {
                    Log.i(TAG, "updateConnectionUI: DISCONNECTED")

                    staticIndicator.isVisible = false

                    with(switchBottomSheet.toggle) {
                        isEnabled = true
                        setText(R.string.connection_label_connect)
                        setTextColor(getColor(R.color.white))
                        strokeColor = ColorStateList.valueOf(getColor(R.color.white))
                    }

                    switch.expand()

                    inAppReviews()
                }

                Status.INITIALIZING -> {
                    Log.i(TAG, "updateConnectionUI: INITIALIZING")

                    staticIndicator.isVisible = false

                    with(switchBottomSheet.toggle) {
                        isEnabled = false
                        setText(R.string.connection_server_state_initializing)
                        setTextColor(getColor(R.color.grey_C4C4C4))
                        strokeColor = ColorStateList.valueOf(getColor(R.color.grey_C4C4C4))
                    }
                }

                Status.CONNECTING -> {
                    Log.i(TAG, "updateConnectionUI: CONNECTING")

                    staticIndicator.isVisible = false

                    with(switchBottomSheet.toggle) {
                        isEnabled = false
                        setText(R.string.connection_server_state_connecting)
                        setTextColor(getColor(R.color.grey_C4C4C4))
                        strokeColor = ColorStateList.valueOf(getColor(R.color.grey_C4C4C4))
                    }

                    hasLandingPageShown = false
                }

                Status.CONNECTED -> {
                    Log.i(TAG, "updateConnectionUI: CONNECTED")

                    with(switchBottomSheet.toggle) {
                        isEnabled = true
                        setText(R.string.connection_label_disconnect)
                        setTextColor(getColor(R.color.white))
                        strokeColor = ColorStateList.valueOf(getColor(R.color.white))
                    }

                    switch.collapse()

                    if (hasLandingPageShown) loadConfig() else launchLeadingPage()

                    lifecycleScope.launch {
                        dataStore.saveConnectedCount()
                    }
                }

                Status.DISCONNECTING -> {
                    Log.i(TAG, "updateConnectionUI: DISCONNECTING")

                    staticIndicator.isVisible = false

                    with(switchBottomSheet.toggle) {
                        isEnabled = false
                        setText(R.string.connection_server_state_disconnecting)
                        setTextColor(getColor(R.color.grey_C4C4C4))
                        strokeColor = ColorStateList.valueOf(getColor(R.color.grey_C4C4C4))
                    }

                    newsAdapter.submitList(null)
                    newsAnalyzer.removeExpiredClickedNews()
                }
            }
        }
    }

    private fun showError(error: Error) {
        val resource = when (error) {
            Error.NO_PERMISSION -> R.string.error_no_permission
            Error.NO_INTERNET -> R.string.error_no_internet
            Error.INVALID_CONFIG -> R.string.error_invalid_config
            Error.GET_CONFIG_ERROR -> R.string.error_directory_server
            Error.NO_PROXY_AVAILABLE -> R.string.error_no_proxy_available
            Error.VPN_SERVICE_NOT_EXISTS, Error.CREATE_TUN_FAILED, Error.START_LEAF_FAILED -> R.string.error_vpn_service
        }

        if (resource == NO_RESOURCE) return

        showMaterialAlertDialog {
            setTitle(R.string.error_title)
            setMessage(resource)
            setCancelable(false)
            setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            setNegativeButton(R.string.feedback_page_title) { dialog, _ ->
                findNavController().navigate(R.id.feedbackFragment)
                dialog.dismiss()
            }
        }
    }

    private fun loadConfig() {
        lifecycleScope.launch(IO) {
            val config = root.getConfig() ?: return@launch

            // news
            newsAnalyzer.loadNews(config)

            // update UI
            withContext(Main) {
                binding.staticIndicator.isVisible = config.static

                removeAllCookies {
                    newsAdapter.submitList(newsAnalyzer.getPinnedAndRecommendedNews())
                }
            }
        }
    }

    private fun onNewsItemClick(item: NewsModel) {
        if (item is NewsModel.HeadlineNews) {
            newsAnalyzer.addClickedNews(item.categories)
        }
        openWebFragment(item.url)
    }

    // rate app
    private fun inAppReviews() {
        if (!installFromGooglePlay(requireContext())) return

        lifecycleScope.launch {
            delay(600)
            val count = dataStore.readConnectedCount()
            if (count in setOf(10, 15, 20)) requireRatingApp(requireActivity())
        }
    }

    private fun launchLeadingPage() {
        lifecycleScope.launch {
            val url = root.getConfig()?.redirectUrl ?: return@launch
            if (url.isNotEmpty()) {
                hasLandingPageShown = true
                openWebFragment(url)
            }
        }
    }

    private fun openWebFragment(url: String) {
        findNavController().navigate(
            ConnectionFragmentDirections.actionConnectionFragmentToWebFragment(url)
        )
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.fragment_connection, menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)

        landingPage = menu[0].apply { isVisible = root.status == Status.CONNECTED }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.menu_open_landing_page -> {
                launchLeadingPage()
                true
            }

            else -> false
        }
    }

    override fun onStop() {
        super.onStop()

        lifecycleScope.launch(IO) {
            dataStore.saveHasLandingPageShown(hasLandingPageShown)
        }
    }

    override fun onDestroyView() {
        vpnFlowsJob?.cancel()
        vpnFlowsJob = null

        switch.onDestroyView()

        super.onDestroyView()
    }
}