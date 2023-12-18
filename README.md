# VCheck SDK for Android

[VCheck](https://vycheck.com/) is online remote verification service for fast and secure customer access to your services.

## Features

- Document validity: Country and document type identification. Checks for forgery and interference (glare, covers, third-party objects)
- Document data recognition: The data of the loaded document is automatically parsed
- Liveliness check: Determining that a real person is being verified
- Face matching: Validate that the document owner is the user being verified
- Easy integration to your service's Android app out-of-the-box

## How to use
#### Installing via JitPack

You can check the most recent version of SDK via [![](https://jitpack.io/v/VCheckOrg/vcheck_android.svg)](https://jitpack.io/#VCheckOrg/vcheck_android) and import it with Gradle:
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
    .designConfig(yourDesignConfig)
    .partnerEndCallback {
        onVCheckSDKFlowFinish()
    }
    .onVerificationExpired {
        onVerificationExpired()
    }
    .start(this@MyActivity)
```

#### Explication for required properties

| Property | Parameter Type | Description |
| ----------- | ----------- | ----------- |
| verificationToken | String | Valid token of recently created VCheck Verification |
| verificationType | VerificationSchemeType | Verification scheme type |
| languageCode | String | 2-letter language code (Ex.: "en" ; implementation's default is "en") |
| environment | VCheckEnvironment | VCheck service environment (dev/partner) |
| partnerEndCallback | Function | Callback function which triggers on verification process and SDK flow finish |
| onVerificationExpired | Function | Callback function which triggers when current verification goes to expired state |
| start | Activity | Triggers SDK flow start. Should be put as the final function in the chain |


#### Optional properties for verification session's logic and UI customization

| Property | Type | Description |
| ----------- | ----------- | ----------- |
| designConfig | String? | JSON string with specific fixed set of color properties, which can be obtained as a VCheck portal user |
| showCloseSDKButton | Boolean? | Should 'Return to Partner' button be shown |
| showPartnerLogo | Boolean? | Should VCheck logo be shown |


