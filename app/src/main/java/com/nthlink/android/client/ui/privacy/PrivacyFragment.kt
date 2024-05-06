package com.nthlink.android.client.ui.privacy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nthlink.android.client.App.Companion.POLICY_URL
import com.nthlink.android.client.databinding.FragmentPrivacyBinding
import com.nthlink.android.client.storage.saveAgreePrivacy
import com.nthlink.android.client.ui.LaunchActivity
import kotlinx.coroutines.launch
import tw.hankli.brookray.core.extension.openWebPage

class PrivacyFragment : Fragment() {

    private var _binding: FragmentPrivacyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrivacyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.privacySubmit.setOnClickListener {
            lifecycleScope.launch {
                saveAgreePrivacy(this@PrivacyFragment.requireContext(), true)
                (requireActivity() as LaunchActivity).moveToMainActivity()
            }
        }

        binding.privacyPolicy.setOnClickListener { openWebPage(POLICY_URL) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}