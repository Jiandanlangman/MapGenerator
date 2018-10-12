package com.jiandanlangman.mapgenerator

import java.awt.Image
import java.io.File
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


class MapGenerator(private val mapName: String, private val tileSize: Int, private val outputDir: String, private vararg val sourceFiles: String) {


    companion object {
        private val MESSAGE_DIGEST = MessageDigest.getInstance("MD5")
        private val CHARSET = Charset.forName("UTF-8")

        @Synchronized
        private fun hashCode(data: String): String {
            val md5 = MESSAGE_DIGEST.digest(data.toByteArray(CHARSET))
            val hex = StringBuilder()
            for (b in md5) {
                var n = b.toInt()
                if (n < 0)
                    n += 256
                val d1 = n / 16
                val d2 = n % 16
                hex.append(Integer.toHexString(d1))
                hex.append(Integer.toHexString(d2))
            }
            return hex.toString()
        }

    }


    @Synchronized
    fun generator(onProgressChangeListener: ((progressOrErrorCode: Int) -> Unit)?) {
        if (checkParameters()) {
            var progress = 0
            val levelImages = ArrayList<BufferedImage>()
            sourceFiles.forEach {
                val levelImage = ImageIO.read(File(it))
                if (Math.max(levelImage.width, levelImage.height) >= tileSize) {
                    levelImages.add(levelImage)
                    progress++
                    onProgressChangeListener?.invoke(progress)
                }
            }
            val lastImage = levelImages.last()
            val lastSize = Math.max(lastImage.width, lastImage.height)
            if (lastSize >= tileSize) {
                var inSimpleLevelCount = 1
                while (true) {
                    val inSampleSize = Math.pow(2.0, inSimpleLevelCount.toDouble()).toInt()
                    if (lastSize / inSampleSize < tileSize)
                        break
                    val levelImageWidth = lastImage.width / inSampleSize
                    val levelImageHeight = lastImage.height / inSampleSize
                    val levelImage = BufferedImage(levelImageWidth, levelImageHeight,
                            BufferedImage.TYPE_INT_ARGB)
                    levelImage.graphics.drawImage(
                            lastImage.getScaledInstance(levelImageWidth, levelImageHeight, Image.SCALE_SMOOTH), 0, 0, null)
                    levelImages.add(levelImage)
                    inSimpleLevelCount++
                    progress++
                    onProgressChangeListener?.invoke(progress)
                }
            }
            var tileCount = 0
            levelImages.forEach {
                tileCount += Math.ceil(it.width.toDouble() / tileSize).toInt() * Math.ceil(it.height.toDouble() / tileSize).toInt()
            }
            val tempFile = File(outputDir, ".__$mapName.tmp__")
            if (tempFile.exists())
                tempFile.delete()
            val tzos = ZipOutputStream(FileOutputStream(tempFile))
            val resEntryName = "res" + File.separator
            val resEntry = ZipEntry(resEntryName)
            tzos.putNextEntry(resEntry)
            tzos.closeEntry()
            val configBuilder = StringBuffer()
            configBuilder.append("{")
            val separator = if (System.getProperty("os.name").startsWith("windows", true)) "\\\\" else "/"
            configBuilder.append("\"separator\":\"$separator\"")
            configBuilder.append(",")
            configBuilder.append("\"name\":\"$mapName\"")
            configBuilder.append(",")
            configBuilder.append("\"tileSize\":$tileSize")
            configBuilder.append(",")
            val sourceImage = ImageIO.read(File(sourceFiles[0]))
            val sourceWidth = sourceImage.width
            val sourceHeight = sourceImage.height
            configBuilder.append("\"sourceWidth\":$sourceWidth")
            configBuilder.append(",")
            configBuilder.append("\"sourceHeight\":$sourceHeight")
            configBuilder.append(",")
            val levelCount = levelImages.size
            configBuilder.append("\"levelCount\":$levelCount")
            configBuilder.append(",")
            configBuilder.append("\"levelsInfo\":{")
            var generatorTileCount = 0
            for (level in 0 until levelCount) {
                val key = levelCount - 1 - level
                val resLevelEntryName = resEntryName + hashCode("$mapName-$key") + File.separator
                val resLevelEntry = ZipEntry(resLevelEntryName)
                tzos.putNextEntry(resLevelEntry)
                tzos.closeEntry()
                configBuilder.append("\"$key\":{")
                val levelImage = levelImages[level]
                val levelImageWidth = levelImage.width
                val levelImageHeight = levelImage.height
                val horizontalTileCount = Math.ceil(levelImageWidth.toDouble() / tileSize).toInt()
                val verticalTileCount = Math.ceil(levelImageHeight.toDouble() / tileSize).toInt()
                configBuilder.append("\"width\":$levelImageWidth")
                configBuilder.append(",")
                configBuilder.append("\"height\":$levelImageHeight")
                configBuilder.append(",")
                configBuilder.append("\"horizontalTileCount\":$horizontalTileCount")
                configBuilder.append(",")
                configBuilder.append("\"verticalTileCount\":$verticalTileCount")
                configBuilder.append("}")
                if (level != levelCount - 1)
                    configBuilder.append(",")
                for (y in 0 until verticalTileCount)
                    for (x in 0 until horizontalTileCount) {
                        val cropX = x * tileSize
                        val cropY = y * tileSize
                        val cropWidth = if (cropX + tileSize <= levelImageWidth)
                            tileSize
                        else
                            tileSize - (cropX + tileSize - levelImageWidth)
                        val cropHeight = if (cropY + tileSize <= levelImageHeight)
                            tileSize
                        else
                            tileSize - (cropY + tileSize - levelImageHeight)
                        val bi = BufferedImage(cropWidth, cropHeight, BufferedImage.TYPE_INT_ARGB)
                        bi.graphics.drawImage(levelImage.getSubimage(cropX, cropY, cropWidth, cropHeight), 0, 0, null)
                        val tileEntryName = resLevelEntryName + hashCode("$mapName-$y-$x") + ".res"
                        val tileEntry = ZipEntry(tileEntryName)
                        tzos.putNextEntry(tileEntry)
                        val baos = ByteArrayOutputStream()
                        ImageIO.write(bi, "png", baos)
                        tzos.write(baos.toByteArray())
                        tzos.closeEntry()
                        baos.reset()
                        baos.close()
                        generatorTileCount++
                        onProgressChangeListener?.invoke((generatorTileCount.toDouble() / tileCount * (98 - progress)).toInt() + progress)
                        System.gc()
                    }
            }
            configBuilder.append("}")
            configBuilder.append("}")
            val sb = StringBuilder()
            configBuilder.toString().forEach {
                sb.append((it.toInt() * it.toInt()).toChar())
            }
            val configEntry = ZipEntry("config.dat")
            tzos.putNextEntry(configEntry)
            tzos.write(sb.toString().toByteArray())
            tzos.closeEntry()
            tzos.flush()
            tzos.finish()
            tzos.close()
            val outputFile = File(outputDir, "$mapName.map")
            if (outputFile.exists())
                outputFile.delete()
            tempFile.renameTo(outputFile)
            onProgressChangeListener?.invoke(100)
            levelImages.clear()
            System.gc()
        } else
            onProgressChangeListener?.invoke(ErrorCode.PARAMETERS_IS_WRONG)
    }


    private fun checkParameters(): Boolean {
        if (mapName.trim() == "")
            return false
        if (tileSize < 64 || tileSize > 768)
            return false
        val outputDirFile = File(outputDir)
        if (outputDirFile.exists() && !outputDirFile.isDirectory)
            return false
        if (!outputDirFile.exists())
            outputDirFile.mkdirs()
        if (sourceFiles.isEmpty())
            return false
        sourceFiles.forEach {
            if (!File(it).exists())
                return false
        }
        return true
    }


    object ErrorCode {
        const val PARAMETERS_IS_WRONG = -1
    }


}