package com.rod.lib

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.*

/**
 * Created by Rod Zhang on 2020/10/9
 */
@Suppress("UnstableApiUsage")
class CodeStyleDetector : Detector(), Detector.UastScanner {

    companion object {
        val ISSUE = Issue.create(
            id = "CodeStyleDetector",
            briefDescription = "命名不规范",
            explanation = """
                统一命名风格
            """.trimIndent(),
            category = Category.CORRECTNESS,
            priority = 8,
            severity = Severity.ERROR,
            implementation = Implementation(
                CodeStyleDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>>? {
        return listOf(UField::class.java, ULocalVariable::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return CodeStyleUastHandler(context)
    }

    class CodeStyleUastHandler(private val context: JavaContext) : UElementHandler() {

        override fun visitField(node: UField) {
            if (node.name.length >= 2
                && node.name[0].isLowerCase()
                && node.name[1].isUpperCase()
            ) {
                val newName = node.name[1].toLowerCase().toString() + node.name.subSequence(2, node.name.length)
                context.report(
                    ISSUE, context.getLocation(node), "变量命名应使用小驼峰命名法",
                    LintFix.create()
                        .name("删除第一个小写字母，并把其后的大写字母改成小写")
                        .replace()
                        .pattern(node.name)
                        .with(newName)
                        .build()
                )
            }
        }

        override fun visitLocalVariable(node: ULocalVariable) {
            if (node.name.length >= 2
                && node.name[0].isLowerCase()
                && node.name[1].isUpperCase()
            ) {
                val newName = node.name[1].toLowerCase().toString() + node.name.subSequence(2, node.name.length)
                context.report(
                    ISSUE,
                    context.getLocation(node.toUElement()!!),
                    "变量命名应使用小驼峰命名法",
                    LintFix.create()
                        .name("删除第一个小写字母，并把其后的大写字母改成小写")
                        .replace()
                        .pattern(node.name)
                        .with(newName)
                        .build()
                )
            }
        }
    }
}