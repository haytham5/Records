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
import android.util.Log

@Composable
fun CollectionScreen(
    recordsViewModel: RecordsViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    var dbHandler = DBHandler(LocalContext.current)
//
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

    dbHandler.addNewAlbum("Anamanaguchi", "Scott Pilgrim vs. The World: The Game - The Soundtrack", "Electronic", "https://lh3.googleusercontent.com/2sqjK2N9Scov_d648frq15OGusrwIpACvBlBaQrbQ85QFR-QpH5eBE6B_XV4v6bpxb72wz_a4z2lqxb7=w544-h544-l90-rj",true, true)

    dbHandler.addNewAlbum("Animal Collective", "Merriweather Post Pavillion", "Indie", "https://lh3.googleusercontent.com/YKzWDaDzUl5arsewREZq4tdMh--k7Q3YVGoSCRvKdXHN2Y0uuUFTzcdDSd7fAyphKYJTGCnL5zCnaAQ=w544-h544-l90-rj",false, false)

    dbHandler.addNewAlbum("Animal Collective", "Strawberry Jam", "Indie", "https://lh3.googleusercontent.com/4KhQqwAqhG6DHewn3Q33x8wsRTSQTG7wdrMIxcbgFVNvRSJz36G0JI3TEzPnl7Ni3cpdPiIiiXj2Xs_g=w544-h544-l90-rj",false, false)

    dbHandler.addNewAlbum("Arcade Fire", "Funeral", "Indie", "https://lh3.googleusercontent.com/GWqZ2EW2bqMolKoX43ETfrYvjbpvEKvj-BPFXpHtjs41KR-36SyJ3EjcYfmh4Snli9v8Nb20HTA0ohs=w544-h544-l90-rj",false, false)

    dbHandler.addNewAlbum("Arcade Fire", "The Suburbs", "Indie", "https://lh3.googleusercontent.com/-A1sZesJEvbfs1lrKU1rTpId6XQk-f50dpjnTghVXH3IBYGwLC0uecl1oP2pRqnAM4n1QmgzcQL2c_OB=w544-h544-l90-rj",false, false)

    dbHandler.addNewAlbum("Arctic Monkeys", "When The Sun Goes Down", "Rock", "https://lh3.googleusercontent.com/hxFse-eN1z-evVs3OAx2uQgYHpXCVOzoYY8nLd_ish1sIOLKp5-QHj57JP7nFaRHk8ERnMIzXY0eHBuT=w544-h544-l90-rj",true, true)

    dbHandler.addNewAlbum("Arctic Monkeys", "Whatever People Say I Am, That's What I'm Not", "Rock", "https://lh3.googleusercontent.com/vrfwx_J_BAlkHSV_IrlFmT8aYmsrbYrc5I2enR8zmWRps0bKWxCEc7QGsimQL-5cKs7KULeQOSZ0FXd9yA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Arctic Monkeys", "Brianstorm", "Rock", "https://lh3.googleusercontent.com/6_ssxNNdy_XHCkcCu3yXKVH45bi8MN87gP9jS4lcFYH9h7qz-qMAvMLQr3FFqih2UhWBUr4wraZv2CFs=w544-h544-l90-rj",true, true)

    dbHandler.addNewAlbum("A\$AP Rocky", "Long.Live.A\$AP", "Hip-Hop", "https://lh3.googleusercontent.com/Oa7LlzALtpit3vY3QTq6mSpPeIJOIHCARLmXjzOMKdJHRfw3VGg2TXhoWbSDc-gic9hOeNe-1DzVKHg=w544-h544-l90-rj",true, true)

    dbHandler.addNewAlbum("A\$AP Rocky", "At.Long.Last.A\$AP", "Hip-Hop", "https://lh3.googleusercontent.com/43gfFzpw1452CijUMKrzkh5bFR6eRvnBdsMMjSy19KF_nNxMydY5zNSi-q1P_7VxRcUJt_SNLn90NycdyA=w544-h544-l90-rj",false, false)

    dbHandler.addNewAlbum("Backstreet Boys", "Millenium", "Pop", "https://lh3.googleusercontent.com/9_bcTlzFw20NRGy5R90FK3c0QEcDSfFMXpxp7mNqF6hk8g7EktfVMH_bnXItO-VCpcM5r0rwdzw_34mL9g=w544-h544-l90-rj",true, true)

    dbHandler.addNewAlbum("Joey Bada\$\$", "B4.DA.\$\$", "Hip-Hop", "https://lh3.googleusercontent.com/w6nVZ0YE4P6Dbv4xPbYaDokm0HlWF47rMkZup1fpMAok8GIqu-2D7p3_PvfL2Pk8WzXr-m_rg-M5qUQ=w544-h544-l90-rj",true, true)

    dbHandler.addNewAlbum("Joey Bada\$\$", "All-Amerikkkan Bada\$\$", "Hip-Hop", "https://lh3.googleusercontent.com/6rf5aE4zuSeT8ut0rPNVco7RSVJRXlqD9gMQyZozg0JYneRbLi7qML9f2q_omwbPDzY3Hw7VB5-TkwEs=w544-h544-l90-rj",true, true)

    dbHandler.addNewAlbum("Erykah Badu", "Mama's Gun", "R&B", "https://lh3.googleusercontent.com/2-ZluU3Ncllrah6eK_CwTHDU6NajbpfPUGQyLzh__ogXdrG6ORLZ9mqzSFWJpsc-nSkqntXVXP2nI-jH=w544-h544-s-l90-rj",false, false)

    dbHandler.addNewAlbum("Corinne Bailey Rae", "Corinne Bailey Rae", "R&B", "https://lh3.googleusercontent.com/nMICy_zvWmKOrgZzC-mXvs9TRJHspHn0hekM0Fexzn8oAoMDfml2pDyNDijo9YbMfOqXgoEOB81hPKQ=w544-h544-l90-rj",true, true)

    dbHandler.addNewAlbum("Battles", "Gloss Drop", "Rock", "https://lh3.googleusercontent.com/fVpp1cOeAgwXynMQaWLioT-4GQ-loOS-rKamiALlrB01gbGes0rvEQ2Xm9IKa1yznv36XMvXMJPaLNDA=w544-h544-l90-rj",false, false)

    dbHandler.addNewAlbum("Kendrick Lamar", "To Pimp a Butterfly", "Hip-Hop", "https://lh3.googleusercontent.com/L1iBW0CcjEQaXLP1coivJbjf7zSUncQ65_GpKHakOaRI81kS5pRV498PSg3VSmQg7LRMB0cJ6d-HzooO=w544-h544-l90-rj",true, true)

    dbHandler.addNewAlbum("Alabama Shakes", "Sound & Color", "Blues", "https://lh3.googleusercontent.com/-BSnFP-OR_61cSRg_lYPG_Ry6D8uKUSm4uXNgqJYrHcJRyI19OSfOcRWMTRXXvhHZaugj_wLJf4KvbwJ=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Aphex Twin", "Selected Ambient Works Volume II", "Electronic", "https://lh3.googleusercontent.com/sH5tCW-kIkjmAGLEk_l_vnup1JC94I9CieCoO1Sy1tZHo0nVV0mlsvd3NkQN-QmlOIJ_JBS_4V0U_wg=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Arctic Monkeys", "Favorite Worst Nightmare", "Rock", "https://lh3.googleusercontent.com/KqQ-EhMSWEPnT5JbM_5xCtFsrDNv0W-NLz9KVDYls6JesVf5TmuzvP0ZSua47IT-NIgPBwBqh1TuDfyU=w544-h544-l90-rj",true, true)

    dbHandler.addNewAlbum("Arctic Monkeys", "Humbug", "Rock", "https://lh3.googleusercontent.com/1qAlv-2A5p19Mto2RnJH7ng1YYF369CiOA0DZzNMPv-60NmWGCiie1TREEtFf_5YiyrhBrSp8R2ah_w=w544-h544-l90-rj",false, false)

    dbHandler.addNewAlbum("Arctic Monkeys", "Suck It And See", "Rock", "https://lh3.googleusercontent.com/POSbzl8nGh11ZULMTgyKjN8P8puG7VEbRUu3ki_YhLCNu34OOcZoqm14hTTLczvEsxGOgWHnwn-V3k-hIA=w544-h544-l90-rj",false, false)

    dbHandler.addNewAlbum("Arctic Monkeys", "AM", "Rock", "https://lh3.googleusercontent.com/TwIWuNMyEgD1YFkKGe8B7wtqgFd2TrdqEmz7ll2K-oC-g8ryADK0FfY6BN_Bq0W_jswtJn9NcLNi_zW9=w544-h544-l90-rj",true, true)

    dbHandler.addNewAlbum("Arctic Monkeys", "Tranquility Base Hotel & Casino", "Rock", "https://lh3.googleusercontent.com/SklighIkbPuDlnUl5l5McUoRy16yqOowjXxtkd-eZVbWJu8oeE_kWWXxHjefzWIxLO5O-PoGywqo2oLRiw=w544-h544-l90-rj",false, false)

    dbHandler.addNewAlbum("Arctic Monkeys", "The Car", "Rock", "https://lh3.googleusercontent.com/hmnfhlmqmpAmGxDYdcmsI2FvJpqYRKvWJ_KGLHOPaf1SyWrngMLjYVvJu5Dqe8Czn2uLAiWXWY2UBxK8=w544-h544-l90-rj",false, false)

    dbHandler.addNewAlbum("BADBADNOTGOOD", "III", "Jazz", "https://lh3.googleusercontent.com/fjmKoHbiGqNagDG-QWHxIFmDjFyyaa9_RvIM7HyAkfZTHXps4yWxASw6zwVrIeNM5lp1-gHfPXt6XjQ5=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("BADBADNOTGOOD", "Talk Memory", "Jazz", "https://lh3.googleusercontent.com/FluIuc-89UB1BUxcr2U514N_jJa0p3XItNJEHw_HFj9D9MCmYCh-dLb4aXrrFWYqeEcveHGwofxepAUXOQ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("beabadoobee", "Beatopia", "Indie Pop", "https://lh3.googleusercontent.com/kohBdfQHUnO_ACItfnLF_xWQTixlPvrUseGZl9PceI501dXO78m72qDViF7OCcwCVKxt04YAnOc-8FMJ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Beach House", "Teen Dream", "Alternative", "https://lh3.googleusercontent.com/lwPSN3s9Yq8NmywrXpYuQ3ACcE7iYavaBGLafTOoSn16PvLOiec5ISPjlBSlD1jfwnEOcRyu5dAGM3MBZw=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("The Beatles", "White Album", "Rock", "https://lh3.googleusercontent.com/8MFj-k2DNUXPKOw8BawKI291ty1Wh8V4M3J6fiq4itWkjw34ncAem60h80eMzsD2XjczahdZEF69CQ8=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("The Beatles", "Revolver", "Rock", "https://lh3.googleusercontent.com/r8_4I_rvh2kHa9Y-mSTH72Z84ncYx0SzPVLXXqaLEPYQrWqB03dizqePdZXBtAUa_La2woSY6czcx1U=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("The Beatles", "Abbey Road", "Rock", "https://lh3.googleusercontent.com/bmG1q9eu3ub2CtYcgArvzpiehqUpZGuLsOa_B0Bxkwxdfsk9r7nRzAQy1P5dTjqerODLxq3LycWGWW5m=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The Beatles", "Let It Be", "Rock", "https://lh3.googleusercontent.com/0uSK3j19kosq8SmrnZZ_mlw3kL6ZWFcLRgt0cqhACJcA6cEfLgCscIllVfF-LjkuV3zhuYG6MSFih6PdMw=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Beyonce", "Lemonade", "R&B", "https://lh3.googleusercontent.com/CAhOO7wXdAj71wilHYNJSMxdX0rXxZ1NTiuDR2-bZ_lG1LIIgMRuwMmf95K2eXOd8uuXs_uO4N8uLCe0=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Beyonce", "Renaissance", "R&B", "https://lh3.googleusercontent.com/w25UrZ1ZCARH_x9b88JhkghA_yvjP8ID-AijtvPGpIhegcu6uKsMbOsxVr_eOMd-Y-fY2HzGQAu0VGk=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Big K.R.I.T.", "4EVA Is a Mighty Long Time", "Hip-Hop", "https://lh3.googleusercontent.com/tYylVai13KkYNRTyPxQx8rXqfMlMrLxOisoMIJkWM8DYn6IRpElnXVye0W3xr0UD8XQu6Y7Zbw_KOW6W=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Big Sean", "Detroit 2", "Hip-Hop", "https://lh3.googleusercontent.com/CJSaAXR7OyAzdhwaA-rJjmTNqCj8_G6UfVjiyNLESFt8GHgQtQmd4RP3ts3iIGxDqMJxTeh5qJ9LpVs=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Bjork", "Vespertine", "Alternative", "https://lh3.googleusercontent.com/hbuDQxMx3HOVTtumqSMzm64SiIymiVJqbl0DmEeNb8KEsUEuIIBO59QLW1xbt4LIms5vWGjm5kEovOKdmQ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Black Keys", "Chulahoma", "Rock", "https://lh3.googleusercontent.com/4wl--hHR2MHoyGLEHYm2OGkN-CIUAR-W-iXjIr1PwtNsAJp6gXt26o5C4Ga0_PqJKetXD386Hx-3MF86gA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Black Keys", "Rubber Factory", "Rock", "https://lh3.googleusercontent.com/aSZ6c9T6EIzYCLdVLHbtjN5P93-RPVOlxKZ6258ujiDdy3C-3sV0BOSdWrjOqKUd_IKUBaQlavutYPifNA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Black Keys", "El Camino", "Rock", "https://lh3.googleusercontent.com/oXkMqBsAK1FwXEdhwgRIpnWt989JE2eW7aJZtIa93lpZoFDYkyRtRbqut7v8DCDeA8ZL3zre4cbcQYKBnw=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Black Keys", "Delta Kream", "Rock", "https://lh3.googleusercontent.com/CZCBaJyLElPveUb2JPVY121GXYNBPsclz89vv3DybrwhTYSDZuU3KX-GDM4sl0A5yD6ItthWe374jOo=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Black Sabbath", "Paranoid", "Metal", "https://lh3.googleusercontent.com/upMg2Acc6jSVeEpCAPXxRBUYIHIO6rDCOHPAGNs9I17aObrhnCQ8HvQlDFyY0MxmtCkCOlAKkMTY9Ynl=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Black Sabbath", "Master Of Reality", "Metal", "https://lh3.googleusercontent.com/C8N3YZKZYjdICar0guifXRTEt6TVC0yL-WFJAB7ss17x9as2EXzZQRmnmlyyZ0_iqXEfY1TOKWaS5zGk=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Black Thought & Danger Mouse", "Cheat Codes", "Hip-Hop", "https://lh3.googleusercontent.com/6Y5VjqCn9RDkbMtlGMcuUIGQBzMfura8LaVodYOxneWJG2NfC25QWpcKFxC_KZpD2i1iG3Szh3btUnJaWA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("James Blake", "James Blake", "Electronic + R&B", "https://lh3.googleusercontent.com/5OCNMOQhbsogaoBw9rcYwh8GyAh3mLh7gxRzB0DjcLGU1HkyttveEeJT-xIwGTcfHXFg_LyOCft_Be-y=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("James Blake", "Overgrown", "Electronic + R&B", "https://lh3.googleusercontent.com/HA71F90pKO2xhVHjaP4alaZ8NVjeFKlpHpyfCqCmvFGxRkRI8HRnrgpCbG4T6YdGlynONQmprMLpizM=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("James Blake", "The Color In Anything", "Electronic + R&B", "https://lh3.googleusercontent.com/ZAPTDFcCr_xD_0rv7lJnpV6lSS0WleBQp5H4qmRl6TFZURsn_02jaZawnNkiI18KIWvrr5B6Tg7QgTmuEQ=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("James Blake", "Assume Form", "Electronic + R&B", "https://lh3.googleusercontent.com/prLj9XwJPidRA6fstWrZPKa4TkgGhlt1gZMiCZAAtKhEcvzK3dq8iLRY985uBicpDcoZJGaT4-AN0DCn=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("James Blake", "Friends That Break Your Heart", "Electronic + R&B", "https://lh3.googleusercontent.com/zcMaPECHLRSt7UPDlDEaMHvJ1UpRnYq25yZ6qv3f21ZhfXXd8Faby9UB1eH6R-GovJrIDcx7bwWy2gTt2g=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Boards of Canada", "Music Has The Right To Children", "Electronic", "https://lh3.googleusercontent.com/oS5AAToY3DnWpAAgYXEP7_gwGk9E72CaGv97msKogAfurIJxMYPQjmXRVwonEVXdj_P96T_GsT0eCQof=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Bon Iver", "For Emma, Forever Ago", "Alternative", "https://lh3.googleusercontent.com/2GJImF6YnRG2_8Nt94G-D7BtyAefQztNYEzyR6V3EeZsdOJPWTNMa3Xo8p7F3_MG50NgscBbPRHL_MRRgw=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Bon Iver", "Blood Bank", "Alternative", "https://lh3.googleusercontent.com/MRcymkKdrM-c0RIKrD_0d13q8S-fxmbZggcFcLbdznRWp-gr-g0ZVgfenFP2JuI9QyZwD0_HRsrZhcA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Bon Iver", "Bon Iver", "Alternative", "https://lh3.googleusercontent.com/Fben3OhnBAnkhYp2hQG-Au5gDgnMe0aPfMmXozX2QT5yGCpxZY4om67TtmLL8J-fJYWNzH2CrSkV5eNy=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Bon Iver", "22, A Million", "Alternative", "https://lh3.googleusercontent.com/32iyoC2RbDN0RIm8_5h16WMRuGWisdanzkDiBs-rYAsx_UCfTUXpVPrnUwdUBIeXHFVsjIMMSv4w6ck=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Bon Iver", "i,i", "Alternative", "https://lh3.googleusercontent.com/8u5eS_ii4V9A0S5-tIaRRoRl3pQgCQxyarb8Vsvk-Yx88VyZoeOKNI1ztPDMb3fJbgZgyLj-uxK4c5FB=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("David Bowie", "Blackstar", "Alternative", "https://lh3.googleusercontent.com/osVduUi-Vi0PYVTb7HXhTBXkbbZ4GiPnpchUEGzYeiflNEkvFyifs8N5FOx_-EYdIYp1ThBDsb21zbM=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("David Bowie", "The Rise and Fall of Ziggy Stardust and the Spiders from Mars", "Alternative", "https://lh3.googleusercontent.com/ldn2zd7THwOFCPWJkEc1IM7mL1HcaR6IEF9oUHc90VvkJYu7CHIsRpLg2ajQugWCB_hmsDcXMS4N4XMU=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Brand New", "The Devil And God Are Raging Inside Me", "Emo", "https://lh3.googleusercontent.com/tGtj5Qhm9iMc54OMcKrziNoGz1BmNn8tjaV60ly5UfddAwy15LIix2Zz5UogKgrBNyV1jMZ_vwY_hyg=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Breakbot", "By Your Side", "Electronic + Dance", "https://lh3.googleusercontent.com/r4DZZrALLj12xZkDC9oy_PAUfFbkAxGAGbCL-BMmAzRjVOyQK--SyibphpvOcTwe448BG_6oE6MHuaa55Q=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Bright Eyes", "I'm Wide Awake, It's Morning", "Alternative", "https://lh3.googleusercontent.com/_I0YDDLAvEZlhYmfxN5pFl059i9tmUse_JwaSMGK0szaJJvoEWUvrLTYlOMLDMmRtabjjD7DShDuLeA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("BROCKHAMPTON", "Roadrunner: New Light, New Machine", "Hip-Hop", "https://lh3.googleusercontent.com/D9LtV5EeyBc9XECNCPaRuXV-ThH5rxfJh5M3Jk-coLGwWHy3u2jXWXB1kjM9CRnpeBn1Sby3A7Zl2_2m4g=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("BROCKHAMPTON", "The Family", "Hip-Hop", "https://lh3.googleusercontent.com/WZ9ZOMMsfzNKqmiH8090HYVGd5tyaboGOLqepjMffZHapFgHomO_-H23sqmgU4Er8FPGvZVfBeAGn7BpFA=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Danny Brown", "Atrocity Exhibition", "Hip-Hop", "https://lh3.googleusercontent.com/wVGinGqUFsjw8y9s_kHQ87J8Aaxg8NBclP-Fg0WTz2Ns8bFDBBobjaRefF3kInj-t_Cwdcs85g9EZcg=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Bo Burnham", "Inside", "Pop + Electronic", "https://lh3.googleusercontent.com/K0m3RQNNJDMZ-huRUHoNjZmBxPywTNNdDmlPTfWfYH1DKcLeRVPtvb0Awqm4WOfQC4PEUa18JjofiedE=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Cherub", "Year of the Caprese", "Pop + Electronic", "https://lh3.googleusercontent.com/NZiGTlOpHeJLmwL2MUa4m2I9fcX2Uq4Xe9PbyrgPUN4r817_jM1h7DY9dhQXfndTM6aALtOIeJFmlMv3=w544-h544-s-l90-rj", true, true)

    dbHandler.addNewAlbum("Quelle Chris", "Being You Is Great, I Wish I Could Be You More Often", "Hip-Hop", "https://lh3.googleusercontent.com/Lbj-gDxLHZZe3f6k4SlEEYj_T74SHKArjA8Y5AZMLFbg2zVbXDciKxji4ChYQUnmbFsxbCQeeDnIOTOSIg=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Quelle Chris", "Innocent Country 2", "Hip-Hop", "https://lh3.googleusercontent.com/fTfOoKv4XuBguCzA-4fW9mjuKyq3tpkH-G7tVR_r6NjgJh8VALbUGSIZG1-ZGFTAZohyVX0FSgXZzfC-=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Chromeo", "Business Casual", "Pop + Electronic", "https://lh3.googleusercontent.com/jIoZFGAgzJTFekKLBlYhq8QGHnOnfiDlsL9hzQE4zdo1Hqg1ZJ2G749t9oevEeRvTupOsob07HzIOu0LgA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Chromeo", "White Women", "Pop + Electronic", "https://lh3.googleusercontent.com/n_sqbHXksS7kN2DWbW4vUhnzk1pWTVneJSRHeIeZpkoBebiHQxdWjCbnMl5UleYloFp4m06lUOw61Xqm=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("City and Color", "Bring Me Your Love", "Alternative", "https://lh3.googleusercontent.com/Oyhy68Z-eigycV3did1cBqctxdrB9a1ScZ-N-BWHGpFNU9-660LymxXBku0fcADLlFlpEVOE21ZiggWZ7A=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("City and Color", "Little Hell", "Alternative", "https://lh3.googleusercontent.com/Ona9PdpuAOMtb5hEtH6EouhTR2nozGFT2LHOKiIhyhixLAmPXgdrSlT4Kbw7VPG4KoKM52wIcaYICvsW=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Clipping.", "CLPPNG", "Hip-Hop", "https://lh3.googleusercontent.com/_reLTB2B1xFrEvmLM1CgGcPg4TLcJdnyZV8u0DPuDOX-zW664LW8u_1jrruRNVsukR_Qz7oE5V26n4YB=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Kurt Cobain", "Montage of Heck", "Alternative", "https://lh3.googleusercontent.com/1jJdUr0rGFEHhj0TtmumXk5CJIn3nG0-yonvw85vOVmwC-0250c80aaoOnVDpxZC1FwUEcOKkVcALfU=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("John Coltrane", "Ballads", "Jazz", "https://lh3.googleusercontent.com/FERIxMR4QmP60wzvOIZnvWZkBJohfWhkmU1ykDGibp8iu0od0WCPm8h9PI_dsLLyrgt76n0z0SLBBFqx=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Common", "Like Water For Chocolate", "Hip-Hop", "https://lh3.googleusercontent.com/6l0ksUuAdotayzeJqkPouOAkhnbyz0lTcAb8t29ojHxiOhWEQG2MCWFI5PHxrqaj_r97mdjfQ2qhmRh2=w544-h544-s-l90-rj", true, true)

    dbHandler.addNewAlbum("Common", "Be", "Hip-Hop", "https://lh3.googleusercontent.com/jOmvoXNqA7ytOEyJG2SoSJ-3YWHxS0TAsRQpETfUziegaFFG4xD5-z1vvgZPEz1MuF6JXUe4nJYv-iKg=w544-h544-s-l90-rj", true, true)

    dbHandler.addNewAlbum("Denzel Curry", "Imperial", "Hip-Hop", "https://lh3.googleusercontent.com/SNipfgw-vEnjdm8mrF8_lAsevkWBv2lowXKvhKNtOg4UmYaCOi2C_fr_JHHz46sOnBweyVF18-XszJVh=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Denzel Curry", "TA13OO", "Hip-Hop", "https://lh3.googleusercontent.com/QPo7ihgY6A0pCBGrXXi1QJjEJiSUL9y-GZPSnemyWNhAwnp-6H6a__B3-U-84QGNLaPeQpWz9afPv7BI=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Denzel Curry", "ZUU", "Hip-Hop", "https://lh3.googleusercontent.com/Jdya-2v03qMV-h3YNtL_pABsThMlfBS4LYOzybf3YDT_fdKDSD-9yRNXG2ZzlKKrirn8fQNPLijfGp4=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Denzel Curry & Kenny Beats", "UNLOCKED", "Hip-Hop", "https://lh3.googleusercontent.com/dKgsDnjBPRJEGrj_8s0NviPwxzAUN3AQlJ9Of8oSgJMq1M28YjlCR_s-TqOrPZfYoX9aw4WG-z65QL7N=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Denzel Curry", "Melt My Eyez See Your Future", "Hip-Hop", "https://lh3.googleusercontent.com/lV0uOpigfKhkbjGCnlaP_RuAz1X6q6GVUvDmKMKt0oZgbrrtBYwGLACb-RGdfTR2Y9tg9w-t80GMVGa-HA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Cut Copy", "In Ghost Colors", "Electronic", "https://lh3.googleusercontent.com/txiW64E2jKqrrwThTysPoVwhAI1wO7ZBSMWEI6WdZgD8M18WJMXUv_Eh8EnN8k-yHGR3BXzQgKwIYZbc=w544-h544-s-l90-rj", true, false)

    dbHandler.addNewAlbum("CZARFACE", "Every Hero Needs a Villain", "Hip-Hop", "https://lh3.googleusercontent.com/9o4ivJr3lLNYmmqiBCiXNqG200usbc3Nlast9Int2Hj_KjcUyd4iq6JLVx3w3OLfsL4MC7PbVEQEUGY=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("C418", "Minecraft Volume Alpha", "Electronic + Ambient", "https://lh3.googleusercontent.com/mR9tAGGsc0rLt29HN0GsgA5Q8hU_ZJps6w8GxEPwcuUF4GU6RS8Aup2jrhi4U-cFh5sHG8-Hvdn-Pbo=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("C418", "Minecraft Volume Beta", "Electronic + Ambient", "https://lh3.googleusercontent.com/xXmjE7BPejCsw-IsYVa3v_qzsmgNd6ByXoFcVHlmyk3uRswZh-ra19NQqRe47vxLrzwwR7-QFA3xznc=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Daft Punk", "Homework", "Electronic + Dance", "https://lh3.googleusercontent.com/1gXa4i8m_NecS7xNqLqzxy6rdOWBh_L6U91YYv5ULNdmvvYT1Pr6ld7_SyUs5L52pEQheVyfU9SnXUm9=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Daft Punk", "Discovery", "Electronic + Dance", "https://lh3.googleusercontent.com/6Qy_3-Unlf67FwRfF75ghzWw__5wmbJjAg7x6p8qQqMdRxvvFCEmyFlEedmbunivDbF-yUEXbb3AVI0=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Daft Punk", "Human After All", "Electronic + Dance", "https://lh3.googleusercontent.com/NJ2_cbvShSDodKDfMgccx0deTdetTd9-jyYaquF5vQaYv8D6igBV8gId7_nyjkiAsp7c4OaR7ah5X_E=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Daft Punk", "Random Access Memories", "Electronic + Dance", "https://lh3.googleusercontent.com/N55arCGj69gtw6thXK8JUPisxoVYiwuIEQ7I6SGlkEyNcSJ7xIWPe76Vuu1SiUqRyx5w9qvR_zV8fV3CWQ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("D'Angelo", "Voodoo", "R&B", "https://lh3.googleusercontent.com/1124p9CHu9SV3XgxvRCbmyzsHkeFGqNkMK95BgoS9500yaHxwebyXLPWW1k44jvCUr0mTUNwQF56dOSD=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("D'Angelo", "Black Messiah", "R&B", "https://lh3.googleusercontent.com/FBMrSKkxes_yNSWloqaJqCnkB6lcOh0bo5E9qhMgYWeA0Xi6EGSV2kOLHfhDbTSyBOYzSRYHBbOq-l37hw=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Darkside", "Psychic", "Electronic", "https://lh3.googleusercontent.com/GzG1k1VBML479tVLv8-A1KjXmt4LxD46yDt7-PZVqGT5QqL1Z2fX_pCITY-cQdXRTJFPo4LLnwnbisfi=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Miles Davis", "Kind of Blue", "Jazz", "https://lh3.googleusercontent.com/oWD_2XIk6Q-QhPQ2MxU1zVSOrpqFwSPMQpVbZhbanh0SuE9JQ9tEtmwajZDncLt7jr0QIOSLRzjGPgI=w544-h544-s-l90-rj", true, true)

    dbHandler.addNewAlbum("Death Cab For Cutie", "Transatlaticism", "Alternative", "https://lh3.googleusercontent.com/Y5So9ah1pwJqvLElpGRjB78t-owAvsquIoqod6NriQ1QIEHyjwDWaPyI9LYofHFHwm88p7X_-LW4PXkOsQ=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Death From Above 1979", "You're a Woman, I'm a Machine", "Rock", "https://lh3.googleusercontent.com/bxTt7Nll0XOkJ8ALKw5qt8kj__rY1Mh6rPE3wwcs7n9xv1exZ0Ayx6y86Mfjg65hlVm1Xy5G53BxWco=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Death From Above 1979", "The Physical World", "Rock", "https://lh3.googleusercontent.com/67-FkR9W0G0u2ufhL-3HH1CI-HygOto93oYj0flEmF3pjPnirJnUEaG44YZQqriU5XuJVl9aVeIkdIRz=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Death From Above 1979", "Outrage! Is Now", "Rock", "https://lh3.googleusercontent.com/r0Z8aq_0eQ-quspFWqiwdoOE_9VMuONvJ5fwyOlMdFTJu-uV4V7nlpDF2P0pHgV0-LLWLncfWpyDAzIU=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Death Grips", "The Money Store", "Electronic + Alternative", "https://lh3.googleusercontent.com/WmWd-wg3LsIjvefwD2VvfqCeFLMmCTZlj8QlGSL373bonYFy_SCZK_qsWBIfRF7vbl3aU0GTGEAErSs=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Mac DeMarco", "This Old Dog", "Alternative", "https://lh3.googleusercontent.com/EyoVvyz6OU37qG5TixB5089qX3AwzkoLBWbTeo-PExE-XDBktc74QPw4psN6_g0dft1S05ZAidx_8nRI=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Destroyer", "Kaputt", "Alternative", "https://lh3.googleusercontent.com/ar3UeHf08saZc3IENgcmQBJCEfF5uSrc9xxwHiTLUmxM4IQRbYwKUu_mzEGp1jZ7juOs2cIocDDJU1Y=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Destroyer", "Poison Season", "Alternative", "https://lh3.googleusercontent.com/SKSE3mW6vix79jdMMgLtcfiNlanaJapBSeLALZt57fvk9gAYTd-Zinmo_8SJYY3BuTU2juPisAXDL8E=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Dirty Projectors", "Bitte Orca", "Alternative", "https://lh3.googleusercontent.com/Mk8FOva1YMhqfBAY3A5a5MgxP6KUngg1kMCmvfjBLM2xSL3MWjBN5W_bERQFH241GHaX61lCkjc-RSrlQg=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("DMX", "Exodus", "Hip-Hop", "https://lh3.googleusercontent.com/BftmNUASKyiKFBMKC841f0TUOyTtapjnDICw78KFtIUKkZdOhhIVzJjnCYUdtTjJ6_rBKHGz-HYP6mM=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("DOMi & JD BECK", "NOT TiGHT", "Jazz", "https://lh3.googleusercontent.com/QxjCAJDq-rV6AOIZSr-PebiPlBvJy2jBq0yNicOIdgxNhE7FMShMfcqv1GuEGrdPxzaKbmF2mT9LoI5y=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("DNCE", "Swaay", "Pop", "https://upload.wikimedia.org/wikipedia/en/a/a0/DNCE_-_SWAAY_%28Album_Cover%29.png", true, true)

    dbHandler.addNewAlbum("Drake", "Thank Me Later", "Hip-Hop", "https://lh3.googleusercontent.com/uXJr10BiQzxjWVGhi-fYzFJKqVOj6oy_Zqjm-rIpveDly5YD3kLy40lsr71JnRWCH6wgEgFq7F7KvitB=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Drake", "Take Care", "Hip-Hop", "https://lh3.googleusercontent.com/CqG8av9tm_qRWWwVhDFE1CpDZ2ChNInS0nmAyWfP5RkCuLdYNAKs4c8_azTbN3Uk3PjX-8zhVZSO_dM=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Drake", "Nothing Was The Same", "Hip-Hop", "https://lh3.googleusercontent.com/aSp4EnXqOygwH4tf43nVcizpTsb9vyYRygWw0Zu55MwysjThKNkICGHtwW94hpj_sQ1oC9_fGfGWuiIM=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Drake", "If You're Reading This, It's Too Late", "Hip-Hop", "https://lh3.googleusercontent.com/SlmTZt3dmf2H4G92zV34XKiQACYdDKwve16hSMK6UZ63Gas7WGYYoKq6M83KL_MXhA2r8HYLwp4OqZI=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Drake", "Scorpion", "Hip-Hop", "https://lh3.googleusercontent.com/9Oe4acEXgmAlCKgcgI6JlSXi2Tj30u6anzvfGBrunGO-fLhBTgzy-ei1ugPJpZDD5ArKFod9H4RTA5g0=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Nick Drake", "Pink Moon", "Alternative", "https://lh3.googleusercontent.com/YAMU3C4Ok20BAxL9bYH00p6A0kpw7P-Frq4joYTw4T5ogCSotqr70uuYTOsRDwuq9-Alf_QL0ppwE0e1yA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Dr. Dre", "The Chronic", "Hip-Hop", "https://upload.wikimedia.org/wikipedia/en/1/19/Dr.DreTheChronic.jpg", false, false)

    dbHandler.addNewAlbum("Dr. Dre", "Compton", "Hip-Hop", "https://lh3.googleusercontent.com/fsquuB35XDpFJY6aOs5a6G1YnM_W22tnvzvcADkZC4_92tAqmBztam1Ik_iq-DCPUvQP8vpauqsw7hu7=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The Drums", "The Drums", "Electronic + Pop", "https://lh3.googleusercontent.com/XftzwajgoOI0GMI69zFsyxN5NW73gWL30XoMVxXpRrCNzo8eYPQ3X98i8Wf28Inj-QQKM9b_VFA57ObknQ=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Dua Lipa", "Future Nostalgia", "Pop + Dance", "https://lh3.googleusercontent.com/FsKRGdJdubNmpx-f5r1GSL9vknmVtv1tAYu4WGmKZYoke-g0i5SKxRFnPY8HxIx9TEkAqMIPX6JkBplG=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Bob Dylan", "The Freewheelin' Bob Dylan", "Alternative", "https://lh3.googleusercontent.com/fGFzpmso362w36ndYkLaSwALZg9jIQbn21qkf75uIvrVf_v0kFDwSoZOtrbin2kHfDQvkCi9lKJ-O7k=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Ella Mai", "Ella Mai", "R&B", "https://lh3.googleusercontent.com/TqLZmcnSXiAogyfopdHkn88RgAacgm2jK0XNoAIaR1uNoECWdmOLyiScsMZHX9MtJI63g_weU1oQbac=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Eminem", "Marshall Mathers LP", "Hip-Hop", "https://lh3.googleusercontent.com/O-SeXg61tYo15uWSBzWPVoUlBx2mQAioidlHq3y-AurmaQZI2Vso7BJoGeDNAm2g6HquAq1z4g-1EBI=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Eminem", "Marshall Mathers LP 2", "Hip-Hop", "https://lh3.googleusercontent.com/MN50zTHFkftHsLUWgtbJm6bsfr_4v6ymnPyq2Py-QAmODCLzOCZXfUUJd_DA24Z-Kg5AADrVmaaR2dF9gQ=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Evening Hymns", "Spirit Guides", "Alternative", "https://lh3.googleusercontent.com/J05A9q-W_GHBljcUhCJ-VK3TJSKxsfNgt7XzNj5dfQZLEI2uU4lnUYN7x6haRadMcZ02KVuYf6p1CRln=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Explosions In The Sky", "The Earth Is Not A Cold Dead Place", "Instrumental Rock", "https://lh3.googleusercontent.com/TiphIJKRC3aT6yYRzPzJO7VJdzttWVkJVv6_hiAUzsAF1nDTGTgOV88SpTUWn_m3aSCybBXMju0HDQmU=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Father John Misty", "I Love You, Honeybear", "Alternative", "https://lh3.googleusercontent.com/VYqqjICfOeZ_dL4Q3wORbj1313DK8eJdSPN4jRVsGvWrleLLvxCEy8GGuCcv49jMZqu-COePt6eBvCqr=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("FKA Twigs", "LP1", "R&B", "https://lh3.googleusercontent.com/zuI1LooQXbJY-xvQvsjukuOds7jenb80p59FOVt2lD8e3m8sy4greRnB_hAauYUTLVbGuGir1VmimWAsFw=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Fleet Foxes", "Helplessness Blues", "Alternative", "https://lh3.googleusercontent.com/AtpooDdL_ZZlN4AQXcvF5ndiuhWwvFiwwOhOCaKxVwQFd5VceRq9GHcJdot2uUlXey4brnQHtTmrgoxE=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Fleet Foxes", "Crack-Up", "Alternative", "https://lh3.googleusercontent.com/_-LijllpoNmzirv6UZ4-sR0sTfjcUp6Z0jvTLX6jbgl3suzQbl6VM2P9sv_BsuEudVealRFb7Bz58WCSCw=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Fletwood Mac", "Rumours", "Pop + Alternative", "https://lh3.googleusercontent.com/q9xfDFsJXtmAU8MNFyGCMMHe7upmI6S8eDNrPdbJFRr-jeo_xNy8JDutjP2xdpmtl0xfQJYleYrXQGc=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Florence and The Machine", "Lungs", "Alternative", "https://lh3.googleusercontent.com/af5hg1bnRhz5rISIoIxxskutD78hUTDQfo_5q1-5qbz4FQrcyJ7OaP-_If2GuI9Li5AZITFlVgyO0aDJ=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Flying Lotus", "Los Angeles", "Electronic + Instrumental Hip-Hop", "https://lh3.googleusercontent.com/2agVLl8rRRNTIjQ3dZXjdCoX39m8ex-7e9lTfyDAExWuMdeMo6OluaY7la7S6kQwLMAfk7bVYS6pS4LKrQ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Flying Lotus", "Cosmogramma", "Electronic + Instrumental Hip-Hop", "https://lh3.googleusercontent.com/-sVc32H7wgLZI_l4m_8Jah2PFzjgKFJjfD8PVCEnK0SPLU5K3g8jvb0ekylwkJUWn_BvFqkWNSSjxxY=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Flying Lotus", "You're Dead!", "Electronic + Instrumental Hip-Hop", "https://lh3.googleusercontent.com/8hYDDultP8oNNrcJTIAycBNDm6j3sydMzF16VIiYOz626cLOtWFRhU1kptYjzPB0VV-HuyVEtTuEUC0PcA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Flying Lotus", "Flamagra", "Electronic + Instrumental Hip-Hop", "https://lh3.googleusercontent.com/QyhC0gJG035BWqAM6pLGD_vmLqsOsuN8yowarUoRRfQ8JZtXvZzpNAn6nRQVmzQAWgWcIW2UBNHzd1V1=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Foo Fighters", "Greatest Hits", "Rock", "https://lh3.googleusercontent.com/z2ewiJO461Wgy39cibqByj-9PvqeeVjwTPhVlBzJ__e0G27piB3LWAoDJA3PLiN6AMMJNtSQlTPgQmzI=w544-h544-s-l90-rj", true, false)

    dbHandler.addNewAlbum("Four Tet", "Rounds", "Electronic", "https://lh3.googleusercontent.com/dcs-9RQsiPlCvWAC4Bk-2W_P_EVWyUF0GKJWmJy7gDurf3Ik9H5ro6M2nM4tVzCsYAS6kLyAt-5T9l0=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Foxygen", "We Are the 21st Century Ambassadors of Peace & Magic", "Alternative", "https://lh3.googleusercontent.com/0Sape2C5A4pfYHwA_MKnRw-iSqfvL0oueXqZHCfgbyjLlfdJvZZG22FtuaTvFjUVf7caVkMpwqjThicv=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Free Nationals", "Free Nationals", "R&B", "https://lh3.googleusercontent.com/jtwvL7g9CZB5d3XD6TSXTvJtryyWqDEk3ps1E4zcAEB5CR99AN9HgcWD6zLa7_psepa-qKWMKgmLvEA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Marvin Gaye", "What's Going On", "R&B", "https://lh3.googleusercontent.com/wDZ9b8Tk_8QyuTurmJ9N8uZ31xF095slzLLCaEvw_XO_e7tEeKVf0BlGkbuPyIcNRhtWQtQMdkqHC-61=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Marvin Gaye", "Let's Get It On", "R&B", "https://lh3.googleusercontent.com/TbIt0CM9uaVRYnYpT0Sfb60FkAKb3qilvpAfXcnU60W8USIyeZNn44CKtUfRgFGAXQ2X9pLU5-NhYTQ=w544-h544-s-l90-rj", true, false)

    dbHandler.addNewAlbum("Freddie Gibbs", "Pinata", "Hip-Hop", "https://lh3.googleusercontent.com/MX7la-sSaRpbPiPq0KKZMWfwz5rXE0VFWzWJkij9bGMMyOSSt_Mgh_P9M7HAF16-4qul7c_-CqPxSvFp=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Freddie Gibbs", "Bandana", "Hip-Hop", "https://lh3.googleusercontent.com/Pr0VXBlynWcqLU5HryKOymeD4zrrxPuvLP2lFf_mKkqeiYgRk1EvVDyfB-2rEu_2EGQA0m_Cif5PPas=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Freddie Gibbs", "Alfredo", "Hip-Hop", "https://lh3.googleusercontent.com/urOSoW8NJs-3fS3gpPKLtvQFOj2xI3MuLgD6vbk9hMnotB6bnIZep0NR23Aget96fynf8Hv01cJ0ZUI=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Freddie Gibbs", "Soul Sold Seperately", "Hip-Hop", "https://lh3.googleusercontent.com/QYH3O3zZnr6NbdWeSBcCVfR6ItiC3NliDsX4ZDZkegOgP5OvlHc3VyKSinU0CKgSxzeeLNfd8AyL_1c=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Girls", "Album", "Punk", "https://lh3.googleusercontent.com/9UhQu4SyhgejMIMtnT5M0A90taTo6e2M8O3bRCORgoR11I8Ur3PV-L0SHgS6FA9ljBwVGmLffXYIVdgD=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Giveon", "When It's All Said And Done... Take Time", "R&B", "https://lh3.googleusercontent.com/6fFiEXtIyQBPsgi5Sf-JWCRSucObgSMp3DcrF4z2Uc9p9v-3qkPLSIjDho2L2n8XIXcVH8yQ_1j6stBv=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Ghost", "Meliora", "Metal", "https://lh3.googleusercontent.com/EoUYn_Lef6Bd0OKflXG4TOBMBZGzbN1M4-vmGeiP-pGieEtJZ3S1JErTMumBDxZdIMltWQZxxJxCO1or=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Robert Glasper", "Black Radio", "Jazz + R&B", "https://lh3.googleusercontent.com/PUqMUdjmTXPfH-DZYhZ8CbFXcWGZjiYxNNJ4D9_VBMabNjQRZycq67jxT83w5UWJuCFn7ppI9_NR0Q2LoQ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Robert Glasper", "Black Radio III", "Jazz + R&B", "https://lh3.googleusercontent.com/FO4kM4CIRcHWxwYMQK8TqKt3yoj6_HwoFugbdDUeuSp8TLlMW8QimXWQxFqULkbTD0M5u7lIo5KJgus=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Ghostface Killah", "Ironman", "Hip-Hop", "https://lh3.googleusercontent.com/zRlVVq8mkSdStBqhPTt3LxnIq1A94m6J1w-C88l5basUjT1s454nhK_JyxIolKE5uJmNJwZSwN_2tKI1=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Gorillaz", "Demon Days", "Alternative", "https://lh3.googleusercontent.com/_3LJKVh0dy-71CwQzWKt0q7JHI7M2rxIMGGAdT0wEhmGEEELRX98ACJFdKK5Fyl226nuNfQ0OAMc-JtP=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Ariana Grande", "Yours Truly", "Pop", "https://lh3.googleusercontent.com/cwJUlOPNvE63q6JxQgIMHjF03yf6NQIBWB4i0hY6l8U125A16HwbJ7tnQqz74CAxtU_9CwY8IAxOz3ku=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Ariana Grande", "thank u, next", "Pop", "https://lh3.googleusercontent.com/3LV6GoLom0Vb_oNFTXI_e2NIua0D-WocCifEFnSQattWpLfswpayWPzZ2BeClAB5QxR2EnGZkgh7QI7j=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Ariana Grande", "positions", "Pop", "https://lh3.googleusercontent.com/2-_pSt_yjP16a7YPYGAHO4g9HLcNYdnXCfH-wXxNxXTGG7XWdJ93xLaHK92JrXGuVtjA86nbogM3Kx9l=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Grouplove", "Never Trust A Happy Song", "Alternative", "https://lh3.googleusercontent.com/PASCfipUn_piu1wRRgHZJs16jLNzlcYIJF6Rs1-ZkQ54VzIFZvh2LJlKZ654Wp6qKOEPYge-th9knuk=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Vince Gualardi", "A Charlie Brown Christmas", "Jazz", "https://lh3.googleusercontent.com/YX7e5O8XBNOAAEU1gbaXMmbUzpOSruRyB-3ZvODXNBa2ppJlue8IGCjk8iZqJQbSsuR0Ui7YxqICGrw=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Vince Gualardi", "A Boy Named Charlie Brown", "Jazz", "https://m.media-amazon.com/images/I/71lOEljHW0L._AC_SX466_.jpg", true, true)

    dbHandler.addNewAlbum("GZA", "Liquid Swords", "Hip-Hop", "https://lh3.googleusercontent.com/AluHgYEAyyw7f40L7C_T2C6fYdeG1pVynd-XE0J1KEEmdkW_gLoc3wfgIh7OVBG_tEFtEfc6A1Jvi0Y=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Herbie Hancock", "Headhunters", "Electronic", "https://lh3.googleusercontent.com/Y7M-KwaL3xCb7p3JlSh1eOIQoh-2UBHpmP_O1erze6CAdPSr0v9-_Cle91oT2y9SiyS_dUOSGUagnQY=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Jack Harlow", "Come Home, The Kids Miss You", "Hip-Hop", "https://lh3.googleusercontent.com/a5kb_FPW0DYzfrTNiP3gl71BNmXX7sH5lLrleJcmubo5mQaM-5x6VDU1gICUwBZelscqXYUeVqhYo12K=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Calvin Harris", "Funk Wav Bounces, Vol. 1", "Pop + Dance", "https://lh3.googleusercontent.com/VHdOBJElHdTf7hsG9c3jlmG68hB_z-jB0KR-lhCQET0ksmn70QWKo_emVsxsxd1oFMTZVEpdWgSq_cM=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Calvin Harris", "Funk Wav Bounces, Vol. 2", "Pop + Dance", "https://lh3.googleusercontent.com/xqpwQqwE4y-h_pZXE6l4rI0Fp9h_WPp0fOfQj5gcTjqEPr2aL7IJOH69JrIYFWdT-t9CJomxB9T7h6N4IA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Homeshake", "Helium", "Electronic + Alternative", "https://lh3.googleusercontent.com/0K3LaxwpQMUelj5f_duSvB9Ww-FTuCse-GWsZTwMouRXV6_4--Yp2Glfvss-YouN2eTdvkD459OZS5gl=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Ben Howard", "Every Kingdom", "Alternative", "https://lh3.googleusercontent.com/-2dJPyavFhTBlT8U5WSSxPtqcbKnx_rHIcR_PKNEz4j-4CQKtSSoIrj60ILKcu2ZATdJQv5OoQHfezg=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("How To Dress Well", "Love Remains", "Electronic + Alternative", "https://lh3.googleusercontent.com/nEG9jBwGK7jRXsqPEZl_I-lnYwWTHrdpnkHeeW2UOHfCaTYQi6phPXm7OASIeFIe3BcQMbgLD8_xLdJT=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The Internet", "Hive Mind", "R&B", "https://lh3.googleusercontent.com/LS0CXIYErYXxlzDTgZRMYFmu8KZUVkrP7sUzXXijk7OKvU8VmsjrLsRZ4BSRzZx_ER_mBf68XTYQD-D6=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Iron & Wine", "The Creek Drank The Cradle", "Alternative", "https://lh3.googleusercontent.com/XCOvTzRJ12Tozzem7Y8RfFPfOIG_mVaB428gHI-lP34vbMiPmqTjxVaNMH427GF_6x7i3OrG1j48rVOP=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Iron & Wine", "Our Endless Numbered Days", "Alternative", "https://lh3.googleusercontent.com/Gcwdy6x64MZz6UCn-HIsMFdP-xeSrOZGUqHowKakUtBgk-JHvX5UkfcI8EJSdN5wI0D6QizNB25E6K91_Q=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Iron & Wine", "Woman King", "Alternative", "https://lh3.googleusercontent.com/icGZyHj5xIYaabBHkON7UltuW8oaF4jSx-Zuj2B_DPmqPjnCYdT7dVXc3oLXE521NvhScm3X-lLo8EXJ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Iron & Wine", "The Shepherd's Dog", "Alternative", "https://lh3.googleusercontent.com/2iS5a6y9fDb2CVlia0wF87DkVqlXwNI_H_kjNxIj3nofX_8gbVCKZEedjQep6SyRTIqVabZUaNz7IegU=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Iron & Wine", "Around The Well", "Alternative", "https://lh3.googleusercontent.com/Iuuego2VGpR1M7S5MLG7AiisA1p5_n2NpTg1GG8ul7O8sUNksrPrLB7luhqNT_0CwhsFGqNVuD44zXo=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Iron & Wine", "Ghost on Ghost", "Alternative", "https://lh3.googleusercontent.com/OfBEJ8xbE0SNQfER-iyqRNrSmsqe7p1S1iG84bI4mml5R5xdaRwf0e3j6k-OPrRCIEW62qzjA41H8xw3SQ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Nicolas Jaar", "Space Is Only Noise", "Electronic", "https://lh3.googleusercontent.com/saa1PzZq8Qz51_mkkUX_WLONf4CLB09nwOMF7ASdZ6jWsW0RzEEO_PL7VszzepTTpS-tbGTnTabLSW06=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Nicolas Jaar", "Sirens", "Electronic", "https://lh3.googleusercontent.com/B265_bjIUZNPeWfe-rXi55Yw3jrKSUXHh20C0IdyFR277LCmKqm60OlnyKiYJozG_SQlTMNZkLnNpoc=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Micheal Jackson", "Off The Wall", "Pop", "https://lh3.googleusercontent.com/VeDOfVySuk98NJ1dYfFWOKUnY72OW-oeZ9nYQPn-Pz0eu1aE3gw-EFZbUCHY4ABOQvhuVGgHRTBhU4s=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Micheal Jackson", "Thriller", "Pop", "https://lh3.googleusercontent.com/URvHCfI2iyGAlAwqqBFeaFhU9DeKk_iuX40OIIIj8Zp0wIT3BVsJ2JRMwLLbUB9EZS7t7oDlMrI2S3OvGA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Micheal Jackson", "Bad", "Pop", "https://lh3.googleusercontent.com/7BMiO7nt2XYlLtuNAfdK38TxWOB2KjQeQuIeERdCIwjMm-8ifEAlN2obE64_jeaSPO7NtNbo12vb5RgIEw=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Micheal Jackson", "Dangerous", "Pop", "https://lh3.googleusercontent.com/acFvHA1OEoI0HBPPG33zidd9n9aG1OTvo7XQQeFjEeQObGv6R3464BvFijHerp3Sit5UeHvQnx6LMoE=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Micheal Jackson", "XScape", "Pop", "https://lh3.googleusercontent.com/byoydb0eKI_IzW48CVLw2aaLarAqD0rC4e5pi8bUNx6RbdZc6L8FgyP341fcl1xTbs9KudO7QW4eThxE=w544-h544-s-l90-rj", true, true)

    dbHandler.addNewAlbum("Jay-Z", "The Blueprint", "Hip-Hop", "https://lh3.googleusercontent.com/i5rIbvpZHN_NTTRiN6CAeb87cK_3nHbKUL3YcyPkWJKd2n9HJyaIBEBZlS6NLUb4oUXLxQlzkQZv3pAd=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("Jay-Z", "4:44", "Hip-Hop", "https://lh3.googleusercontent.com/-LK3bmeS5W4y1P60VZHpdfLEV4NIlZ2JI2uA8gAe8BMK1Q66J-vKaZg6UJU0I1OGdCdvRa8Jx_eKt-vg=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("J. Cole", "2014 Forest Hills Drive", "Hip-Hop", "https://lh3.googleusercontent.com/kMrbZiHDjddAGv58UvL1T-I5ddCzUAAclrmIMaY3ty4-jAxHueE4pqJ-SX8o_ggXimkPMcsKxh9Ev45P9Q=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("J. Cole", "4 Your Eyez Only", "Hip-Hop", "https://lh3.googleusercontent.com/GH88zfbJnZciQoBt2jw6CHb7ODC4pYAK2hDJsZLTMES614krFbofidiwDhy4WVO8lgFqL7Q8W1qCOo69=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("J. Cole", "The Off-Season", "Hip-Hop", "https://lh3.googleusercontent.com/hFDL9kSJU1jLk2MAbTybueKos0-c_U8uV1n7BtqkGVKYcIgDJQP8dDwBQBbsZDxkhIhhWMIfCip_5-Y=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("J Dilla", "Donuts", "Instrumental Hip-Hop", "https://lh3.googleusercontent.com/HDhXSELzte-32PaZvJ1F5yimtYSgMQ4eJBLg39yLDX2wbw-wCXu3ZzXry5M5fmmUGm8-MkPnZveOrPI7=w544-h544-s-l90-rj", true, true)

    dbHandler.addNewAlbum("Joji", "Smithereens", "R&B", "https://lh3.googleusercontent.com/ObLzZwv6cAEx6a07Mcy8X9FbaeeCwj6ayAxbfUq5Bp5zJSb5fcnNl2jD4cxV5WasMPwORZ8jpMIRdcfb=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Norah Jones", "Come Away With Me", "Alternative + Pop", "https://lh3.googleusercontent.com/1ILUsnIBFNDUbUPJgWvi-p07jPUtA2Mm8tfwa6-1zvS0jr4uA2ay-2I46EXEkYhdCRtqj66WNx_uej8=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Justice", "Cross", "Electronic + Dance", "https://lh3.googleusercontent.com/E7X8YjLthibjQBkpGhTQ42VzD98Td0ShU4VHmmcQ0ut76zNMyI5XlFJDV9bP6MDPG0IXiW0hEr0Do0s=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Kero Kero Bonito", "Bonito Generation", "Pop + Electronic", "https://lh3.googleusercontent.com/Zk5ujlNBA1BD6VfPIiebheHxL6TCdQr6ZW3YTkFKaQw2F6mOp-dk_HPSv1Wy_kU2V6-mPMYqZwtoiiwtWw=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("keshi", "always", "Pop + Alternative", "https://lh3.googleusercontent.com/4FQW8KMxGpe1P1lh42YKJbb4W9TSS1bcVbNPX-qbe7LIDMxd-di6nwpg8sWVTBOkmujJOWD_Nm2knMU5=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("keshi", "gabriel", "Pop + Alternative", "https://lh3.googleusercontent.com/lV3RPEjaE6cHOzbHTx1ykEh7BhvXjDdEHxZHh6DZKdX_z8N1V_sZ1_Xe68hqEC8tFcPAE4UyM_M3l5I=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The Killers", "Hot Fuss", "Rock", "https://lh3.googleusercontent.com/soFyZYOggw-Ae05b91WiVDnq9ahXLOrNJiR4wWiQemfclVliG_BHht1bH6f0cjLB0KzKlyGBTsPrtQEn=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("King Gizzard & The Lizard Wizard", "Nonagon Infinity", "Rock", "https://lh3.googleusercontent.com/wYaEW71GWtQN1VrIFW4Opi-DMgSTrhvRoQJ5AcU-ei_osXTbXpwXB96TD-lzYkj9NAf2MbPeAzV4DQY=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("King Krule", "6 Feet Beneath The Moon", "Alternative", "https://lh3.googleusercontent.com/_JPxJ2HRkQZXfpz4pTx418oThqLQrOiEpO7DBqB1cXdAugBYOWTeKh0gAfhm2yXpPnvzxp_VIGZNam1_=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Kendrick Lamar", "good kid, m.a.a.d. city", "Hip-Hop", "https://lh3.googleusercontent.com/Fz9_8koA1VbRz51kyUaOHIVDQu7LCx2W0lDjytEXz4KPGL3VIV5LS2F0uISIHHCvqQpbgHl3oCWIG6I=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Kendrick Lamar", "To Pimp a Butterfly", "Hip-Hop", "https://lh3.googleusercontent.com/L1iBW0CcjEQaXLP1coivJbjf7zSUncQ65_GpKHakOaRI81kS5pRV498PSg3VSmQg7LRMB0cJ6d-HzooO=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Kendrick Lamar", "untitled, unmastered.", "Hip-Hop", "https://lh3.googleusercontent.com/WyQpPjYrmjGtwLIRdn-VBj19xwskKywTDil_TDd7vtvG47E4WbKfEl8uciJ7u6yjUdnbV4LizVoEF-kw=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Kendrick Lamar", "DAMN.", "Hip-Hop", "https://lh3.googleusercontent.com/kJhdRiXooM4L1CLW3EtC-okkwhPUrhFu6psYDJW17RMxCuNtXiQ-e3OfdmQ_m6ktyVmLKaP9KXEVIM2JHA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Kendrick Lamar", "Mr. Morale & The Big Steppers", "Hip-Hop", "https://lh3.googleusercontent.com/ZSS6In_G6CBqzWhCtltNUxoirj2MUwOJ1MaVEauxTePsjBt6OwOtC6jN1YHd3OqnJbRq84rLk-Cy4dW3=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Avril Lavigne", "Let Go", "Rock", "https://lh3.googleusercontent.com/MIAqmIn_RcVQpBbnF6iqTOJq0gHTiZXlcGQwEHjKrglyt0RWhCJ5USCeQtbM2JR2UTCQmpJK6QXWl5M=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Led Zeppelin", "IV", "Rock", "https://lh3.googleusercontent.com/bIhbKGpuSJlLs-uZczW9ptX6t1jWcbWCwIf1gKzHaT7Wnro6E851_HtRzZ0I0SbITzvvyDsGSqbsb4E=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Ari Lennox", "age / sex / location", "R&B", "https://lh3.googleusercontent.com/K6-zdrMCjxVvEVt2yObhFs3XW5j1ds7ya3vzMglh7ezGkaI5dx1zDJAAoR_QMvypgufLQfWpvFa0-Kg=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Lil Uzi Vert", "LUV is Rage 2", "Hip-Hop", "https://lh3.googleusercontent.com/k9Uf0CQSGm9OdAOvLkaaSLqJVbm74H3sF8vUArpA4SrQM4vYvR5OBOCNiguLKwMTLC77y1dUFSUbLAVk=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Little Simz", "Sometimes I Might Be Introvert", "Hip-Hop", "https://lh3.googleusercontent.com/TWllI0de14Q58Nw9DSm6Vde0gmVCdD-JzfsiJoa723ekFaH5nRZxxbN64jMLbO3Q8dgkr3scbNo7veGZ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Logic", "No Pressure", "Hip-Hop", "https://lh3.googleusercontent.com/qMGukkaz6MkMAbDSzJjq1U7oaNeUHh10l3Eb6zMlbmQ4li02w6dhd30qhbQQ4uT484NCnLyllevknhfd=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Logic", "Vinyl Days", "Hip-Hop", "https://lh3.googleusercontent.com/m8jXPnY9BKN0JRgdDCnkiOKRx_zeZR9B08erwQrCQkdjqtCc6uAfY3Yqnl3zc3hM-VJJyHqs2q5a6hAp=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Lower Dens", "Escape From Evil", "Pop", "https://lh3.googleusercontent.com/8HZoqR-P50tW-9yYUraE6ByQ-I1JxAGjEdN6IVg1aih6aDD6KYceBsy4pshPOcDGGyjrSsGume_QAGot=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Lucky Daye", "Painted", "R&B", "https://lh3.googleusercontent.com/0ydwSWFDBE4X33qaKAGpWDw-oLbKtdZUpnXJwFAO5nYVO8lwZ26KMZWOBIqrBuO0jtPXbPwZEPPP8AQ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Lupe Fiasco", "The Cool", "Hip-Hop", "https://lh3.googleusercontent.com/vhO86RdnF2VfjLm7SRlEgq8KpHro-O0u2N8Oun1WFQknv7-Q2coT0JF8SkvMOeyUWfNMaS__ATLNo0yR=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Lupe Fiasco", "Tetsuo & Youth", "Hip-Hop", "https://lh3.googleusercontent.com/O3o6-PRZtuIV3e7KlbqYRK1-Ur2PDOGpbX7X6UdXoTZfCtx0pBESfzKVlHkyd1qPIOdnFUE7we4Mcfy5=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Lupe Fiasco", "Dril Music In Zion", "Hip-Hop", "https://lh3.googleusercontent.com/IPmWzqTpN2JN_hNMdq_9CyG7v7A83ZBt517RQ192PFr_r2N5Di5DeikH2qCO8hlWxSBTn1xnjsqZP2mQUg=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Lykke Li", "Wounded Rhymes", "Alternative", "https://lh3.googleusercontent.com/01uANHMjE7bjN8a4cPIfRWUScFPdNSoHtg8kiER4C_hDMGmIVoKjahUcDeti07Cdor1W1busOVdHKez3bQ=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Madlib", "Shades of Blue", "Instrumental Hip-Hop", "https://lh3.googleusercontent.com/zxGCwe6sYwaVfioWF4h17kiSVC3yL6P-XwY33OkekPoSN4jtkF1I0yjoGP9rVQewuIB56Q3FnDG9v0HU=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Madvillain", "Madvilainy", "Hip-Hop", "https://lh3.googleusercontent.com/qVZAYoIAzy5WEq4p2RmS8MMKNM2IbgQEnXw3hYEaMFWyXKQ2bGd6uq2gzKzvrNwUog4lvFi9vJI2r1XV=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Magdalena Bay", "Mercurial World", "Pop + Electronic", "https://lh3.googleusercontent.com/LIDnqGMImyu_4P4pOYtYheP2tA_NXm0YNttI09nGQgBLucBXVM9IX1blQFHXJWp8lzu1L6nkVFmHDBkv=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Maroon 5", "Songs About Jane", "Rock + Pop", "https://lh3.googleusercontent.com/b2Qn9rmBW7jWJ3elYCEdk3zXwrlpdH8wCNyHiuHbwzsVTlt8xA2sQSts-qD9U1mIXFYQGTBWtsCwqdgIDg=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Bruno Mars", "Doowops and Hooligans", "Pop", "https://lh3.googleusercontent.com/-MBagZMcp-AsKziX22bIEvPjNlxPPyU8hgXC07dXlt9GmlBTA1hZI9yyAuCP0mswnSBNRMOvoI1y9_Y=w544-h544-s-l90-rj", true, false)

    dbHandler.addNewAlbum("Bruno Mars", "Unorthodox Jukebox", "Pop", "https://lh3.googleusercontent.com/m0WX28nJGAiRC8PgPrT7LrC5vRTWlSlQmnelZb2VR0HWDS-e8GPO0MvhEPM0C-KTjTtnvZsOS1R8y1cg=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Bruno Mars", "24k Magic", "Pop", "https://lh3.googleusercontent.com/L0TeCOIXRykdcItgMBf2qrO72RDFn59k3wLd3OFlXO3y4dfrSeR-jD1NCPKiJsRN7gaf4ot_dWSVjfwXZA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Massive Attack", "Mezzanine", "Electronic", "https://lh3.googleusercontent.com/eIwxQmmZOei9XmijUXzWsD9UIIPqW4xlzIHSyDq4hsB2KvAHC5vBQzg7Q-nnCdxKog3-5EZUyPsvzJkwMA=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("John Mayer", "Room For Squares", "Rock + Pop", "https://lh3.googleusercontent.com/y_RTFtwTAk98KQrIEuWBiSzYw8b7vc23J8Hv6_WmkzgUomTuqvYIu99oo6F5nYo3dVFifu4-zQecli4=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("John Mayer", "Heavier Things", "Rock + Pop", "https://lh3.googleusercontent.com/gREklZME37o4Yuf1oeKKTqfxlwGRm2oLeoxooQptK_CsjeQkF-YW2V_vWThFSnmGD8iubgXfRAC0ciddkg=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("John Mayer", "Continuum", "Rock + Pop", "https://lh3.googleusercontent.com/ZZB2xyt2onR7PxtYxAi4ryrQEY6OCzwReIeDWGMi5WckxjPXHeIimtL1c7ewLp9JopObbwU8f7ILA0BS=w544-h544-s-l90-rj", true, true)

    dbHandler.addNewAlbum("John Mayer", "Where The Light Is (Live in Los Angeles)", "Rock + Pop", "https://lh3.googleusercontent.com/Zf1E8O0mjplnOu_gzBcsjmJ9A7Yd-y_zz5aOC3NpI8ICy9wuvAyzbRbzikc7wIIs-xKE-WIGBWw9Rw=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("John Mayer", "Battle Studies", "Rock + Pop", "https://lh3.googleusercontent.com/NgEdgScqwRLrUuaoFzcgBHKlmaThJWdfx-q5UvzgYSdzy_jzx5AfwSpSsxKJbUDn45r8tb5E79taQSQ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("John Mayer", "Born and Raised", "Rock + Pop", "https://lh3.googleusercontent.com/Ib_DtKmjxbCFuxru5hY-JFsEf1FhsuiZDvokbgaVHUwYqfOMi3IuoQEjOvLEshpupdh5DeNuq6ZXpFm1=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("John Mayer", "Paradise Valley", "Rock + Pop", "https://lh3.googleusercontent.com/7bL2SwRtaHz5uHW2n_54kj-m2SSmHk4aOyNuERHkt2bP8hSFk61GcIfG3FuODozh7SNzna3n8Aw-bOJS9A=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("John Mayer", "The Search For Everything", "Rock + Pop", "https://lh3.googleusercontent.com/vVrb8RZZnlfjptDDeF056lqTt-N9K4m3a7ccs79eBt2wW7c1DbBvS34k3T-txLQ49dtttYdnjw50aEyI=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("John Mayer", "Sob Rock", "Rock + Pop", "https://lh3.googleusercontent.com/e96z3awL3K-pRBkXNHiZ45FvLFs17jR4umTqqSHwSfrYDj1ex0_uBU-6681SDK1N8LUbP6QjfUAulhRP9g=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("MGMT", "Congratulations", "Electronic + Pop", "https://lh3.googleusercontent.com/mT9MyMKSqMYZbF7C4SwW6iIHxQ_In2J2RdCInBm3i8sMd5_a1kSp1JDENv6nQKem3Rha6kVZIlu4GuYs=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("Milk & Bone", "Deception Bay", "Alternative + Electronic", "https://lh3.googleusercontent.com/2ZXiUuO1uLPy84hWO8sw8j3aKHmcP7X7K5jUI7m6nr-nsb_llCWC4Al6FOLZ04IYgP3MPod_vgQemOBk=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Mac Miller", "GO:OD AM", "Hip-Hop", "https://lh3.googleusercontent.com/zGWNROSmZ1RZf-84904K0GjZrOwqKj9scxoG9-e0xozUk-gsz2uriWk9cTcqMDQWlAITb545J2IC-ADSWA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Mac Miller", "Swimming", "Hip-Hop", "https://lh3.googleusercontent.com/YxyJh9VYIKgPAulnGiK6OMmBm2r_3eSTsm1myZu1qSKXlb77sSKlGl8VOgxU0tS1LJreNMntkktrp2Y=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Mac Miller", "Circles", "Hip-Hop", "https://lh3.googleusercontent.com/CEFIvocPbLa479SM1xgzhKUebp_m4e4mInjQtDAASjKB3T8vJF8BvNJNnzaQ9nNaxD316162j3Xs-z3E=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Charles Mingus", "Saint & The Sinner Lady", "Jazz", "https://lh3.googleusercontent.com/1IJjqFSNA6X2XMOuWBgJPo0GWD5Jh1JlUaknaNh_Q7uwGkh8W1Q-eUPNYH0qcO5tILbV97pa1fDef8em2Q=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Metallica", "Master of Puppets", "Metal", "https://lh3.googleusercontent.com/wK5h2K63Ey5234foKQXFeY0zDJ-a53NOCY2DKcmATlaIbhFjawJ_oXUBT6-dhaCN8xgNreuXobcUsIU=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("MF DOOM", "MM...FOOD", "Hip-Hop", "https://lh3.googleusercontent.com/F5Vdxlr4kBDXOlbwFpmhd8SMygnIztEksCjIivh4UkFIANOt0iaMVjOpjiot0t_o1Mp8ZZIxjMG-wEA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("MNDSGN", "Yawn Zen", "Instrumental Hip-Hop", "https://lh3.googleusercontent.com/uoRs4xFSZO4hf9pvdj__i0TFjxwbfyKR7JzAPDorPM6o4cZbY2ukS5VYtsvDvqJ1umPKUG0mt5Wowxwo=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Mobb Deep", "The Infamous", "Hip-Hop", "https://lh3.googleusercontent.com/0bw6RvOmvAbmlwi2G4oGZQb-6KSAG6E81CJYabW-e8UOOW8VuoTgvP-MfUKElc6NuITarLAUGCBYoitj=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Modest Mouse", "Good News For People Who Love Bad News", "Alternative + Rock", "https://lh3.googleusercontent.com/dJ-iSh9Rl-ChP79Kp4hkCm1lCcE-b1VE79WJDLxdyRJdHWPQVF5zCn859KvuiLOuKS9-ESRkTzHIbett=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Janelle Monae", "The Archandroid", "Pop", "https://lh3.googleusercontent.com/K4wLyHfv9Bq_kMwiKjT7TugmrXvQXyEIsCWYETZc1KAslmxrX3N5_2LWQrSAUosznjb_MfJxtXujDbQ=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("Janelle Monae", "Dirty Computer", "Pop", "https://lh3.googleusercontent.com/aKlmWrjKPIg5btz_lH6d0N2kT-TyYMjkro_Hz4A0aMCdcddAySHULNxaqeBPWiZeiMfYOm_suPSrgW3o=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Mos Def & Talib Kweli", "Blackstar", "Hip-Hop", "https://lh3.googleusercontent.com/GISSOPj3ZDYzeQSE9Z_YDC6_P7jOyofshKH4uxKRRmuFgIsT-H7JiK2AeXZUKYtWpq_VOaNhmujqALIi=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("Mos Def", "Black On Both Sides", "Hip-Hop", "https://lh3.googleusercontent.com/7qmM4fddGmYPlFfOloDVtSz8YG1M0JahYsD62_lRnGZ55WKF_pxdJdbcg183HyJBFIhum2E26UrceBg=w544-h544-s-l90-rj", true, true)

    dbHandler.addNewAlbum("Mumford & Sons", "Sigh No More", "Country + Pop", "https://lh3.googleusercontent.com/gRWTQpvgc2D1z14J0TOaLsxMkLE3_LhXSVxTL-d4TAPHAdPvL7OFEklAxF1RIRbMMV41JbT7NNTZFjGu=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("My Bloody Valentine", "Loveless", "Alternative", "https://lh3.googleusercontent.com/trQZLZIPWC_Fho4L36yIexCEwjrntb5rfOtpPkVfgbFNcTyjjszrUxtCZczuvBGjqQBALTfr5Wog6cMh=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("My Chemical Romance", "Three Cheers For Sweet Revenge", "Rock", "https://lh3.googleusercontent.com/5JtzMbsUJfBViHSlte4ddyD7kd__O7w7JtK1Icwwb8xXMQZkgy7DYfKnVS57oEvEBQwlv6Z1VhbVdcpx8A=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("My Chemical Romance", "The Black Parade", "Rock", "https://lh3.googleusercontent.com/v_9D7-qcGPdGaKb-sA5wBwpY9SXsUZdzMLFcaR_AQRsY84iQnOEfI9hKUh5B1Veb_HElggrbVRDVp5yl=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Nails", "You Will Never Be One Of Us", "Metal", "https://lh3.googleusercontent.com/8BsHxk9JyX-eEPuNkeD5Il0c8K8X1ZBGYIvTFkrZxJouWNfsFaM6zI1TB26BeY6c_9wvJKJpx1l114KUkg=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Nas", "Illmatic", "Hip-Hop", "https://lh3.googleusercontent.com/iZi5OosrE2sMR4X88FCFM3yBv-Uh-C-kKRbtolbVgyniO7pKHFiD3RjdLypWVnHHAzjus-eMMmfHRERq=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Nas", "It Was Written", "Hip-Hop", "https://lh3.googleusercontent.com/XWN06yt-4l0fa0PNG1zPEdr_6_sdri2AZFbf7Qqzcjw6obeSGQ-W5dK-jIJS4XBIom5ujfuyfVBneo45ag=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Nas", "King's Disease", "Hip-Hop", "https://lh3.googleusercontent.com/oXlsXGlxYvx9P94gV-B7DVsOi2fvvsK83fV9-oKKqCpbixVoXc4AIVrqusopCgTS5j5XrN5myPr2RkOh=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Nas", "King's Disease II", "Hip-Hop", "https://lh3.googleusercontent.com/7fjxkjvmems-mq_V9GgmK78RSu9YPOi_MTPVGajdX3kUxDrxDZZQ6h1OyZRnV6SDef6QOADrdVl3d8Y=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Neon Indian", "Vega INTL. Night School", "Electronic + Dance", "https://lh3.googleusercontent.com/49yZ-oloZ9TcwoK6Le-Ke8Usi--hIGyGA5pbzNjP5oyMvrcoG0ydarMuxUX6S9SrbX3EFRKvJZ4t20c=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Nirvana", "Bleach", "Alternative + Rock", "https://lh3.googleusercontent.com/nhcuMa9GzEnthhuKJuHd_VU4VOC5hu_-J7f_lbbdgxG35ZVa971LwLW6d5v6RSq-sx4HWiBt2u9va0U=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Nirvana", "Nevermind", "Alternative + Rock", "https://lh3.googleusercontent.com/5zD1MULLvF5bvatvjcyAyBHJZyvql_yrnrPfW2g5Sp6a5kRoMxVUlQp9iK6j8YHoIl03ZGwmN4NFW7M=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Nirvana", "In Utero", "Alternative + Rock", "https://lh3.googleusercontent.com/AU54rAq1sj8xzHtArON5sp8gE7eUc_4c2I1suZz85nkTCpBYIxOYil6vH5PL4Ue49MtHWqbRWefjC2mW=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Nirvana", "Unplugged In New York", "Alternative + Rock", "https://lh3.googleusercontent.com/b6z65aoWHX0Hn9O3AmXJx40nLWePzY4deNqx801T5jTnZZDKVV4VrCMM7cXvZ4j7rva3EXxxCFylwAM=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("No Age", "Nouns", "Alternative + Rock", "https://lh3.googleusercontent.com/TzpcXxZz9FFfaPo1g6ZnkkjblfP1meCruRb3lweUdJ_TKnfiKGkzvQsrbdr3RC-mlh0j2GfKFZ4se5SpXA=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("N.W.A.", "Straight Outta Compton", "Hip-Hop", "https://lh3.googleusercontent.com/OdcbQqwD8UQ5wEHaa4r5ewbIPjc1V10kgxGb-AbB9VXUWSRkt_Y0g3wuUdhvZ98XKDbUD1NxL1k56dE=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("NxWorries", "Yes Lawd!", "R&B", "https://lh3.googleusercontent.com/8-nTL1Y-Zr3eLJO1woZEAnqW_Uz1Y5eqtvrzqbtC_Jpnzw0_v7Yy0fg3J0u76m9UgK9mHkssNEH8TNxh=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Conor Oberst", "Conor Oberst", "Alternative", "https://lh3.googleusercontent.com/tvOX8svLtI7_ZZpVPeecuTLAeLWTuuLoyYDl0cyHwmQ62aRge0Viizu9rVvGHSr2XWxM4QtnSNHOqeud=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("Frank Ocean", "Channel Orange", "R&B", "https://lh3.googleusercontent.com/0yu8Hk3siMPLzbkwOdGqgFpHlIl8ySOz-Ccj4k9KCuFf0HIF2q8xdjp_jCmOb3WoHk1tawPs4q0bjmnwGQ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Frank Ocean", "Blond", "R&B", "https://lh3.googleusercontent.com/TWBi2M7D8gIwoo3NmhGfoVKI-PuzDunLVYpmLCbeP8Uw2YWpnjttlxmVvpVaO8uSjmLPjHgy6iGXxlPF=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Frank Ocean", "Endless", "R&B", "https://upload.wikimedia.org/wikipedia/en/1/15/Frankoceanendlessart.png", true, true)

    dbHandler.addNewAlbum("Oddisee", "The Odd Tape", "Instrumental Hip-Hop", "https://lh3.googleusercontent.com/KkgFqr29-DQLXx_aqJTWywhW-w-BV-K5ev0yjtr42_BTyOP3iNkv4sx77XcmKsHvyy0DkYiTNkzC9pg=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Of Monsters And Men", "My Head Is An Animal", "Pop", "https://lh3.googleusercontent.com/trWSJMR2GIw4TTSX4CiK5Sk6bLrk-ylcYCFDj7X0h9zCo5z0UM9Fp_EJP16ydPRjHcvEsv_WxO9yy1DX=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Outkast", "ATLiens", "Hip-Hop", "https://lh3.googleusercontent.com/me6ErprXlOV1KH1relokCISvAJMRoMh808n4nX4gBDjzG3f4Q9s3XM7_f7Rc_lvMUns7I9rvGWTGRh5MiA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Outkast", "Aquemini", "Hip-Hop", "https://lh3.googleusercontent.com/91RWoTyO0kNQyqXdn29lrX0jt_n5Lcr6apAt0nhG7Ic0g_I4StKpWu353UKDc2XdN1f7V_q8PXyBxYgDwQ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Outkast", "Speakerboxx / The Love Below", "Hip-Hop", "https://lh3.googleusercontent.com/tXyqObnHPgj6ve_-zcl3mpTUNqp6edWnqyTEOtQ4FzYKQ5Y8fDuHp7oGdKqodrZF18ZmOFSlitBAluEHxg=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Outkast", "Stankonia", "Hip-Hop", "https://lh3.googleusercontent.com/-w7l2pShuVAn3VsUfamt5s9IEFB_CnjceJFXitNTvjQE1d8a0OURF7e-_vkOQk8n0uOTaNYOKeFrVoz7GA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Anderson .Paak", "Malibu", "R&B", "https://lh3.googleusercontent.com/ml4Ts2OtsiR8g7xFYi7_JJbtN-GP-4ulZ05R6exGWwkBq7Z0zVSKjdxk7_i2m6Wt_5lNkNLghlPa5gnF=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Anderson .Paak", "Oxnard", "R&B", "https://lh3.googleusercontent.com/1g4wtvCcSxy9Io8GroklHFNAETiT5jb4ZUpICBGv4J1zKa8uKYIsYsYLYyf6Ybv98Y09OLK0w7FfoZc=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Paramore", "After Laughter", "Pop + Rock", "https://lh3.googleusercontent.com/iONeOYKFfpo2wtX2eTdPgrgtnmkL9gMwuEAY2TgoA1eS6oJN7dTh4lbHYa6cueoqrKEPh9BhWPF9ArA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Parliament", "Mothership Connection", "Dance", "https://lh3.googleusercontent.com/6-cxRi8tfv2cX7TPb5dl1_hoqChmv-ezKjjgUfcqgGeNOpPixMP0ttIuocnbfjROr4Rk8IkREkxXQmRH=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Passion Pit", "Gossamer", "Pop + Electronic",  "https://lh3.googleusercontent.com/w1RdeWZ2cu6vz5iQ9KNzvqHWQmpp_1g1QpsniknFKbFKnMa_wmZ16gnLusVVsTPuuo3lMX2-ZeFbgBES=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Pearl Jam", "Yield", "Rock", "https://lh3.googleusercontent.com/PhlIi_pK0vnfw5wevRCykba4z60yWUQP2aa7nzSXbyGeJTfmazRYpWGdMyYrlREcMaqevHaEGIgBoIU=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Perfume Genius", "No Shape", "Alternative + Pop", "https://lh3.googleusercontent.com/F-g2pEY3dpw3Y5790r9X8DAc28Y94qbgW4_HIv77kYqoACZfPDOEhXaNc5TslQYtYNp13MRsGL5NSGY=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Katy Perry", "Teenage Dream", "Pop", "https://lh3.googleusercontent.com/F7cLXycMv8oqsK1J9KJ0MGyJeRWdLudRaes2Wc1qwpvRRCkEvpf0aZmNAJSGiFC-3Qc58pk5kV2vEZM=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Pete Rock & C.L. Smooth", "The Main Ingredient", "Hip-Hop", "https://lh3.googleusercontent.com/coPrb31tUi-zuEVNNd8VP1qOsR9MCZStYM7Go_S0DpWw4T8jVkJE9HdEzaBjo96Wp0VJ0T-xLw7IhMuB=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Phoenix", "Wolfgang Amadeus Phoenix", "Pop + Rock", "https://lh3.googleusercontent.com/QHrRYH59ZwYWDpL0AGCfiQKNaD3SqgrI2t0Y8fGCSxkCd3lyqDVygcPESwSSOh9uvKoeHgo1cDWlUL-E=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Phoenix", "Bankrupt!", "Pop + Rock", "https://lh3.googleusercontent.com/-J8PySXXStIamqmUDjMJKznEY6ZpyqgkC8jhwHTZzqE5lTLcxZr7PauFFR8rA2APAfDX64anfSSVnKJ1=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Pink Floyd", "Dark Side of the Moon", "Rock", "https://lh3.googleusercontent.com/_paRRN5EbhFVQfQIxRa7tmHXiX_aPu4Xbz_HD3M87GO5HCLD6Tj4Va7otQWsSWxxoXg6ByWX0HYGFnwi=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("Pink Floyd", "Animals", "Rock", "https://lh3.googleusercontent.com/sPRYt8KYFeHha_I_wsPzypy_X1eZq1evEeY2pufcCj8T7iZZeIYdB1hvFDaizL-VrXLHjkRwkCdkUyUG6w=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Pop Smoke", "Meet the Woo, Vol. 2", "Hip-Hop", "https://lh3.googleusercontent.com/TyS1IDFlBoN-b1VBg2_rFa048n2gkPykYwfkQyi9xFjXteIRdMmoYrteGZXpTi8oHDxOOoG5GciH5e8c=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Portishead", "Dummy", "Electronic", "https://lh3.googleusercontent.com/IrlcyPIAb0ceQm5CcqFnn0LJZYby1WCnEalLuW5_FixHlflBUH6Zm1nCnqimWY2bYd3UP36PKygOLI-cjA=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("PJ Harvey", "Let England Shake", "Alternative", "https://lh3.googleusercontent.com/y09n_YTGtaXQrm-8lpRO4d2IYpKN1cLlbnJfUmQMB6ypScM1i58Gok0_oxHdfoBhxbC-OsUAwZyGNkM8=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Portugal. The Man", "Evil Friends", "Alternative + Pop", "https://lh3.googleusercontent.com/UnNxib0sKCqqOUH63wlHT4PqJYDzzR7OmU_pOWWm5aqceGgpWgAn4xC-aA9wzDc2Nf0MJnc5Yk2gQpYA=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Post Malone", "Beerbongs & Bentleys", "Pop + Hip-Hop", "https://lh3.googleusercontent.com/2tVo4wzeIZp713873Nf0kC-XFl2-7OGaWiPPXIdQcvbW60ckARocPuaOnOBQh-Rj75Mf-dHoMgQP-H8=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Post Malone", "Twelve Carat Toothache", "Pop + Hip-Hop", "https://lh3.googleusercontent.com/76GbHLexZ4BUOWmyU0PH8r0bVjEjvUsvuWJWm203Pvj9S8uXycCWFwfV6DAkpnpjRypeqCTh9V7_EVn4=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Playboi Carti", "Playboi Carti", "Hip-Hop", "https://lh3.googleusercontent.com/GR0WXVZb_b7GZsl4iiTq2ekEYFhytQjra_gGnRJyjXDPnRT7lzr6tKavxKrOO4EWJnK3MBzuQNOuR-uA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Playboi Carti", "Die Lit", "Hip-Hop", "https://lh3.googleusercontent.com/yjvaQsJ0ycMEIfndcN-AXC1GW3EhVU6OocgBKmwzyECK2-DJO0-APMLusK0Cd06dIzkpDs5o_RWbV28e=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Sean Price", "Jesus Price Superstar", "Hip-Hop", "https://lh3.googleusercontent.com/vIlbod_U6vNBUGjcSAgThYrV0wv4VbwZYm8BVQF-uVhzBFNfuRkOXeqQGHSCZMMcSEx2z40ELz6j8-aZ=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Prince", "Purple Rain", "Pop", "https://lh3.googleusercontent.com/ip64DOshSsDnZLW629HQSQHZUupwWvNaH_1I1RPxWNe-vxYcxQcmfGmtaJFVXrMdfFdK62OxJatf-PF7=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Prince", "1999", "Pop", "https://lh3.googleusercontent.com/NGRaiE3BCVyQxD2Wvsvhc0abEpRjrcQEZN6lQpzIOY74DiCCnugo_OOVm42UhGSBa9DpbHMCdkW9pnKs=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Pusha T", "King Push – Darkest Before Dawn: The Prelude", "Hip-Hop", "https://lh3.googleusercontent.com/8YL7FbkSe7WKzQonPoBmQ-Uq9JvgtApjKakXui6yREnp_QdcUpvvoTsj3278dMu43hhogp3Z8z3_LOc=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Pusha T", "Daytona", "Hip-Hop", "https://lh3.googleusercontent.com/XZJinje-56d5eZqgLTjarkBx8DjJFkUFNv3_UPMq7knnBZ63JUi8msWmkHv_MILgDVqrruhgPEmag4He=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Pusha T", "It's Almost Dry", "Hip-Hop", "https://lh3.googleusercontent.com/cu7R1gfcTgEO3JKu2x8monh4MpgkviZULdobyZwCF5RNjf5B-96W7RnpEt5Sx6xTNLK8PCC5wS-OFumT=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Queens of The Stone Age", "Songs For The Deaf", "Rock", "https://lh3.googleusercontent.com/zDtWxThQH0bpHIZlGbtfZncBuoaVZOu-r4sdezb5o_n6hSTl527Sc-dJSBLJd2nHSQJ687KDxhiGLLgmDw=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Queens of The Stone Age", "...Like Clockwork", "Rock", "https://lh3.googleusercontent.com/e-EjgX24U1plOQUQNBJoyJGzAP99_NFS9DvL60e0hrPxQnsKPOT3Pn1R7_2mUlNXbpHceu87ItszVFSrQA=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Radiohead", "The Bends", "Alternative", "https://lh3.googleusercontent.com/TWWT47cHLv3yAugk4h9eOzQ46FHmXc_g-KmBVy2d4sbg_F-Gv6xrPglztRVzp8D_l-yzOnvh-QToM8s=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Radiohead", "OK Computer", "Alternative", "https://lh3.googleusercontent.com/SPHeXqlEhzw-pPbAx3AQU4HSD-XuSMlPtLsptfvHOjOTd6F_1ZbELaOYn1d8-jGZ5HW8O1R0pLqausuVZw=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Radiohead", "Amnesiac", "Alternative", "https://lh3.googleusercontent.com/IztVMgq0rRLUhtQa6IxD4Vx145SQYQjnUQtuSHUl12b4B6Oyf-e-M0S1lrtIBnXCQFSeNABeanlxcdk=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Radiohead", "In Rainbows", "Alternative", "https://lh3.googleusercontent.com/BAl1FqoDaFrpWSSrr95Yjii4fJSeitdRcVt7f-uOVrhxQfmo5ActMNysFErQfI-CiOZR4GH2fePoVQtd=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Radiohead", "A Moon Shaped Pool", "Alternative", "https://lh3.googleusercontent.com/zhUubAG27CeM7pofkoJFJwyWsN0mqrTYeQ7NkWH0YLKan8gykddpDzxRu8ih-vGwACPGgLx4Bu6qwBIS=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Carly Rae Jepsen", "Curiosity EP", "Pop", "https://lh3.googleusercontent.com/grIbZ-j2-v0V2aWqqOjWiEmlsCddyVWCb2QLxGhB63nomMnWs0SP8CZDG-Fbbe7rWyVczpKO7PWYfOs=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Carly Rae Jepsen", "Emotion", "Pop", "https://lh3.googleusercontent.com/FX7sOVcbDnEdrtjr0Z09thGRIGdswXyBQ7Fgn-JdM5BH7L2ew20FhTJKyD269sr9E8quPVqY4nYIPOw=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Carly Rae Jepsen", "The Loneliest Time", "Pop", "https://lh3.googleusercontent.com/3a40ufsXop7aipKCV2LXQUX1JKOqRWEwI8AjoLHt0jtRrppwKFbfc7KEOcbYYvuFv6J6HwjEAeSM-2KR=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Raekwon", "Only Built For Cuban Linx...", "Hip-Hop", "https://lh3.googleusercontent.com/fHInlfn_mdXb1KVEbx-UarJBW74R86UvZRT4KGP9jwmPTsA7dP1rXPHY0Oabw_dF4qDryte02uBfoLM=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Rapsody", "Laila's Wisdom", "Hip-Hop", "https://lh3.googleusercontent.com/JmWXdph5BPqepQ1AsTHe8Cq-S3AKlVRF8N0G337DzPGcKo9Sx1MAcXdBudQbbALOI_81KBVqkK7233doWQ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Ratking", "So It Goes", "Hip-Hop", "https://lh3.googleusercontent.com/CoJVJ2mUrtxUusSwB-ft99Verg9-AiI04fHL_RjIt2JWeph5Kmrf6VDt95WZlv8-OxtGeKZVR2GNR0X2=w544-h544-s-l90-rj", true, true)

    dbHandler.addNewAlbum("R.E.M.", "Automatic For The People", "Rock", "https://lh3.googleusercontent.com/-mqJ3j-plKJV3msFM1TK2FEJxvenm59rL4MWbHPkd3J9MIgL-90Z6CsJIC9rnbge6wuANx6CcFrNffig=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Remo Drive", "Greatest Hits", "Rock + Alternative", "https://lh3.googleusercontent.com/8DIIPlKQ97mbQYbV4OVWpCkSszZirFUDhxtKmdMttIc277YRY1HjS_qoF4hQjYYVJvmaaEA2n3emIFbT=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Porter Robinson", "Worlds", "Electronic", "https://lh3.googleusercontent.com/BCp8A6TghyNWacfh3BHUBwnJFTARZ0zWz2Mg36MAntsBREImW_5eJaVe2q10kKl2ZbByn0Zi_2dbZRQ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Porter Robinson", "Nurture", "Electronic", "https://lh3.googleusercontent.com/NZG4Twrg6gDvxq1_nDyifCwx4irFkGthCKUMKw8DBg0u4Bgb3PNY15ORPrB1rZBqH7L55PgqvT3n-ceB=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Jay Rock", "Redemption", "Hip-Hop", "https://lh3.googleusercontent.com/sx9PfHz9wRQyqRW-yypp4alN-uLemhWzZ7dBg5r8Jg307xTkuU21qw9DraT2uA7S17blwoSjP-Sb-rEL=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Roddy Ricch", "Please Excuse Me For Being Antisocial", "Hip-Hop", "https://lh3.googleusercontent.com/mCjduPWc4GvReWhFy04ufFgkBt9be4XRjwkeCwRZu0XaaKk1-lRqPjjQ-5gk0rMjEoBl19QZS7p-RA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The Roots", "Things Fall Apart", "Hip-Hop", "https://lh3.googleusercontent.com/OzywcQBWcvxPIWu2YkWRAIvdPqA6YzTnvU_YPXBkaeJgt0fvdzjCeCeZ_xoB58GMTtfakR68vaWz_iMUWg=w544-h544-s-l90-rj", true, true)

    dbHandler.addNewAlbum("The Roots", "Game Theory", "Hip-Hop", "https://lh3.googleusercontent.com/28Dgscm4zSsegRTr0FSwdIqmSieFdFYFqOaPc6JVqslZMk1etLqwu4UgJJvmAB04Nw_JGaokYvrY04o=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The Roots", "How I Got Over", "Hip-Hop", "https://lh3.googleusercontent.com/D0CQv-6hcGqmTFLSgpYG7aCFRftphOpKB2U7kS0x3YkpltvB2xOG8HlCkY22uLXZpwNttpeujVjqLPOI=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Jeff Rosenstock", "We Cool?", "Rock + Alternative", "https://lh3.googleusercontent.com/UgkzbsnvLh0Ca7h9ijfNqdGdOFN7WIRD7uFo532p3WGpux--pEPWEZekZmMBBEVRjLyT8hYxbpRT6ZwL=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Rick RosS", "Rather You Than Me", "Hip-Hop", "https://lh3.googleusercontent.com/9tRjcO_0wmOO0sOY0qxHmtCTDXjwqwCYGUAlD4iIvvULRWpqX8NI0F-7L0CAuNzaWleFFtKAjk4_JQfLNQ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Run The Jewels", "Run The Jewels", "Hip-Hop", "https://lh3.googleusercontent.com/GYH-SSPz8iGt2_IZ2gUmRpDVW4cc9n0dn8n6X47P87dPGaK5N5swiNXwXdqnMWVPc3ZQE-BOfq1g6Ku1=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Run The Jewels", "Run The Jewels 2", "Hip-Hop", "https://lh3.googleusercontent.com/O1dS3OaeWgXVK25BT1-Lu98Fhzqp64BFbFRRci2TdeU5kJ828QNvo8UmOGNo8w1n9xEGPVAN_sAKBd_KGA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Run The Jewels", "Run The Jewels 3", "Hip-Hop", "https://lh3.googleusercontent.com/DErvPcJNQvGyb5lrgREAKUKzyHY5MEhufiQ4cjzHkhZl3UGGTGL1fH0po-Skla8ibdi1LuzviC-xUkxhiA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Run The Jewels", "Run The Jewels 4", "Hip-Hop", "https://lh3.googleusercontent.com/mHbPRN09cMI8SVjc-AuJdhNu9R0GJgOuuufmNvQKb2MJflNtGY7pWn8tVp5civs5tmO2g8UBuhCSVXKP=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Rush", "Moving Pictures", "Rock", "https://lh3.googleusercontent.com/RVlVd1FQ8dVH6VGLD0d5muPy1v5rNQ7YKgCfMHPEiXAaBUmSA9zd8PWyIYPomlN-IOaROduMWVgcTBxH=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Rustie", "Glass Swords", "Electronic", "https://lh3.googleusercontent.com/uTqLMiOF3bTGO1LMB6Y-EsnygGMAeKw5PKr9OgmvSSdtTk7fCBnNJ9-9lii7SgVEaKfSsw6iPK9Kxck=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Sade", "Love Deluxe", "R&B", "https://lh3.googleusercontent.com/9ilAy6s_FzWgujSC8fBt5CrBb2Ak_uqJU-pMUY-G-tjxh0_vEexJzLozTrlmbhxgpmtrSGUove8spuLSCQ=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Ryuichi Sakamoto", "async", "Electronic + Alternative", "https://lh3.googleusercontent.com/a_kwsBn2a42z39_rWTA1ROKROc_i6ITi2rC4ogaOyDgqsXUxG-fVHz3tAb29VYcCxn4fqfUNjHgStRU=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Sampha", "Process", "R&B", "https://lh3.googleusercontent.com/lr0KW39xhLMI0LSXakHdxsYmOpS1EskR-FjZzpx26py65XeG_j3NPrPwipFruU93n4ZQXdoCQVzI_h7q=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("ScHoolboy Q", "Blank Face LP", "Hip-Hop", "https://lh3.googleusercontent.com/rj9FggYQ8MM4VBtXntmKHZX8VkH7U1pLNu27xJWkYITJ9wINUUhXGSwYXHZTA0TewmmKUZb-BNaZc44=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("ScHoolboy Q", "CrasH Talk", "Hip-Hop", "https://lh3.googleusercontent.com/dLYHlhUHQciNZHUwdV7w9tb0_nt0y96fgNjkbKJcwfClQtnzGlcZpOva3-E3ywX4LtG923kD2XV9E1Lj=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Travis Scott", "Rodeo", "Hip-Hop", "https://lh3.googleusercontent.com/hoXqYCKFckmlvHe3g77yrUjZcz1J_JMigPZDV_Bu9c-iqnI9RdODnxLlQFgxswD0pYy3RYz-gcves7mKWg=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Travis Scott", "Astroworld", "Hip-Hop", "https://lh3.googleusercontent.com/PSIZ9cf9hpESZwcSz2ylS5I-zIREqCSagxV-X4CJqefrE0sRCktRtFw-a7PlkLygmg7k1nZREKCaSzY=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("DJ Shadow", "Endtroducing...", "Electronic", "https://lh3.googleusercontent.com/svMtb2XjC3jQCykpp6xWpS5AodMi8Hd8804lSk3Df4vkfiggKEPvVkrK0Ut1yqZBRnzRP7WieJfDkn6n=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Ed Sheeran", "Divide", "Pop", "https://lh3.googleusercontent.com/xpDEOr2TeqEn1QpXosXhqtj149FzNnTgAG3oqPnpTxTbQk-oceO90Sz4Axq0s4Jp_QLGQha_um6_EG3WGQ=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Silk Sonic", "An Evening With Silk Sonic", "R&B + Pop", "https://lh3.googleusercontent.com/qJ4H3BCzYqhaDNHGTVjLopdRKj4-Q3YvZAd_8S1pK-X7qbjVzg73yd_zafTXDOWWij1hRIEsyHAO5tvlCA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Troye Sivan", "Bloom", "Pop", "https://lh3.googleusercontent.com/_tLSf37u7UPBIfBjZuLf2aGWDYHEqVYEapgF9z3GmrKDsrS-edVMnmNQV2Nv2NAU0tg5tB9GaOHCkmyQ=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Solange", "A Seat at The Table", "R&B", "https://lh3.googleusercontent.com/g7HF5qrOJOTRkjdmK28_fSVveKLNCvRJb7m3KSGnp9YdbIlWN9JkcrTgtc4ub8WRQyJ-8hewNA-IxeQL=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Spider-Man", "Into The Spider-Verse Soundtrack", "Hip-Hop + Pop", "https://lh3.googleusercontent.com/IBwK4LOs4sO-Cd-_5YO4XC2B2N1hvyBvKfr60tT_ljIuwmOuVWWW2NypbcvzKgScVAbRsWEYbmsjfcm2cw=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Snoop Dogg", "Doggystyle", "Hip-Hop", "https://upload.wikimedia.org/wikipedia/en/thumb/6/63/SnoopDoggyDoggDoggystyle.jpg/220px-SnoopDoggyDoggDoggystyle.jpg", true, true)

    dbHandler.addNewAlbum("Britney Spears", "...Baby One More Time", "Pop", "https://lh3.googleusercontent.com/5MIh2Dgbv0cR0Ts8Y0IXOE2oiyvA7WxLD_Nm1-F6dRiLP_ZzS2Knxu1nVLUA2NxBdeYe2nCEtbAwTM4=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Vince Staples", "Vince Staples", "Hip-Hop", "https://lh3.googleusercontent.com/yoyUkkrNpExm7vHZtBMAeqmWzIRv6KX1u2R2OqbiBPKwMkTBvk9UP5eA6n8i7Y5hewA-JCftl2vej4Y=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Vince Staples", "Ramona Park Broke My Heart", "Hip-Hop", "https://lh3.googleusercontent.com/BBcXRcH6u5sj2OEumXQUSCo_88wAa_iZujuJMpzXCN-YU8-QMba_OLkON-MaLbMOFHaO35plNZ5T190=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Chris Stapleton", "Starting Over", "Country", "https://lh3.googleusercontent.com/eMk9wT35sidc8Jpwbwt9iNnhXE6Gp-eQT6U60nkAAEe5nVv6KvSzCC78O9ESc4VrrNVRToOjyuCt1Y_2=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The Strokes", "Is This It?", "Rock", "https://lh3.googleusercontent.com/pC3z_hnQSQCi4PGNdImJyFwOUwuneY39EnsMiGZhqjREZMxWnpSptv97zcl7nLojlf4KeOj5z25DZjpS=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The Strokes", "Room on Fire", "Rock", "https://lh3.googleusercontent.com/IkjmGRNEs8pjJNVufyMdJxbZkPAcgRpfgalWkkKDBwTP6V_jiwkkWAwA3HrwVsmE3inaA_vqul68Rr94=w544-h544-s-l90-rj", true, true)

    dbHandler.addNewAlbum("Sufjan Stevens", "Michigan", "Alternative", "https://lh3.googleusercontent.com/KQ6v0A16LZzSfbR1Q5_Tu96D04MKpBd4kx2UKzDMojIerP6RPsuoOinseKA_ZQV6HTG-RiP6SEmdGstFgg=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Sufjan Stevens", "Illinois", "Alternative", "https://lh3.googleusercontent.com/IvPyKvQ_KrAz6rXnYNxOLO-TKmUQMcG7xdROJW2CIxQs60GO80FzmZPhFxOv7axd4WEoh_KI02dXnHXVmA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Sufjan Stevens", "The Avalanche", "Alternative", "https://lh3.googleusercontent.com/_FwNSNMPVcKPvR_2YFlGDDxh9CQ0_Z6N4I0TBETqTSYD06W-aMJys5SkTanNhwW4SPFo3rIqM04UKHU=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Moses Sumney", "Aromanticism", "Alternative + R&B", "https://lh3.googleusercontent.com/x_-uYNSW7-vuOiN9ovcrIBea_TOHCs2k_y4ot2TIoAJvPQeeiLJ78SrHC5xrahnBgsA5mqBQ0SaU-eCH=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Sun Kil Moon", "Benji", "Alternative", "https://lh3.googleusercontent.com/MA_gjaBL8AqgaETo-GGnFqMj7rJP4v76MUt-y5WF523oXhz8wQ_2QEW9Q_IL_gSqPpiWbz4aCEl6HANUEA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Taylor Swift", "Taylor Swift", "Pop", "https://lh3.googleusercontent.com/R6GBw3KUAjP_NM265NU242PGSMVbjZM5LIE1UN1ljW-w9byKleSlyLdnkYKwOkl37GGGGMk2Ike40Ii2XQ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Taylor Swift", "Fearless (Taylor's Version)", "Pop", "https://lh3.googleusercontent.com/huIYugJjpimikk-tSDJyDU09Jr8vDVJuLOh5uGG6T45c5MEtYvFW_9NlqkHHSaLl231kLBgAS8rtDfQ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Taylor Swift", "Speak Now", "Pop", "https://lh3.googleusercontent.com/PNZlZc5kNUgLhpXn5vNkVl8wEsFHg26uEbNMRLRcTioJ8qnAEqV3EOjLwEh7YYeRmNul6TLXEv1v1fk=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Taylor Swift", "Red", "Pop", "https://lh3.googleusercontent.com/Wi2uP1dGCbTVfTH3RDOxOPJiBb2sgXe48T6DG7srLvnNl9JnXbh7ivPbGHEaASI3CxPR419W1sMZH7j02Q=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Taylor Swift", "Red (Taylor's Version)", "Pop", "https://lh3.googleusercontent.com/d_T3L3Ed5ynZLMVs0Ely4aqTfBZ-y9P9azzlLuKvT2re1QQuxqTxRPoSv082zBOBWmXZZRZKqIFbR7YI=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Taylor Swift", "Lover", "Pop", "https://lh3.googleusercontent.com/OhxDTHQOQzSrcdgH9hzqzp1v22GYDE-QKnkryvCeq4ddx-3K3_c8oDXN0E6NvHlMn1q4XV59aHr0oL4f=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Taylor Swift", "folklore", "Pop", "https://lh3.googleusercontent.com/q7JVahTldHxztLXfCJfgjf5DHr9mi18m2HKpS_IJqBqvvzfAJKPCh_rg-9v-aoGqp-Cc6PHW3ceFMlIQ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Taylor Swift", "evermore", "Pop", "https://lh3.googleusercontent.com/bwzsAqLcL50V9soJS0kIXARXFqK-0XnKF9uiBtk566D03JNGUsS6l2qu2bhsAbjAp4IHOvaSyWLSJ_Y=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Taylor Swift", "Midnights", "Pop", "https://lh3.googleusercontent.com/omCs21jqwK4Ss_VZxPFKwQP5z0UY0vi_8gXu4XNxHKDgE-GHYHWkIw80XR1uzFgdyhM3PvVUZeZ8iAfF=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("SZA", "Ctrl", "R&B", "https://lh3.googleusercontent.com/s7E4gqyaVGBCYoyJoF_pHm4LSbCkjd97DWlvOiZTqvECnaCyvi3-rArFcEdjvLriTBlh07Diu_bjLy4=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The Tallest Man On Earth", "Shallow Grave", "Alternative", "https://lh3.googleusercontent.com/eq_k9lcgxxjApRRof-UsGsG9qsT6rww4rAXDF8qG_N0Y785Uai4jxtqeClGMeqAKEH6JlBiVgd2_RQWlnA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The Tallest Man On Earth", "The Wild Hunt", "Alternative", "https://lh3.googleusercontent.com/Dx7sStCk9DP866HKTE3rv9_4OMJT-ZKFr82M138bLXPmJpHD1jOmD0MqiP_NA1kXCj_Zors5I7nKkyFk=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The Tallest Man On Earth", "Sometimes The Blues Is Just A Passing Bird", "Alternative", "https://lh3.googleusercontent.com/h1Z3KSjSaZrMypZrlRJHvrKwR0qrz0lYyo4oqbufAI5BR1D0JkBiFHgy99j6oJfdN8xwdWvYcaajYimD=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The Tallest Man On Earth", "There's No Leaving Now", "Alternative", "https://lh3.googleusercontent.com/Guk8TBz6k64dorUqXcZmWd-E3HOGlZ6f9O43SxszW7ihkvYuM1kyUuREvCq0EAxKufB3YeHtWjWvf28=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The Tallest Man On Earth", "Dark Bird is Home", "Alternative", "https://lh3.googleusercontent.com/TXg7KACJ3zD_65Olb7JyZmn_m_mqjx-CfYI1v77Wz681fEK9uI8bd5McoqqWyZh2kywVNLNCeNFFouDS=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Tame Impala", "Innerspeaker", "Alternative + Rock", "https://lh3.googleusercontent.com/iSwaFM9M5-GENCA9p30R6z_-nePmC81wb4dEk0UwBSV1bCkP7qYBsP--Vx0RmEn1YEQc-kJKvEr_3z0s=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Tame Impala", "Lonerism", "Alternative + Rock", "https://lh3.googleusercontent.com/BpDRqoOatQNrzpRmxR-cFXsbxzdYKRnTnSCJ7kg6dC4KDE16FsgcG94WY0Xy9op5CrPuLo8ay2gWUQJW=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Tame Impala", "Currents", "Alternative + Rock", "https://lh3.googleusercontent.com/J67cuSWAzGMlj8d9orcAZjPHsl8RWcXIXkT1d8mGmx9jmXPvXkYpFzuLnucmaqJwVMqxPlSq1GbqPeQy=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Tame Impala", "The Slow Rush", "Alternative + Rock", "https://lh3.googleusercontent.com/NyzedsOoL8vWtdUH41_k3cpbu9mxFnRD6pUbLPdWrOfQ4SKKVYnzHEymGkLj0NLEJW_75jdb6jlETO_5=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Teyana Taylor", "K.T.S.E.", "R&B", "https://lh3.googleusercontent.com/bEnibjDRRFy7JdmnDS0EZHxAMqga-t3IwMrnDfiUOVcSzv0iXyFndOJAOeRAGX_cmu2pza48dbM5pS8=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("THEY.", "Amanda Tape", "R&B", "https://lh3.googleusercontent.com/giWfDqyjS6WHf-NYC1QPMeSEj4X6irEU78kav2oxGNs1SfHnLZH_DpP9Q9tXnNKjggpvTHd6zlWpqGEJrQ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Thundercat", "Drunk", "R&B", "https://lh3.googleusercontent.com/H_9qn2QHEkgsKordtwkBpdV366_WSgpWFD_oaLVdR5FStwHZIJNrapucp-4voth5jS7xFfXJMZp_tMcC=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Thundercat", "Drank", "R&B", "https://lh3.googleusercontent.com/UkY4CGBQuI2pRgv0jyh5DH0dT6eDhL_sMqlydGxzN7yRzD7ODvuT75FIRrvXrXxI3qnpGpa-1wB-ke_o=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Bryson Tiller", "True To Self", "R&B", "https://lh3.googleusercontent.com/EFnQRSK_AVYyCyDAfHb7e2jfwtcxvBNTwQ-x8-ahYvWiPtv6QMvJ9PB1CzS0KJfTqAe7bSQps74F__U2=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Titus Andronicus", "The Monitor", "Alternative", "https://lh3.googleusercontent.com/H230Y4o8VL09zopzzkk_vp8ISoeWmSt3ik7CtRUJrhEDhIYfaY4419NNUdN32UyB31wpFxfzge5p7BWs=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Ty Dolla \$ign", "Beach House 3", "R&B", "https://lh3.googleusercontent.com/cA5eDZuPaJn5qnoIJczoidEk8L3HEW2ebYIxjiPnpyUGls6dZ96IZoSy_B400Qk3nmSc4y8w5Gjtb94=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Tobacco", "Ultima II Massage", "Electronic", "https://lh3.googleusercontent.com/JVIsLy0dNLLS4s2Mr5iVS28tPjItfmAResGQY-upBkHJqnt654xydxo26S3z30GeET9tW3Dx22Zym8w=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("A Tribe Called Quest", "The Low End Theory", "Hip-Hop", "https://lh3.googleusercontent.com/XJQMgeqoiDrwFmEAzmPvf_r2HWby9-PVd_uUn9ju39l6zsG_n6Itq9Lt6V4B4UmzcnWAIenfVRGEH9g=w544-h544-s-l90-rj", true, true)

    dbHandler.addNewAlbum("A Tribe Called Quest", "We Got It from Here... Thank You 4 Your Service", "Hip-Hop", "https://lh3.googleusercontent.com/Y5mE0rr1pa-AJGBhQO3L6-2z1hsD9RIFhSBCCI-9VA3683S-DtgE6k_0PwCto6VncJ53OPf_t_fdyOif=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Trippie Redd", "A Love Letter To You 3", "Hip-Hop", "https://lh3.googleusercontent.com/bODwPlbbGGvFVy_RC3-W2DqnhuPMLFKDD5T7DfUkqaoncrI71yAlizxSw-NBtsFFIArHAofYR469bhDG7w=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Trippie Redd", "A Love Letter To You 4", "Hip-Hop", "https://lh3.googleusercontent.com/OX6BwCwMpCDY22HFwhoFlgjqGk9MUVAfOkZATWRzSnFoT2deKyYu_x6QCxbNAplcfzM0PNgqfH1Clo_ROg=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Trippie Redd", "Trip At Knight", "Hip-Hop", "https://lh3.googleusercontent.com/cgWfnNXbkKPDytllJi6AQD7yFiSpDE0ThwewtVq0OmTgv-0qnNB4FL4wc3bkc4eKW6fvSgujDiUFJGDZ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Tyler, The Creator", "Flower Boy", "Hip-Hop", "https://lh3.googleusercontent.com/SSCyQ5wOePg3hVVSHc8AzdIgXZmTXN6mtn0EC7LmCftm8eNRMqA3cpOXnhgPaVe12loh0LFW8QKJY0zV=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Tyler, The Creator", "IGOR", "Hip-Hop", "https://lh3.googleusercontent.com/_c4JMCiDeaC2RRfShXddOuIV_A7oCL4m1R6-YK-3TDlsYgNQTXwxV0f-TTJrsO1StMt07qW3O6XNPSNt=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Tyler, The Creator", "Call Me If You Get Lost", "Hip-Hop", "https://lh3.googleusercontent.com/m0w-goi-wZRtRdzc0oYNkSfHiQQ6YmKHFkuCFSek4rYyl0-t0m9q_8vvXNzP-5nCk_NGYyDgvib_HiBg=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Usher", "Confessions", "R&B", "https://lh3.googleusercontent.com/MBYo3NSSYjhz4pM40dR-TLfLVhlW_G6D5b8zispPyn-fp1PYDk4iidmLJFD27jZ40nf-YgHlQAZJLXQ=w544-h544-s-l90-rj", true, true)

    dbHandler.addNewAlbum("Vampire Weekend", "Vampire Weekend", "Rock + Alternative", "https://lh3.googleusercontent.com/TDN-aVJ63c_WGJ8qKP2LRi-URdFY5eJ59cTJixCHt2AqpAL6lRLxJSfUVuqcqtoDHB1Rn0zrdwLm2C8rKw=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Vampire Weekend", "Contra", "Rock + Alternative", "https://lh3.googleusercontent.com/xjJmNojfhi_iPkbZPKvX0M6hxRbozC-x2giDj-FbM3OUDX75rQDbOVwiUVKnktxhdmX8KkMFh6AlplPfJg=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Vampire Weekend", "Modern Vampires Of The City", "Rock + Alternative", "https://lh3.googleusercontent.com/YYSLYsAXGWMjJ7TupfvD51bkqyimQ5k-MF8lsdFQPPRXDbME4W8SiLZrL-8F8yfY6Rhh4IxwvMqGzEL8=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The Voidz", "Virtue", "Rock", "https://lh3.googleusercontent.com/EPoXf-m_EUL1C3Kp1Bna3RafuVbj9fa-GI_XdCShdzQYqPmC4UEQP331d1q9SW9jrMTkxtKLwvCJoOWK=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Summer Walker", "Over It", "R&B", "https://lh3.googleusercontent.com/rYdKXnaonVcMIBOjoKxi3LhBp0KEmVODY8BtZfFLLJtuSb3XZku_T-frhnA-bGnOvrjggw0ti9ZWVc6pZA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Summer Walker", "Still Over It", "R&B", "https://lh3.googleusercontent.com/WqD6vzujSjVuNg4_UpS9CkHjITZd-H7kKXGdpQWJyZeCAXO_9Wkk29DmdIKY5gWk8-E8nr9GEZ1iHeX-=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Kamasi Washington", "The Epic", "Jazz", "https://lh3.googleusercontent.com/m2aD5Ss9VHvyy10yelRBiTkiqlx5iwOCvFmYmDj77vVybNqKABLzItHiv0X1CSJz38ljqO4YfqjQu4jT=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The Weeknd", "Trilogy", "R&B", "https://lh3.googleusercontent.com/NaqVnuvpOy8QUHmnTYxjH8J3Pm5-SSryq7GZzCP4SDhhHcO9UI0gvPza6qf2OXhIk0SJ98Mb3i1r_hLM=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The Weeknd", "Kiss Land", "R&B", "https://lh3.googleusercontent.com/kLevyszGNbIkjyziQ4wbRWrZHLS4rYax8FgZclHDmx8NMmWlSeGcoBAdNYwXePnY2SWBHL1uW-NcZZM=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The Weeknd", "Beauty Behind The Madness", "R&B", "https://lh3.googleusercontent.com/RNsC4IIArffVIpusLyonzKv_ijRbLmZYXkrQtKNrtLomjWr_sGnvPoDRoNSSDzq9F_ax-TOFmxuYS6hMRg=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The Weeknd", "Starboy", "R&B", "https://lh3.googleusercontent.com/dcxXIIlest09vnvKznWM9VWQXu1EL7lKxBzXGzwgmVjmMNBm1dEWT_0qn1xrEZYyKF_qRE1TLq8P_JY_mQ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The Weeknd", "My Dear Melancholy,", "R&B", "https://lh3.googleusercontent.com/AJ-AV9FxrD8AuPrZoq3IfhnjA0AzT5_xJ3_fdoFT7bPgbh4Z93tpECz_vr9-K82yAci22ylU01E7T8Fcew=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The Weeknd", "After Hours", "R&B", "https://lh3.googleusercontent.com/JDKz3Anlyo49xBhFcFx13QD_Tk4-kqdiYTo15gtkL93nE8biWyZ7o0BPyW6RnXVxcXaJ5DgU5nJ_0NjJ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The Weeknd", "Dawn FM", "R&B", "https://lh3.googleusercontent.com/5teqUPmWiFmagN0RggBKRXSW1zUj5_fVCEhbVhN6qt519EyHj6njy1x8dnJcRWNhQ5cl4dZgGaxbyqgv=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Weezer", "Blue Album", "Rock", "https://lh3.googleusercontent.com/CPL43SCvMotdc3SM4Ebj57QD3h6lDcXj_pMxTVBKL8ex4pdM4lDF0ZinVINkjLH_De-FDU1Oy89KSlE=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("Weezer", "Pinkerton", "Rock", "https://lh3.googleusercontent.com/8dI7Ysfj7cOWagVYnQvNMWnTFgcCqvRBOux4E8dYrvfa8JzIVbeaVU2o-oS1_FPhOHnUbTYf8LLMMHA=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Weezer", "White Album", "Rock", "https://lh3.googleusercontent.com/NGpA8FK0yxGkWd4n3e-5UmHidF8i6Z5cehPyfMFOyXigW7VU27VAM07qFx-_mSGZgd7BCNZl11CuKNK1DQ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Kanye West", "College Dropoout", "Hip-Hop", "https://lh3.googleusercontent.com/0tTCyDcqCFtvEs95I_HhisHOkj1IlmnJ_3tplZhfoYQGp1NVgekJ9gO-ZFHhQCzK6ycHJR9dUQfstUUK=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Kanye West", "Late Registration", "Hip-Hop", "https://lh3.googleusercontent.com/c-AY0SDYVHyA9v9YnM8E8gCkxiyaTud8--8LclhnS6w-xnM9D-Imgquv3PgoZp6TTrXgOvCgs-8kMoLv7w=w544-h544-s-l90-rj", true, false)

    dbHandler.addNewAlbum("Kanye West", "Graduation", "Hip-Hop", "https://lh3.googleusercontent.com/LENzUxyoVSRKUEZrw6nG5cOajuDDnFaIQIbD3LW95HaLLNOW4IHHvpGl0nmCoyFpiQE93jtT2FJTJk2Oqg=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Kanye West", "My Beautiful Dark Twisted Fantasy", "Hip-Hop", "https://lh3.googleusercontent.com/tSZcuqpZPhJIK8ItC4tkokRmIA6zEo408LFJnWb-3Nm8fv5adFiE1ArPTd_UqhaC-o8KI8tMNZIuFEo=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Kanye West", "The Life Of Pablo", "Hip-Hop", "https://lh3.googleusercontent.com/aPKvJlUpWx0i61bLgr5VGYadl7jzK-mtJTt5utKUkZgjLB98IHqIaptWpsJpJQ-fLIYIZ0j7cJN2cYjs4Q=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Kanye West", "Ye", "Hip-Hop", "https://lh3.googleusercontent.com/tpValRI48hXKycZyjpJOQAVsmGqV7w3lUGECnFpcOypZpduwWnlzdiowfgGkiAr1Sy2K4nSny2eore-7=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Kanye West", "Kids See Ghosts", "Hip-Hop", "https://lh3.googleusercontent.com/xRhLzotVKyeBwYPHcWDFzD-ndNcPLXIH9ZVNy_C7Lpt66dZ_lfR47boH7ussxcYOkYBwvbo5_KRKUYFi=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Kanye West", "Donda", "Hip-Hop", "https://lh3.googleusercontent.com/gd7qsOzpy8aoUf0SV1E-LZiUjo03QYiDNFRwSwQc3h5rBG8JHuk1P_nsB7I9A4rS_cMpyU18D8SEOA7iDw=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("The White Stripes", "The White Stripes", "Rock", "https://lh3.googleusercontent.com/khmRpv3WOm-5dvmHI4fnVHp3wVRQqvuXb1r-63sEDwXrj_MqplREwkxkrrEqyFxmtZcf85HgzfrHC_uU=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("The White Stripes", "White Blood Cells", "Rock", "https://lh3.googleusercontent.com/l-s4Ez9O60bh_pxOzWChbHm3SKEYwyiAoyfCUqMY51ahU0R2f1OJAmtyd_ZQBPxXxueGA0Na0_O38SBO=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("The White Stripes", "Elephant", "Rock", "https://lh3.googleusercontent.com/tntNX15loRQ3Bqmw4MJunOy_lC43qU2Dqu3eBGWBSbQodGgzIDbnNDLtHCYpsKggh592RmaHnDdSacEe=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Jack White", "Boarding House Reach", "Rock", "https://lh3.googleusercontent.com/T5lGms9opavMXtJ-i7UY5kZ0l09FVzL4BAR7mN71M-e_otwx7EU7mE_1wbf_BqrmKeVBgCZ7wMbueTpW4w=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Wiki", "Half God", "Hip-Hop", "https://lh3.googleusercontent.com/2qcBqMiGCUmZ4oTPsfv0eBn39rCiGqfFPqC8_elWAczC5nDwMQcsbHxuikLu5oIvrg1ZWT-2Q-foYVHA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Wu-Tang Clan", "Enter the 36 Chambers", "Hip-Hop", "https://lh3.googleusercontent.com/c2WKaqmSVr7gj0MpH4T3c0F5OYbjr7icqcreQ9OzfWRM0Z1UnXiEFiM7A2qnEEXLeesqaAaCIA-PhII=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("YG", "Still Brazy", "Hip-Hop", "https://lh3.googleusercontent.com/n3E3GJUaVoEl_pGaHi413KfP7dAaoyBtYR0-W24HC4xSfrLILSFysdLKHgGcjGA9uRITPAfJ8eZsJ_z3=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Kali Uchis", "Isolation", "R&B", "https://lh3.googleusercontent.com/8kM3nBwWf-gD8t_K39yxC1M-CarzZQHg5ivhXaFS7DvuhXG92Gyb3_ErDleINgyHmXG_ffuBipi_g-fUCw=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("The 1975", "The 1975", "Rock + Pop", "https://lh3.googleusercontent.com/gGAQN9dzZKS0qpiINapelSyGMz1k2UWq-bLYe1ouoswckWMls2fAP837cmtWZmsTSZiirt9CcSSb87c=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The 1975", "I like it when you sleep, for you are so beautiful yet so unaware of it", "Rock + Pop", "https://lh3.googleusercontent.com/EHXXuKoEMVoGC_pcLfUXb9zxfdZKLM15vLYWNIGAZOxZ92iJxTSdkGvFjrSWeHL2C1oGp6-OFLWMsndQiw=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The 1975", "A Brief Inquiry Into Online Relationships", "Rock + Pop", "https://lh3.googleusercontent.com/o0oou8dtH8UTZ-m5tjOJK2KT247zfTCKuuufDkxGw6wfto0CZRnCHSRCVr3kd5UNkNgSptN87BZinfE4=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The 1975", "Notes On A Conditional Form", "Rock + Pop", "https://lh3.googleusercontent.com/R5ff05n4k26iW2P4ipZf9CmTVAwCKYd5iby0Rjl-LvcmqqDXw7QyKZ_O0brRtUDBV0aUZe1Wsirp-41Q=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The 1975", "Being Funny In A Foreign Language", "Rock + Pop", "https://lh3.googleusercontent.com/m7ZUrwsZi-5yF1j8SQVKaNbdlSPqs6OrFiKiHdGHq3Fpqe73qxhamzuea7ojk1afu0LCLl1hb6EMtGI=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("2 Chainz", "Pretty Girls Like Trap Music", "Hip-Hop", "https://lh3.googleusercontent.com/w7ijtlgdqvhcqZ--98LoScYfZ0DJaGa5nfDxFzBmQ1I2ccaGPr8iVP7adiyg_BJNyKIKuMiPApD4geBI=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("21 Savage & Metro Boomin", "Savage Mode II", "Hip-Hop", "https://lh3.googleusercontent.com/vkfmpLE1SMPUfGD3ELKveBz2KAmGi7oi4aHWJfSS6GUo_6isAgYyRcWhtRRnF-vhUqAn1VeYmUhsNTDe=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Africa Hitech", "93 Million Miles", "Electronic + Dance", "https://lh3.googleusercontent.com/rIb2br3EuFZOPf5oGS_PsFkF5gQUU91ctwS7pfhQoVdd7xQUNy8L4Zt_9Yx6Qot2z0Wv7vRKmE9F-3Q=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Backstreet Boys", "Backstreet's Back", "Pop", "https://lh3.googleusercontent.com/DNTBNZ2VyTE2mSwGRFnrNWijWoI7JUyaaDfCH0x1Z2jtq3WH-UtU8Unz2gyBtEh67UoxTHXGWhH640Copg=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("James Bay", "Chaos And The Calm", "Alternative", "https://lh3.googleusercontent.com/9Bx6SCqyeJOPIXG5aqZBoKCq87Fccd1-Y8WvKqYrYDFVGymVIFvWNy1qh6wcNwi4AIYYItCIWRvX1iBA=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("The Beatles", "1", "Pop", "https://lh3.googleusercontent.com/O0BM0qveWB40423x8L9AdJxuMcBhFg0x_UtCbFQ_pRwbF412bmnlKj420gEPx1wwwAVpyXHpHp35cAU=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Beck", "Midnight Vultures", "Alternative", "https://lh3.googleusercontent.com/woQSChj_wtVqyqTt5D1cFXKeAMi8dEuTZUHW6N0RK8OxMohb-A10vk8-Z4Ftvkn9dcDQSa7bamPbvjs=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Beck", "One Foot In The Grave", "Alternative", "https://lh3.googleusercontent.com/Hqc3aOzpPXRyj12c9nj6TZS0bCm0yDzUmlKN-TRQ9weEKTfOp-XCD81J9RFpUlOoxJ95VZT0HkBkfL82Sg=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Beck", "Odelay", "Alternative", "https://lh3.googleusercontent.com/CJ_L-8O8L_YJ3UwbaXW1--f_VEaEvjhpNRLxi5-q6nLRudB5LD1O3arycUpjpkkClz0fw9bVDNBdCyA=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Bet.e and Stef", "Seeds", "Alternative", "https://lh3.googleusercontent.com/tD6YPMBturK3TFX_QF4WmFntMYonzkmH5wzgWhJLn4PGSNdTHODkIhA0NI4S_jR_mvxAB3k_ZrNE-ePi0w=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Bjork", "Debut", "Alternative", "https://lh3.googleusercontent.com/pcNyWx_F5gshjc5j4GJJ4fleSU4cwmSca6bdKoEhbuwypu0iZEEoRD_irmYP_WgwiVlctMhkicXwDvIJ=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Bjork", "Homogenic", "Alternative", "https://lh3.googleusercontent.com/9R5v_qsGqoCvf8M44UZeDbfdE7niovhGM5-f_FJ2HxI2WrqoDfMa1lU3w92FqlGCEvkdgTNr9deo-Scw=w512-h512-l90-rj", false, false)

    dbHandler.addNewAlbum("Bjork", "Medulla", "Alternative", "https://lh3.googleusercontent.com/75oHKn1sYMOsx2NCRotHN0rhDolphLq4LizqcrOQo6Nnw74nEyjwifSG7lx7FjHkeh23JwJKH2PEj-27Ag=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Black Lips", "Good Bad Not Evil", "Rock", "https://lh3.googleusercontent.com/HPxE_KMuCUow0CVndRebRTSE3QodLQm4UgLRF8gFrEBF3z4LVJjdNoUWXIhbTbKVNdv1i4ZWC4oRD0xQ=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Art Blakey", "Moanin'", "Jazz", "https://lh3.googleusercontent.com/QG4f3Ii3egC6XHGEHfeIsHSV8hnEcUtdYHvl7hRkawSE74L9vQoeHnQ3dOzUJkBBc2w5O6-tg30bBO_s=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Blink-182", "Enema Of The State", "Rock", "https://lh3.googleusercontent.com/gvJRhYmobGgvP6ehbkQK_y1zSSKKBDWdnxR9QTFQ3iMYvTXPP7jbkf8UAEN_u7RMAMDtIVcTpWHtQ6Jk4A=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Broken Social Scene", "You Forgot It In People", "Rock", "https://lh3.googleusercontent.com/LVnEGtDG67JFuzzdrd4ApCv_drgw1tKhAC_oSHGM7imf57a2JveAS7EHGmeMSjwX3ZdhvPDzgUVHfUs=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Dave Brubeck", "Time Out", "Jazz", "https://lh3.googleusercontent.com/bHgJCIXh3PZS8OHZC_L4YhQ8pv18iW6JZcqpkKCyiOScZ-is4yPxNQagVmE-AlgioEwd1aMaNFI-q1D_=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Busdriver", "RoadKill Overcoat", "Hip-Hop", "https://lh3.googleusercontent.com/HxI-yn7hCZEFIBEbRB32-zmMc1OPxQHtXLjQ_qbr8lDIzWJ-T8dldjtacZPZcEthyXXdj5Epyid3Tj5i=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Caribou", "Swim", "Electronic", "https://lh3.googleusercontent.com/efMk4IEiHc0kINVMb-tWTZBsmGqFsnABqjgIyDru5Fg8Vu-ME-C6Ps7YAfGnkJCjTV2LyH2GxkB_hBII=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Johnny Cash", "At Folsom Prison", "Country", "https://lh3.googleusercontent.com/E7gCGRiWjrCbUyE_F66Gg5yG4cDYSK0ZMSTGZbx22Y2X8HEy6XhVkdaHhWGPeiXL8QrNArodDstVkgC1=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Cat Power", "Moon Pix", "Alternative", "https://lh3.googleusercontent.com/XMlqEmN_fvBMzp0rLtdfDzCzKC1dCq3ZsQdsuf-4jhpIzUNFCpghNd8v-jkU0dBwO7t-1USka4O6GlA=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Cat Power", "The Covers Record", "Alternative", "https://lh3.googleusercontent.com/ceY1W4N-3STBiZtzGtxTBbouBXAM8zb8tHOI2pU7UVXaQ8qSSWgwW7fTTl3MkSv5SfoQzlE4qRXGWAuKOw=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Cat Power", "You Are Free", "Alternative", "https://lh3.googleusercontent.com/xUKYU-zp3kKVYsN4hJW6wJaUQq50iCvhq39QZ08t3Lo_uv6vFYQhTHxjrQMz3UcPXKBJC7nhB5Mv0PMW=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Ray Charles", "The Genius", "R&B + Pop", "https://lh3.googleusercontent.com/wCNkETP3gXYwGcF1tNfjirC903Ul8AHkdfOxE3VS2KiXQ4gIj03zBiMIrOVWUnWCSDFXAsyX9tdShBKQ=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Ray Charles", "Georgia on My Mind", "R&B + Pop", "https://lh3.googleusercontent.com/ZSjFb76tGbOyuMDieH3h0vmXMJb0TydLKfrMKGNwwZuMrKYp_zlav2vgmpTA8wq-yKdH5OZKMDE-_Fg=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Coldplay", "Parachutes", "Rock + Pop", "https://lh3.googleusercontent.com/I5_krVuun7yUejNFkiDfJanYL4qt14YFFERLdM5WJhTwyk4h0WE_wKMPAr1_Jjg0v8bNshKNnxStENNO=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Coldplay", "A Rush Of Blood To The Head", "Rock + Pop", "https://lh3.googleusercontent.com/-5tnsyG-BDCZj2gipRW710kLtHWPDVmlvt2BMwJ4StWqrfMxRUYvLsirYPClRDg1qnDU3avAgVn66eS20A=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Coldplay", "X&Y", "Rock + Pop", "https://lh3.googleusercontent.com/5A8HNW2FmsutZWsPRyXGvaGKt1bAPmNm4HUnwzlceEZ4Nv52oGSIpnxXn8TZlFXCASRzBwfoSBrkaKTV=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Coldplay", "Viva La Vida", "Rock + Pop", "https://lh3.googleusercontent.com/bxZTBpY7erVQlnSEarqeZlaDtIecqXKG2SUfatYuEdtiay85xd0N6AyFSaBBPIs7H_i0chNZGYPkbmc=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("John Coltrane", "Giant Steps", "Jazz", "https://lh3.googleusercontent.com/MDtOAa30lUv3QktG_EZ2LVIwQr8xfNlIfKRJuVgRsdVvW2esg9B6QReVSDX5WIhrxZIIRtjSiAeYFpHy=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("John Coltrane", "A Love Supreme", "Jazz", "https://lh3.googleusercontent.com/CTb8zOED76PoNoiziGGytUDU5SUNjGGLvn4-pNSLJYWkD2ZQdAMZOwmimBjGy1b7H_Y1FKY0xzA4AoY=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Counting Crows", "Films About Ghosts (Best Of)", "Rock", "https://lh3.googleusercontent.com/8p-oyqS42g1g-NqQ3wf00eWCoT0xo_Bi5HHuLsKShzfyp4lfQgURrDyYlwrkQHz_gbVlxEm2oD62cOM=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("The Cure", "Disintegration", "Rock", "https://lh3.googleusercontent.com/ZwLb72lvLwBOCviowaCX4X3Naw2Ra80DwkZBC689cnovGlQBmIeqw0ZbKUDB98pxjozSm6LjBzod1viVWQ=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("Miley Cyrus", "Bangerz", "Pop", "https://lh3.googleusercontent.com/A76sb-FLJxaMfhxyqvuVrZ8bpJ05KcGEwRLEkQXza0oDwPfZ8_nawzlYIl5VqkdjaPQ7xex5hycaIFzy=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Miles Davis", "Birth Of The Cool", "Jazz", "https://lh3.googleusercontent.com/XwN49AuEnZ1YbGko8xYewrKynyZgT7uIo0GR_qcyqFZHjTQFTy0KZTU8pETfKHWSLcZoW_6Tyf94t_k=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Miles Davis", "Ballads & Blues", "Jazz", "https://lh3.googleusercontent.com/42h2MrJbDKaiuroJ4ELTsqtvJaShFuzG1hu_rKxAiaABuygFgPA9GO0_AWzFV3PH4htEA-MCWrgJB5Cx=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Miles Davis", "In A Silent Way", "Jazz", "https://lh3.googleusercontent.com/gp_VtlN4UWDjw19662_ViUniyL_Emro0Aa8kswx6WY7x3mpWyefxXDBvNdBxrsSqbnv9GXfEp6dLjEww=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("deadmau5", "4x4=12", "Electronic + Dance", "https://lh3.googleusercontent.com/LbJ--XaNwj69MZ70PevvcdH-Yioe3V8ZC9QrH2gm3jWk7m9FTeE636y8To5KC5YkcmroQYBX3dpS3KiT=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("deadmau5", "> album title goes here <", "Electronic + Dance", "https://lh3.googleusercontent.com/iTgtbMm1AMINN82Nz9MgheVZpW_qBCvFbmXn4qHVVtGsHzf6Z_Xds6FxpLfYdKPYLKYxWbZjPpxKoTs=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Death Cab For Cutie", "Narrow Stairs", "Alternative", "https://lh3.googleusercontent.com/z_JYrI52CMlGbm2Ghmrga3hPxshRxsUBIErHuJqKu5D_wSmnCFeyA8xHrhNnSYYNHrQH93mFqPACn7Ba=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Django Unchained", "Original Soundtrack", "Various", "https://lh3.googleusercontent.com/mfAWO1duTdhp68CzeT2WALdZA9WAk5NEPpuYDlvAHllP_KmgLOEqS47cENYe2NvKtDBp7FqDYAP-WpXd=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("DJ Shadow", "The Private Press", "Electronic", "https://lh3.googleusercontent.com/BB_i7_tBvXnjOwQ2-gMn-yyuB7RnqOY-zIF09aeCteMp2LaI1nK7ZyynjS3LhVeiUYl-2J5AJtU_xyYIuw=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("Bob Dylan", "Bob Dylan", "Alternative", "https://lh3.googleusercontent.com/uVBcGedFd46mGqNcxzp-SQh3kVN1tQzOX1iQsNb6wNuvdbut4DU2-ZkkoQSCC2P8KBzCwh05XTJcCA=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Bob Dylan", "Highway 61 Revisited", "Alternative", "https://lh3.googleusercontent.com/TL2EK5rqsv4nf2Fe3keiVfvTALiMJmPmTjhtfPH_6P511Lan2gTIBNCwGv1B0jFuC6omHpPaKtd_F2A=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Bob Dylan", "Greatest Hits", "Alternative", "https://lh3.googleusercontent.com/olPm_ztDY_PbcMz462itkiYLjO5qXaWQyfdZrYnWPi-o5t-T2Ds3_5DUybBiM7z3jjgR0Fc9mc-6MSG8Ew=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Fall Out Boy", "From Under The Cork Tree", "Rock", "https://lh3.googleusercontent.com/JM-ZCXPByV_ZtMEcJ6daaQZRZm0EE2c-ndAOuz39XuMde8Ns7C7MbnnAe8xI3QtyrlOSMrZ23A6IZ6qJ=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("Fall Out Boy", "Infinity On High", "Rock", "https://lh3.googleusercontent.com/xgxVFoIkLyQUIoiduh-mw4LQ2ja5s3w3b64zQGDg68oR4ad3wlkAuvpI-Ftppo0yAxcIFgrLGdnSKR4=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("Fantastic Plastic Machine", "The Fantastic Plastic Machine", "Electronic", "https://upload.wikimedia.org/wikipedia/en/thumb/5/51/Thefantasticplasticmachine1997.png/220px-Thefantasticplasticmachine1997.png", false, false)

    dbHandler.addNewAlbum("Father John Misty", "Fear Fun", "Alternative", "https://lh3.googleusercontent.com/W3m_-s6PGd-eJvBElL8tbNHRLYqo3vE0aWDet4YOVFuwzeH73B2zYWptLZgdC26r2KVhuG_ss-j_noHJ=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Feist", "Let It Die", "Alternative", "https://lh3.googleusercontent.com/sTH0tjgsHZoXxEqLJf-eYA_6TwYFnxJ5BzKz5RhoPoiSWsAdBvfrghUemcZSRYZdkbx6FVC-tJtr79fq=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Liam Finn", "FOMO", "Alternative", "https://lh3.googleusercontent.com/4njhnF1SF7NpNYVKgCYZynP9O-af608_yokWdEwsV3UMOY5tcrE2w2CqUaYXtlF6iPf-uYO4ajjhB5YeOA=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("The Flaming Lips", "Yoshima Battles The Pink Robots", "Rock", "https://lh3.googleusercontent.com/ib1Sobk0P6EHfObEhXV6b8NKAUK7rjzkkZ22POdPIkxRNT1Koec2uUTujz5qkOKs2YFrxuk5rmFBoSnW=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Fleetwood Mac","The Very Best Of Fleetwood Mac", "Alternative + Pop", "https://lh3.googleusercontent.com/oPHbeFMYGcxsLbS_eW_5NOkLODJe9DnDMshmktcmnMocgBvlftF5mPIXrD9vKo70sJ9mDrUHqxHDsLI=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Franz Ferdinand", "Franz Ferdinand", "Rock + Pop", "https://lh3.googleusercontent.com/LYjHpF-QIHav0sUvGmnJs74GRb4_PWTtDYiOSVwBAEk0pWxkyPaEKscjuhXrspmrZCL0uf__nw63Gjk=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Marvin Gaye", "Midnight Love", "R&B", "https://lh3.googleusercontent.com/Li8cKVThxaMztM2xhAJidXPt1e9ddzKB_Nyi7I6bBlqhO_NFU7DQ21iIfVNbXNV90RYXcvAYO112zw4=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Ghost", "If You Have Ghost", "Metal", "https://lh3.googleusercontent.com/S7oK7JJ_ACU_gEBK83Yy3qaobkVm9aKZy0s077npr0cF4lFY-inVFLx9rsQ1hlpmAjz3sgwmTq1NQ20Q=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Ginuwine", "Ginuwine... The Bachelor", "R&B", "https://lh3.googleusercontent.com/jBPMuoOpRuzznpvkRoObvK4tOzjb4mjFeQIsyeCZ78VqtojDPsmB-NMj6dIneWnS-XospYQ41LP7mS1BXQ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Gotye", "Making Mirrors", "Pop", "https://lh3.googleusercontent.com/NbCyN_DtQhYjnbzhAV75BxRTQBeHqb7hUAAqeQ1rQIa6Okm-Aj7frMWzyqQ6eiretSa1PLSd3DsxrUA=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Grimes", "Art Angels", "Alternative + Pop", "https://lh3.googleusercontent.com/2fbbfduE6uxJBm6xShn7egLjH8gXdR8NFIobpjBeYrr-ROhCyneh8V9ZkgzmOAh7ZqmZ5df1q18y1Tc=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Grinderman", "Grinderman 2", "Rock", "https://lh3.googleusercontent.com/PiyuzPfmlJQCrqViaSbZX1d9cEbcNEHntxdlXGcts4qN5Jlj3_dXTkM7rncJuz-Ov9xdDgOOKKVAlVkX=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Grizzly Bear", "Veckatimest", "Alternative", "https://lh3.googleusercontent.com/4T2jndASWtKbwsVTLHIENi3P-db7sEeGx36aIFvzPuWv4yNrV5UliKk43SGTeWxo4Du_wFMFLYUjFKM=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("The Smiths", "The Queen Is Dead", "Alternative", "https://lh3.googleusercontent.com/4NnXRCMexmEeSZ4g1UZUPF5YV1N8mM_6e2D09RzMC8SbruN4Cp6RW-FN5M8WjtHOqork-9UBSF-BOJo=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Women", "Public Strain", "Metal", "https://lh3.googleusercontent.com/YbPQwC9WWZtmz6nw3tHKsOcj6xXXfMzSABA1S4QEMbw5KQv_ZBTpzIPdo2DG4efAjQm7G1yC7PIXjQQ5=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Nas", "Untitled", "Hip-Hop", "https://lh3.googleusercontent.com/DKHL-L0em6Pm2eTMOOoad0HFelOo8yIIynhKqFex0l5KkEP-Bjk53Dk0kaCx22cyfiuhSQ7qIlh2ohM1=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Puff Daddy & The Family", "No Way Out", "Hip-Hop", "https://lh3.googleusercontent.com/FkOfDKVbkil06O1ITWTz1BKQ1HBqMYwUO_oqGKl5EG0xxXMobL4SEUY6GQb-QJyMaYOdyd5MDoevyBI=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Of Montreal", "Satanic Panis In The Attic", "Alternative", "https://lh3.googleusercontent.com/egmbJW3R9pHB4UvEzRnwuzXbUsEqNNkEaDr_bA_wjLu2k-fqDa_H6fgtwh-2i3ZpI22G3yQ5cymB0Epn6A=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Of Montreal", "The Early Four Track Recordings", "Alternative", "https://lh3.googleusercontent.com/65VMl3I0C-xWWiGBZtDiqudE51QjwtpjQR_Ci1Q4Uu2dW540p4wBQHqGPdOp7SCwS4yx0qsI4aizdMoujg=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Of Montreal", "Skeletal Lamping", "Alternative", "https://lh3.googleusercontent.com/HDUQE6nroJ4ffYv9cvk9S6Fnb12TTD4YEM5WNLF9kun4lJo_djz5pS6GhgJap46yjTESFUfrnRa1P_ReIA=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Justice", "A Cross The Universe", "Electronic + Dance", "https://lh3.googleusercontent.com/n6cxTIGV2VAm97kN1FwoJ-ZgEPHY1b1V3bwqWo9shVuUc-h-e6NQB7M9UUpmg09MkdKo7mqvQ0_kOmY=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Portishead", "Roseland NYC Live", "Electronic + Alternative", "https://lh3.googleusercontent.com/ZlP--RbqBi-EzvmQ66dZE_9Exa2Nlhd6COU4rc01JVTfGnGzdQm2S7VT9h3jn3nbLLATQXe74AmF-l4=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("Avril Lavigne", "Under My Skin", "Rock + Pop", "https://lh3.googleusercontent.com/NNIHZ-JjwNkUFkmOjPVv6z56OqCoFfLDP2yi3SggDxz93ImaBNKGtaZYxBnZmRaM4ZzYNxp2SzfQJgc=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Avril Lavigne", "The Best Damn Thing", "Rock + Pop", "https://lh3.googleusercontent.com/O3IWr-ZW_kS78ZLxl5wjsNS5z6r68vEF5fFJiFaw_WfIr_n_gW9w2fcCGw1w61PfOMC-Zs_lnDBfzpDg=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Avril Lavigne", "Goodbye Lullaby", "Rock + Pop", "https://lh3.googleusercontent.com/ez6RwTkgz0BTuCJ7G0V8uU_XaAfgTixYwC9EIfvSuXnpVvemc_Od3kCcxCJgSym2hdsd3bgNnQFLNk0=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("Linkin Park", "Hybrid Theory", "Rock", "https://lh3.googleusercontent.com/lkNJVMp3siDfSw_zVYj3tE6fQS7G6Gj2gzoxrIwS7K4TwqmL_uSBb9-PKACNLIP_7XLQib4tC5XwWbpD=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Judas Priest", "Screaming For Vengeance", "Metal", "https://lh3.googleusercontent.com/B1ZC8rKD-nhItIDbuIX7vhGMPsYfM9gBcB4aGFY3C8M2OwpOV8zPTdQFRdO-sCb_wOTVxKkH9Ck77C5W=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("Tears For Fears", "Songs From The Big Chair", "Pop", "https://lh3.googleusercontent.com/MtgMpFQr1pm0uhUaU0cAX-84xEOyzMKaP_q6dskGMejcAHqiphJYAHcw7n_mPtyJkxlLhBdn8V0Xj5sp2A=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Harry Styles", "Fine Line", "Pop", "https://lh3.googleusercontent.com/S81rF7hb7asyWLPyVpaUmHUlUrdY-2yWh4R-OYUiaT-rLNf3z-ipKX_A1z6YDKeqaXph7iP38h73QGE=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Yes", "Close To The Edge", "Rock", "https://lh3.googleusercontent.com/0mLZyyz11RA4brWTNQB6sZLJcfyQKtijCtzQfz0EWcixjJKxexvqNalH7f7zQAM4i0jPScdGu98bqyijMw=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Justin Timberlake", "Justified", "Pop", "https://lh3.googleusercontent.com/zkslflRTQydxSJ7lG4d4vFXt-dcm04d8q-4ZTvT3SsTNvLjPTMB0bNuhV3rhbz6oDExuK8UBbs2eWCe8=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Justin Timberlake", "The 20/20 Experience", "Pop", "https://lh3.googleusercontent.com/ztD2B7k8LiAzFKmx789KbeS3L0jOcf_2ulzipQ7KaWXbrB4j9kYrXq2wiH27vbNZW_ywqsw5aJTKiOQh=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Interpol", "Antics", "Rock", "https://lh3.googleusercontent.com/6Z14CqKbsVfzG5ebByc_Y7S7oGoojqWIYEJP3ffD7gx4D-Ho5TULHqL5eTZF6SDXWnvy0if4yNeqt3Xq=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("N.E.R.D.", "In Search Of...", "Pop", "https://lh3.googleusercontent.com/bTh3K8oqsOP6xKpdNcK_6tRTCUkYzL6ovufwSi1fQ4FHYNitC6jIEmnbntr_JGHyTJzpl1qOXudOgFT2JA=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("M.I.A.", "Arular", "Hip-Hop", "https://lh3.googleusercontent.com/6Nkof5hYUU2w0jptR1rDflAl_87IzygTjmspkwX7m9gkB3llFzzFDuXXLYG0dvNsQhuRQzRLeE1MbpQ=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Sonny Rollins", "Saxophone Colossus", "Jazz", "https://lh3.googleusercontent.com/RaYfwwcEldf8do50khQx-T3AnlXxrVeDcIFYkWv5BCjFESSEpA2C5_ANTj0LF5jc4Ggey22Igv--RINsUw=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Pearl Jam", "No Code", "Rock", "https://lh3.googleusercontent.com/BIVkRLfeGRoGkcfWY6KKh8w0TCb9l8SnbSDVE4pIvgpCK41CIVascp2wvEFpCjs6kmzVZpLy50V8ElwuBg=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("Lil Wayne", "Tha Carter III", "Hip-Hop", "https://lh3.googleusercontent.com/IRcpxDdezOlyNZSlYoFERHO7M6asJPAjTOK9v451eyWzO2Bym1F3F4qsiS2zbvXF5AqHJWLNjBTE83mB6g=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("Brian Eno", "Ambient 1: Music For Airports", "Electronic", "https://lh3.googleusercontent.com/S9aunKtLZIFJI43YPQWaS8dRYa0yd1oVPG-_PYbOpjurhD35h2X8c83zNwbLQxsF_IC2gqgso_WwuuA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Mogwai", "Earth Division EP", "Alternative + Rock", "https://lh3.googleusercontent.com/FkW3J-ZCBv0uO00q1g4YdJ9f-Dhr6BzzBKu7A4t8YdgIuvBZnLEOlhwxtNqvshUnJ0-gcQ4jd6Yzm4bN=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Public Enemy", "Fear Of A Black Planet", "Hip-Hop", "https://lh3.googleusercontent.com/h-miKhB4ipTqYU8eEkoXIOrH2NS8WlyGeu-rLygsCW7Cdj1MQRbA25fTCwSCR1iPB0Oubskh3d4NHpIi=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Public Enemy", "It Takes A Nation Of Millions To Hold Us Back", "Hip-Hop", "https://lh3.googleusercontent.com/BWz1TypZPFmmm62kKqm9TKP6kpf99V0Ln9eoetNTljEw-WwNtS0fTYtsHZAp1wvWTwQLScwdE1R2be0m=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Wolf Parade", "At Mount Zoomer", "Alternative", "https://lh3.googleusercontent.com/XUVWZg5E264Vuy5g4TxqIJeYavtQS1BL3HrxKQgY9zivcVGdaR28MidVbFDHvS9NNWQ40OuLKaVLToz5=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Tom Waits", "Rain Dogs", "Alternative", "https://lh3.googleusercontent.com/UEnXfiPr4P0DxQ4F6MuoDxfRgWLHtzT-xjp0LAe7ehIiPVwtIkLGqrhBcliybks6jkfDPhbIhdAsUGo=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("TLC", "Fanmail", "Pop", "https://lh3.googleusercontent.com/CTK5Zp3J5DoEn-9aNpMmWdwZ-NjbsTB-6H71JVj3XFDPXbi87Mx33DkgVF6nX-EeWydOLUSupVlelvlj=w544-h544-s-l90-rj", true, false)

    dbHandler.addNewAlbum("The Streets", "Original Pirate Material", "Electronic", "https://lh3.googleusercontent.com/Wt561wiFf0z-Ygyzd3yY-uejPBC-2MPCkuqNpAQOQPaS_ZAFP0SbIVN5B0JlXukDDINQ8E8hqJXGSLE3=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Led Zeppelin", "III", "Rock", "https://lh3.googleusercontent.com/3pSmEJHaiESTU61aZfkobVrQfubuua5_q9isiAmDtRDemuMPlPcP0mpi6Ch3CQTcC6kuzLhzo7p2uMU=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("*NSYNC", "No Strings Attached", "Pop", "https://lh3.googleusercontent.com/WgTgMgIxFlodDnXO3GKEtmE4vWfjmOkGl8EKyzgfojv4PAE5ebA_EPf-yWrrqzRP1Ht6R38I7NWFAorz=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Pantera", "Vulgar Display Of Power", "Metal", "https://lh3.googleusercontent.com/xfX88ACRutKYFjzHwddX-6ooMR9XVwrGVOUdGGCfIWSLWox0H5stlwtl1Hwx_Y7tbMuMiIIAu-GO_FE=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Ice Cube", "Death Certificate", "Hip-Hop", "https://lh3.googleusercontent.com/yi3Zhe2fAyX2Z37JJjmrJ9Wfnysv-7BnQJw8bS6e-oPwwyY9HQaI2_a2_J-RoW8clcyaDAOdCVf-skGZ=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Oasis", "(What's The Story) Morning Glory?", "Pop + Alternative", "https://lh3.googleusercontent.com/dYbQpS9V4OHvKYvvcNkFyfV3n16GuvcGxu-4XG6Q7PdAMpk3HfIoAdQWNtIh7iM4ppaqEnyrPLJn3yg=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Hanson", "Middle of Nowhere", "Pop", "https://lh3.googleusercontent.com/xgNv_6w8tLVF4cEimzOWk6iNvGXRtXhzfZxFtdarE6nMKqQZfP8phvYNxDdS4ya9804__b1Uz2ruchYY=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("Jamiroquai", "The Return of the Space Cowboy", "Electronic + Pop", "https://lh3.googleusercontent.com/djSI1Rtd4_MJdYZwqR1LWQQIy26LBgrSYlgxtb9Iy_am88DOeYXNR26pxTvDGmOdr7ez1Ft_kHLyg3baYQ=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Jamiroquai", "Travelling Without Moving", "Electronic + Pop", "https://lh3.googleusercontent.com/tgd7-IsBrQDftMARWjKXHMLimjB6B3igE1lddbh5lx4FUtPVw3jHD1iI-DwSdiKJcWIs3KVrolG7jxMq=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Jamiroquai", "Synkronized", "Electronic + Pop", "https://lh3.googleusercontent.com/oVm0P3c-BUSQZhbjb1RNj_NXDOaR0eY-CzNE35lpFiFrN-Ta_9CTGzwdNr23bCswF6DTMQoJ3bMC8GHL=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Massive Attack", "Collected", "Electronic", "https://lh3.googleusercontent.com/b1948K5px6W1sV7KyIdPyu5CN2gD-u4YKHtXgr4VE0DcDuCOLGOpL_8sYTvAykD8A-t7lF4w8V5cEK8nqQ=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Super Smash Brothers", "A Smashing Soundtrack (3DS)", "Electronic", "https://ssb.wiki.gallery/images/thumb/a/ad/Super_Smash_Bros._for_Nintendo_3DS_Wii_U_%E2%99%AA%E2%80%94A_Smashing_Soundtrack%E2%80%94.JPG/200px-Super_Smash_Bros._for_Nintendo_3DS_Wii_U_%E2%99%AA%E2%80%94A_Smashing_Soundtrack%E2%80%94.JPG", false, false)

    dbHandler.addNewAlbum("Melvins", "Houdini", "Rock", "https://lh3.googleusercontent.com/dNhYIc7Hun2V2eKqxSx-FBqytGYYUqXDIDcWMw5UkjGrdERT4mbV_KmfIpJ5wQBysbW_3gk3yLtGqZ0A=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Muse", "Origin Of Symmetry", "Rock", "https://lh3.googleusercontent.com/IimEK_f6ZYM-yBCQbdU-P4wUxggRZxKfawmaflEW40x5jk6fdLoo5CtBG61LNQqDnpnDqGvlhdk6vnOC=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Muse", "The 2nd Law", "Rock", "https://lh3.googleusercontent.com/p0Acx9zzSc4Hd5tIBzwHceLToIapGSaWpbcCGXEwN161kQmrqDJ0c-yTyptapdTUAJK_BVciBhNibsw-=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("The Magnetic Fields", "Distortion", "Rock", "https://lh3.googleusercontent.com/3HPC25KofVU31gxdvC2mJno8WlFtkYLwl34vwXXoSyq8DQdvXd9yQMvNw4QffZqIYTxYx9z-fre2reo=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("The Magnetic Fields", "i", "Rock", "https://lh3.googleusercontent.com/FQ2Rm0-tVbiyx54Oc0WsCfAciIOggrmnms_-6dLVkRku33tHW7WpQ-IysJdJy5k2652ZUm-O5yaK-9l8=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("fun.", "Some Nights", "Pop", "https://lh3.googleusercontent.com/d7bJNoH_IybM9ZOI9Q7uQrT-5rBpNwxXFb-DHysqjLzjlwr9NRCmOmwE3Hv61INMN9At6KCYBWVTxKD_=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("tUnE-yArDs", "W H O K I L L", "Alternative", "https://lh3.googleusercontent.com/2JCuGOhIENYdpALV9N8gHgY6Xad_Qh_hn4l5WvcCJqF8WXpcJiyTgUZXS7yIcg9DubDLokRGpkKnm7XhlQ=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Wolf Parade", "Apologies To The Queen Mary", "Alternative", "https://lh3.googleusercontent.com/qkGh6uXETLZ8VyyMBO7sTXzjTeZb7GSN8CgKaL0eQJCFiqI1HqwehIjJIi4wEIQq8uKyYTEvYEJSOknf=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Syd Matters", "A Whisper and a Sigh", "Alternative", "https://lh3.googleusercontent.com/k_ZyAC2ul8htdEGmepo1cU0WXsSH8cVQF8T1hsz-7X48RDGU_FuTaDScapn3p9Fy7hLDYQ4NeXHiAGM-=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Your Favorite Enemies", "Between Illness and Migration (Tokyo Sessions)", "Alternative", "https://lh3.googleusercontent.com/5cTNTADG2dmWLaZRG6EKp58FrJ8weKT9tybkDTtRLaNVFZdiG-HKqOdJsibVswD3di5_1rVR7JC8Cgw=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("The Velvet Underground & Nico", "Self-Titled", "Alternative + Rock", "https://lh3.googleusercontent.com/gQ9piFHdRXSbdwtVMSovDew5RMD21-GDlSYNUJwVIzQSY2EddiyQILi166Zy3CdlGbmIygg_8VA-eZ6t=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("Morcheeba", "Big Calm", "Eletronic", "https://lh3.googleusercontent.com/jBB1DppZrbazEN8WkCBzN-m7JKiUnfS1hZZNv6XAkqqPkYviyfyVOcfL0ayTNlAQc4Z2MabmkMHHS5NsaA=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("Alanis Morisette", "Jagged Little Pill", "Pop", "https://lh3.googleusercontent.com/6Wh20YKGd7vuFVBqUW9Sh5uEFqDMwCVOQpOw3htYa5G3NmXvYdm2nqSXgjITeNyShHuRK6XFdefBlvpZNA=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Hot Chip", "Made In The Dark", "Electronic", "https://lh3.googleusercontent.com/xrNzpLTskgv8mCQRiuCzMn2NPG355Ih3V_6RHK47iAK3J-qLc-fPGNfU1-PEzQxYz1f_cirxiF2h-sVe=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Joy Division", "Closer", "Alternative", "https://lh3.googleusercontent.com/58bG5PIQwqDF1ny7G16YprLDZ6ciju7cbjx45dDaP-ThDJhAEV-gUGQjaY9baUK7Nh5NCdTmiZt0ZAk=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Yes", "Classic Yes", "Rock", "https://lh3.googleusercontent.com/NU7knFdEPwg9xqkYKxpZ7RCHWhXsirmco_z6EltE2cYIB4r8XUqZruFrdyDwnPqhYegpFGHtN6fgtVw=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Jeremih", "All About You", "R&B", "https://lh3.googleusercontent.com/4PTF1JqxKryKHpV1B5ORNpaP-i7Vnz-DV7Dr8v60EU66cUYBSqSTAfu-c_lS8UNMjauje7-OK-6RP7G3=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Hedley", "Storms", "Rock", "https://lh3.googleusercontent.com/ziCW49BOZ35e9HaRfD0-Ldp3xsYJHuQjzk7p8aMQsQ88YkUqv_KSaDEOBBAz2X34jouq4RaBnt2_-3Y=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("August Rush", "Music From The Motion Picture", "Pop", "https://lh3.googleusercontent.com/yCNWHufQxazTHIbCvEhVuDAB4D-cVbdWzfWy8DJre6VuMvfjW4S_oM9RdNEaUhadheBT468r4AeqXyLS=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Earth, Wind & Fire", "Collections", "Pop", "https://lh3.googleusercontent.com/Y64ZVq6FDxZwOa7TcBu4G5S6PWch1xBq8EMFxJMxi1nuFb5gimV1RC5VGGAUFog4HKxPc2jyFaGGlbzv=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Passenger", "All The Little Lights", "Alternative + Pop", "https://lh3.googleusercontent.com/9va_O0B2rTWjHJsA00aHVyhfoJddCh-QCUxMvFKa-2B9VpI4b-Yhifn3JnBTPK8yUFurutOcq4ks2ro=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Simon & Garfunkel", "The Simon And Garfunkel Collection", "Alternative", "https://lh3.googleusercontent.com/lGCk1cdxVoS8wvOBmU5vgiqt2bxKG3s1oWmOZlvUaVWQIknUSG8TbRvAtIQxhtO5x2BX7-ugQtcF8bJI=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("The Zephyrs", "A Year To The Day", "Alternative", "https://lh3.googleusercontent.com/R7OCt4oC8PWs942F9MBQfN5YUHL2Or2T2FXjzy1KoS5gtJ68bg6Sk7nR_-xOfLhAxyEy4guhccJ4tjY=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Arin Ray", "Platinum Fire", "R&B", "https://lh3.googleusercontent.com/0bMK-q9Snj7vNXhwdeVJ6lwL0s3curcvQJU0AjxduGh5QV_oG00wcX2BuoaJAjz3NR8wmW3tIK0tW2sg=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Santana", "Supernatural", "Rock + Pop", "https://lh3.googleusercontent.com/Of3zBUDoSRn5QKgEXb0TXkf7ttAikU2W8l34MfXbODX6jtN0cXTMduyXEOEHqzOgBY5hVySW3yh6YJEU=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Jack Johnson", "In Between Dreams", "Alternative", "https://lh3.googleusercontent.com/fqRalSBqL8b42b5mQzfl0ep2guCCPhG4K1c_zToGB8Uh7I2t7eHsQB2tbYqfx4Sa0Q1XPWgJRtY1mJ2hNw=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("Peter Bjorn And John", "Writer's Block", "Alternative", "https://lh3.googleusercontent.com/LNJ2g2BaaEf4vn1clf3h32bg9zHkQS2bu47kqDWUd6lmC4upsiI8jBKKWEE4sr0ZCIBHppqTdl_6AAqc=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Norah Jones", "Begin Again", "Alternative", "https://lh3.googleusercontent.com/ZYS7X3mCMlt5gTNYXbspH7Hp9q858Dkhs6rNfPHjJeTulrgwQh6s31HhSr6S5DmZo02LJPgvEUVcUjna-w=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("2Pac", "Me Against The World", "Hip-Hop", "https://lh3.googleusercontent.com/-tCO6jGI7IYw0QxKsTuY1-npUMzHnVHV2N6a2JOdDgO50vbdntYqHGlHqF2gZY8FUxjXoL9jh_bTWkp-=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Curtis Mayfield", "Superfly", "Pop", "https://lh3.googleusercontent.com/TB3GvL9JlEoJODQj1V9cSxNyV12KhAnsglvVpDfZhw4LLwHjXZDfSgu-hvpRV5CrzDmPifUB_2q6QEsv=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Frank Zappa", "Hot Rats", "Rock + Jazz", "https://lh3.googleusercontent.com/OpWW8nB_IyQBm3ySDeODwhhzJ6MEXyhv_tch2Tn50Y-GcurI-O33hD2soqCj2ZNrIa5650CbDtgQhzS-=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Regina Spektor", "Begin To Hope", "Alternative", "https://lh3.googleusercontent.com/t7POUbxUq1WmKCs6ltdaF2K1ZC2nZ0Cw49hm6uWBKBjV3L9zi6wrZOnK4jHvaRXJdUt-FT8rIUAT-wc7=w512-h512-l90-rj", false, false)

    dbHandler.addNewAlbum("Hozier", "Hozier", "Alternative + Pop", "https://lh3.googleusercontent.com/5VtaIcUJnEqpa6IxWGq3NsBMdLNgGjXfxM4d0IdHII8AqCYjjX-h65f0v3aU-Nudc4RA95VaaaTgKPkEHA=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("Rihanna", "Loud", "R&B", "https://lh3.googleusercontent.com/kTMZukq-sgFuHrEGLVschZ-4p2A1GUZHxbT8woilv68XDrILQk_p1pgaQXMvujUCasHbVANmX-4d3q0GXQ=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("U2", "Achtung Baby", "Rock", "https://lh3.googleusercontent.com/NDE18nuRbggttoJ-K-g1lP8rBwTt9k334hBFvw4VxSyceX7_yRNCKLjIO8dCwZINV_9ZVb4qiE8uZ5Ps4Q=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("jarhead", "LP6", "Alternative", "https://lh3.googleusercontent.com/4goyHVWG47cJnXJqAVidSntvB0ZiX6RZikAGPnBcx1PZjrKfFrT-Y23DquOEKqeyURPrieZgrgY4_TQ=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("MSTRKRFT", "The Looks", "Electronic", "https://lh3.googleusercontent.com/OwpE1GZ08OtWyQbyNWOTPFQvkaWXS33w5ztQDsOhtGjFsmEJoQHsHUPCnJhO5r2udlBF9o2lTnED0PPq=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Justice", "Audio, Video, Disco", "Electronic + Dance", "https://lh3.googleusercontent.com/Vo4FXJ-vBjmWCMO6R0LcESirg1KgkWNWkPF6b5X91zqsnJssBS2t4lnseNS4pfOKlNjkwRjHKU9FNfut=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Usher", "8701", "R&B", "https://lh3.googleusercontent.com/grFhFW9Vdns8dHCVG_854Zo5cpm2UJNaHoprWcl7HFtooRLjLMbDi7IgLS_DXNEPj-5mld2iTdaOnpwl=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Lorde", "Pure Heroine", "Pop", "https://lh3.googleusercontent.com/5VJie4A0982gncqSzs56jN8adzdXpe0_YZP61sWdO6ZII-XsMBvGh_ouWKDsk_sN-fJnQBGPN36LBPzq_Q=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Meat Loaf", "Bat Out Of Hell", "Rock", "https://lh3.googleusercontent.com/BwAqkqHo3fogVcNNxB2hfWQLopCLV5TlTDzoXA4Y7rc0JVyPU_cytiuk0ovH-F0r8bkKAoO6CScpvBE=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("The Smashing Pumpkins", "Mellon Collie and the Infinite Sadness", "Rock", "https://lh3.googleusercontent.com/VMWBpSXgG5fcJ4bGGRpbrZm8YaSq33FM_k35KQ9da9vu-PZAxHdnvRL2vIFFoqAyucQ0IlHFHrBAwFhk=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Sam Smith", "In The Lonely Hour", "Pop", "https://lh3.googleusercontent.com/7sMjbNSjtNC2vVrXNtXT1m2bD6vxtIF2CceQIFBn2Dq-PcUbkcA0prNgWAREezxeHAaHrzO9-_xd-FM=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("The Neighbourhood", "I Love You.", "Pop", "https://lh3.googleusercontent.com/rcGbeinqN-Z-9fLWpGlxeZlvH6tQZGDubHqEPDk91Wp3XKkWNuhNUQQnFWnaZHn9LPeaikxQlUE3O3ZBXg=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Lauryn Hill", "MTV Unplugged 2.0", "R&B + Hip-Hop", "https://lh3.googleusercontent.com/sq-2qmUoqLcEsQRqphnRALmDfiaJeKPCAnclFKfUlyKYwK_NpklTm_U5bd5kYgg0F4dccUOIbp6ygUaI=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Lauryn Hill", "The Miseducation of Lauryn Hill", "R&B + Hip-Hop", "https://lh3.googleusercontent.com/kzP53b9Oive-yiNndGp2YBcqZOfzE-CTymboQI0jkkDqBP789RNhTqIu07GXxdYgsUDYLPFqlWhwJtY=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Rex Orange Country", "Pony", "Pop", "https://lh3.googleusercontent.com/cXI8U-IQRQfwPGtAco8_m1gw0f7p-NnCoBexiY2i6icq2wRucQLe1GUHjnEeDWv41uLGoTO_Crz2ZZjb=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("The Who", "Who's Next", "Rock", "https://lh3.googleusercontent.com/fDzkNt6wBIM7RhaQ-cjNEQ_7DGjPf1aagdBiHIIAObzgYfa-9Lp6kSJgaIthJL2J6V7gchxmjxl-baU=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Kid Cudi", "Man On The Moon II: The Legend Of Mr. Rager", "Hip-Hop", "https://lh3.googleusercontent.com/6wM_KX9RCk5ANbSquMLNDurflUgA9eM2mSWm24Fl3TvwSrhZn3lYpONeGH7Gn-W0YWNTu6DZ9J2ByceZsw=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Charles Mingus", "Mingus Ah Um", "Jazz", "https://lh3.googleusercontent.com/Onj0XrA8T1Plzzz9yeJFXFdUALkAg0jd2__iidORhG6ofAyAWXyCzNSZ0U49xq4y0JYQfb370zGhD6M=w544-h544-s-l90-rj", false, false)

    dbHandler.addNewAlbum("BJ The Chicago Kid", "1123", "R&B", "https://lh3.googleusercontent.com/EA6OYeEUlBjRFv-P7DquJ0mdH_GCck2BW_flXZ9Vwekn_XszeJR4DUJSssEgP8E7GTJIJMZ3cWT_faTH=w544-h544-l90-rj", true ,  true)

    dbHandler.addNewAlbum("The Black Keys", "Magic Potion", "Rock", "https://lh3.googleusercontent.com/cOEWUFqjnFNVNuypK1D8yYLM5be6Ix529BnhYnwCEppPGI_OL8j0iSFPloddKY1jt7v9ZWi8Cnw-rCYZjA=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("The Black Keys", "Attack & Release", "Rock", "https://lh3.googleusercontent.com/E_PLHpmUwVSzxYGz53FRXJX7sacx41I1wafnscKcGmS5Jh0vnQ3L_tKvvpQga9XT5Glb1djUZF-Ep2MD=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("The Black Keys", "Brothers", "Rock", "https://lh3.googleusercontent.com/vcaNjTPYCL3bYmfk057gaRkdIJPXSLMIPJnCtW56uB7qc0SyGGwHkYPxtdt-tGlss7Y5rm4GlSsbFOE=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("The Black Keys", "Turn Blue", "Rock", "https://lh3.googleusercontent.com/2xTz3jwPkh_YUebIsDuboBMnMLZuPgCUrEJwfdcnL8AhNoEyR95Wm9fLjcgaHHpfjpj-0zNqvWLeGfSj=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("The Black Keys", "Let's Rock", "Rock", "https://lh3.googleusercontent.com/xyfw8qz-hoTyWgnEGY0cfuBpjEdKcnct7T3jTUAB8Av2RlYEkrUa9KneLtj0JDYxGFnYDYMd0qwu886g=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Denzel Curry", "Imperial", "Hip-Hop", "https://lh3.googleusercontent.com/SNipfgw-vEnjdm8mrF8_lAsevkWBv2lowXKvhKNtOg4UmYaCOi2C_fr_JHHz46sOnBweyVF18-XszJVh=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Dirty Projectors", "Lamp Lit Prose", "Alternative", "https://lh3.googleusercontent.com/Fom0mDOs0cgRsDo6zXFTYeptFJqDH44s0Ydm9bCDhxD8tbKOzk2gH4Er6ggSqLhcDF5iaiIgWSLnTLE=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Drake", "Views", "Hip-Hop", "https://lh3.googleusercontent.com/JJ4qrztnVvNCH6zWgo_She400LSMMlG7zzN_zNZMmmPodKACJf-_lOyzXiwvKTYxFVFP7GsuIr2z0-j3=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Fleet Foxes", "Fleet Foxes", "Alternative", "https://lh3.googleusercontent.com/mE1sAq003Srcwbp6aNlnCTZ1__hmT4OkLijVLsM4YUlXFZ1glgdMNSvbMoiL1e88pBWv2xAjt78lSbSz=w544-h544-l90-rj", false, false )

    dbHandler.addNewAlbum("Gorillaz", "Song Machine, Season One: Strange Timez", "Alternative", "https://lh3.googleusercontent.com/vJaHAxrfdjX4xu1qJKmfBSS51t8RLXL7c2YKjaD8kuQVfjHLHlQhaFCsqoycCA-wfi7AIccK8aPnn00l=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Jack Harlow", "Come Home The Kids Miss You", "Hip-Hop", "https://lh3.googleusercontent.com/a5kb_FPW0DYzfrTNiP3gl71BNmXX7sH5lLrleJcmubo5mQaM-5x6VDU1gICUwBZelscqXYUeVqhYo12K=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Iron & Wine", "Kiss Each Other Clean", "Alternative", "https://lh3.googleusercontent.com/yoU0k-Bgw8S_hCZwfbkWIzKhovzzCn3DzROV64f5LYadE-Pmx5oV0KuKrhaC7bhu5DUGgmE-0zYRlCKS=w544-h544-l90-rj", false ,  false)

    dbHandler.addNewAlbum("Joji", "Nectar", "R&B", "https://lh3.googleusercontent.com/P5oSSF7f99_9mmLdxZaLaviljzqWhoDTBfvES9KtY6RUik6H68LyNAiPQm5KFIZLqlImMV4BdVJude6xNA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Joji", "Ballads 1", "R&B", "https://lh3.googleusercontent.com/rOUiGja81fBLATOfzH7YbfID3nhNr5Wv4yu_H22YEkegNkS4NbPdZdMQvmE_AO0CuwyLkdrOc6jPvocn8A=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Kenny Beats", "Louie", "Intrumental Hip-Hop", "https://lh3.googleusercontent.com/OorJxP3YDDbBT35crPc6zETfjzi7Qv9FrlLjkR2WUvIjlHJN3lFAFuobU62nimRhd87yDU7vt2r-P7E=w544-h544-l90-rj", true , true )

    dbHandler.addNewAlbum("Kid Cudi", "Man On The Moon: The End of Day", "Hip-Hop", "https://lh3.googleusercontent.com/qQi4x98fo78a-fURI1IbnRTQyb_TiiQFsw32MkuvHox3lOKhOy-m6bF73mu0zKnaIYcDOtC4AUET5X8=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Kid Cudi", "Man On The Moon III: The Chosen", "Hip-Hop", "https://lh3.googleusercontent.com/kKK3R7F4NoCAWLL8CSYZDwW2pxxF9GMYpHdwMqwEwCUDz06469rUHqybbetPmbh4aDwEb1YD78asITg=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("King Gizzard & The Lizard Wizard", "In San Francisco '16", "Rock", "https://lh3.googleusercontent.com/dZbmu0qTSIuUq1UfTV4OWisRDl3G88qTYm2QoUtlDKsI8T04dV8iY9UOyd_a63E-pRC9KUZoSMorMQNJgA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Knxwledge", "Hud Dreems", "Instrumental Hip-Hop", "https://lh3.googleusercontent.com/M9cLB-PWjdNtDzJaK1NaKT9h5kspcQpnNFEQnj3-g1vjfLuaXWfJNCd8XbKvlIgA6E4k3BF61c-d8hM=w544-h544-l90-rj", true , true)

    dbHandler.addNewAlbum("Knxwledge", "1988", "Instrumental Hip-Hop", "https://lh3.googleusercontent.com/CJGuW6tu87xtCboM9YZHm3kAB8fhJZyWTX9xpGhXTk0Mo3nTkyQRCqsbhIeQxqmx0Llbtfd8KtZOQF_Q=w544-h544-l90-rj", true , true)

    dbHandler.addNewAlbum("Little Simz", "GREY Area", "Hip-Hop", "https://lh3.googleusercontent.com/8jNKiaFVLFbkNdv7ieWrR1L9QK8mW8wgk9weOrBf6AmCGUb7KYJXsuKGjYvZ3aG4xlopeGDJuPBBNvHlWA=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Metro Boomin", "NOT ALL HEROES WEAR CAPES", "Hip-Hop", "https://lh3.googleusercontent.com/uK5Q04SNjhzT74uGkKVm3mH--1bfVBhARTnlTw3v9_1__T8zODvyE7GcGWQgZ1lDviDt6JnT_bGZyLxv=w544-h544-l90-rj",true ,true )

    dbHandler.addNewAlbum("Metro Boomin", "HEROES & VILLAINS", "Hip-Hop", "https://lh3.googleusercontent.com/ii_8E4zawhW8ARpz0GAzFkLGzg9dUOmlmz4Z3skCudi_0IZ6pSKggTj_6iAtPsey7ZaCVHNLfYswhfNr=w544-h544-l90-rj",true ,true )

    dbHandler.addNewAlbum("John Mayer", "TRY!", "Rock + Pop", "https://lh3.googleusercontent.com/Nh8Qs8HyQYyafMrUfGAsPyP6XBV_0M3nGzkdxJh4bGJjaaNM0ZqkmClHX03t9JFnTg6EL56tzY890bGG=w544-h544-s-l90-rj", true, true)

    dbHandler.addNewAlbum("Janelle Monae", "The Electric Lady", "Pop", "https://lh3.googleusercontent.com/NcFHJ8WUA9vDp5wFqX30Nsa9lY4FoTKJDlVn0TzQiXjsYaHDK-bcP6f_a9Uxn2Wags96UEdrOFwoxGMBqw=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Nas", "Magic", "Hip-Hop", "https://lh3.googleusercontent.com/CkcG5MVwLuB_WIdf9lJZhTBkRCPGrPTsuJO6f36fTPb7vjAxKFKnBpAJvlxnZky_BBPDzMWhY9PJlGk=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Post Malone", "Stoney", "Pop + Hip-Hop", "https://lh3.googleusercontent.com/zynDFbA-mfOMc_aepq3p4vAqV_0d71BLJRVF5rblgqkIkIh8LOrZgOy54Pmm--3Wa54c8VwGV6zzVGuE=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("NxWorries", "Yes Lawd! Remixes", "R&B", "https://lh3.googleusercontent.com/o0y2KjnXW1XtnykeAdzX5eAtplJ6t2Jshnhy-UCpJgRGPfb3cuoc2l6L9KN_5-VQ90Obgt6cIw_kSYB1=w544-h544-l90-rj", true, false)

    dbHandler.addNewAlbum("Portugal. The Man", "Woodstock", "Alternative + Pop", "https://lh3.googleusercontent.com/2RWIYY_Ax9lrzDt2D1ZqQwiDZ90MX8wUPthN9dBO4SvpR1-pBCHE4MtA_akzcCSnbMqbuOxwwvUT31KV=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Ol' Dirty Bastard", "Return to the 36 Chambers: The Dirty Version", "Hip-Hop", "https://lh3.googleusercontent.com/PogEakMkUkhw12maINxJMnwCkQIear2HNNB-3iDhphlNpsJ9_M0YLX46wW4fP_0CmLGY4GsxFki6Pbg=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Of Montreal", "Lousy with Sylvianbriar", "Alternative", "https://lh3.googleusercontent.com/J6ekj_hJqytbBEUpUddBcO6LVad2kjRYSl5cpSotDditKFqq7GlK8a5zleG5l-OTercZxihaMjGCyYI=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Pusha T", "My Name Is My Name", "Hip-Hop", "https://lh3.googleusercontent.com/adZSTrsNQL4ZCy_TvnA4mM834goK9vsTfDI_A62RZzjjtngHx9NVIm_8A2E8BGU2duKv7roJLngIV3Rf=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Rhye", "Woman", "Electronic + Pop", "https://lh3.googleusercontent.com/Ddh9IjtN_YKL5npMSFIPlbcsZbY_5Hqz1OdlGQB2sp8XJdrMenmq0A3t3rBgI1V0jKmFaY_5tjuNj7ULSA=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("St. Vincent", "St. Vincent", "Alternative", "https://lh3.googleusercontent.com/fCfKIH566Y7WP63wikC7De4AGk3LlAPL1Uc79ewdSlm5gWLkreXWqSO1nVYR6YCskBY2n5UK1_nGe8M=w544-h544-l90-rj",false ,false )

    dbHandler.addNewAlbum("Timber Timbre", "Hot Dreams", "Alternative", "https://lh3.googleusercontent.com/IrWECvEUMTszkZ3wVIVux3Pd1y09ZzlV4P6iC3TJIIpBlZ71ljYIsCywMekHNhK_GxOoNFzUmG-9aCK9=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("Timber Timbre", "Timber Timbre", "Alternative", "https://upload.wikimedia.org/wikipedia/en/4/47/Timber_Timbre_2009_Album.jpg",false, false)

    dbHandler.addNewAlbum("Timber Timbre", "Creep On Creepin' On", "Alternative", "https://lh3.googleusercontent.com/1M48nd7PIuTCMndBaekqczCYybAlDiOYptJKKjlXZ51g5Qk8zKjx4MZqmnLFmlGptOxKif-Lwu63-zQ=w544-h544-l90-rj",false, false)

    dbHandler.addNewAlbum("Timber Timbre", "Sincerely, Future Pollution", "Alternative", "https://lh3.googleusercontent.com/DJlR_6LZUBoN5vV1cIip-fVeugp0No3yn6pH69qEScIc0lByAyhFR66v4u5jEqV2hOPvo07IheWXwvQ=w544-h544-l90-rj",false, false)

    dbHandler.addNewAlbum("Trippie Redd", "Pegasus", "Hip-Hop", "https://lh3.googleusercontent.com/jRc6BkspEzlogCTpCyc_Ahtl-nyH56-0_npo36bveUAhi3vp3S91UXXo_Wfr_lmUIN16InbcqStkF79PSQ=w544-h544-l90-rj", true, true)

    dbHandler.addNewAlbum("Jack White", "Lazaretto", "Rock", "https://lh3.googleusercontent.com/pYgnBzawCzglwcT9Ff6tbVha6rTTGrTOciJ88wvwrltzNR8MPbsSFDUPQpmByyMRzN4vswfeY1xuU7MJ=w544-h544-l90-rj", false, false)

    dbHandler.addNewAlbum("The White Stripes", "Icky Thump", "Rock", "https://lh3.googleusercontent.com/bJNnYip46gxea7hUHUNIlXYFhTdaEr1pdLWb0i1wWP6nnvc-IOsNPtPGkpygHBeIrvqLfwYkG_phNAQ=w544-h544-l90-rj", false, false)
}

