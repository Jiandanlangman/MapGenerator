package com.jiandanlangman.mapgenerator.gui

import com.jiandanlangman.mapgenerator.Constants
import com.jiandanlangman.mapgenerator.MapGenerator
import com.jiandanlangman.mapgenerator.model.Version
import com.jiandanlangman.mapgenerator.util.JSONUtil
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.concurrent.Executors
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class MapGeneratorUIFrame(titleBarHeight:Int) : JFrame("地图制作工具v${Constants.VERSION}") {


    private val generatorMapThreadPool = Executors.newFixedThreadPool(1)

    private val sourceFilesChooser by lazy {
        val sourceFilesChooser = JFileChooser()
        sourceFilesChooser.fileSelectionMode = JFileChooser.FILES_ONLY
        sourceFilesChooser.dialogTitle = "选择源文件"
        val filters = sourceFilesChooser.choosableFileFilters
        for (filter in filters) {
            sourceFilesChooser.removeChoosableFileFilter(filter)
        }
        val sourceFilesFilter = SourceFilesFilter()
        sourceFilesChooser.addChoosableFileFilter(sourceFilesFilter)
        sourceFilesChooser.fileFilter = sourceFilesFilter
        updateFont(sourceFilesChooser, Font.FONT_DIALOG)
        sourceFilesChooser
    }

    private val outputDirFileChooser by lazy {
        val outputDirFileChooser = JFileChooser()
        outputDirFileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        outputDirFileChooser.dialogTitle = "选择输出目录"
        updateFont(outputDirFileChooser, Font.FONT_DIALOG)
        outputDirFileChooser
    }

    private val aboutDialog by lazy {
        AboutDialog(this)
    }

    private lateinit var sourceFilesField: JTextField
    private lateinit var outputDirField: JTextField
    private lateinit var tileSizeComboBox: JComboBox<Int>
    private lateinit var mapNameFiled: JTextField
    private lateinit var generatorButton: JButton
    private lateinit var progressBar: JProgressBar
    private lateinit var openSourceFileButton: JButton
    private lateinit var selectOutputDirButton: JButton

    private var isGenerating = false

    init {
        System.setProperty("awt.useSystemAAFontSettings", "on")
        System.setProperty("swing.aatext", "true")
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        val height = titleBarHeight + Dimens.menuBarHeight  + Dimens.margin * 2 * 6 + Dimens.viewHeight * 5 + Dimens.menuBarHeight
        val width = (height / 9f * 16f).toInt()
        setSize(width, height)
        setLocationRelativeTo(null)
        isResizable = false
        defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) = windowClosing()
        })
        initMenuBar()
        initContentPanel()
    }

    private fun initMenuBar() {
        val menuBar = JMenuBar()
        menuBar.preferredSize = Dimension(width, Dimens.menuBarHeight)
        val fileMenu = JMenu("文件")
        fileMenu.font = Font.FONT_MENU
        val selectSourceFilesMenuItem = JMenuItem("选择源文件")
        selectSourceFilesMenuItem.font = Font.FONT_MENU_ITEM
        selectSourceFilesMenuItem.addActionListener {
            chooseSourceFile()
        }
        fileMenu.add(selectSourceFilesMenuItem)
        val selectOutputDirMenuItem = JMenuItem("选择输出目录")
        selectOutputDirMenuItem.font = Font.FONT_MENU_ITEM
        selectOutputDirMenuItem.addActionListener {
            chooseOutputDir()
        }
        fileMenu.add(selectOutputDirMenuItem)
        val resetTileSizeMenuItem = JMenuItem("重置瓦片大小")
        resetTileSizeMenuItem.font = Font.FONT_MENU_ITEM
        resetTileSizeMenuItem.addActionListener {
            tileSizeComboBox.selectedIndex = 2
        }
        fileMenu.add(resetTileSizeMenuItem)
        val exitMenuItem = JMenuItem("退出")
        exitMenuItem.font = Font.FONT_MENU_ITEM
        exitMenuItem.addActionListener {
            windowClosing()
        }
        fileMenu.add(exitMenuItem)
        menuBar.add(fileMenu)
        val helpMenu = JMenu("帮助")
        helpMenu.font = Font.FONT_MENU
        val checkUpdateItem = JMenuItem("检查更新")
        checkUpdateItem.font = Font.FONT_MENU_ITEM
        checkUpdateItem.addActionListener {
            checkUpdate()
        }
        helpMenu.add(checkUpdateItem)
        val changeLogItem = JMenuItem("更新日志")
        changeLogItem.font = Font.FONT_MENU_ITEM
        changeLogItem.addActionListener {
            Desktop.getDesktop().browse(URI.create("http://101.132.235.215/ManagementServer/maplibrary/mapGeneratorChangeLog"))
        }
        helpMenu.add(changeLogItem)
        val aboutMenuItem = JMenuItem("关于")
        aboutMenuItem.font = Font.FONT_MENU_ITEM
        aboutMenuItem.addActionListener {
            aboutDialog.isVisible = true
        }
        helpMenu.add(aboutMenuItem)
        menuBar.add(helpMenu)
        jMenuBar = menuBar
    }

    private fun initContentPanel() {
        val contentPanel = contentPane
        contentPanel.layout = FlowLayout(FlowLayout.CENTER)
        val contentBox = Box.createVerticalBox()
        add(contentBox)
        val label1 = JLabel()
        label1.preferredSize = Dimension(Dimens.margin, Dimens.margin * 2)
        contentBox.add(label1)
        contentBox.add(createSourceFileBox())
        val label2 = JLabel()
        label2.preferredSize = Dimension(Dimens.margin, Dimens.margin * 2)
        contentBox.add(label2)
        contentBox.add(createOutputDirBox())
        val label3 = JLabel()
        label3.preferredSize = Dimension(Dimens.margin, Dimens.margin * 2)
        contentBox.add(label3)
        contentBox.add(createTileSizeBox())
        val label4 = JLabel()
        label4.preferredSize = Dimension(Dimens.margin, Dimens.margin * 2)
        contentBox.add(label4)
        contentBox.add(createMapNameBox())
        val label5 = JLabel()
        label5.preferredSize = Dimension(Dimens.margin, Dimens.margin * 2)
        contentBox.add(label5)
        generatorButton = JButton("      制作      ")
        generatorButton.font = Font.FONT_BUTTON_TEXT
        generatorButton.preferredSize = Dimension(Dimens.buttonWidth, Dimens.viewHeight)
        generatorButton.isEnabled = false
        generatorButton.addActionListener {
            generatorMap()
        }
        contentBox.add(generatorButton)
        val label6 = JLabel()
        label6.preferredSize = Dimension(Dimens.margin, Dimens.margin * 2)
        contentBox.add(label6)
        contentPanel.add(createBottomBar())
    }

    private fun createSourceFileBox(): Box {
        val box = Box.createHorizontalBox()
        val sourceFileLabel = JLabel("源文件")
        sourceFileLabel.font = Font.FONT_LABEL
        sourceFileLabel.horizontalAlignment = JLabel.RIGHT
        sourceFileLabel.verticalAlignment = JLabel.CENTER
        sourceFileLabel.preferredSize = Dimension(Dimens.buttonWidth, Dimens.viewHeight)
        box.add(sourceFileLabel)
        val marginLabel1 = JLabel()
        marginLabel1.preferredSize = Dimension(Dimens.margin, Dimens.margin)
        box.add(marginLabel1)
        sourceFilesField = JTextField()
        sourceFilesField.isEnabled = false
        sourceFilesField.font = Font.FONT_LABEL
        sourceFilesField.preferredSize = Dimension(width / 5 * 3, Dimens.viewHeight)
        box.add(sourceFilesField)
        val marginLabel2 = JLabel()
        marginLabel2.preferredSize = Dimension(Dimens.margin, Dimens.margin)
        box.add(marginLabel2)
        openSourceFileButton = JButton("打开")
        openSourceFileButton.font = Font.FONT_BUTTON_TEXT
        openSourceFileButton.preferredSize = Dimension(Dimens.buttonWidth, Dimens.viewHeight)
        openSourceFileButton.addActionListener {
            chooseSourceFile()
        }
        box.add(openSourceFileButton)
        return box
    }

    private fun createOutputDirBox(): Box {
        val box = Box.createHorizontalBox()
        val outputDirLabel = JLabel("输出目录")
        outputDirLabel.font = Font.FONT_LABEL
        outputDirLabel.horizontalAlignment = JLabel.RIGHT
        outputDirLabel.verticalAlignment = JLabel.CENTER
        outputDirLabel.preferredSize = Dimension(Dimens.buttonWidth, Dimens.viewHeight)
        box.add(outputDirLabel)
        val marginLabel1 = JLabel()
        marginLabel1.preferredSize = Dimension(Dimens.margin, Dimens.margin)
        box.add(marginLabel1)
        outputDirField = JTextField()
        outputDirField.isEnabled = false
        outputDirField.font = Font.FONT_LABEL
        outputDirField.preferredSize = Dimension(width / 5 * 3, Dimens.viewHeight)
        box.add(outputDirField)
        val marginLabel2 = JLabel()
        marginLabel2.preferredSize = Dimension(Dimens.margin, Dimens.margin)
        box.add(marginLabel2)
        selectOutputDirButton = JButton("选择")
        selectOutputDirButton.font = Font.FONT_BUTTON_TEXT
        selectOutputDirButton.preferredSize = Dimension(Dimens.buttonWidth, Dimens.viewHeight)
        selectOutputDirButton.addActionListener {
            chooseOutputDir()
        }
        box.add(selectOutputDirButton)
        return box
    }

    private fun createMapNameBox(): Box {
        val box = Box.createHorizontalBox()
        val mapNameLabel = JLabel("地图名称")
        mapNameLabel.font = Font.FONT_LABEL
        mapNameLabel.horizontalAlignment = JLabel.RIGHT
        mapNameLabel.verticalAlignment = JLabel.CENTER
        mapNameLabel.preferredSize = Dimension(Dimens.buttonWidth, Dimens.viewHeight)
        box.add(mapNameLabel)
        val marginLabel1 = JLabel()
        marginLabel1.preferredSize = Dimension(Dimens.margin, Dimens.margin)
        box.add(marginLabel1)
        mapNameFiled = JTextField()
        mapNameFiled.font = Font.FONT_LABEL
        mapNameFiled.preferredSize = Dimension(width / 5 * 3, Dimens.viewHeight)
        mapNameFiled.document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(e: DocumentEvent?) = checkDatas()

            override fun insertUpdate(e: DocumentEvent?) = checkDatas()

            override fun removeUpdate(e: DocumentEvent?) = checkDatas()
        })
        box.add(mapNameFiled)
        val marginLabel2 = JLabel()
        marginLabel2.preferredSize = Dimension(Dimens.margin, Dimens.margin)
        box.add(marginLabel2)
        val placeholder = JLabel()
        placeholder.preferredSize = Dimension(Dimens.buttonWidth, Dimens.margin)
        box.add(placeholder)
        return box
    }

    private fun createTileSizeBox(): Box {
        val box = Box.createHorizontalBox()
        val tileSize = JLabel("瓦片大小")
        tileSize.font = Font.FONT_LABEL
        tileSize.horizontalAlignment = JLabel.RIGHT
        tileSize.verticalAlignment = JLabel.CENTER
        tileSize.preferredSize = Dimension(Dimens.buttonWidth, Dimens.viewHeight)
        box.add(tileSize)
        val marginLabel1 = JLabel()
        marginLabel1.preferredSize = Dimension(Dimens.margin, Dimens.margin)
        box.add(marginLabel1)
        tileSizeComboBox = JComboBox()
        tileSizeComboBox.font = Font.FONT_LABEL
        tileSizeComboBox.preferredSize = Dimension(width / 5 * 3, Dimens.viewHeight)
        tileSizeComboBox.addItem(64)
        tileSizeComboBox.addItem(128)
        tileSizeComboBox.addItem(256)
        tileSizeComboBox.addItem(512)
        tileSizeComboBox.selectedIndex = 2
        box.add(tileSizeComboBox)
        val marginLabel2 = JLabel()
        marginLabel2.preferredSize = Dimension(Dimens.margin, Dimens.margin)
        box.add(marginLabel2)
        val placeholder = JLabel()
        placeholder.preferredSize = Dimension(Dimens.buttonWidth, Dimens.margin)
        box.add(placeholder)
        return box
    }

    private fun createBottomBar(): JComponent {
        val bottomBar = JPanel()
        bottomBar.background = Color(222, 222, 222, 255)
        bottomBar.layout = FlowLayout(FlowLayout.LEFT)
        bottomBar.preferredSize = Dimension(width, Dimens.menuBarHeight)
        val statusLabel = JLabel("  当前进度：")
        statusLabel.foreground = Color.BLACK
        statusLabel.font = Font.FONT_HINT
        bottomBar.add(statusLabel)
        progressBar = JProgressBar()
        progressBar.maximum = 100
        progressBar.minimum = 0
        progressBar.isStringPainted = true
        progressBar.font = Font.FONT_HINT
        progressBar.preferredSize = Dimension(128, Dimens.menuBarHeight / 2)
        bottomBar.add(progressBar)
        return bottomBar
    }


    private fun updateFont(comp: Component, font: java.awt.Font) {
        comp.font = font
        if (comp is Container)
            for (i in 0 until comp.componentCount)
                updateFont(comp.getComponent(i), font)
    }


    private fun chooseSourceFile() {
        sourceFilesChooser.selectedFile = File(sourceFilesField.text)
        val result = sourceFilesChooser.showDialog(this, "选择")
        if (result == JFileChooser.APPROVE_OPTION) {
            val sourceFile = sourceFilesChooser.selectedFile
            sourceFilesField.text = sourceFile.absolutePath
            outputDirField.text = sourceFile.parentFile.absolutePath
            val mapName: String
            if (sourceFile.name == "source.files") {
                mapName = "unDefined"
            } else {
                sourceFilesField.text = sourceFile.absolutePath
                mapName = sourceFile.name.substring(0, sourceFile.name.lastIndexOf("."))
            }
            mapNameFiled.text = mapName
            checkDatas()
        }
    }

    private fun chooseOutputDir() {
        outputDirFileChooser.selectedFile = File(sourceFilesField.text)
        val result = outputDirFileChooser.showDialog(this, "选择")
        if (result == JFileChooser.APPROVE_OPTION) {
            outputDirField.text = outputDirFileChooser.selectedFile.absolutePath
            checkDatas()
        }
    }

    private fun checkDatas() {
        generatorButton.isEnabled = mapNameFiled.text.trim() != "" && sourceFilesField.text != ""
        progressBar.value = 0
    }

    private fun generatorMap() {
        openSourceFileButton.isEnabled = false
        selectOutputDirButton.isEnabled = false
        tileSizeComboBox.isEnabled = false
        mapNameFiled.isEnabled = false
        generatorButton.isEnabled = false
        isGenerating = true
        generatorMapThreadPool.execute {
            val sourceFile = File(sourceFilesField.text)
            val sourceFiles = if (sourceFile.name == "source.files") {
                val list = ArrayList<String>()
                val br = BufferedReader(FileReader(sourceFile))
                while (true) {
                    val str = br.readLine() ?: break
                    list.add(File(sourceFile.parentFile, str).absolutePath)
                }
                list.toTypedArray()
            } else arrayOf(sourceFile.absolutePath)
            MapGenerator(mapNameFiled.text, tileSizeComboBox.selectedItem as Int, outputDirField.text, *sourceFiles)
                    .generator {
                        if (it == 100 || it == MapGenerator.ErrorCode.PARAMETERS_IS_WRONG) {
                            if (it == MapGenerator.ErrorCode.PARAMETERS_IS_WRONG)
                                JOptionPane.showMessageDialog(this, "\n    参数错误，地图制作失败！\n", "提示", JOptionPane.ERROR_MESSAGE)
                            else {
                                progressBar.value = it
                                JOptionPane.showMessageDialog(this, "\n    地图制作成功！\n", "提示", JOptionPane.PLAIN_MESSAGE)
                            }
                            openSourceFileButton.isEnabled = true
                            selectOutputDirButton.isEnabled = true
                            tileSizeComboBox.isEnabled = true
                            mapNameFiled.isEnabled = true
                            generatorButton.isEnabled = true
                            isGenerating = false
                            return@generator
                        }
                        progressBar.value = it
                    }
        }
    }

    private fun windowClosing() {
        if (isGenerating) {
            if (JOptionPane.showConfirmDialog(this, "地图制作还没完成，确定关闭吗？", "提示", JOptionPane.YES_NO_OPTION) == 0)
                System.exit(0)
        } else
            System.exit(0)
    }

    private fun checkUpdate() {
        val version = try {
            val connection = URL("http://101.132.235.215/ManagementServer/maplibrary/mapGeneratorCheckUpdate").openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doInput = true
            val baos = ByteArrayOutputStream()
            val `is` = connection.inputStream
            val buffer = ByteArray(8196)
            while (true) {
                val readLength = `is`.read(buffer)
                if (readLength == -1)
                    break
                baos.write(buffer, 0, readLength)
            }
            `is`.close()
            connection.disconnect()
            val version = JSONUtil.fromJSON(String(baos.toByteArray()), Version::class.java)
            baos.reset()
            baos.close()
            version
        } catch (ignore: Exception) {
            Version()
        }
        if (version.errorCode == 0 && version.data.versionCode > Constants.VERSION_CODE && version.data.url.isNotEmpty()) {
            if (JOptionPane.showConfirmDialog(this, "发现新版本，需要立即下载更新吗？", "版本更新", JOptionPane.YES_NO_OPTION) == 0)
                Desktop.getDesktop().browse(URI.create(version.data.url))
        } else
            JOptionPane.showMessageDialog(this, "\n    当前已是最新版本！\n", "版本更新", JOptionPane.PLAIN_MESSAGE)

    }

}