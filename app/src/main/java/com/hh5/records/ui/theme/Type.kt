package com.hh5.records.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hh5.records.R

val Righteous = FontFamily(
    Font(R.font.righteous)
)

val Overpass = FontFamily(
    Font(R.font.overpass_regular),
    Font(R.font.overpass_bold, FontWeight.Bold)
)

// Set of Material typography styles to start with
val Typography = Typography(
    h1 = TextStyle(
        fontFamily = Righteous,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp
    ),
    h2 = TextStyle(
        fontFamily = Righteous,
        fontWeight = FontWeight.Normal,
        fontSize = 25.sp
    ),
    h3 = TextStyle(
        fontFamily = Overpass,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    h4 = TextStyle(
        fontFamily = Overpass,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp
    ),
    h5 = TextStyle(
        fontFamily = Overpass,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    ),
    body1 = TextStyle(
        fontFamily = Overpass,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    )
)