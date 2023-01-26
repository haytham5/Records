package com.hh5.records.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DiscFull
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hh5.records.R

@Composable
fun StartRecordsScreen(
    onCollectionClicked: () -> Unit,
    onStatsClicked: () -> Unit,
    onShuffleClicked: () -> Unit,
    modifier: Modifier = Modifier
) {

    Scaffold(modifier = modifier, topBar = {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colors.primary),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = modifier.weight(1f))

                Text(
                    text = stringResource(R.string.shuffle),
                    style = MaterialTheme.typography.h1,
                    color = MaterialTheme.colors.primary,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Visible,
                    modifier = modifier
                        .fillMaxWidth()
                        .weight(5f)
                )

                IconButton(onClick = { /*TODO Make Exit Application*/ },
                    modifier = modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        tint = MaterialTheme.colors.onPrimary,
                        contentDescription = ""
                    )
                }
            }
        }
    }) {
        Column(
            modifier = modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = modifier.padding(16.dp))

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
                    onShuffleClicked()
                }) {
                Text(
                    style = MaterialTheme.typography.h3,
                    text = "Shuffle"
                )
            }
        }

    }

}

@Preview
@Composable
fun StartRecordsPreview(){
    StartRecordsScreen(onCollectionClicked = {}, onStatsClicked = {}, onShuffleClicked = {})
}