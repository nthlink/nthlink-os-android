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
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.nthlink.android.client.App.Companion.TAG
import com.nthlink.android.client.R
import com.nthlink.android.client.databinding.FragmentConnectionBinding
import com.nthlink.android.client.storage.readConnectedCount
import com.nthlink.android.client.storage.readHasLandingPageShown
import com.nthlink.android.client.storage.saveConnectedCount
import com.nthlink.android.client.storage.saveHasLandingPageShown
import com.nthlink.android.client.ui.MainViewModel
import com.nthlink.android.client.utils.MarginItemDecoration
import com.nthlink.android.client.utils.getColor
import com.nthlink.android.client.utils.installFromGooglePlay
import com.nthlink.android.client.utils.removeAllCookies
import com.nthlink.android.client.utils.requireRatingApp
import com.nthlink.android.client.utils.vibrate
import com.nthlink.android.core.RootVpn
import com.nthlink.android.core.RootVpn.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tw.hankli.brookray.core.constant.NO_RESOURCE

class ConnectionFragment : Fragment(), MenuProvider {
    private var _binding: FragmentConnectionBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()
    private val newsAdapter = NewsAdapter()

    private var landingPage: MenuItem? = null
    private var hasLandingPageShown: Boolean = false

    private lateinit var vpn: RootVpn
    private lateinit var switch: SwitchBottomSheet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vpn = RootVpn.Builder()
            .setNotificationIcon(R.drawable.ic_nthlink_logo_white)
            .build(this)

        // restore hasLandingPageShown
        lifecycleScope.launch(Dispatchers.IO) {
            hasLandingPageShown = readHasLandingPageShown(requireContext())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnectionBinding.inflate(inflater, container, false)
        switch = SwitchBottomSheet(binding.switchBottomSheet, ::onBottomSheetExpanded)
        return binding.root
    }

    private fun onBottomSheetExpanded() {
        if (vpn.status == Status.CONNECTED && binding.newsList.isVisible) loadNews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.switchBottomSheet.dragHandle.setOnClickListener { switch.toggle() }
        binding.switchBottomSheet.toggle.setOnClickListener {
            vpn.toggle()
            vibrate()
        }

        newsAdapter.onNewsItemClick = ::onNewsItemClick

        with(binding.newsList) {
            setHasFixedSize(false)
            adapter = newsAdapter
            addItemDecoration(MarginItemDecoration(18))
        }

        requireActivity().addMenuProvider(this, viewLifecycleOwner)

        lifecycleScope.launch {
            mainViewModel.toHomeFlow.collect {
                if (vpn.status == Status.CONNECTED) switch.collapse() else switch.expand()
            }
        }

        with(vpn) {
            onStatusChanged = { updateUI(it) }
            onErrorOccurred = { showError(it) }
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

            when (status) {
                Status.DISCONNECTED -> {
                    Log.i(TAG, "updateConnectionUI: DISCONNECTED")

                    with(switchBottomSheet.toggle) {
                        isEnabled = true
                        setText(R.string.connection_label_connect)
                        setTextColor(getColor(R.color.white))
                        strokeColor = ColorStateList.valueOf(getColor(R.color.white))
                    }

                    switch.expand()

                    inAppReviews()
                }

                Status.CONNECTING -> {
                    Log.i(TAG, "updateConnectionUI: CONNECTING")

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

                    if (hasLandingPageShown) {
                        loadNews()
                    } else {
                        hasLandingPageShown = true
                        launchLeadingPage()
                    }

                    lifecycleScope.launch { saveConnectedCount(this@ConnectionFragment.requireContext()) }
                }

                Status.DISCONNECTING -> {
                    Log.i(TAG, "updateConnectionUI: DISCONNECTING")

                    with(switchBottomSheet.toggle) {
                        isEnabled = false
                        setText(R.string.connection_server_state_disconnecting)
                        setTextColor(getColor(R.color.grey_C4C4C4))
                        strokeColor = ColorStateList.valueOf(getColor(R.color.grey_C4C4C4))
                    }

                    newsAdapter.submitList(null)
                }
            }
        }
    }

    private fun showError(error: RootVpn.Error) {
        val resource = when (error) {
            RootVpn.Error.NO_ERROR -> NO_RESOURCE
            RootVpn.Error.NO_PERMISSION -> R.string.error_no_permission
            RootVpn.Error.NO_INTERNET -> R.string.error_no_internet
            RootVpn.Error.DIRECTORY_SERVER_ERROR -> R.string.error_directory_server
            RootVpn.Error.VPN_SERVICE_ERROR -> R.string.error_vpn_service
        }

        if (resource == NO_RESOURCE) return

        AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setTitle(R.string.error_title)
            .setMessage(resource)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton(R.string.feedback_page_title) { dialog, _ ->
                findNavController().navigate(ConnectionFragmentDirections.actionConnectionFragmentToFeedbackFragment())
                dialog.dismiss()
            }
            .show()
    }

    private fun loadNews() = vpn.dsConfig?.run {
        val notifications = notifications?.map {
            NewsModel.Notification(it.title, it.url)
        } ?: emptyList()

        val headlines = headlineNews.map {
            NewsModel.Headline(it.title, it.excerpt, it.image, it.url)
        }

        val news = ArrayList<NewsModel>().apply {
            addAll(notifications)
            addAll(headlines)
        }

        removeAllCookies { newsAdapter.submitList(news) }
    }

    private fun onNewsItemClick(item: NewsModel) {
        openWebFragment(item.getNewsUrl())
    }

    private fun inAppReviews() {
        if (!installFromGooglePlay(requireContext())) return

        lifecycleScope.launch {
            delay(600)
            val count = readConnectedCount(this@ConnectionFragment.requireContext())
            if (count in setOf(10, 15, 20)) requireRatingApp(requireActivity())
        }
    }

    private fun launchLeadingPage(): Boolean {
        vpn.dsConfig?.redirectUrl?.let { openWebFragment(it) }
        return true
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

        landingPage = menu[0].apply { isVisible = vpn.status == Status.CONNECTED }
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

        lifecycleScope.launch(Dispatchers.IO) {
            saveHasLandingPageShown(
                requireContext(),
                hasLandingPageShown
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        switch.onDestroyView()
        _binding = null
    }
}