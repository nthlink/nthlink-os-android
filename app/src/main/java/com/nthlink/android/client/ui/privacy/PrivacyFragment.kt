package com.nthlink.android.client.ui.privacy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.nthlink.android.client.R
import com.nthlink.android.client.databinding.FragmentPrivacyBinding
import com.nthlink.android.client.storage.datastore.CommonDataStore
import com.nthlink.android.client.ui.LaunchActivity
import com.nthlink.android.client.ui.common.BindingFragment
import com.nthlink.android.client.utils.openWebPage
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class PrivacyFragment : BindingFragment<FragmentPrivacyBinding>() {
    private val commonDataStore: CommonDataStore by inject()

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): FragmentPrivacyBinding {
        return FragmentPrivacyBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.privacySubmit.setOnClickListener {
            lifecycleScope.launch {
                commonDataStore.saveAgreePrivacy(true)
                (requireActivity() as LaunchActivity).moveToMainActivity()
            }
        }

        binding.privacyPolicy.setOnClickListener { openWebPage(getString(R.string.url_policies)) }
    }
}