package com.thpsg.browserfortesting

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.PopupMenu
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var activity: Activity
    private lateinit var progressDialog: ProgressDialog
    private lateinit var webView: WebView
    private lateinit var urlTv: EditText
    private lateinit var param: EditText
    private var content: String = ""
    private var console: String = ""
    private var requestType: Spinner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = this
        setContentView(R.layout.activity_main)
        urlTv = findViewById(R.id.url)
        param = findViewById(R.id.param)
        webView = findViewById(R.id.web)
        requestType = findViewById(R.id.spinner)
        progressDialog = ProgressDialog(activity)
        progressDialog.setMessage(getString(R.string.loading))

        getSavedUrl()
        settingWebView()
        settingAction()
    }

    private fun getSavedUrl() {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
        val url = sharedPref.getString("URL", "")
        urlTv.setText(url)
        urlTv.setSelection(url.length)
    }

    private fun settingAction() {
        findViewById<View>(R.id.go).setOnClickListener {
            hideKeyboard(activity)
            if (TextUtils.isEmpty(urlTv.text.toString())) {
                webView.loadUrl(getString(R.string.link_google))
                spinner.setSelection(0)
                param.visibility = View.GONE
                return@setOnClickListener
            }
            var url = urlTv.text.toString()
            if (!urlTv.text.toString().contains("http")) {
                url = "https://$url"
            }

            if (activity.getString(R.string.post) == requestType?.selectedItem.toString()) {
                val param = param.text.toString()
                webView.postUrl(url, param.toByteArray())
            } else {
                webView.loadUrl(url)
            }

            saveUrl()
        }

        findViewById<View>(R.id.menu).setOnClickListener { view -> showDeviceMenu(view) }

        findViewById<View>(R.id.clear).setOnClickListener {
            urlTv.setText("")
            spinner.setSelection(0)
            param.setText("")
            webView.loadUrl(getString(R.string.blank))
            clearUrl()
        }

        findViewById<Spinner>(R.id.spinner).onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                val selectedItem = adapterView.selectedItem
                if (activity.getString(R.string.get) == selectedItem) {
                    param.visibility = View.GONE
                } else {
                    param.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
    }

    override fun onBackPressed() = webView.goBack()

    private fun settingWebView() {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                urlTv.setText(url)
                progressDialog.show()
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                progressDialog.hide()
                webView.evaluateJavascript("(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();") { html ->
                    content = html
                    content = content.replace("\\u003C", "<")
                    content = content.replace("\\\"", "\"")
                    content = content.substring(1, content.length - 1)
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                val newLine = "${consoleMessage.message()}\n--- Line ${consoleMessage.lineNumber()} of ${consoleMessage.sourceId()}"
                console = "$console$newLine\n\n"
                return true
            }
        }
        webView.canGoBack()
        webView.canGoForward()

        webView.settings.apply {
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
            builtInZoomControls = true
            javaScriptEnabled = true
            allowContentAccess = true
            setAppCacheEnabled(true)
            setSupportZoom(true)
            setAppCachePath("")
            databaseEnabled = true
            domStorageEnabled = true
            setGeolocationEnabled(true)
            saveFormData = true
            setGeolocationEnabled(true)
        }

        WebView.setWebContentsDebuggingEnabled(true)
    }

    private fun showDeviceMenu(v: View) {
        val popup = PopupMenu(this, v)
        popup.inflate(R.menu.menu_main)
        invalidateOptionsMenu()
        popup.setOnMenuItemClickListener { item ->
            val intent = Intent(activity, ContentActivity::class.java)
            when (item.itemId) {
                R.id.action_view_console -> {
                    intent.putExtra(VIEW_OPTION, ContentActivity.ViewOption.CONSOLE)
                    intent.putExtra(DATA, console)
                    activity.startActivity(intent)
                    true
                }
                R.id.action_view_content -> {
                    intent.putExtra(VIEW_OPTION, ContentActivity.ViewOption.CONTENT)
                    intent.putExtra(DATA, content)
                    activity.startActivity(intent)
                    true
                }
                R.id.action_settings -> {
                    Toast.makeText(activity, getString(R.string.feature_will_be_available), Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun saveUrl() {
        val url = urlTv.text.toString()
        activity.getPreferences(Context.MODE_PRIVATE).edit().apply {
            putString("URL", url)
            apply()
        }
    }

    private fun clearUrl() {
        activity.getPreferences(Context.MODE_PRIVATE).edit().apply {
            clear()
            apply()
        }
    }

    companion object {
        var VIEW_OPTION = "OPTION"
        var DATA = "DATA"

        private fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            val view = activity.currentFocus ?: View(activity)
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}