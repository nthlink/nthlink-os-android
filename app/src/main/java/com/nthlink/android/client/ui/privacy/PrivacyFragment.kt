package com.nthlink.android.client.ui.privacy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nthlink.android.client.R
import com.nthlink.android.client.databinding.FragmentPrivacyBinding
import com.nthlink.android.client.storage.datastore.saveAgreePrivacy
import com.nthlink.android.client.ui.LaunchActivity
import com.nthlink.android.client.utils.openWebPage
import kotlinx.coroutines.launch

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

        binding.privacyPolicy.setOnClickListener { openWebPage(getString(R.string.url_policies)) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}