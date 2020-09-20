package com.reactnativeasdktinkoff

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.facebook.react.bridge.*
import org.json.JSONObject
import ru.tinkoff.acquiring.sdk.TinkoffAcquiring
import ru.tinkoff.acquiring.sdk.TinkoffAcquiring.Companion.RESULT_ERROR
import ru.tinkoff.acquiring.sdk.localization.AsdkSource
import ru.tinkoff.acquiring.sdk.localization.Language
import ru.tinkoff.acquiring.sdk.models.DarkThemeMode
import ru.tinkoff.acquiring.sdk.models.enums.CheckType
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.utils.Money

class AsdkTinkoffModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), ActivityEventListener {

    companion object {
      private const val PAYMENT_REQUEST_CODE = 0
      lateinit var promise: Promise
    }

    init {
      reactContext.addActivityEventListener(this)
    }

    override fun getName(): String {
        return "AsdkTinkoff"
    }

    @ReactMethod
    fun pay(json: String, p: Promise) {
      promise = p

      val data = JSONObject(json)

      val sdk = TinkoffAcquiring(
        data["TerminalKey"] as String,
        data["Password"] as String,
        data["PublicKey"] as String
      )

      val paymentOptions = PaymentOptions().setOptions {
        orderOptions {
          orderId = (data["OrderId"] as Int).toString()
          amount =  Money.ofCoins((data["Amount"] as Int).toLong())
          recurrentPayment = false
        }
        customerOptions {
          checkType = CheckType.NO.toString()
          customerKey = data["CustomerKey"] as String
        }
        featuresOptions {
          useSecureKeyboard = true
          localizationSource = AsdkSource(Language.RU)
          darkThemeMode = DarkThemeMode.DISABLED
//          cameraCardScanner = CameraCardIOScanner()
        }
      }

      sdk.openPaymentScreen(currentActivity as FragmentActivity, paymentOptions, PAYMENT_REQUEST_CODE)
    }

    private fun handlePaymentResult(resultCode: Int) {
      when (resultCode) {
        RESULT_OK -> promise.resolve("SUCCESS")
        RESULT_CANCELED -> promise.resolve("CANCELED")
        RESULT_ERROR -> promise.resolve("ERROR")
      }
    }

    override fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, data: Intent?) {
      when (requestCode) {
        PAYMENT_REQUEST_CODE -> handlePaymentResult(resultCode)
      }
    }

    override fun onNewIntent(intent: Intent?) = Unit
}
