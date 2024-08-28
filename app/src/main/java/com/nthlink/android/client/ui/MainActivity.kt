package com.nthlink.android.client.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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
import com.nthlink.android.client.updates.InAppUpdate
import com.nthlink.android.client.updates.UpdateResult
import com.nthlink.android.client.utils.EMPTY
import com.nthlink.android.client.utils.installFromGooglePlay
import com.nthlink.android.client.utils.openWebPage
import com.nthlink.android.client.utils.showAlertDialog
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        initInAppUpdate()
        initView()
    }

    private fun initInAppUpdate() {
        inAppUpdate = InAppUpdate.getInstance(this)

        lifecycleScope.launch {
            inAppUpdate.updateResultFlow.collect { result ->
                when (result) {
                    is UpdateResult.CheckHasNewUpdate -> {
                        displayNewUpdate(true)

                        if (result.silent) return@collect
                        showAlertDialog(R.string.update, R.string.update_has_new_apk) {
                            downloadApp()
                        }
                    }

                    is UpdateResult.CheckUpToDate -> {
                        if (result.silent) return@collect
                        showAlertDialog(R.string.update, R.string.update_is_up_to_date)
                    }

                    is UpdateResult.CheckFailed -> {
                        if (result.silent) return@collect
                        showAlertDialog(R.string.update, R.string.something_went_wrong)
                    }

                    UpdateResult.UpdateOk -> {
                        displayNewUpdate(false)
                    }

                    UpdateResult.UpdateCanceled -> {
                        showAlertDialog(R.string.update, R.string.update_canceled)
                    }

                    UpdateResult.UpdateFailed -> {
                        showAlertDialog(R.string.update, R.string.something_went_wrong)
                    }
                }
            }
        }
    }

    private fun displayNewUpdate(show: Boolean) {
        with(binding.drawer.menu.findItem(R.id.menu_item_update)) {
            if (show) setActionView(R.layout.action_view_update) else setActionView(null)
        }
    }

    private fun initView() {
        // init drawer
        binding.appVersion.text = getString(R.string.about_version, BuildConfig.VERSION_NAME)

        binding.drawer.setupWithNavController(navController)
        binding.drawer.setNavigationItemSelectedListener(this)
        binding.drawer.menu.findItem(R.id.menu_item_rate_app).isVisible =
            installFromGooglePlay(this)

        val appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayout)

        // init toolbar
        setSupportActionBar(binding.layoutToolbar.toolbar)
        binding.layoutToolbar.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_home -> toHome()
            R.id.menu_item_about -> toAbout()
            R.id.menu_item_follow_us -> toFollowUs()
            R.id.menu_item_help -> toHelp()
            R.id.menu_item_feedback -> toFeedback()
            R.id.menu_item_privacy_police -> toPrivacyPolice()
            R.id.menu_item_download -> downloadApp()
            R.id.menu_item_update -> updateApp()
            R.id.menu_item_rate_app -> rateApp()
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
        inAppUpdate.checkForUpdate()
        return closeDrawer()
    }

    private fun rateApp(): Boolean {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$packageName")
                )
            )
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }

        return closeDrawer()
    }

    private fun closeDrawer(): Boolean {
        binding.drawerLayout.close()
        return true
    }
}