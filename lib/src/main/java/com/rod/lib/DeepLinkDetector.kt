package com.rod.lib

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.sun.org.apache.xerces.internal.dom.AttrNSImpl
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULiteralExpression
import org.w3c.dom.Element
import java.net.URI
import java.util.*
import kotlin.collections.ArrayList

@Suppress("UnstableApiUsage")
class DeepLinkDetector : Detector(), XmlScanner, Detector.UastScanner {

    companion object {
        val ISSUE = Issue.create(
            id = "ImoDeepLink",
            briefDescription = "只在代码中声明了 DeepLink",
            explanation = """
                为了保证 DeepLink 在 Imo 内各场景中都能正常工作，
                需要同时在代码和 AndroidManifest.xml 的 DeepLinkActivity 中声明 scheme/host
            """.trimIndent(),
            category = Category.CORRECTNESS,
            priority = 8,
            severity = Severity.ERROR,
            implementation = Implementation(
                DeepLinkDetector::class.java,
                EnumSet.of(Scope.JAVA_FILE, Scope.MANIFEST)
            )
        )
        val DEEPLINK_MAP = HashMap<String, ArrayList<String>>()
    }

    /**
     * deeplink 定义于 intent-filter 下的 data 结点
     */
    override fun getApplicableElements(): Collection<String>? {
        return listOf("data")
    }

    override fun visitElement(context: XmlContext, element: Element) {
        // 过滤，只要父结点的 android:name 值中包含 DeepLinkActivity 的 element
        if ((element.parentNode
                ?.parentNode
                ?.attributes
                ?.getNamedItem("android:name") as AttrNSImpl)
                .value.contains("DeepLinkActivity")
        ) {
            val schema = element.getAttribute("android:scheme")
            val host = element.getAttribute("android:host")
            val hostList = DEEPLINK_MAP[schema] ?: ArrayList()
            hostList.add(host)
            // 把现有已定义的保存起来
            DEEPLINK_MAP[schema] = hostList
        }
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>>? {
        // 表示我们要检查的是方法调用，包括普通方法和构造函数及数组的初始化调用
        // 这里我们主要是需要检查构造函数，因为在代码中注册时
        // deeplink 的 url 是通过构造函数传入的
        return listOf(UCallExpression::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return DeepLinkUrlHandler(context)
    }

    class DeepLinkUrlHandler(private val context: JavaContext) :
        UElementHandler() {

        /**
         * 此方法和 getApplicableUastTypes 中返回的 UCallExpression 对应
         */
        override fun visitCallExpression(node: UCallExpression) {
            if (node.classReference?.resolvedName == "DeepLinkTemplate") {
                val url = (node.valueArguments[0] as ULiteralExpression).value as String? ?: return
                val uri = URI.create(url)
                val hostList = DEEPLINK_MAP[uri.scheme]
                if (hostList == null || !hostList.contains(uri.host)) {
                    // Lint 是先扫描 AndroidManifest.xml 后扫描 java/kotlin 文件
                    // 如果代码中的 url 没有在 AndroidManifest 中场景，则报告 Issue
                    context.report(ISSUE, context.getLocation(node), "I get U")
                }
            }
        }
    }
}