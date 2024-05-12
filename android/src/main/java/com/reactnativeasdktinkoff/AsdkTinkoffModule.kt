package com.reactnativeasdktinkoff

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.util.Log
import ru.tinkoff.acquiring.sdk.TinkoffAcquiring.Companion.RESULT_ERROR
import androidx.fragment.app.FragmentActivity
import com.facebook.react.bridge.*
import org.json.JSONObject
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.TinkoffAcquiring
import ru.tinkoff.acquiring.sdk.localization.AsdkSource
import ru.tinkoff.acquiring.sdk.localization.Language
import ru.tinkoff.acquiring.sdk.models.DarkThemeMode
import ru.tinkoff.acquiring.sdk.models.enums.CheckType
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.payment.PaymentListener
import ru.tinkoff.acquiring.sdk.payment.PaymentListenerAdapter
import ru.tinkoff.acquiring.sdk.utils.Money



class AsdkTinkoffModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), ActivityEventListener {


    companion object {
      private const val PAYMENT_REQUEST_CODE = 0
        private const val GOOGLE_PAY_REQUEST_CODE = 1
        lateinit var promise: Promise
        lateinit var data: JSONObject
    }

//  private val paymentListener = createPaymentListener()
//
//  private fun createPaymentListener(): PaymentListener {
//    return object : PaymentListenerAdapter() {
//
//      override fun onSuccess(paymentId: Long, cardId: String?) {
//        promise.resolve(writableMapOf("Success" to true))
//      }
//
//      override fun onError(throwable: Throwable) {
//        promise.resolve(writableMapOf("Success" to false))
//      }
//    }
//  }

    init {
      AcquiringSdk.isDebug = true
      reactContext.addActivityEventListener(this)
    }


    override fun getName(): String {
        return "AsdkTinkoff"
    }

    @ReactMethod
    fun Pay(j: String, p: Promise) {
        promise = p
        data = JSONObject(j)


      val customer = data["CustomerKey"] as String
      val userEmail = data["email"] as String

      val PublicKey = data["PublicKey"] as String
      val TerminalKey = data["TerminalKey"] as String


     val tinkoffAcquiring = TinkoffAcquiring(reactApplicationContext.applicationContext, TerminalKey, PublicKey)


        val paymentOptions = PaymentOptions().setOptions {
          orderOptions {
            orderId = data["OrderId"] as String
            amount =  Money.ofCoins((data["Amount"] as Int).toLong())
            recurrentPayment = false
          }
          customerOptions {
            checkType = CheckType.NO.toString()
            customerKey = customer
            email = userEmail
          }
          featuresOptions {
            useSecureKeyboard = true
            fpsEnabled = true
            tinkoffPayEnabled = true
            localizationSource = AsdkSource(Language.RU)
            darkThemeMode = DarkThemeMode.DISABLED
          }
          setTerminalParams(TerminalKey, PublicKey)
        }

       tinkoffAcquiring.openPaymentScreen(currentActivity as FragmentActivity, paymentOptions, PAYMENT_REQUEST_CODE )

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
                is Long -> map.putString(key, value.toString())
                else -> throw IllegalArgumentException("Unsupported value type ${value::class.java.name} for key [$key]")
            }
        }
        return map
    }

  private fun handlePaymentResult(resultCode: Int, data: Intent?) {
    when (resultCode) {
      RESULT_OK -> promise.resolve(writableMapOf("Success" to true, "canceled" to false, "PaymentId" to data?.getLongExtra(TinkoffAcquiring.EXTRA_PAYMENT_ID, -1)))
      RESULT_CANCELED -> {
        promise.resolve(writableMapOf("Success" to false,"canceled" to true, "PaymentId" to data?.getLongExtra(TinkoffAcquiring.EXTRA_PAYMENT_ID, -1)))
      }
      RESULT_ERROR -> {
        promise.resolve(writableMapOf("Success" to false, "canceled" to false, "PaymentId" to data?.getLongExtra(TinkoffAcquiring.EXTRA_PAYMENT_ID, -1)))
      }
    }
  }

//  private fun handlePaymentResult(resultCode: Int, intent: Intent?) {
//    when (resultCode) {
//      RESULT_OK -> promise.resolve(writableMapOf("Success" to true, "paymentId" to intent?.getLongExtra(TinkoffAcquiring.EXTRA_PAYMENT_ID, -1)))
//      RESULT_CANCELED -> promise.resolve(writableMapOf("Success" to false))
//      RESULT_ERROR -> promise.resolve(writableMapOf("Success" to false))
//    }
//  }

  override fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      PAYMENT_REQUEST_CODE -> handlePaymentResult(resultCode, data)
    }
  }

  override fun onNewIntent(intent: Intent?) = Unit
}
