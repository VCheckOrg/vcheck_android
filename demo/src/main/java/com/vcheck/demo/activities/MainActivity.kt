package com.vcheck.demo.activities

import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.vcheck.demo.*
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.domain.VCheckDesignConfig
import com.vcheck.sdk.core.domain.VCheckEnvironment
import com.vcheck.sdk.core.domain.VerificationSchemeType


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener,
    View.OnTouchListener {

    private lateinit var datasource: Datasource

    private var wasLocaleSelectedByUser = false

    private var mLangReceiver: BroadcastReceiver? = null

    private var languageCode: String = "uk"

    private var designConfig : VCheckDesignConfig = VCheckDesignConfig.getDefaultThemeConfig()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitle(R.string.demo_app_title)

        datasource = (application as DemoApp).appContainer.datasource

        setLangSpinner()

        setupLangReceiver()

        val startFullSDKFlowBtn = findViewById<AppCompatButton>(R.id.launchSDKFullFlowCommonBtn)
        val startDocOnlySDKFlowBtn = findViewById<AppCompatButton>(R.id.launchSDKDocOnlyCommonBtn)
        val startLivenessOnlySDKFlowBtn = findViewById<AppCompatButton>(R.id.launchSDKLivenessOnlyCommonBtn)

        val changeDesignConfigButton = findViewById<AppCompatButton>(R.id.uploadDesignConfigBtn)

        val indicator = findViewById<CircularProgressIndicator>(R.id.progress_indicator)
        val scrollView = findViewById<ScrollView>(R.id.content_scroll_view)

        indicator.isVisible = false
        scrollView.isVisible = true

        startFullSDKFlowBtn.setOnClickListener {
            launchSDK(VerificationSchemeType.FULL_CHECK, indicator, scrollView)
        }
        startDocOnlySDKFlowBtn.setOnClickListener {
            launchSDK(VerificationSchemeType.DOCUMENT_UPLOAD_ONLY, indicator, scrollView)
        }
        startLivenessOnlySDKFlowBtn.setOnClickListener {
            launchSDK(VerificationSchemeType.LIVENESS_CHALLENGE_ONLY, indicator, scrollView)
        }

        changeDesignConfigButton.setOnClickListener {
            readClipboardConfigData()
        }
    }

    override fun onResume() {
        super.onResume()
        //Hiding partner app's action bar as it's not used in SDK
        if (supportActionBar != null && supportActionBar!!.isShowing) {
            supportActionBar?.hide()
        }
    }

    private fun launchSDK(verifScheme: VerificationSchemeType,
                          indicator: CircularProgressIndicator,
                          scrollView: ScrollView) {
        scrollView.isVisible = false
        indicator.isVisible = true

        Thread {
            val timestampResponse = datasource.getServiceTimestamp().execute()
            if (timestampResponse.isSuccessful) {
                val timestamp = (timestampResponse.body() as String).toLong()
                val requestBody = datasource.prepareVerificationRequest(
                    timestamp, VCheckSDK.getSDKLangCode(),
                    VerificationClientCreationModel(
                        PARTNER_ID,
                        PARTNER_SECRET,
                        verifScheme)
                )
                val createResponse = datasource.createVerificationRequest(requestBody).execute()
                if (createResponse.isSuccessful) {
                    val response = (createResponse.body() as CreateVerificationAttemptResponse)
                    datasource.setVerificationId(response.data.id)

                    runOnUiThread {
                        Handler(Looper.getMainLooper()).postDelayed({
                            indicator.isVisible = false
                            scrollView.isVisible = true
                        }, 900)
                    }
                    startSDK(response.data.token, verifScheme)
                } else {
                    runOnUiThread {
                        indicator.isVisible = false
                        scrollView.isVisible = true
                    }
                    Log.d("VCheck Demo - error: ",
                        "Creating verification failed was with error code: ${createResponse.code()}")
                }
            } else {
                runOnUiThread {
                    indicator.isVisible = false
                    scrollView.isVisible = true
                }
                Log.d("VCheck Demo - error: ",
                    "Cannot get service timestamp for check verification call: ${timestampResponse.code()}")
            }
        }.start()
    }

    private fun startSDK(verifToken: String, verifScheme: VerificationSchemeType) {

        VCheckSDK
            .verificationToken(verifToken)
            .verificationType(verifScheme)
            .environment(VCheckEnvironment.DEV)
            .languageCode(languageCode)
            .designConfig(designConfig)
            .partnerEndCallback {
                onVCheckSDKFlowFinish()
            }
            .onVerificationExpired {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Verification expired. Try again",
                        Toast.LENGTH_LONG).show()
                }
            }
            .start(this@MainActivity)
    }

    private fun onVCheckSDKFlowFinish() {
        val intent: Intent?
        try {
            intent = Intent(this@MainActivity, CheckVerifResultActivity::class.java)
            this@MainActivity.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Error while returning to app: ${e.message}",
                Toast.LENGTH_LONG).show()
        }
    }

    private fun readClipboardConfigData() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val item = clipboard.primaryClip!!.getItemAt(0)
        val possibleJsonData = item.text.toString()
        if (possibleJsonData.isNotEmpty()) {
            try {
                designConfig = Gson().fromJson(possibleJsonData, VCheckDesignConfig::class.java)
                Toast.makeText(this@MainActivity, "JSON text data successfully copied from clipboard",
                    Toast.LENGTH_LONG).show()
            } catch (e: JsonSyntaxException) {
                designConfig = VCheckDesignConfig.getDefaultThemeConfig()
                Toast.makeText(this@MainActivity, "Non-valid JSON was passed while " +
                        "initializing VCheckDesignConfig instance. Persisting VCheck default theme",
                    Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this@MainActivity, "Clipboard has no text!",
                Toast.LENGTH_LONG).show()
        }
    }

    private fun setLangSpinner() {

        languageCode = ContextUtils.getSavedLanguage(this@MainActivity)

        val langSpinner = findViewById<Spinner>(R.id.lang_spinner)

        val adapter = ArrayAdapter.createFromResource(
            this@MainActivity,
            R.array.languages, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        langSpinner.adapter = adapter
        langSpinner.onItemSelectedListener = this@MainActivity
        langSpinner.setOnTouchListener(this@MainActivity)

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
        ContextUtils.setSavedLanguage(this@MainActivity, langCode)
        ContextUtils.updateLocale(this@MainActivity, langCode)
        languageCode = langCode
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