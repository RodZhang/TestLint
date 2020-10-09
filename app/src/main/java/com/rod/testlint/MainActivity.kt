package com.rod.testlint

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    private val mCodeStyle = "Hungarian"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mTest = "test"

        DeepLinkTemplate("imo://test_lint")
        DeepLinkTemplate("imo://test_lint2", listOf("imo://test_lint3"))
    }
}