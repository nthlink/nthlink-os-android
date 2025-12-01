package com.nthlink.android.client.ui.launch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.nthlink.android.client.databinding.FragmentLaunchBinding
import com.nthlink.android.client.storage.datastore.CommonDataStore
import com.nthlink.android.client.ui.LaunchActivity
import com.nthlink.android.client.ui.common.BindingFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LaunchFragment : BindingFragment<FragmentLaunchBinding>() {
    private val commonDataStore: CommonDataStore by inject()

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): FragmentLaunchBinding {
        return FragmentLaunchBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            delay(2000)
            if (commonDataStore.readAgreePrivacy()) {
                (requireActivity() as LaunchActivity).moveToMainActivity()
            } else {
                findNavController().navigate(
                    LaunchFragmentDirections.actionLaunchFragmentToPrivacyFragment()
                )
            }
        }
    }
}