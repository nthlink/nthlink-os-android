package com.nthlink.android.client.ui.feedback

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.nthlink.android.client.App.Companion.TAG
import com.nthlink.android.client.BuildConfig
import com.nthlink.android.client.R
import com.nthlink.android.client.databinding.FragmentFeedbackBinding
import com.nthlink.android.client.utils.openWebPage
import com.nthlink.android.client.utils.showMaterialAlertDialog
import com.nthlink.android.client.utils.showProgressDialog
import com.nthlink.android.core.Root
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class FeedbackFragment : Fragment() {

    private var _binding: FragmentFeedbackBinding? = null
    private val binding get() = _binding!!

    private lateinit var issueCategories: Array<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initIssueCategory()
        initDescription()
        initSubmit()
        binding.privacyPolicy.setOnClickListener { openWebPage(getString(R.string.url_policies)) }
    }

    private fun initIssueCategory() {
        issueCategories = resources.getStringArray(R.array.issue_categories)
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            issueCategories
        )

        binding.inputIssueCategory.run {
            setAdapter(adapter)

            // Default value
            setText(issueCategories[0], false)
        }
    }

    private fun initDescription() {
        binding.feedbackSubmit.isEnabled = false

        binding.inputDescription.addTextChangedListener {
            binding.feedbackSubmit.isEnabled = it?.isNotEmpty() ?: false
        }
    }

    private fun initSubmit() {
        binding.feedbackSubmit.setOnClickListener {
            val feedbackType = getFeedbackType()
            val description = binding.inputDescription.text.toString()
            val email = binding.inputEmail.text.toString()

            lifecycleScope.launch(Main) {
                val progressDialog = showProgressDialog()
                val result = withContext(IO) {
                    try {
                        Root.feedback(
                            feedbackType = feedbackType,
                            description = description,
                            appVersion = BuildConfig.VERSION_NAME,
                            email = email
                        )
                        true
                    } catch (e: Throwable) {
                        Log.e(TAG, "feedback error: ", e)
                        false
                    }
                }
                progressDialog.dismiss()

                if (result) {
                    showMaterialAlertDialog {
                        setMessage(R.string.feedback_submit_success_message)
                        setCancelable(false)
                        setPositiveButton(R.string.ok) { dialog, _ ->
                            findNavController().navigateUp()
                            dialog.dismiss()
                        }
                    }
                } else {
                    showMaterialAlertDialog {
                        setMessage(R.string.feedback_submit_failed_message)
                        setCancelable(false)
                        setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
                    }
                }
            }
        }
    }

    /**
     * display feedback type in multi languages but send it to server in English.
     */
    private fun getFeedbackType(): String {
        val feedbackType = binding.inputIssueCategory.text.toString()

        return if (Locale.getDefault().language == Locale.ENGLISH.language) feedbackType else {
            val index = issueCategories.indexOf(feedbackType)
            val config = Configuration(resources.configuration).apply { setLocale(Locale.ENGLISH) }
            requireContext().createConfigurationContext(config).resources
                .getStringArray(R.array.issue_categories)[index]
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}