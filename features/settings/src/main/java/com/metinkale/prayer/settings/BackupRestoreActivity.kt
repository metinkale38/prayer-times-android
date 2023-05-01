/*
 * Copyright (c) 2013-2023 Metin Kale
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.metinkale.prayer.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.metinkale.prayer.App
import com.metinkale.prayer.CrashReporter.recordException
import com.metinkale.prayer.Module
import com.metinkale.prayer.dhikr.data.DhikrDatabase
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupRestoreActivity : AppCompatActivity() {
    public override fun onCreate(bdl: Bundle?) {
        super.onCreate(bdl)
        setContentView(R.layout.settings_backuprestore)
    }


    private val restore =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result?.data?.data?.also { uri ->
                    contentResolver?.openInputStream(uri)?.use { stream ->
                        DhikrDatabase.getDatabase(applicationContext).close()
                        var success = true

                        filesDir.listFiles()?.forEach { it.delete() }
                        File(filesDir.parentFile, "databases").listFiles()?.forEach { it.delete() }
                        File(filesDir.parentFile, "databases").listFiles()?.forEach { it.delete() }
                        val unzipPath = filesDir.parentFile
                        if (unzipPath != null) {
                            Zip.unzip(stream, unzipPath.absolutePath + "/")
                        }
                        System.exit(0)
                        Module.TIMES.launch(this@BackupRestoreActivity)

                    }
                }
            }
        }

    fun restore(@Suppress("UNUSED_PARAMETER") v: View?) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.type = "application/zip"

        restore.launch(intent)
    }


    private val backup =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result?.data?.data?.also { uri ->
                    contentResolver?.openOutputStream(uri)?.use { stream ->
                        val zip = Zip(stream)
                        var files = filesDir
                        if (files.exists() && files.isDirectory)
                            files.list()
                                ?.filter { !File(it).isDirectory }
                                ?.filter { !it.contains(".Fabric") }
                                ?.filter { !it.contains("leakcanary") }
                                ?.filter { !it.contains("ion") }
                                ?.filter { !it.contains("crashlytics") }
                                ?.filter { !it.contains("phenotype") }
                                ?.filter { !it.contains("google") }
                                ?.forEach { file ->
                                    zip.addFile("files", files.absolutePath + "/" + file)
                                }

                        files = File(files.parentFile, "databases")
                        if (files.exists() && files.isDirectory)
                            files.list()
                                ?.filter { !it.contains("evernote") }
                                ?.filter { !it.contains("google") }
                                ?.forEach {
                                    zip.addFile("databases", files.absolutePath + "/" + it)
                                }


                        files = File(files.parentFile, "shared_prefs")
                        if (files.exists() && files.isDirectory)
                            files.list()
                                ?.filter { !it.contains("evernote") }
                                ?.filter { !it.contains("google") }
                                ?.forEach {
                                    zip.addFile("shared_prefs", files.absolutePath + "/" + it)
                                }
                        zip.closeZip()


                    }
                }
            }
        }

    fun backup(@Suppress("UNUSED_PARAMETER") v: View?) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        intent.type = "application/zip"
        intent.putExtra(
            Intent.EXTRA_TITLE,
            "com.metinkale.prayer." + DateFormat.format("yyyy-MM-dd HH.mm.ss", Date()) + ".zip"
        );

        backup.launch(intent)
    }

    class Zip(outputStream: OutputStream) {
        var out: ZipOutputStream
        var data: ByteArray

        init {
            out = ZipOutputStream(BufferedOutputStream(outputStream))
            data = ByteArray(BUFFER)
        }

        @Throws(IOException::class)
        fun addFile(folder: String?, name: String) {
            val fi = FileInputStream(name)
            val origin = BufferedInputStream(fi, BUFFER)
            val entry = ZipEntry(
                (if (folder == null) "" else "$folder/") + name.substring(
                    name.lastIndexOf("/") + 1
                )
            )
            out.putNextEntry(entry)
            var count: Int
            while (origin.read(data, 0, BUFFER).also { count = it } != -1) {
                out.write(data, 0, count)
            }
            origin.close()
        }

        @Throws(IOException::class)
        fun closeZip() {
            out.close()
        }

        companion object {
            const val BUFFER = 2048
            fun unzip(inputStream: InputStream, to: String): Boolean {
                try {
                    ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
                        var ze: ZipEntry?
                        while (true) {
                            ze = zis.nextEntry
                            if (ze == null) break
                            val baos = ByteArrayOutputStream()
                            val buffer = ByteArray(1024)
                            var count: Int
                            val filename = ze.name
                            File(to + filename).parentFile?.mkdirs()
                            try {
                                FileOutputStream(to + filename).use { fout ->
                                    // reading and writing
                                    while (zis.read(buffer).also { count = it } != -1) {
                                        baos.write(buffer, 0, count)
                                        val bytes = baos.toByteArray()
                                        fout.write(bytes)
                                        baos.reset()
                                    }
                                }
                            } catch (e: IOException) {
                                Toast.makeText(App.get(), "Could not restore $filename",Toast.LENGTH_SHORT).show()
                                recordException(e)
                            }
                            zis.closeEntry()
                        }
                    }
                } catch (e: IOException) {
                    recordException(e)
                    return false
                }
                return true
            }
        }
    }
}