package com.hh5.records.ui

import android.content.Context
import android.os.Build
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hh5.records.data.DBHandler
import com.hh5.records.ui.theme.RecordsTheme
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.hh5.records.R
import com.hh5.records.data.AlbumModel
import com.hh5.records.ui.RecordsUiState
import com.hh5.records.ui.RecordsViewModel

@Composable
fun CollectionScreen(
    recordsViewModel: RecordsViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    var dbHandler = DBHandler(LocalContext.current)

//    dbHandler.clear()
//    addAlbums(dbHandler)

    val recordsUiState by recordsViewModel.uiState.collectAsState()
    var clickedSearch by remember { mutableStateOf(false) }
    var clickedChips by remember { mutableStateOf(false) }

    var favorite by remember { mutableStateOf(false) }
    var listened by remember { mutableStateOf(false) }

    favorite = recordsUiState.filterFavorite
    listened = recordsUiState.filterListened

    recordsViewModel.updateRecords(filter(dbHandler.readAlbums()!!, favorite, listened, recordsUiState))

    Scaffold(
        modifier = modifier,
        topBar = {
            val density = LocalDensity.current

            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .background(color = colors.primary)
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = modifier
                    .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {

                    Box(modifier = modifier.weight(1f)) {
                        if(!clickedChips) {
                            SearchButton(
                                clicked = clickedSearch,
                                onClick = {
                                    clickedSearch = !clickedSearch
                                }
                            )
                        }

                        else if(!clickedSearch && clickedChips) {
                            AddButton(LocalContext.current)
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = modifier.weight(5f)
                    ) {
                        CollectionHeader(clickedSearch, clickedChips, recordsUiState)

                        RecordsSearchBar(
                            density = density,
                            clickedSearch = clickedSearch,
                            value = recordsViewModel.searchText,
                            clickedChips = clickedChips,
                            onTextFieldValueChanged = {recordsViewModel.updateSearch(it)},
                            onTextFieldDone = {
                                recordsViewModel.search()
                                clickedSearch = !clickedSearch
                            },
                            onListenClicked = {
                                recordsViewModel.setListened()
                                clickedChips = !clickedChips
                            },
                            onFavoriteClicked = {
                                recordsViewModel.setFavorite()
                                clickedChips = !clickedChips
                            }
                        )
                    }

                    Box(contentAlignment = Alignment.CenterEnd, modifier = modifier.weight(1f)) {
                        if(!clickedSearch) {
                            ChipsButton(
                                clicked = clickedChips,
                                onClick = {
                                    clickedChips = !clickedChips
                                }
                            )
                        }

                        else if(clickedSearch && !clickedChips) {
                            ClearButton(
                                clicked = clickedSearch,
                                onClick = {
                                    recordsViewModel.clearSearch()
                                    clickedSearch = !clickedSearch
                                }
                            )
                        }
                    }
                }
            }
        }
    ) {
        LazyColumn(modifier = Modifier.background(colors.background)) {
            items(recordsUiState.records.chunked(2)) {
                AlbumItem(album1 = it[0], album2 = it[1], recordsViewModel = recordsViewModel, hide = it[0] == it[1], dbHandler = dbHandler)
            }
        }
    }
}

