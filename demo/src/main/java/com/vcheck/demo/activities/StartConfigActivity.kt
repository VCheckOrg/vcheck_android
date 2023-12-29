package com.vcheck.demo.activities

import android.content.*
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.vcheck.demo.ContextUtils
import com.vcheck.demo.Datasource
import com.vcheck.demo.DemoApp
import com.vcheck.demo.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.domain.VCheckDesignConfig

class StartConfigActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener,
    View.OnTouchListener {

    private lateinit var datasource: Datasource

    private var languageCode: String = "uk"
    private var wasLocaleSelectedByUser = false
    private var mLangReceiver: BroadcastReceiver? = null

    private lateinit var etPartnerId: EditText
    private lateinit var btnPartnerIdClear: ImageButton
    private lateinit var btnPartnerIdPaste: ImageButton

    private lateinit var etSecret: EditText
    private lateinit var btnSecretClear: ImageButton
    private lateinit var btnSecretPaste: ImageButton

    private lateinit var etDesignConfig: EditText
    private lateinit var btnDesignConfigClear: ImageButton
    private lateinit var btnDesignConfigPaste: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_config)

        datasource = (application as DemoApp).appContainer.datasource

        val confirmPartnerDataBtn = findViewById<AppCompatButton>(R.id.confirmPartnerDataBtn)

        etPartnerId = findViewById(R.id.etPartnerId)
        btnPartnerIdClear = findViewById(R.id.btnPartnerIdClear)
        btnPartnerIdPaste = findViewById(R.id.btnPartnerIdPaste)

        etSecret = findViewById(R.id.etSecret)
        btnSecretClear = findViewById(R.id.btnSecretClear)
        btnSecretPaste = findViewById(R.id.btnSecretPaste)

        etDesignConfig = findViewById(R.id.etPartnerDesignConfig)
        btnDesignConfigClear = findViewById(R.id.btnDesignConfigClear)
        btnDesignConfigPaste = findViewById(R.id.btnDesignConfigPaste)

        setFastPartnerDataActions()

        confirmPartnerDataBtn.setOnClickListener {
            validatePartnerData()
        }

        setLangSpinner()
        setupLangReceiver()
    }

    override fun onResume() {
        super.onResume()
        //Hiding partner app's action bar as it's not used in SDK
        if (supportActionBar != null && supportActionBar!!.isShowing) {
            supportActionBar?.hide()
        }
    }

    private fun setFastPartnerDataActions() {
        btnPartnerIdClear.setOnClickListener {
            etPartnerId.text.clear()
        }
        btnPartnerIdPaste.setOnClickListener {
            etPartnerId.setText(getClipboardData())
        }
        btnSecretClear.setOnClickListener {
            etSecret.text.clear()
        }
        btnSecretPaste.setOnClickListener {
            etSecret.setText(getClipboardData())
        }
        btnDesignConfigClear.setOnClickListener {
            etDesignConfig.text.clear()
        }
        btnDesignConfigPaste.setOnClickListener {
            etDesignConfig.setText(getClipboardData())
        }
    }

    private fun getClipboardData(): String {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val data: ClipData.Item? = clipboard.primaryClip?.getItemAt(0)

        return if (data == null || data.toString().isEmpty()) {
            Toast.makeText(this@StartConfigActivity, getString(R.string.err_clipboard_has_no_data),
                Toast.LENGTH_LONG).show()
            ""
        } else {
            Toast.makeText(this@StartConfigActivity, getString(R.string.clipboard_pasted),
                Toast.LENGTH_SHORT).show()
            data.toString()
        }
    }

    private fun validatePartnerData() {
        if (validatePartnerId() && validateDesignConfig() && validateSecret()) {

            datasource.setSecret(etSecret.text!!.toString())
            datasource.setPartnerId(etPartnerId.text?.toString()!!.toInt())

            val intent: Intent?
            intent = Intent(this@StartConfigActivity, LaunchSchemeActivity::class.java)
            this@StartConfigActivity.startActivity(intent)
        }
    }

    private fun validatePartnerId(): Boolean {

        val data = etPartnerId.text?.toString()?.toIntOrNull()

        if (data == null) {
            Toast.makeText(this@StartConfigActivity, getString(R.string.err_invalid_partner_id),
                Toast.LENGTH_LONG).show()
        }
        return data != null
    }

    private fun validateSecret(): Boolean {

        val data = etSecret.text?.toString()

        if (data == null || data.isEmpty()) {
            Toast.makeText(this@StartConfigActivity, getString(R.string.err_invalid_partner_secret),
                Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun validateDesignConfig(): Boolean {

        val possibleJsonData = etDesignConfig.text?.toString()

        if (possibleJsonData != null && possibleJsonData.isNotEmpty()) {
            return try {
                VCheckSDK.designConfig(Gson().fromJson(etDesignConfig.text!!.toString(),
                    VCheckDesignConfig::class.java))
                true
            } catch (e: JsonSyntaxException) {
                VCheckSDK.designConfig(VCheckDesignConfig.getDefaultThemeConfig())
                //TODO may add stateful validation for TFs is needed
//                Toast.makeText(this@StartConfigActivity, "Non-valid JSON was passed while " +
//                        "initializing VCheckDesignConfig instance. Persisting VCheck default theme",
//                    Toast.LENGTH_LONG).show()
                true
            }
        } else {
            return false
        }
    }


    private fun setLangSpinner() {

        languageCode = ContextUtils.getSavedLanguage(this@StartConfigActivity)
        datasource.setLang(languageCode)

        val langSpinner = findViewById<Spinner>(R.id.lang_spinner)

        val adapter = ArrayAdapter.createFromResource(
            this@StartConfigActivity,
            R.array.languages, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        langSpinner.adapter = adapter
        langSpinner.onItemSelectedListener = this@StartConfigActivity
        langSpinner.setOnTouchListener(this@StartConfigActivity)

        langSpinner.viewTreeObserver.addOnGlobalLayoutListener {
            (langSpinner.selectedView as TextView).setTextColor(Color.WHITE)
        }

        langSpinner.post {
            when (languageCode) {
                "uk" -> langSpinner.setSelection(0)
                "en" -> langSpinner.setSelection(1)
                "ru" -> langSpinner.setSelection(2)
                "pl" -> langSpinner.setSelection(3)
                else -> langSpinner.setSelection(1)
            }
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        if (wasLocaleSelectedByUser) {
            when (position) {
                0 -> onLangSelected("uk")
                1 -> onLangSelected("en")
                2 -> onLangSelected("ru")
                3 -> onLangSelected("pl")
            }
            recreate()
        }
    }

    private fun onLangSelected(langCode: String) {
        ContextUtils.setSavedLanguage(this@StartConfigActivity, langCode)
        ContextUtils.updateLocale(this@StartConfigActivity, langCode)
        languageCode = langCode
        datasource.setLang(languageCode)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}

    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        wasLocaleSelectedByUser = true
        return false
    }

    private fun setupLangReceiver(): BroadcastReceiver? {
        if (mLangReceiver == null) {
            mLangReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    recreate()
                }
            }
            registerReceiver(mLangReceiver, IntentFilter(Intent.ACTION_LOCALE_CHANGED))
        }
        return mLangReceiver
    }

    override fun attachBaseContext(newBase: Context) {
        languageCode = ContextUtils.getSavedLanguage(newBase)
        val localeUpdatedContext: ContextWrapper =
            ContextUtils.updateLocale(newBase, languageCode)
        super.attachBaseContext(localeUpdatedContext)
    }
}