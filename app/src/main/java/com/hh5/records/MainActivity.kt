package com.hh5.records

import android.os.Build
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import com.hh5.records.ui.theme.RecordsTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.animation.*

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.M)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RecordsTheme {
                RecordsApp()
            }
        }
    }
}

