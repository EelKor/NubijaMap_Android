package com.underbar.nubijaapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.*

class RentPageActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rent_page)

       // WebView Id 세팅
       webView = findViewById(R.id.webView1)

       // WebView 설정
       webView.settings.apply {
           this.javaScriptEnabled = true
           this.javaScriptCanOpenWindowsAutomatically = true
           this.loadWithOverviewMode = true
           this.builtInZoomControls = false
           this.cacheMode = WebSettings.LOAD_DEFAULT
           this.setSupportMultipleWindows(true)
           this.domStorageEnabled = true
       }

       // WebView 클라이언트 설정
       webView.webViewClient = WebViewClient()
       webView.webChromeClient = WebChromeClient()

       // WebView 위치 정보 설정
       webView.webChromeClient = object : WebChromeClient() {
           override fun onGeolocationPermissionsShowPrompt(
               origin: String?,
               callback: GeolocationPermissions.Callback?
           ) {
               super.onGeolocationPermissionsShowPrompt(origin, callback)
               callback?.invoke(origin, true, false)
           }

       }
       webView.loadUrl("https://app.nubija.com")

    }

    override fun onBackPressed() {
        if (webView.canGoBack())
            super.onBackPressed()
        else
            finish()
    }
}