package com.nthlink.android.client.ui.about

import android.view.LayoutInflater
import android.view.ViewGroup
import com.nthlink.android.client.databinding.FragmentAboutBinding
import com.nthlink.android.client.ui.common.BindingFragment

class AboutFragment : BindingFragment<FragmentAboutBinding>() {
    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): FragmentAboutBinding {
        return FragmentAboutBinding.inflate(inflater, container, false)
    }
}