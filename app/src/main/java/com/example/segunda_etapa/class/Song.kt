package com.example.segunda_etapa.`class`

import android.graphics.Bitmap
import android.net.Uri

data class Song(val title: String, val artist: String, val bitmap: Bitmap?, val uri: Uri?, var licked: Boolean)