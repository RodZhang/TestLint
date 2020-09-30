package com.rod.lib

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.Issue

@Suppress("UnstableApiUsage")
class CustomRegistry : IssueRegistry() {

    override val issues: List<Issue>
        get() = listOf(DeepLinkDetector.ISSUE)

    override val api: Int = com.android.tools.lint.detector.api.CURRENT_API
}