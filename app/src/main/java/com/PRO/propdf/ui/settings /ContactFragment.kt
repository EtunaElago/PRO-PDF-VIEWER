package com.PRO.propdf.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.PRO.propdf.R
import com.PRO.propdf.databinding.FragmentContactBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactFragment : Fragment() {

    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupContactInfo()
        setupClickListeners()
        setupForm()
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.contact_us)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupContactInfo() {
        binding.apply {
            textContactDescription.text = getString(R.string.contact_description)
            textSupportEmail.text = getString(R.string.support_email)
            textWebsite.text = getString(R.string.website_url)
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            // Email
            layoutEmail.setOnClickListener {
                sendEmail()
            }
            
            // Website
            layoutWebsite.setOnClickListener {
                openWebsite()
            }
            
            // FAQ
            layoutFaq.setOnClickListener {
                openFaq()
            }
            
            // Report Bug
            layoutReportBug.setOnClickListener {
                reportBug()
            }
            
            // Feature Request
            layoutFeatureRequest.setOnClickListener {
                requestFeature()
            }
        }
    }

    private fun setupForm() {
        binding.buttonSend.setOnClickListener {
            sendContactForm()
        }
    }

    private fun sendEmail() {
        val email = getString(R.string.support_email)
        val subject = getString(R.string.app_name) + " - Support"
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.send_email)))
    }

    private fun openWebsite() {
        val websiteUrl = getString(R.string.website_url)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl))
        startActivity(intent)
    }

    private fun openFaq() {
        val faqUrl = getString(R.string.faq_url)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(faqUrl))
        startActivity(intent)
    }

    private fun reportBug() {
        val email = getString(R.string.support_email)
        val subject = getString(R.string.app_name) + " - Bug Report"
        val body = """
            Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}
            Android Version: ${android.os.Build.VERSION.RELEASE}
            App Version: ${com.PRO.propdf.BuildConfig.VERSION_NAME}
            
            Please describe the bug below:
            
            
        """.trimIndent()
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.report_bug)))
    }

    private fun requestFeature() {
        val email = getString(R.string.support_email)
        val subject = getString(R.string.app_name) + " - Feature Request"
        val body = """
            I would like to request the following feature:
            
            
            Additional details:
            
            
        """.trimIndent()
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.request_feature)))
    }

    private fun sendContactForm() {
        val name = binding.editName.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
        val message = binding.editMessage.text.toString().trim()
        
        if (validateForm(name, email, message)) {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${getString(R.string.support_email)}")
                putExtra(Intent.EXTRA_SUBJECT, "Contact Form - ${getString(R.string.app_name)}")
                putExtra(Intent.EXTRA_TEXT, """
                    Name: $name
                    Email: $email
                    
                    Message:
                    $message
                """.trimIndent())
            }
            startActivity(Intent.createChooser(emailIntent, getString(R.string.send_message)))
            
            // Clear form
            binding.editName.text?.clear()
            binding.editEmail.text?.clear()
            binding.editMessage.text?.clear()
            
            // Show success message
            binding.textFormSuccess.visibility = View.VISIBLE
        }
    }

    private fun validateForm(name: String, email: String, message: String): Boolean {
        var isValid = true
        
        binding.textInputLayoutName.error = null
        binding.textInputLayoutEmail.error = null
        binding.textInputLayoutMessage.error = null
        
        if (name.isBlank()) {
            binding.textInputLayoutName.error = getString(R.string.name_required)
            isValid = false
        }
        
        if (email.isBlank()) {
            binding.textInputLayoutEmail.error = getString(R.string.email_required)
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textInputLayoutEmail.error = getString(R.string.invalid_email)
            isValid = false
        }
        
        if (message.isBlank()) {
            binding.textInputLayoutMessage.error = getString(R.string.message_required)
            isValid = false
        } else if (message.length < 10) {
            binding.textInputLayoutMessage.error = getString(R.string.message_too_short)
            isValid = false
        }
        
        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}