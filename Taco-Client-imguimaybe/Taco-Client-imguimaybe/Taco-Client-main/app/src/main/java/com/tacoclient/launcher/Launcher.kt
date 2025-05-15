package com.tacoclient.launcher

import android.content.Context
import android.content.pm.PackageManager.GET_META_DATA
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Objects
import java.util.concurrent.Executors
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

const val LAUNCHER = "launcher.dex"
const val LIBS = "lib/arm64"
data class McInfo(val apks: List<String>, val nativeLibraryDir: String? = null)

private fun copyFile(from: InputStream, to: File): File {
    to.parentFile?.takeIf { !it.exists() }?.also { require(it.mkdirs()) { "Failed to create directories: ${it.absolutePath}" } }
    from.use { Files.copy(it, to.toPath(), StandardCopyOption.REPLACE_EXISTING) }
    return to
}

fun Context.prepareLauncher(onFinish: (ArrayList<String>) -> Unit = {}) {
    Executors.newSingleThreadExecutor().execute {
        try {

            val appInfo = packageManager.getApplicationInfo("com.mojang.minecraftpe", GET_META_DATA)
            val mcInfo = McInfo(
                mutableListOf<String>().apply {
                    add(appInfo.sourceDir)
                    appInfo.splitSourceDirs?.let { addAll(it) }
                },
                appInfo.nativeLibraryDir
            )

            val libApk = mcInfo.apks.firstOrNull { ZipFile(it).contains(LIBS) }
            if (libApk == null) throw Exception("Failed to load Minecraft: Not arm64-v8a")
            val baseApk = mcInfo.apks.firstOrNull { ZipFile(it).contains("classes.dex") }

            val cacheDexDir = File(codeCacheDir, "dex").apply { deleteRecursively() }
            val pathList = Objects.requireNonNull(classLoader.javaClass.superclass).getDeclaredField("pathList").apply { isAccessible = true }[classLoader]
            val addDexPath = pathList.javaClass.getDeclaredMethod("addDexPath", String::class.java, File::class.java)
            val addNativePath = pathList.javaClass.getDeclaredMethod("addNativePath", MutableCollection::class.java)
            val nativeDir = mcInfo.nativeLibraryDir

            (if (nativeDir == null || (File(nativeDir).listFiles().isNullOrEmpty())) {
                ZipFile(libApk).runOperation({ !it.isDirectory && it.name.startsWith(LIBS) }) { entry, zipFile ->
                    if (!copyFile(zipFile.getInputStream(entry), File(codeCacheDir, entry.name)).exists()) throw Exception("Failed to extract ${entry.name}")
                }
                codeCacheDir.resolve(LIBS).absolutePath
            } else nativeDir).run {
                addNativePath.invoke(pathList, listOf(this))
            }

            copyFile(assets.open(LAUNCHER), File(cacheDexDir, LAUNCHER)).run {
                if (setReadOnly()) {
                    addDexPath.invoke(pathList, absolutePath, null)
                }
            }

            ZipFile(baseApk).runOperation({ it.name.endsWith(".dex") && !it.name.contains("/") }) { entry, zipFile ->
                copyFile(zipFile.getInputStream(entry), File(cacheDexDir, entry.name)).run {
                    if (setReadOnly()) {
                        addDexPath.invoke(pathList, absolutePath, null)
                    }
                }
            }
            onFinish(ArrayList(mcInfo.apks))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun ZipFile.contains(entry: String): Boolean = use { entries().toList().firstOrNull { it.name.startsWith(entry) } != null }
inline fun ZipFile.runOperation(noinline condition: (ZipEntry) -> Boolean, operation: (ZipEntry, ZipFile) -> Unit) = with(this) {
    entries().asSequence().filter(condition).forEach { operation(it, this) }
}