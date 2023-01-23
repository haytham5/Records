package com.hh5.records.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.hh5.records.R
import com.hh5.records.data.AlbumModel
import com.hh5.records.data.DBHandler

@Composable
fun ShuffleScreen(
    recordsViewModel: RecordsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var dbHandler = DBHandler(LocalContext.current)

    val recordsUiState by recordsViewModel.uiState.collectAsState()

    var shuffleAlbumToggle by remember { mutableStateOf(false) }

    val albumArtist = remember {
        mutableStateOf(TextFieldValue())
    }

    val albumGenre = remember {
        mutableStateOf(TextFieldValue())
    }

    var favoriteToggle by remember { mutableStateOf(false) }
    var newToggle by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    var shuffleClicked by remember {mutableStateOf(false)}

    var shuffledAlbum = recordsUiState.shuffledAlbum

    if(shuffleClicked) {

        var filteredAlbums = filter(dbHandler.readAlbums()!!, favoriteToggle, newToggle, albumArtist.value.text, albumGenre.value.text)

        if(filteredAlbums.isNotEmpty()) {
            recordsViewModel.setShuffle(filteredAlbums.random())
        }

        else {
            Toast.makeText(LocalContext.current, "No Albums fit those specifications!", Toast.LENGTH_SHORT).show()
        }

        shuffleClicked = !shuffleClicked
    }


    Scaffold(
        modifier = modifier,
        topBar = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colors.primary),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = modifier
                    .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = modifier.weight(1f))

                        Text(
                            text = stringResource(R.string.shuffle),
                            style = MaterialTheme.typography.h1,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Visible,
                            modifier = modifier
                                .fillMaxWidth()
                                .weight(5f)
                        )

                    IconButton(onClick = {
                        shuffleAlbumToggle = !shuffleAlbumToggle
                    }) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            tint = MaterialTheme.colors.onBackground,
                            contentDescription = stringResource(R.string.shuffle)
                        )
                    }

                    if (shuffleAlbumToggle) {
                        Dialog(
                            onDismissRequest = {
                                shuffleAlbumToggle = !shuffleAlbumToggle
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
                                    Spacer(modifier = Modifier.height(20.dp))

                                    TextField(
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.h4,
                                        shape = RoundedCornerShape(50.dp),
                                        colors = TextFieldDefaults.textFieldColors(
                                            backgroundColor = MaterialTheme.colors.primary,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            disabledIndicatorColor = Color.Transparent
                                        ),
                                        keyboardOptions = KeyboardOptions.Default.copy(
                                            imeAction = ImeAction.Next
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = { focusManager.moveFocus(FocusDirection.Next) }
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 10.dp, end = 10.dp, bottom = 20.dp),
                                        value = albumArtist.value,
                                        onValueChange = { albumArtist.value = it },
                                        placeholder = { Text(text = "Artist") }
                                    )

                                    TextField(
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.h4,
                                        shape = RoundedCornerShape(50.dp),
                                        colors = TextFieldDefaults.textFieldColors(
                                            backgroundColor = MaterialTheme.colors.primary,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            disabledIndicatorColor = Color.Transparent
                                        ),
                                        keyboardOptions = KeyboardOptions.Default.copy(
                                            imeAction = ImeAction.Next
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = { focusManager.moveFocus(FocusDirection.Next) }
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 10.dp, end = 10.dp, bottom = 20.dp),
                                        value = albumGenre.value,
                                        onValueChange = { albumGenre.value = it },
                                        placeholder = { Text(text = "Genre") },
                                    )

                                    Row(modifier = modifier.height(80.dp).padding(start = 10.dp, end = 10.dp, bottom = 20.dp)) {
                                        Button(
                                            modifier = modifier.weight(1f).fillMaxHeight(),
                                            colors = ButtonDefaults.buttonColors(
                                                backgroundColor = if(newToggle) MaterialTheme.colors.secondaryVariant else MaterialTheme.colors.secondary,
                                                contentColor = MaterialTheme.colors.onBackground),
                                            shape = RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp),
                                            enabled = !favoriteToggle,
                                            onClick = {
                                                newToggle = !newToggle
                                            }) {
                                            Text(
                                                style = MaterialTheme.typography.h3,
                                                text = "New"
                                            )
                                        }

                                        Button(
                                            modifier = modifier.weight(1f).fillMaxHeight(),
                                            colors = ButtonDefaults.buttonColors(
                                                backgroundColor = if(favoriteToggle) MaterialTheme.colors.secondaryVariant else MaterialTheme.colors.secondary,
                                                contentColor = MaterialTheme.colors.onBackground),
                                            enabled = !newToggle,
                                            shape = RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp),
                                            onClick = {
                                                favoriteToggle = !favoriteToggle
                                            }) {
                                            Text(
                                                style = MaterialTheme.typography.h3,
                                                text = "Favorites"
                                            )
                                        }
                                    }

                                    Button(
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = MaterialTheme.colors.secondary,
                                            contentColor = MaterialTheme.colors.onBackground),
                                        modifier = modifier.clip(RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 10.dp, bottomEnd = 10.dp)),
                                        onClick = {
                                            shuffleClicked = !shuffleClicked
                                            shuffleAlbumToggle = !shuffleAlbumToggle
                                        }) {
                                        Text(
                                            text = stringResource(R.string.shuffle),
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
    ) {
        if (shuffledAlbum != null) {
            AlbumSelectedDisplay(shuffledAlbum, dbHandler)
        }

        else {
            Box (modifier = Modifier
                .fillMaxSize()
                .padding(25.dp)
            ) {
                Text(
                    style = MaterialTheme.typography.body1,
                    text = "Click the Shuffle Button to get a random album set to your specifications!",
                    textAlign = TextAlign.Center,
                    modifier = modifier.align(Alignment.Center)
                )
            }

        }
    }
}

