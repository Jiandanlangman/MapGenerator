package com.jiandanlangman.mapgenerator

import com.jiandanlangman.mapgenerator.gui.MapGeneratorUIFrame
import com.jiandanlangman.mapgenerator.gui.TitleBarHeightMeasurer
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*

object MapGeneratorMain {

    private const val PARAM_NAME_NAME = "--name"
    private const val PARAM_NAME_TILE_SIZE = "--tileSize"
    private const val PARAM_NAME_OUTPUT_DIR = "--output"
    private const val PARAM_NAME_SOURCES = "--sources"
    private const val PARAM_NAME_HELP = "--help"

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.any { it.contains(PARAM_NAME_HELP) }) {
            printHelp()
            System.exit(0)
            return
        }
        var name = ""
        var tileSize = 256
        var output = ""
        val sources = ArrayList<String>()
        var i = 0
        while (i < args.size) {
            val valueIndex = i + 1
            if (valueIndex < args.size)
                when (args[i]) {
                    PARAM_NAME_NAME -> name = args[valueIndex].trim()
                    PARAM_NAME_TILE_SIZE -> tileSize = args[valueIndex].trim().toInt()
                    PARAM_NAME_OUTPUT_DIR -> output = File(args[valueIndex].trim()).canonicalFile.absoluteFile.absolutePath
                    PARAM_NAME_SOURCES -> args[valueIndex].trim().split(",").forEach { sources.add(File(it).canonicalFile.absoluteFile.path) }
                }
            i += 2
        }
        if (output == "" && sources.isNotEmpty())
            output = File(sources[0]).canonicalFile.absoluteFile.parentFile.absolutePath
        if (name != "" && sources.isNotEmpty()) {
            System.out.println("以文本界面模式运行...")
            var sourceFiles: File? = null
            sources.filter { File(it).name == "source.files" }.forEach { sourceFiles = File(it).canonicalFile.absoluteFile }
            val ss = if (sourceFiles != null) {
                val list = ArrayList<String>()
                val br = BufferedReader(FileReader(sourceFiles))
                while (true) {
                    val str = br.readLine() ?: break
                    list.add(File(sourceFiles!!.parentFile, str).absolutePath)
                }
                list.toTypedArray()
            } else
                sources.toTypedArray()
            MapGenerator(name, tileSize, output, *ss).generator {
                System.out.println(it)
                when (it) {
                    -1 -> System.out.print("参数错误，地图制作失败！")
                    100 -> System.out.print("地图制作成功！")
                }
            }
        } else {
            System.out.println("以图形界面模式运行...")
            TitleBarHeightMeasurer.measure {
                MapGeneratorUIFrame(it).isVisible = true
            }
        }
    }

    private fun printHelp() {
        System.out.println("地图制作工具 v${Constants.VERSION}")
        System.out.println("用法：java -jar <当前程序的名称> $PARAM_NAME_SOURCES <源文件路径> [$PARAM_NAME_OUTPUT_DIR <输出目录>] [$PARAM_NAME_NAME <地图名称>] [$PARAM_NAME_TILE_SIZE <瓦片大小>]")
        System.out.println("用法示例：")
        System.out.println("  java -jar MapGenerator.jar $PARAM_NAME_SOURCES /home/test/1.png")
        System.out.println("  java -jar MapGenerator.jar $PARAM_NAME_SOURCES /home/test/1.png $PARAM_NAME_NAME test")
        System.out.println("参数说明：")
        System.out.println("  $PARAM_NAME_SOURCES: 源文件路径，必须是png或jpg格式的图片。")
        System.out.println("  $PARAM_NAME_OUTPUT_DIR: 输出路径，可省略，省略时输出目录默认采用和源文件相同目录。")
        System.out.println("  $PARAM_NAME_NAME: 地图名，可省略，省略时默认采用源文件的文件名。")
        System.out.println("  $PARAM_NAME_TILE_SIZE: 瓦片大小，值为64, 128, 256, 512中的一个，可省略，默认值为256。")
    }

}