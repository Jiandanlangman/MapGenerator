package com.jiandanlangman.mapgenerator.gui

import java.awt.Toolkit

object Dimens {
     val dpi = Toolkit.getDefaultToolkit().screenResolution / 96f
     val viewHeight = (40f * dpi + .5f).toInt()
     val margin = (viewHeight / 4f + .5f).toInt()
     val buttonWidth = (80f * dpi + .5f).toInt()
     val menuBarHeight = (31 * dpi + .5f).toInt()
}