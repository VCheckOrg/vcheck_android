package com.vcheck.demo.activities

import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.vcheck.demo.*


class PartnerFormActivity : AppCompatActivity() {

    private lateinit var datasource: Datasource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partner_form)

        datasource = (application as DemoApp).appContainer.datasource

        val etCompany = findViewById<EditText>(R.id.field_company_name)
        val etEmail = findViewById<EditText>(R.id.field_email)
        val etName = findViewById<EditText>(R.id.field_name)
        val etPhone = findViewById<EditText>(R.id.field_phone)

        val btnSend = findViewById<Button>(R.id.btn_send)
        val agreementCheckbox = findViewById<CheckBox>(R.id.agree_checkbox)

        btnSend.setOnClickListener {
            btnSend.isVisible = false
            if (isDataValid(etCompany, etEmail, etName, etPhone, agreementCheckbox)) {
                Thread {
                    try {
                        makeFormSendRequest(etCompany, etEmail, etName, etPhone, btnSend)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.start()
            } else {
                btnSend.isVisible = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //Hiding partner app's action bar as it's not used in SDK
        if (supportActionBar != null && supportActionBar!!.isShowing) {
            supportActionBar?.hide()
        }
    }

    private fun makeFormSendRequest(etCompany: EditText,
                            etEmail: EditText,
                            etName: EditText,
                            etPhone: EditText,
                            btnSend: Button) {
        val result = datasource.sendPartnerApplicationRequest(
            PartnerApplicationRequestData(
                etCompany.text.toString(),
                etEmail.text.toString(),
                etName.text.toString(),
                etPhone.text.toString())
        ).execute()
        runOnUiThread {
            if (result.isSuccessful) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@PartnerFormActivity)
                builder.setCancelable(false)
                builder.setTitle("")
                builder.setMessage(R.string.partner_form_successfully_sent)
                builder.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    backToStart()
                }
                val alert: AlertDialog = builder.create()
                alert.show()
            } else {
                btnSend.isVisible = true
                Toast.makeText(this@PartnerFormActivity,
                    R.string.partner_form_request_error, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isDataValid(etCompany: EditText,
                            etEmail: EditText,
                            etName: EditText,
                            etPhone: EditText,
                            agreementCheckbox: CheckBox): Boolean {
        if (isNotMinimalLength(etCompany.text)) {
            etCompany.error = getString(R.string.et_enter_valid_company)
            return false
        } else {
            etCompany.error = null
        }
        if (!etEmail.text.isValidEmail()) {
            etEmail.error = getString(R.string.et_enter_valid_email)
            return false
        } else {
            etEmail.error = null
        }
        if (isNotMinimalLength(etName.text)) {
            etName.error = getString(R.string.et_enter_valid_name)
            return false
        } else {
            etName.error = null
        }
        if (etPhone.text != null && etPhone.text.isNotEmpty() && !etPhone.text.isValidPhone()) {
            etPhone.error = getString(R.string.et_enter_valid_phone)
            return false
        } else {
            etPhone.error = null
        }
        if (!agreementCheckbox.isChecked) {
            Toast.makeText(this@PartnerFormActivity,
                R.string.agreement_not_checked, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun isNotMinimalLength(et: CharSequence?): Boolean {
        return et.isNullOrEmpty() || et.length < 2
    }

    private fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    private fun CharSequence?.isValidPhone() = !isNullOrEmpty() && Patterns.PHONE.matcher(this).matches()

    private fun backToStart() {
        val intent: Intent?
        try {
            intent = Intent(this@PartnerFormActivity, MainActivity::class.java)
            this@PartnerFormActivity.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Error while returning to app: ${e.message}",
                Toast.LENGTH_LONG).show()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val languageCode = ContextUtils.getSavedLanguage(newBase)
        val localeUpdatedContext: ContextWrapper =
            ContextUtils.updateLocale(newBase, languageCode)
        super.attachBaseContext(localeUpdatedContext)
    }
}


//(application as DemoApp).appContainer.colorsRepository.resetAppColors()

//(application as DemoApp).appContainer.colorsRepository.buttonsColorHex?.let {
//            btnSend.setBackgroundColor(Color.parseColor(it))
//            val colorFilter = PorterDuffColorFilter(Color.parseColor(it), PorterDuff.Mode.SRC_ATOP)
//            CompoundButtonCompat.getButtonDrawable(agreementCheckbox)?.colorFilter = colorFilter
//        }
//        (application as DemoApp).appContainer.colorsRepository.backgroundPrimaryColorHex?.let {
//            backgroundView.setBackgroundColor(Color.parseColor(it))
//            changeDemoActivityStatusBarColor(Color.parseColor(it))
//        }
//        (application as DemoApp).appContainer.colorsRepository.backgroundSecondaryColorHex?.let {
//            etCompany.background = ColorDrawable(Color.parseColor(it))
//            etEmail.background = ColorDrawable(Color.parseColor(it))
//            etName.background = ColorDrawable(Color.parseColor(it))
//            etPhone.background = ColorDrawable(Color.parseColor(it))
//        }
//        (application as DemoApp).appContainer.colorsRepository.backgroundTertiaryColorHex?.let {
//            //Stub
//        }
//        (application as DemoApp).appContainer.colorsRepository.primaryTextColorHex?.let {
//            etCompany.setTextColor(Color.parseColor(it))
//            etCompany.setHintTextColor(Color.parseColor(it))
//            etEmail.setTextColor(Color.parseColor(it))
//            etEmail.setHintTextColor(Color.parseColor(it))
//            etName.setTextColor(Color.parseColor(it))
//            etName.setHintTextColor(Color.parseColor(it))
//            etPhone.setTextColor(Color.parseColor(it))
//            etPhone.setHintTextColor(Color.parseColor(it))
//            title.setTextColor(Color.parseColor(it))
//        }
//        (application as DemoApp).appContainer.colorsRepository.secondaryTextColorHex?.let {
//            subtitle.setTextColor(Color.parseColor(it))
//            companyNameLabel.setTextColor(Color.parseColor(it))
//            emailLabel.setTextColor(Color.parseColor(it))
//            nameLabel.setTextColor(Color.parseColor(it))
//            phoneLabel.setTextColor(Color.parseColor(it))
//            agreementCheckbox.setTextColor(Color.parseColor(it))
//            agreementCheckbox.setHintTextColor(Color.parseColor(it))
//        }
//        (application as DemoApp).appContainer.colorsRepository.borderColorHex?.let {
//            //Stub
//        }