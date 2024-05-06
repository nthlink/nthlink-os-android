package com.nthlink.android.client.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nthlink.android.client.BuildConfig
import com.nthlink.android.client.R
import com.nthlink.android.client.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val version = getString(R.string.about_version, BuildConfig.VERSION_NAME)
        binding.aboutVersion.text = version
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}