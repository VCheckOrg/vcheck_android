package com.vcheck.demo.activities

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.vcheck.demo.*
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.domain.toStringRepresentation
import kotlin.concurrent.fixedRateTimer


class CheckVerifResultActivity : AppCompatActivity() {

    private lateinit var datasource: Datasource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sdkfinish)

        datasource = (application as DemoApp).appContainer.datasource

        onBackPressedDispatcher.addCallback {
            //Stub; no back press needed here
        }

        val restartDemoBtn = findViewById<MaterialButton>(R.id.btn_restart)
        val becomePartnerBtn = findViewById<Button>(R.id.btn_become_partner)
        val roundIndicator = findViewById<CircularProgressIndicator>(R.id.loadingIndicator)
        val title = findViewById<TextView>(R.id.in_process_title)
        val subtitle = findViewById<TextView>(R.id.in_process_subtitle)
        val imgHolder = findViewById<ImageView>(R.id.in_process_image)
        val backBtn = findViewById<ConstraintLayout>(R.id.closeSDKBtnHolder)

        roundIndicator.isVisible = true
        restartDemoBtn.isVisible = false
        becomePartnerBtn.isVisible = false

        val timer = fixedRateTimer("vcheck_verification_check", false, 0L, 2000) {
            val status = checkFinalVerificationStatus()

            //Log.d("DEMO", "VERIF CHECK STATUS: $status")

            runOnUiThread {
                if (status.isFinalizedAndSuccessful) {
                    roundIndicator.isVisible = false

                    imgHolder.setImageResource(R.drawable.il_demo_success)
                    title.setText(R.string.verification_success_title)
                    subtitle.setText(R.string.verification_success_descr)

                    restartDemoBtn.isVisible = true
                    becomePartnerBtn.isVisible = true
                    restartDemoBtn.setOnClickListener {
                        this.cancel()
                        backToStart()
                    }
                    becomePartnerBtn.setOnClickListener {
                        this.cancel()
                        startActivity(Intent(this@CheckVerifResultActivity, PartnerFormActivity::class.java))
                    }
                }
                if (status.isFinalizedAndFailed) {
                    roundIndicator.isVisible = false
                    becomePartnerBtn.isVisible = false

                    imgHolder.setImageResource(R.drawable.il_demo_error)
                    title.setText(R.string.verification_failed_title)
                    subtitle.setText(R.string.verification_failed_descr)

                    restartDemoBtn.isVisible = true
                    restartDemoBtn.setOnClickListener {
                        this.cancel()
                        backToStart()
                    }
                }
            }
        }

        backBtn.setOnClickListener {
            timer.cancel()
            backToStart()
        }
    }

    override fun onResume() {
        super.onResume()
        //Hiding partner app's action bar as it's not used in SDK
        if (supportActionBar != null && supportActionBar!!.isShowing) {
            supportActionBar?.hide()
        }
    }

    private fun checkFinalVerificationStatus(): VerificationResult {
        val call = datasource.checkFinalVerificationStatus()
        return if (call != null) {
            val response = call.execute()

            //Log.d("VERIF_STATUS_RESULT", "success ? ${response.isSuccessful} | body: ${response.body().toString()}")

            if (response.isSuccessful && response.body() != null) {

                val bodyDeserialized: FinalVerifCheckResponseModel = response.body() as FinalVerifCheckResponseModel
                val data = bodyDeserialized.data
                VerificationResult(
                    isVerificationFinalizedAndSuccessful(data),
                    isVerificationFinalizedAndFailed(data),
                    isVerificationWaitingForManualCheck(data),
                    data.status, data.scheme, data.createdAt, data.finalizedAt, data.rejectionReasons)
            } else getErrorVerificationResult()
        } else getErrorVerificationResult()
    }

    private fun isVerificationFinalizedAndSuccessful(data: FinalVerifCheckResponseData): Boolean {
        return (data.status.lowercase() == "completed" && data.isSuccess == true)
    }

    private fun isVerificationFinalizedAndFailed(data: FinalVerifCheckResponseData): Boolean {
        return (data.status.lowercase() == "completed" && data.isSuccess == false)
    }

    private fun isVerificationWaitingForManualCheck(data: FinalVerifCheckResponseData): Boolean {
        return data.status.lowercase() == "waiting_manual_check"
    }

    private fun getErrorVerificationResult(): VerificationResult {
        return VerificationResult(
            isFinalizedAndSuccessful = false, isFinalizedAndFailed = false,
            isWaitingForManualCheck = false, status = "sdk_client_error",
            scheme = VCheckSDK.getVerificationType()!!.toStringRepresentation(),
            createdAt = null, finalizedAt = null, rejectionReasons = null)
    }

    private fun backToStart() {
        val intent: Intent?
        try {

            intent = Intent(this@CheckVerifResultActivity, LaunchSchemeActivity::class.java)
            this@CheckVerifResultActivity.startActivity(intent)
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

//        (application as DemoApp).appContainer.colorsRepository.buttonsColorHex?.let {
//            becomePartnerBtn.setBackgroundColor(Color.parseColor(it))
//        }
//        (application as DemoApp).appContainer.colorsRepository.backgroundPrimaryColorHex?.let {
//            backgroundView.setBackgroundColor(Color.parseColor(it))
//            changeDemoActivityStatusBarColor(Color.parseColor(it))
//        }
//        (application as DemoApp).appContainer.colorsRepository.backgroundSecondaryColorHex?.let {
//            card.setCardBackgroundColor(Color.parseColor(it))
//        }
//        (application as DemoApp).appContainer.colorsRepository.backgroundTertiaryColorHex?.let {
//            restartDemoBtn.setTextColor(Color.parseColor(it))
//            restartDemoBtn.strokeColor = ColorStateList.valueOf(Color.parseColor(it))
//        }
//        (application as DemoApp).appContainer.colorsRepository.primaryTextColorHex?.let {
//            popSDKTitle.setTextColor(Color.parseColor(it))
//            val colorFilter = PorterDuffColorFilter(Color.parseColor(it), PorterDuff.Mode.SRC_ATOP)
//            popSDKIcon.colorFilter = colorFilter
//            roundIndicator.setIndicatorColor(Color.parseColor(it))
//            title.setTextColor(Color.parseColor(it))
//        }
//        (application as DemoApp).appContainer.colorsRepository.secondaryTextColorHex?.let {
//            subtitle.setTextColor(Color.parseColor(it))
//        }
//        (application as DemoApp).appContainer.colorsRepository.borderColorHex?.let {
//            //Stub
//        }