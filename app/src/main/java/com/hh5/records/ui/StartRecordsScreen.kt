package com.hh5.records.ui

import android.graphics.Paint.Align
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hh5.records.R

@Composable
fun StartRecordsScreen(
    onCollectionClicked: () -> Unit,
    onStatsClicked: () -> Unit,
    onRandomizerClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = modifier.fillMaxWidth()) {
            Text(
                fontSize = 27.sp,
                fontFamily = FontFamily(
                    Font(R.font.overpass_regular)
                ),
                text = "Welcome to your "
            )

            Text(
                fontSize = 27.sp,
                fontFamily = FontFamily(
                    Font(R.font.righteous)
                ),
                text = "Records"
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = modifier.height(100.dp).fillMaxWidth(),
            onClick = {
                onCollectionClicked()
            }) {
            Text(
                style = MaterialTheme.typography.h3,
                text = "Collection"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = modifier.height(100.dp).fillMaxWidth(),
            onClick = {
                onStatsClicked()
            }) {
            Text(
                style = MaterialTheme.typography.h3,
                text = "Stats"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = modifier.height(100.dp).fillMaxWidth(),
            onClick = {
                onRandomizerClicked()
            }) {
            Text(
                style = MaterialTheme.typography.h3,
                text = "Randomizer"
            )
        }
    }
}

@Preview
@Composable
fun StartRecordsPreview(){
    StartRecordsScreen(onCollectionClicked = {}, onStatsClicked = {}, onRandomizerClicked = {})
}