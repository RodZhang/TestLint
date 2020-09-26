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
class DeepLinkDetector : Detector(), Detector.XmlScanner, Detector.UastScanner {

    companion object {
        val ISSUE = Issue.create(
            id = "ImoDeepLink",
            briefDescription = "hehe",
            explanation = "haha",
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

    override fun getApplicableElements(): Collection<String>? {
        return listOf("data")
    }

    override fun visitElement(context: XmlContext, element: Element) {
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
            DEEPLINK_MAP[schema] = hostList
            println(DEEPLINK_MAP)
        }
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>>? {
        return listOf(UCallExpression::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return DeepLinkUrlHandler(context)
    }

    class DeepLinkUrlHandler(private val context: JavaContext) :
        UElementHandler() {

        override fun visitCallExpression(node: UCallExpression) {
            if (node.classReference?.resolvedName == "DeepLinkTemplate") {
                val url =
                    (node.valueArguments[0] as ULiteralExpression).value as String?
                        ?: return
                val uri = URI.create(url)
                val hostList = DEEPLINK_MAP[uri.scheme]
                if (hostList?.contains(uri.host) == false) {
                    context.report(ISSUE, context.getLocation(node), "I get U")
                }
            }
        }
    }
}