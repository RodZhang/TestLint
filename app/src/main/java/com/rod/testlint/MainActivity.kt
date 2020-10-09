package com.rod.testlint

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DeepLinkTemplate("imo://test_lint")
        DeepLinkTemplate("imo://test_lint2", listOf("imo://test_lint3"))

        printType<String>()
    }

    inline fun <reified T> printType() {
        print(T::class.java)
    }
}