package com.nthlink.android.client.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.nthlink.android.client.R
import com.nthlink.android.client.databinding.ActivityMainBinding
import com.nthlink.android.client.utils.installFromGooglePlay
import tw.hankli.brookray.core.constant.EMPTY
import tw.hankli.brookray.core.extension.openWebPage

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

    private val viewModel: MainViewModel by viewModels()

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
                    binding.drawerLayout.setDrawerLockMode(LOCK_MODE_UNLOCKED)
                }

                else -> binding.drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)
            }
        }

        initView()
    }

    private fun initView() {
        // init drawer
        binding.drawer.setupWithNavController(navController)
        binding.drawer.setNavigationItemSelectedListener(this)
        binding.drawer.menu.findItem(R.id.drawer_menu_item_5).isVisible =
            installFromGooglePlay(this)

        val appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayout)

        // init toolbar
        setSupportActionBar(binding.layoutToolbar.toolbar)
        binding.layoutToolbar.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.drawer_menu_item_1 -> toHome()
            R.id.drawer_menu_item_2 -> toFeedback()
            R.id.drawer_menu_item_3 -> toAbout()
            R.id.drawer_menu_item_4 -> toHelp()
            R.id.drawer_menu_item_5 -> rateApp()
            else -> false
        }
    }

    private fun toHome(): Boolean {
        viewModel.toHome()
        closeDrawer()
        return true
    }

    private fun toAbout(): Boolean {
        navController.navigate(R.id.aboutFragment)
        closeDrawer()
        return true
    }

    private fun toFeedback(): Boolean {
        navController.navigate(R.id.feedbackFragment)
        closeDrawer()
        return true
    }

    private fun toHelp(): Boolean {
        openWebPage("https://s3.us-west-1.amazonaws.com/dwo-jar-kmf-883/help.html")
        closeDrawer()
        return true
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
        closeDrawer()
        return true
    }

    private fun closeDrawer() {
        binding.drawerLayout.close()
    }
}