@Composable
fun RecordsSearchBar(
    density: Density,
    clickedSearch: Boolean,
    clickedChips: Boolean,
    onTextFieldValueChanged: (String) -> Unit,
    onTextFieldDone: () -> Unit,
    onListenClicked: () -> Unit,
    onFavoriteClicked: () -> Unit,
    value: String,
    modifier : Modifier = Modifier
) {

    var newToggle by remember { mutableStateOf(true) }
    var favoriteToggle by remember { mutableStateOf(true) }

    AnimatedVisibility(
        visible = clickedSearch || clickedChips,
        enter = slideInVertically {
            with(density) { 40.dp.roundToPx() }
        } + expandVertically(
            expandFrom = Alignment.Bottom
        ) + fadeIn(
            initialAlpha = 0.3f
        ),
        exit = slideOutVertically() + shrinkVertically() + fadeOut()
    ) {
        if(clickedSearch && !clickedChips) {
            TextField(
                value = value,
                singleLine = true,
                textStyle = MaterialTheme.typography.h4,
                onValueChange = onTextFieldValueChanged,
                placeholder = {
                    Text(
                        style = MaterialTheme.typography.h3,
                        text = "Search..."
                    )
                },
                shape = RoundedCornerShape(50.dp),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = colors.secondary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { onTextFieldDone() }
                ),
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxWidth()
            )
        }

        else if(!clickedSearch && clickedChips) {
            Row(modifier = modifier.padding(5.dp)) {
                Button(
                    modifier = modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if(!newToggle) MaterialTheme.colors.secondaryVariant else MaterialTheme.colors.secondary,
                        contentColor = MaterialTheme.colors.onBackground),
                    enabled = favoriteToggle,
                    shape = RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp),
                    onClick = {
                        onListenClicked()
                        newToggle = !newToggle
                    }) {
                    Text(
                        style = MaterialTheme.typography.h3,
                        text = "New"
                    )
                }

                Button(
                    modifier = modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if(!favoriteToggle) MaterialTheme.colors.secondaryVariant else MaterialTheme.colors.secondary,
                        contentColor = MaterialTheme.colors.onBackground),
                    enabled = newToggle,
                    shape = RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp),
                    onClick = {
                        onFavoriteClicked()
                        favoriteToggle = !favoriteToggle
                    }) {
                    Text(
                        style = MaterialTheme.typography.h3,
                        text = "Favorites"
                    )
                }
            }
        }

    }

}


@Composable
private fun CollectionHeader(
    clickedSearch: Boolean,
    clickedChips: Boolean,
    recordsUiState: RecordsUiState,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    var filterQualifier = ""

    if (recordsUiState.filterFavorite) {
        filterQualifier = "fave "
    } else if(recordsUiState.filterListened) {
        filterQualifier = "new "
    }

    AnimatedVisibility(
        visible = !clickedSearch && !clickedChips,
        enter = slideInVertically {
            with(density) { -40.dp.roundToPx() }
        } + expandVertically(
            expandFrom = Alignment.Top
        ) + fadeIn(
            initialAlpha = 0.3f
        ),
        exit = slideOutVertically() + shrinkVertically() + fadeOut()
    ) {
        Text(
            text = filterQualifier + if(recordsUiState.search.isNullOrBlank()) stringResource(R.string.app_name) else recordsUiState.search,
            style = MaterialTheme.typography.h1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Visible,
            modifier = modifier
                .fillMaxWidth()
        )
    }
}



@Composable
private fun SearchButton(
    clicked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (!clicked) Icons.Default.Search else Icons.Filled.ExpandLess,
            tint = MaterialTheme.colors.onBackground,
            contentDescription = stringResource(R.string.Search)
        )
    }
}

@Composable
private fun ClearButton(
    clicked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Filled.Close ,
            tint = MaterialTheme.colors.onSurface,
            contentDescription = stringResource(R.string.Search)
        )
    }
}

@Composable
private fun ChipsButton(
    clicked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (!clicked) Icons.Default.MoreVert else Icons.Filled.ExpandLess,
            tint = colors.onSurface,
            contentDescription = stringResource(R.string.Filter)
        )
    }
}

