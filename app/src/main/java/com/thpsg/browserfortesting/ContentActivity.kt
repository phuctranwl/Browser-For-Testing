package com.thpsg.browserfortesting

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView

class ContentActivity : AppCompatActivity() {
    private var viewOption: ViewOption? = null
    private var content: String? = null

    internal enum class ViewOption {
        CONTENT,
        CONSOLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)

        val bundle = intent.extras
        if (bundle != null && bundle.containsKey(MainActivity.VIEW_OPTION)) {
            viewOption = bundle.get(MainActivity.VIEW_OPTION) as ViewOption
            content = bundle.get(MainActivity.DATA) as String
        }

        when (viewOption) {
            ViewOption.CONSOLE -> title = "View Console"
            ViewOption.CONTENT -> title = "View Content"
        }

        val contentTv = findViewById<TextView>(R.id.content)
        if (content == null || content?.isEmpty() != false) {
            contentTv.text = getString(R.string.empty)
        } else {
            contentTv.text = content
        }
    }
}
