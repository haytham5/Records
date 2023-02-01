package com.hh5.records.ui

import android.app.Activity
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DiscFull
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.hh5.records.R

@Composable
fun StartRecordsScreen(
    onCollectionClicked: () -> Unit,
    onStatsClicked: () -> Unit,
    onShuffleClicked: () -> Unit,
    modifier: Modifier = Modifier
) {

    var exitToggle by remember { mutableStateOf(false) }
    val activity = (LocalContext.current as? Activity)

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

                IconButton(onClick = { exitToggle = !exitToggle},
                    modifier = modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        tint = MaterialTheme.colors.onPrimary,
                        contentDescription = ""
                    )
                }

                if (exitToggle) {
                    Dialog(
                        onDismissRequest = {
                            exitToggle = !exitToggle
                        }
                    ) {
                        Surface(
                            modifier = modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp)),
                            elevation = 4.dp,
                        ) {
                            Column(
                                modifier = modifier.background(color = MaterialTheme.colors.surface),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Exiting Application!",
                                    style = MaterialTheme.typography.h1,
                                    color = MaterialTheme.colors.onPrimary,
                                    textAlign = TextAlign.Center,
                                    overflow = TextOverflow.Visible,
                                    modifier = modifier
                                        .fillMaxWidth().padding(bottom = 5.dp)
                                )

                                Text(
                                    text = "Are You sure?",
                                    style = MaterialTheme.typography.h3,
                                    color = MaterialTheme.colors.onPrimary,
                                    textAlign = TextAlign.Center,
                                    overflow = TextOverflow.Visible,
                                    modifier = modifier
                                        .fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))


                                Row(horizontalArrangement = Arrangement.Center, modifier = modifier.fillMaxWidth()) {
                                    Button(
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = MaterialTheme.colors.primary,
                                            contentColor = MaterialTheme.colors.onBackground),
                                        modifier = modifier.clip(RoundedCornerShape(bottomStart = 10.dp)).weight(1f),
                                        onClick = {
                                            exitToggle = !exitToggle
                                        }) {
                                        Text(
                                            text = stringResource(R.string.back),
                                            style = MaterialTheme.typography.h1,
                                            textAlign = TextAlign.Center,
                                            overflow = TextOverflow.Visible,
                                            modifier = modifier
                                                .fillMaxWidth()
                                        )
                                    }

                                    Button(
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = MaterialTheme.colors.secondary,
                                            contentColor = MaterialTheme.colors.onBackground),
                                        modifier = modifier.clip(RoundedCornerShape(bottomEnd = 10.dp)).weight(1f),
                                        onClick = {
                                            activity?.finish()
                                        }) {
                                        Text(
                                            text = stringResource(R.string.exit),
                                            style = MaterialTheme.typography.h1,
                                            textAlign = TextAlign.Center,
                                            overflow = TextOverflow.Visible,
                                            modifier = modifier
                                                .fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
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