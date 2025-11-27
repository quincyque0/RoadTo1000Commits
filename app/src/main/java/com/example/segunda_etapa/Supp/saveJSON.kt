package com.example.segunda_etapa.Supp

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException

fun saveJSON(filename: String, data: JSONObject, context: Context): Boolean {
    return try {
        val documentsDir = File(context.getExternalFilesDir(null), "Documents")
        if (!documentsDir.exists()) {
            documentsDir.mkdirs()
            Log.d("LocationService", "Создана папка: ${documentsDir.absolutePath}")
        }

        val file = File(documentsDir, "$filename.txt")
        FileWriter(file, true).use { writer ->
            writer.write(data.toString() + "\n")
        }

        Log.d("LocationService", "Локация сохранена в: ${file.absolutePath}")
        true
    } catch (e: IOException) {
        Log.e("LocationService", "Ошибка сохранения: ${e.message}")
        false
    }
}