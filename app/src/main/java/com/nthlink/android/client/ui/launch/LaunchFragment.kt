package com.nthlink.android.client.ui.launch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.nthlink.android.client.databinding.FragmentLaunchBinding
import com.nthlink.android.client.storage.datastore.readAgreePrivacy
import com.nthlink.android.client.ui.LaunchActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LaunchFragment : Fragment() {

    private var _binding: FragmentLaunchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLaunchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            delay(2000)
            if (readAgreePrivacy(this@LaunchFragment.requireContext())) {
                (requireActivity() as LaunchActivity).moveToMainActivity()
            } else {
                findNavController().navigate(
                    LaunchFragmentDirections.actionLaunchFragmentToPrivacyFragment()
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}