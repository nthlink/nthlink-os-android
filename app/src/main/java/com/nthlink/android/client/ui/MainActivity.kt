package com.nthlink.android.client.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.nthlink.android.client.BuildConfig
import com.nthlink.android.client.R
import com.nthlink.android.client.databinding.ActivityMainBinding
import com.nthlink.android.client.updates.ApkDownloadReceiver
import com.nthlink.android.client.updates.InAppUpdate
import com.nthlink.android.client.updates.InAppUpdateApk
import com.nthlink.android.client.updates.InAppUpdateMessage
import com.nthlink.android.client.updates.InAppUpdatePlay
import com.nthlink.android.client.utils.installFromGooglePlay
import com.nthlink.android.client.utils.openWebPage
import com.nthlink.android.client.utils.showAlertDialog
import com.nthlink.android.core.Root
import com.nthlink.android.core.utils.EMPTY
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, MainActivity::class.java)
            context.startActivity(starter)
        }
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var inAppUpdate: InAppUpdate

    lateinit var root: Root
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // View binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigation Component
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.connectionFragment -> {
                    binding.layoutToolbar.toolbar.title = EMPTY
                    binding.layoutToolbar.logoTitle.isVisible = true
                    binding.drawerLayout.setDrawerLockMode(LOCK_MODE_UNLOCKED)
                }

                else -> {
                    binding.layoutToolbar.logoTitle.isVisible = false
                    binding.drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)
                }
            }
        }

        initRoot()
        initInAppUpdate()
        initView()
        handleIntent(intent)
    }

    private fun initRoot() {
        root = Root.Builder().build(this)
    }

    private fun initInAppUpdate() {
        inAppUpdate = if (installFromGooglePlay(this)) {
            InAppUpdatePlay(this, activityResultRegistry, lifecycleScope)
        } else {
            InAppUpdateApk(lifecycleScope, root, ::navigateToApkUpdateFragment)
        }

        lifecycle.addObserver(inAppUpdate)

        lifecycleScope.launch {
            inAppUpdate.inAppUpdateFlow.collect { result ->
                when (result) {
                    is InAppUpdateMessage.NewUpdateAvailable -> {
                        displayNewUpdateRedPoint(true)
                    }

                    is InAppUpdateMessage.UpToDate -> {
                        if (result.notifyUser) {
                            showAlertDialog(R.string.update, R.string.update_is_up_to_date)
                        }
                    }

                    is InAppUpdateMessage.CheckFailed -> {
                        if (result.notifyUser) {
                            showAlertDialog(R.string.update, R.string.something_went_wrong)
                        }
                    }

                    InAppUpdateMessage.UpdateOk -> {
                        displayNewUpdateRedPoint(false)
                    }

                    InAppUpdateMessage.UpdateCanceled -> {
                        showAlertDialog(R.string.update, R.string.update_canceled)
                    }

                    InAppUpdateMessage.UpdateFailed -> {
                        showAlertDialog(R.string.update, R.string.something_went_wrong)
                    }
                }
            }
        }

        inAppUpdate.checkUpdate(updateIfAvailable = false)
    }

    private fun navigateToApkUpdateFragment(version: String, url: String) {
        if (navController.currentDestination?.id == R.id.apkUpdateFragment) return

        navController.navigate(
            R.id.apkUpdateFragment,
            Bundle().apply {
                putString("version", version)
                putString("url", url)
            }
        )
    }

    private fun displayNewUpdateRedPoint(show: Boolean) {
        with(binding.drawer.menu.findItem(R.id.menu_item_update)) {
            if (show) setActionView(R.layout.action_view_update) else setActionView(null)
        }
    }

    private fun initView() {
        // Init drawer
        binding.appVersion.text = getString(R.string.about_version, BuildConfig.VERSION_NAME)

        binding.drawer.setupWithNavController(navController)
        binding.drawer.setNavigationItemSelectedListener(this)
        binding.drawer.menu.findItem(R.id.menu_item_rate_app).isVisible =
            installFromGooglePlay(this)

        val appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayout)

        // Init toolbar
        setSupportActionBar(binding.layoutToolbar.toolbar)
        binding.layoutToolbar.toolbar.setupWithNavController(navController, appBarConfiguration)

        // Handle overlaps for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(binding.navHostFragment) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<MarginLayoutParams> {
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
            }

            WindowInsetsCompat.CONSUMED
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.navigationView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<MarginLayoutParams> {
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
            }

            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        // Click the update button in the drawer.
        if (intent?.action == ApkDownloadReceiver.ACTION_OPEN_APK_UPDATE) updateApp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_home -> toHome()
            R.id.menu_item_about -> toAbout()
            R.id.menu_item_follow_us -> toFollowUs()
            R.id.menu_item_help -> toHelp()
            R.id.menu_item_feedback -> toFeedback()
            R.id.menu_item_diagnostic -> toDiagnostic()
            R.id.menu_item_privacy_police -> toPrivacyPolice()
            R.id.menu_item_download -> downloadApp()
            R.id.menu_item_update -> updateApp()
            R.id.menu_item_rate_app -> rateApp()
            R.id.menu_item_kill_switch -> showKillSwitchDialog()
            else -> false
        }
    }

    private fun toHome(): Boolean {
        return closeDrawer()
    }

    private fun toAbout(): Boolean {
        navController.navigate(R.id.aboutFragment)
        return closeDrawer()
    }

    private fun toFollowUs(): Boolean {
        navController.navigate(R.id.followUsFragment)
        return closeDrawer()
    }

    private fun toFeedback(): Boolean {
        navController.navigate(R.id.feedbackFragment)
        return closeDrawer()
    }

    private fun toDiagnostic(): Boolean {
        navController.navigate(R.id.diagnosticFragment)
        return closeDrawer()
    }

    private fun toHelp(): Boolean {
        openWebPage(getString(R.string.url_faq))
        return closeDrawer()
    }

    private fun toPrivacyPolice(): Boolean {
        openWebPage(getString(R.string.url_policies))
        return closeDrawer()
    }

    private fun downloadApp(): Boolean {
        openWebPage(getString(R.string.url_download))
        return closeDrawer()
    }

    private fun updateApp(): Boolean {
        inAppUpdate.checkUpdate(true)
        return closeDrawer()
    }

    private fun rateApp(): Boolean {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "market://details?id=$packageName".toUri()
                )
            )
        } catch (_: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "https://play.google.com/store/apps/details?id=$packageName".toUri()
                )
            )
        }

        return closeDrawer()
    }

    private fun showKillSwitchDialog(): Boolean {
        showAlertDialog(
            R.string.kill_switch,
            R.string.kill_switch_dialog_description,
            okListener = { startActivity(Intent(Settings.ACTION_VPN_SETTINGS)) }
        )
        return closeDrawer()
    }

    private fun closeDrawer(): Boolean {
        binding.drawerLayout.close()
        return true
    }
}