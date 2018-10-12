package com.jiandanlangman.mapgenerator.gui

import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.Toolkit

object Font {

    private val dpi = Toolkit.getDefaultToolkit().screenResolution / 96f

    val fontName = when {
        hasFont("Microsoft YaHei UI") -> "Microsoft YaHei UI"
        hasFont("Microsoft YaHei") -> "Microsoft YaHei"
        hasFont("微软雅黑") -> "微软雅黑"
        hasFont("Noto Sans Mono CJK SC Regular") -> "Noto Sans Mono CJK SC Regular"
        hasFont("宋体") -> "宋体"
        else -> Font.SANS_SERIF
    }

    val FONT_MENU = Font(fontName, Font.PLAIN, (16.toFloat() * dpi + 0.5f).toInt())
    val FONT_MENU_ITEM = Font(fontName, Font.PLAIN, (14.toFloat() * dpi + 0.5f).toInt())
    val FONT_HINT = Font(fontName, Font.PLAIN, (12.toFloat() * dpi + 0.5f).toInt())
    val FONT_LABEL = Font(fontName, Font.PLAIN, (14.toFloat() * dpi + 0.5f).toInt())
    val FONT_BUTTON_TEXT = Font(fontName, Font.PLAIN, (14.toFloat() * dpi + 0.5f).toInt())
    val FONT_DIALOG = Font(fontName, Font.PLAIN, (16.toFloat() * dpi + 0.5f).toInt())
    val FONT_DIALOG_BLOD = Font(fontName, Font.BOLD, (16.toFloat() * dpi + 0.5f).toInt())

    private fun hasFont(fontName: String): Boolean {
        GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames.forEach {
            if (it == fontName)
                return true
        }
        return false
    }
}