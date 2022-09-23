# VCheck SDK for Android

[VCheck](https://vycheck.com/) is online remote verification service for fast and secure customer access to your services.

## Features

- Document validity: Country and document type identification. Checks for forgery and interference (glare, covers, third-party objects)
- Document data recognition: The data of the loaded document is automatically parsed
- Liveliness check: Determining that a real person is being verified
- Face matching: Validate that the document owner is the user being verified
- Easy integration to your service's Flutter app out-of-the-box

## How to use
#### Installing via JitPack

You can check the most recent version of SDK via https://jitpack.io/#VCheckOrg/vcheck_android
Importing via Gradle:
```
implementation 'com.github.VCheckOrg:vcheck_android:1.0.x'
```

#### Start SDK flow

```
import com.vcheck.sdk.core.VCheckSDK

//...
        VCheckSDK
            .verificationToken(verifToken)
            .verificationType(verifScheme)
            .languageCode(languageCode)
            .showPartnerLogo(false)
            .showCloseSDKButton(true)
            .environment(VCheckEnvironment.DEV)
            .colorActionButtons(optionalColorHexStr)
            .colorBorders(optionalColorHexStr)
            .colorTextPrimary(optionalColorHexStr)
            .colorTextSecondary(optionalColorHexStr)
            .colorBackgroundPrimary(optionalColorHexStr)
            .colorBackgroundSecondary(optionalColorHexStr)
            .colorBackgroundTertiary(optionalColorHexStr)
            .colorIcons(optionalColorHexStr)
            .partnerEndCallback {
                onVCheckSDKFlowFinish()
            }
            .start(this@MyActivity)
```