@Composable
fun AddButton(context: Context, modifier: Modifier = Modifier) {
    var addAlbumToggle by remember { mutableStateOf(false) }

    var dbHandler = DBHandler(context)

    val albumArtist = remember {
        mutableStateOf(TextFieldValue())
    }
    val albumTitle = remember {
        mutableStateOf(TextFieldValue())
    }
    val albumGenre = remember {
        mutableStateOf(TextFieldValue())
    }
    val albumCover = remember {
        mutableStateOf(TextFieldValue())
    }

    val focusManager = LocalFocusManager.current

    IconButton(onClick = {
        addAlbumToggle = !addAlbumToggle
    }) {
        Icon(
            imageVector = Icons.Filled.Add,
            tint = colors.onSurface,
            contentDescription = stringResource(R.string.Filter)
        )
    }

    if (addAlbumToggle) {
        Dialog(
            onDismissRequest = {
                addAlbumToggle = !addAlbumToggle
            }
        ) {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp)),
                elevation = 4.dp,
            ) {
                Column(
                    modifier = modifier.background(color = colors.surface),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    TextField(
                        singleLine = true,
                        textStyle = MaterialTheme.typography.h4,
                        shape = RoundedCornerShape(50.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = colors.primary,
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
                            backgroundColor = colors.primary,
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
                        value = albumTitle.value,
                        onValueChange = { albumTitle.value = it },
                        placeholder = { Text(text = "Title") }
                    )

                    TextField(
                        singleLine = true,
                        textStyle = MaterialTheme.typography.h4,
                        shape = RoundedCornerShape(50.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = colors.primary,
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

                    TextField(
                        singleLine = true,
                        textStyle = MaterialTheme.typography.h4,
                        shape = RoundedCornerShape(50.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = colors.primary,
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
                        value = albumCover.value,
                        onValueChange = { albumCover.value = it },
                        placeholder = { Text(text = "Cover") }
                    )

                    Button(
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = colors.secondary,
                            contentColor = colors.onBackground),
                        modifier = modifier.clip(RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 10.dp, bottomEnd = 10.dp)),
                        onClick = {
                            dbHandler.addNewAlbum(
                                albumArtist.value.text,
                                albumTitle.value.text,
                                albumGenre.value.text,
                                albumCover.value.text,
                                false,
                                false
                            )

                            addAlbumToggle = !addAlbumToggle

                            Toast.makeText(context, "Album Added to Database!", Toast.LENGTH_SHORT).show()
                        }) {
                        Text(
                            text = "Add",
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

@Composable
private fun AlbumItem(album1: AlbumModel, album2: AlbumModel, hide: Boolean, recordsViewModel: RecordsViewModel, dbHandler: DBHandler, modifier: Modifier = Modifier) {
    Row(modifier = modifier
        .fillMaxWidth()
        .padding(5.dp)) {

        Box(modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .weight(1f)
            .padding(horizontal = 5.dp)
            .padding(top = 5.dp)) {
            AlbumCard(album1, dbHandler)
        }

        Box(modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .weight(1f)
            .padding(horizontal = 5.dp)
            .padding(top = 5.dp)) {
            if(!hide) {
                AlbumCard(album2, dbHandler)
            }
        }

    }
}

@Composable
private fun AlbumCard(albumInput: AlbumModel, dbHandler: DBHandler, modifier: Modifier = Modifier) {
    var albumInfo by remember { mutableStateOf(false) }
    //var albumModel by remember {mutableStateOf(albumInput)}
    val albumModel = albumInput
    var listenedValue by remember {(mutableStateOf(albumModel.listened == 1)) }
    var favoriteValue by remember {(mutableStateOf(albumModel.favorite == 1)) }

    albumModel.listened = if(listenedValue) 1 else 0
    albumModel.favorite = if(favoriteValue) 1 else 0

    val context = LocalContext.current

    Card(
        elevation = 4.dp,
        modifier = modifier
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Column(
            modifier = modifier.background(colors.surface)
        ) {
            Image(
                modifier = modifier
                    .padding(4.dp)
                    .fillMaxSize()
                    .clickable { albumInfo = !albumInfo },
                contentScale = ContentScale.Crop,
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = albumModel.cover)
                        .allowHardware(false)
                        .build()
                ),
                contentDescription = null
            )

            if (albumInfo) {
                Dialog(
                    onDismissRequest = {
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

                        albumInfo = !albumInfo
                    }
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp)),
                        elevation = 4.dp,

                        ) {

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = modifier.background(color = colors.surface)
                        ) {

                            Box(modifier = modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)) {
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
                                        backgroundColor = if(listenedValue) colors.secondaryVariant else colors.secondary,
                                        contentColor = colors.onBackground),
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
                                        backgroundColor = if(favoriteValue) colors.secondaryVariant else colors.secondary,
                                        contentColor = colors.onBackground),
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
            }
        }
    }
}


@Composable
private fun filter(db : ArrayList<AlbumModel>, wantFavorite : Boolean, wantNew : Boolean, recordsUiState: RecordsUiState): List<AlbumModel> {
    var filteredAlbums = mutableListOf<AlbumModel>()

    val sortedDB = db.sortedBy { it.artist }

    if(recordsUiState.search.isNullOrBlank() && !wantFavorite && !wantNew) {
        filteredAlbums.addAll(sortedDB)
    }

    else {
        for(album in db) {
            if(
                album.title.contains(recordsUiState.search, ignoreCase = true) ||
                album.artist.contains(recordsUiState.search, ignoreCase = true) ||
                album.genre.contains(recordsUiState.search, ignoreCase = true)
            ) {
                var filterAlbum = true


                if(wantFavorite && !wantNew) {
                    if(album.favorite == 0) {
                        filterAlbum = false
                    }
                }

                else if(!wantFavorite && wantNew) {
                    if(album.listened == 1) {
                        filterAlbum = false
                    }
                }

                if(filterAlbum) {
                    filteredAlbums.add(album)
                }
            }
        }
    }


    if(filteredAlbums.size%2!=0) {
        filteredAlbums.add(filteredAlbums.last())
    }

    return filteredAlbums
}

@Preview
@Composable
fun RecordsPreview() {
    RecordsTheme(darkTheme = true) {
        CollectionScreen()
    }
}

fun addAlbums(dbHandler: DBHandler) {
    dbHandler.addNewAlbum("AC/DC", "Back In Black", "Rock", "https:/ac/dc/lh3.googleusercontent.com/etTz20YiB4ccbsUO2yLrCY9wSS9GybYF5qJh-j5tu8MLTqP2GjgROiBt_4JUC5rjnnd_RiuWa3ndUAeO=w544-h544-l90-rj",false, false)

    dbHandler.addNewAlbum("Nat Adderley", "The Cannonball Adderley Quintet In San Fransisco - KEEPNEWS COLLECTION", "Jazz", "https://pisces.bbystatic.com/image2/BestBuy_US/images/products/1060/10600842_so.jpg",false, false)

    dbHandler.addNewAlbum("Aesop Rock", "The Impossible Kid", "Hip-Hop", "https://lh3.googleusercontent.com/qMGKXlK6XS_eqoTu27UELrn3BJHakVOuGJEoh8fscMLsxDHlF6HJx-AQxDxaONqd0Fdw0eAjd2wQEXAF=w544-h544-l90-rj",true, true)

    dbHandler.addNewAlbum("The All-American Rejects", "Move Along", "Pop", "https://lh3.googleusercontent.com/QouZWY6wFg4JmCcvieRtZUvIL8w2KxdheVnNTbef9zdFnrmXfORSA7ydpFqSeXmmsTi4rAAkgoRzE8o=w544-h544-s-l90-rj",true, false)

    dbHandler.addNewAlbum("Damon Albarn", "Everyday Robots", "Indie","https://lh3.googleusercontent.com/PG62mLr3AMCu0Su0rxjB1LU5eQpyUAzzMXTXk05USV3hX1CHuBB1XjBfu2kf-IUcEssNWcH1USJ7hOg2zA=w544-h544-l90-rj",false, false)

    dbHandler.addNewAlbum("Alt-J", "An Awesome Wave", "Indie", "https://www.gratefulweb.com/sites/default/files/images/articles/alt-j-album.jpg",true, true)

    dbHandler.addNewAlbum("Alt-J", "This is All Yours", "Indie", "https://lh3.googleusercontent.com/eYeGAmRG1e68wyJlcYI9CZw72lrxterqwjCr9Nd87SmaPXpsvV1yM0ciNLdXtHpba1oi5zT36J6DeTE=w544-h544-l90-rj",true, true)

    dbHandler.addNewAlbum("Aminé", "Limbo", "Hip-Hop", "https://lh3.googleusercontent.com/98ZtsBjBgUQI5jbCPjFiJztTbvWeqdLAxt4dmt13KsFAaM-wMD8N9CujkxFOGBaVXt7gnmIacUoTu5s=w544-h544-l90-rj",true, true)

}