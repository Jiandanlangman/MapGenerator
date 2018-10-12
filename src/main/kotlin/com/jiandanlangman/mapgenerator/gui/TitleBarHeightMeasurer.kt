package com.jiandanlangman.mapgenerator.gui

import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import javax.swing.JFrame

object TitleBarHeightMeasurer {

    private var titleBarHeight = 0

    fun measure(measureCallback: (height: Int) -> Unit) {
        if (titleBarHeight != 0)
            measureCallback.invoke(titleBarHeight)
        else {
            val frame = JFrame()
            frame.isResizable = false
            frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
            frame.contentPane.setSize(100, 0)
            frame.addComponentListener(object : ComponentListener {
                override fun componentMoved(e: ComponentEvent?) {

                }

                override fun componentResized(e: ComponentEvent?) {

                    if (titleBarHeight == 0 && frame.height != 0) {
                        titleBarHeight = frame.height
                        frame.dispose()
                        measureCallback.invoke(titleBarHeight)
                    }
                }

                override fun componentHidden(e: ComponentEvent?) {

                }

                override fun componentShown(e: ComponentEvent?) {

                }

            })
            frame.isVisible = true
        }
    }

}