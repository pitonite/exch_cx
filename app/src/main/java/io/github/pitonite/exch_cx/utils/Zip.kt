package io.github.pitonite.exch_cx.utils

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

fun zipFiles(files: Array<File>, outputZip: OutputStream) {
  ZipOutputStream(BufferedOutputStream(outputZip)).use { zipOutputStream ->
    for (file in files) {
      zipFile(file, file.name, zipOutputStream)
    }
  }
}

@Throws(IOException::class)
private fun zipFile(fileToZip: File, fileName: String, zipOut: ZipOutputStream) {
  if (fileToZip.isHidden) {
    return
  }
  if (fileToZip.isDirectory) {
    if (fileName.endsWith("/")) {
      zipOut.putNextEntry(ZipEntry(fileName))
      zipOut.closeEntry()
    } else {
      zipOut.putNextEntry(ZipEntry("$fileName/"))
      zipOut.closeEntry()
    }
    val children = fileToZip.listFiles()
    if (children != null) {
      for (childFile in children) {
        zipFile(childFile, fileName + "/" + childFile.name, zipOut)
      }
    }
    return
  }
  FileInputStream(fileToZip).use { fis ->
    val zipEntry = ZipEntry(fileName)
    zipOut.putNextEntry(zipEntry)
    val bytes = ByteArray(1024)
    var length: Int
    while ((fis.read(bytes).also { length = it }) >= 0) {
      zipOut.write(bytes, 0, length)
    }
  }
}

fun extractZip(inputStream: InputStream, destinationDir: File) {
  ZipInputStream(BufferedInputStream(inputStream)).use { zipStream ->
    var zipEntry = zipStream.nextEntry
    val buffer = ByteArray(1024)

    while (zipEntry != null) {
      val newFile = resolveZipEntry(destinationDir, zipEntry)

      if (zipEntry.isDirectory) {
        if (!newFile.isDirectory && !newFile.mkdirs()) {
          throw IOException("Failed to create directory $newFile")
        }
      } else {
        // fix for Windows-created archives
        val parent = newFile.parentFile
        if (parent == null || (!parent.isDirectory && !parent.mkdirs())) {
          throw IOException("Failed to create directory $parent")
        }

        // write file content
        FileOutputStream(newFile).use { fos ->
          var len: Int
          while ((zipStream.read(buffer).also { len = it }) > 0) {
            fos.write(buffer, 0, len)
          }
        }
      }
      zipEntry = zipStream.nextEntry
    }
  }
}

@Throws(IOException::class)
private fun resolveZipEntry(destinationDir: File, zipEntry: ZipEntry): File {
  val destFile = File(destinationDir, zipEntry.name)

  val destDirPath = destinationDir.canonicalPath
  val destFilePath = destFile.canonicalPath

  if (!destFilePath.startsWith(destDirPath + File.separator)) {
    throw IOException("Entry is outside of the target dir: " + zipEntry.name)
  }

  return destFile
}
