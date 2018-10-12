package com.jiandanlangman.mapgenerator.gui

import com.jiandanlangman.mapgenerator.Constants
import java.awt.*
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel

class AboutDialog(owner: Frame) : JDialog(owner, "关于", true) {

    init {
        val height = owner.height / 2
        val width = (height / 9f * 16f).toInt()
        setSize(width, height)
        setLocationRelativeTo(null)
        defaultCloseOperation = JDialog.HIDE_ON_CLOSE
        contentPane.layout = BorderLayout()
        contentPane.add(createBottomBar(), BorderLayout.SOUTH)
        contentPane.add(createContentPanel(), BorderLayout.CENTER)
    }

    private fun createContentPanel(): JPanel {
        val panel = JPanel()
        panel.layout = FlowLayout(FlowLayout.LEFT)
        val marginLabel = JLabel()
        marginLabel.preferredSize = Dimension(width, Dimens.margin)
        panel.add(marginLabel)
        val appNameLabel = JLabel("    地图制作工具")
            appNameLabel.horizontalAlignment = JLabel.LEFT
        appNameLabel.verticalAlignment = JLabel.CENTER
        appNameLabel.preferredSize = Dimension(width, (22f * Dimens.dpi + .5f).toInt())
        appNameLabel.font = Font.FONT_DIALOG_BLOD
        panel.add(appNameLabel)

        val versionLabel = JLabel("    v${Constants.VERSION}")
        versionLabel.horizontalAlignment = JLabel.LEFT
        versionLabel.verticalAlignment = JLabel.CENTER
        versionLabel.preferredSize = Dimension(width, (22f * Dimens.dpi + .5f).toInt())
        versionLabel.font = Font.FONT_LABEL
        panel.add(versionLabel)

        val changeLogLabel = JLabel("    通用版本，适用于任何支持Java SE的操作系统。")
        changeLogLabel.horizontalAlignment = JLabel.LEFT
        changeLogLabel.verticalAlignment = JLabel.CENTER
        changeLogLabel.preferredSize = Dimension(width, (22f * Dimens.dpi + .5f).toInt())
        changeLogLabel.font = Font.FONT_LABEL
        panel.add(changeLogLabel)

        val updateTimeLabel = JLabel("${Constants.UPDATE_TIME}    ")
        updateTimeLabel.horizontalAlignment = JLabel.RIGHT
        updateTimeLabel.verticalAlignment = JLabel.CENTER
        updateTimeLabel.preferredSize = Dimension(width, (22f * Dimens.dpi + .5f).toInt())
        updateTimeLabel.font = Font.FONT_LABEL
        panel.add(updateTimeLabel)

        return panel
    }

    private fun createBottomBar(): JPanel {
        val panel = JPanel()
        panel.layout = FlowLayout(FlowLayout.RIGHT)
        panel.background = Color(222, 222, 222, 255)
        val okButton = JButton("确定")
        okButton.font = Font.FONT_BUTTON_TEXT
        okButton.preferredSize = Dimension((Dimens.buttonWidth / 5f * 4f).toInt(), (Dimens.viewHeight / 5f * 4f).toInt())
        okButton.addActionListener {
            isVisible = false
        }
        panel.add(okButton)
        return panel
    }
}