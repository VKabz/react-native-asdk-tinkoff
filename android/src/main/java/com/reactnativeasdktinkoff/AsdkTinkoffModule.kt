package com.reactnativeasdktinkoff

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.facebook.react.bridge.*
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONObject
import ru.tinkoff.acquiring.sdk.TinkoffAcquiring
import ru.tinkoff.acquiring.sdk.TinkoffAcquiring.Companion.RESULT_ERROR
import ru.tinkoff.acquiring.sdk.localization.AsdkSource
import ru.tinkoff.acquiring.sdk.localization.Language
import ru.tinkoff.acquiring.sdk.models.AsdkState
import ru.tinkoff.acquiring.sdk.models.DarkThemeMode
import ru.tinkoff.acquiring.sdk.models.GooglePayParams
import ru.tinkoff.acquiring.sdk.models.enums.CheckType
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.payment.PaymentListener
import ru.tinkoff.acquiring.sdk.payment.PaymentListenerAdapter
import ru.tinkoff.acquiring.sdk.utils.GooglePayHelper
import ru.tinkoff.acquiring.sdk.utils.Money

class AsdkTinkoffModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), ActivityEventListener {

    companion object {
        private const val PAYMENT_REQUEST_CODE = 0
        private const val GOOGLE_PAY_REQUEST_CODE = 1
        lateinit var promise: Promise
        lateinit var data: JSONObject
    }

    private val paymentListener = createPaymentListener()

    private fun createPaymentListener(): PaymentListener {
        return object : PaymentListenerAdapter() {

            override fun onSuccess(paymentId: Long, cardId: String?) {
                promise.resolve(writableMapOf("Success" to true))
            }

            override fun onError(throwable: Throwable) {
                promise.resolve(writableMapOf("Success" to false))
            }
        }
    }

    init {
      reactContext.addActivityEventListener(this)
    }

    override fun getName(): String {
        return "AsdkTinkoff"
    }

    private fun createPaymentOptions(): PaymentOptions {
        val customer = data["CustomerKey"] as String

        return  PaymentOptions().setOptions {
            orderOptions {
                orderId = (data["OrderId"] as Int).toString()
                amount =  Money.ofCoins((data["Amount"] as Int).toLong())
                recurrentPayment = false
            }
            customerOptions {
                checkType = CheckType.NO.toString()
                customerKey = customer
            }
            featuresOptions {
                useSecureKeyboard = true
                localizationSource = AsdkSource(Language.RU)
                darkThemeMode = DarkThemeMode.DISABLED
//          cameraCardScanner = CameraCardIOScanner()
            }
        }
    }

    @ReactMethod
    fun Pay(j: String, p: Promise) {
        promise = p
        data = JSONObject(j)

      val sdk = TinkoffAcquiring(
        data["TerminalKey"] as String,
        data["Password"] as String,
        data["PublicKey"] as String
      )

      sdk.openPaymentScreen(
              currentActivity as FragmentActivity,
              createPaymentOptions(),
              PAYMENT_REQUEST_CODE
      )
    }

    @ReactMethod
    fun GooglePay(j: String, p: Promise) {
        promise = p
        data = JSONObject(j)

        val googlePayHelper = GooglePayHelper(
                GooglePayParams(
                        terminalKey = data["TerminalKey"] as String,
                        environment = WalletConstants.ENVIRONMENT_PRODUCTION
                )
        )

        googlePayHelper.initGooglePay(reactApplicationContext) { ready ->
            if (ready) {
                googlePayHelper.openGooglePay(
                        currentActivity as Activity,
                        Money.ofCoins((data["Amount"] as Int).toLong()),
                        GOOGLE_PAY_REQUEST_CODE
                )
            } else {
                promise.resolve(writableMapOf("Success" to false))
            }
        }
    }

    fun writableMapOf(vararg values: Pair<String, *>): WritableMap {
        val map = Arguments.createMap()
        for ((key, value) in values) {
            when (value) {
                null -> map.putNull(key)
                is Boolean -> map.putBoolean(key, value)
                is Double -> map.putDouble(key, value)
                is Int -> map.putInt(key, value)
                is String -> map.putString(key, value)
                is WritableMap -> map.putMap(key, value)
                is WritableArray -> map.putArray(key, value)
                else -> throw IllegalArgumentException("Unsupported value type ${value::class.java.name} for key [$key]")
            }
        }
        return map
    }

    private fun handleGooglePayResult(resultCode: Int, intent: Intent?) {
        if (intent != null && resultCode == Activity.RESULT_OK) {
            val token = GooglePayHelper.getGooglePayToken(intent)
            if (token == null) {
                promise.resolve(writableMapOf("Success" to false))
            } else {
                val sdk = TinkoffAcquiring(
                        data["TerminalKey"] as String,
                        data["Password"] as String,
                        data["PublicKey"] as String
                )
                sdk.initPayment(token, createPaymentOptions())
                        .subscribe(paymentListener)
                        .start()
            }
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            promise.resolve(writableMapOf("Success" to false))
        }

    }

    private fun handlePaymentResult(resultCode: Int) {
        when (resultCode) {
            RESULT_OK -> promise.resolve(writableMapOf("Success" to true))
            RESULT_CANCELED -> promise.resolve(writableMapOf("Success" to false))
            RESULT_ERROR -> promise.resolve(writableMapOf("Success" to false))
        }
    }

    override fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, data: Intent?) {
      when (requestCode) {
          PAYMENT_REQUEST_CODE -> handlePaymentResult(resultCode)
          GOOGLE_PAY_REQUEST_CODE -> handleGooglePayResult(resultCode, data)
      }
    }

    override fun onNewIntent(intent: Intent?) = Unit
}
