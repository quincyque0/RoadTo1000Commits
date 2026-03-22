package com.example.segunda_etapa.Supp

import android.content.Context
import android.os.Environment
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException

fun saveJSON(filename: String, data: JSONObject, context: Context): Boolean {
    return try {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }

        val file = File(downloadDir, "$filename.json")

        val existingData = JSONArray()
        if (file.exists()) {
            try {
                val content = file.readText()
                if (content.isNotBlank()) {
                    val existingArray = JSONArray(content)
                    for (i in 0 until existingArray.length()) {
                        existingData.put(existingArray.get(i))
                    }
                }
            } catch (e: Exception) {
                Log.e("LocationService", "Ошибка чтения файла: ${e.message}")
            }
        }

        existingData.put(data)

        FileWriter(file, false).use { writer ->
            writer.write(existingData.toString(2))
        }

        Log.d("LocationService", "Файл сохранен в: ${file.absolutePath}")
        true
    } catch (e: IOException) {
        Log.e("LocationService", "Ошибка сохранения: ${e.message}")
        false
    }
}

fun readAllJSON(context: Context, filename: String): JSONArray? {
    return try {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadDir, "$filename.json")

        if (file.exists()) {
            val content = file.readText()
            if (content.isNotBlank()) {
                JSONArray(content)
            } else {
                JSONArray()
            }
        } else {
            JSONArray()
        }
    } catch (e: Exception) {
        Log.e("LocationService", "Ошибка чтения: ${e.message}")
        null
    }
}

fun clearJSON(context: Context, filename: String): Boolean {
    return try {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadDir, "$filename.json")

        if (file.exists()) {
            FileWriter(file, false).use { writer ->
                writer.write("[]")
            }
        }
        true
    } catch (e: IOException) {
        Log.e("LocationService", "Ошибка очистки: ${e.message}")
        false
    }
}