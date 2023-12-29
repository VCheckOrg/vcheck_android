package com.vcheck.demo.activities

import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.vcheck.demo.*
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.domain.VCheckEnvironment
import com.vcheck.sdk.core.domain.VerificationSchemeType

class LaunchSchemeActivity : AppCompatActivity() {

    private lateinit var datasource: Datasource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch_scheme)
        setTitle(R.string.demo_app_title)

        datasource = (application as DemoApp).appContainer.datasource

        val startFullSDKFlowBtn = findViewById<AppCompatButton>(R.id.launchSDKFullFlowCommonBtn)
        val startDocOnlySDKFlowBtn = findViewById<AppCompatButton>(R.id.launchSDKDocOnlyCommonBtn)
        val startLivenessOnlySDKFlowBtn = findViewById<AppCompatButton>(R.id.launchSDKLivenessOnlyCommonBtn)

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
                        datasource.getPartnerId()!!,
                        datasource.getSecret()!!,
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
                        "Verification creation failed with error code: ${createResponse.code()}")
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
            .languageCode(datasource.getLang())
            //.designConfig(designConfig) //we should add this call to chain in actual partner app
            .partnerEndCallback {
                onVCheckSDKFlowFinish()
            }
            .onVerificationExpired {
                runOnUiThread {
                    Toast.makeText(applicationContext, getString(R.string.err_verif_expired),
                        Toast.LENGTH_LONG).show()
                }
            }
            .start(this@LaunchSchemeActivity)
    }

    private fun onVCheckSDKFlowFinish() {
        val intent: Intent?
        try {
            intent = Intent(this@LaunchSchemeActivity, CheckVerifResultActivity::class.java)
            this@LaunchSchemeActivity.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Error while returning to an app: ${e.message}",
                Toast.LENGTH_LONG).show()
        }
    }

}