@Composable
fun AlbumSelectedDisplay(albumInput: AlbumModel, dbHandler: DBHandler, modifier: Modifier = Modifier) {
    val albumModel = albumInput
    val listened = albumModel.listened == 1
    val favorite = albumModel.favorite == 1
    var listenedValue by remember {mutableStateOf(false)}
    var favoriteValue by remember {mutableStateOf(false)}

    listenedValue = listened
    favoriteValue = favorite


    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(25.dp)
    ) {

        Text(
            fontSize = 27.sp,
            fontFamily = FontFamily(
                Font(R.font.righteous)
            ),
            text = "Your Album Is...",
            modifier = modifier.align(Alignment.TopCenter)
        )

        Spacer(modifier = modifier.padding(8.dp))


        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .background(color = MaterialTheme.colors.surface)
                .padding(5.dp)
                .align(Alignment.Center)
        ) {

            Box(modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1f)
            ) {
                Image(
                    modifier = modifier
                        .padding(5.dp)
                        .fillMaxWidth()
                    ,
                    contentScale = ContentScale.FillWidth,
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(data = albumModel.cover)
                            .allowHardware(false)
                            .build()
                    ),
                    contentDescription = null
                )
            }

            Text(
                text = albumModel.artist + " - " + albumModel.title,
                style = MaterialTheme.typography.h2,
                textAlign = TextAlign.Center,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 7.dp)
            )

            Text(
                text = albumModel.genre,
                style = MaterialTheme.typography.h4,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Visible,
                maxLines = 1,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 5.dp)
            )

            Row(
                modifier = modifier.padding(5.dp)
            ) {
                Button(
                    modifier = modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if(listenedValue) MaterialTheme.colors.secondaryVariant else MaterialTheme.colors.secondary,
                        contentColor = MaterialTheme.colors.onBackground),
                    shape = RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp),
                    onClick = {
                        listenedValue = !listenedValue

                        if(favoriteValue) favoriteValue = false

                        albumModel.listened = if(listenedValue) 1 else 0
                        albumModel.favorite = if(favoriteValue) 1 else 0

                        dbHandler.updateAlbum(
                            albumModel.artist,
                            albumModel.title,
                            albumModel.title,
                            albumModel.genre,
                            albumModel.cover,
                            albumModel.listened == 1,
                            albumModel.favorite == 1
                        )

                        if(listenedValue) Toast.makeText(context, "Album Listened!", Toast.LENGTH_LONG).show()
                        else Toast.makeText(context, "Album Set New (Un-Favorited If It Was).", Toast.LENGTH_LONG).show()
                    }
                ) {
                    Text(
                        style = MaterialTheme.typography.body1,
                        text = "Listened"
                    )
                }

                Button(
                    modifier = modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if(favoriteValue) MaterialTheme.colors.secondaryVariant else MaterialTheme.colors.secondary,
                        contentColor = MaterialTheme.colors.onBackground),
                    enabled = listenedValue,
                    shape = RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp),
                    onClick = {
                        favoriteValue = !favoriteValue

                        albumModel.listened = if(listenedValue) 1 else 0
                        albumModel.favorite = if(favoriteValue) 1 else 0

                        dbHandler.updateAlbum(
                            albumModel.artist,
                            albumModel.title,
                            albumModel.title,
                            albumModel.genre,
                            albumModel.cover,
                            albumModel.listened == 1,
                            albumModel.favorite == 1
                        )

                        if(favoriteValue) Toast.makeText(context, "Album Favorited!", Toast.LENGTH_LONG).show()
                        else Toast.makeText(context, "Album Un-Favorited.", Toast.LENGTH_LONG).show()
                    }) {
                    Text(
                        style = MaterialTheme.typography.body1,
                        text = "Favorite"
                    )
                }
            }
        }
    }
}


@Composable
private fun filter(db : ArrayList<AlbumModel>, wantFavorite: Boolean, wantNew : Boolean, artist: String, genre: String): List<AlbumModel> {
    var filteredAlbums = mutableListOf<AlbumModel>()

    for(album in db) {
        if(
            (album.artist.contains(artist, ignoreCase = true) &&
            album.genre.contains(genre, ignoreCase = true)) ||
            (artist.isNullOrBlank() && genre.isNullOrBlank())
        ) {
            if(wantFavorite && !wantNew) {
                if(album.favorite == 1) {
                    filteredAlbums.add(album)
                }
            }

            else if(wantNew && !wantFavorite) {
                if(album.listened == 0) {
                    filteredAlbums.add(album)
                }
            }

            else {
                filteredAlbums.add(album)
            }
        }
    }


    return filteredAlbums
}



@Preview
@Composable
fun ShufflePreview() {
    ShuffleScreen()
}
