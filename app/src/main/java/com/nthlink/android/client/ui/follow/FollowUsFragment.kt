package com.nthlink.android.client.ui.follow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nthlink.android.client.R
import com.nthlink.android.client.databinding.FragmentFollowUsBinding
import com.nthlink.android.client.utils.copyToClipboard
import com.nthlink.android.client.utils.getLoadWebUrlIntent
import com.nthlink.android.client.utils.openWebPage
import com.nthlink.android.client.utils.showAlertDialog


class FollowUsFragment : Fragment() {
    private var _binding: FragmentFollowUsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFollowUsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            fbEnVisit.setOnClickListener {
                openWebPage("https://www.facebook.com/profile.php?id=61560873763629")
            }
            fbZhVisit.setOnClickListener {
                openWebPage("https://www.facebook.com/CNnthLink")
            }
            fbFaVisit.setOnClickListener {
                openWebPage("https://www.facebook.com/NthLinkIR/")
            }
            fbRuVisit.setOnClickListener {
                openWebPage("https://www.facebook.com/NthLinkRU/")
            }
            fbMyVisit.setOnClickListener {
                openWebPage("https://www.facebook.com/NthLinkMM/")
            }
            fbEsVisit.setOnClickListener {
                openWebPage("https://www.facebook.com/NthlinkES/")
            }

            igEnVisit.setOnClickListener {
                openWebPage("https://www.instagram.com/nthlink_vpn/")
            }
            igZhVisit.setOnClickListener {
                openWebPage("https://www.instagram.com/cn_nthlink/")
            }
            igFaVisit.setOnClickListener {
                openWebPage("https://www.instagram.com/ir_nthlink/")
            }
            igRuVisit.setOnClickListener {
                openWebPage("https://www.instagram.com/ru_nthlink/")
            }
            igMyVisit.setOnClickListener {
                openWebPage("https://www.instagram.com/mm_nthlink/")
            }
            igEsVisit.setOnClickListener {
                openWebPage("https://www.instagram.com/es_nthlink/")
            }

            ytVisit.setOnClickListener {
                startActivity(getLoadWebUrlIntent("https://www.youtube.com/@nthLinkApp"))
            }

            tgId.setOnClickListener {
                copyToClipboard("telegram id", "@nthLinkVPN")
                showAlertDialog {
                    setTitle(R.string.copied)
                    setMessage(R.string.copied_telegram_id)
                    setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}