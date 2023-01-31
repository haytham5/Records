package com.hh5.records.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.hh5.records.R
import com.hh5.records.data.AlbumModel
import com.hh5.records.data.DBHandler


@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    var dbHandler = DBHandler(LocalContext.current)

    var clickedStats by remember { mutableStateOf(false) }

    var genreToggle by remember { mutableStateOf(true) }
    var artistToggle by remember { mutableStateOf(false) }
    var lisfavToggle by remember {mutableStateOf(false)}


    val density = LocalDensity.current


    Scaffold(
        modifier = modifier,
        topBar = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colors.primary)
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = modifier.weight(1f))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = modifier.weight(5f)
                    ) {
                        StatsHeader(clickedStats, density)

                        StatsButtons(
                            density = density,
                            clickedStats = clickedStats,
                            genreToggle = genreToggle,
                            artistToggle = artistToggle,
                            lisfavToggle = lisfavToggle,
                            onGenreClicked = {
                                genreToggle = !genreToggle

                                if(artistToggle) artistToggle = false
                                if(lisfavToggle) lisfavToggle = false
                            },
                            onArtistsClicked = {
                                artistToggle = !artistToggle

                                if(genreToggle) genreToggle = false
                                if(lisfavToggle) lisfavToggle = false
                            },
                            onLisFavClicked = {
                                lisfavToggle = !lisfavToggle

                                if(genreToggle) genreToggle = false
                                if(artistToggle) artistToggle = false
                            }
                        )
                    }

                    Box(contentAlignment = Alignment.CenterEnd, modifier = modifier.weight(1f)) {
                        IconButton(
                            onClick = {
                                clickedStats = !clickedStats
                            }) {
                            Icon(
                                imageVector = if (!clickedStats) Icons.Default.MoreVert else Icons.Filled.ExpandLess,
                                tint = colors.onSurface,
                                contentDescription = stringResource(R.string.Filter)
                            )
                        }
                    }


                }
            }
        }
    ) {
        if(genreToggle) {
            GenreChart(dbHandler = dbHandler, modifier)
        }

        else if(artistToggle) {
            ArtistChart(dbHandler = dbHandler, modifier)
        }

        else if(lisfavToggle) {
            LisFavChart(dbHandler = dbHandler, modifier)
        }
    }

}

@Composable
fun StatsButtons(
    density: Density,
    clickedStats: Boolean,
    genreToggle: Boolean,
    artistToggle: Boolean,
    lisfavToggle: Boolean,
    onGenreClicked: () -> Unit,
    onArtistsClicked: () -> Unit,
    onLisFavClicked: () -> Unit,
    modifier: Modifier = Modifier) {


    AnimatedVisibility(
        visible = clickedStats,
        enter = slideInVertically {
            with(density) { 40.dp.roundToPx() }
        } + expandVertically(
            expandFrom = Alignment.Bottom
        ) + fadeIn(
            initialAlpha = 0.3f
        ),
        exit = slideOutVertically() + shrinkVertically() + fadeOut()
    ) {
        Row(modifier = modifier
            .padding(5.dp)
            .fillMaxWidth()) {
            Button(
                modifier = modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if(genreToggle) colors.secondaryVariant else colors.secondary,
                    contentColor = colors.onBackground),
                shape = RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp),
                onClick = {
                    onGenreClicked()
                }) {
                Text(
                    style = MaterialTheme.typography.h5,
                    text = "Genres"
                )
            }

            Button(
                modifier = modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if(artistToggle) colors.secondaryVariant else colors.secondary,
                    contentColor = colors.onBackground),
                shape = RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp, topStart = 0.dp, bottomStart = 0.dp),

                onClick = {
                    onArtistsClicked()
                }) {
                Text(
                    style = MaterialTheme.typography.h5,
                    text = "Artists"
                )
            }

            Button(
                modifier = modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if(lisfavToggle) colors.secondaryVariant else colors.secondary,
                    contentColor = colors.onBackground),
                shape = RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp),
                onClick = {
                    onLisFavClicked()
                }) {
                Text(
                    style = MaterialTheme.typography.h5,
                    text = "Faves"
                )
            }
        }
    }
}

@Composable
fun StatsHeader(clickedStats: Boolean, density: Density, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = !clickedStats,
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
            text = stringResource(R.string.stats),
            style = MaterialTheme.typography.h1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Visible,
            modifier = modifier
                .fillMaxWidth()
        )
    }
}

@Composable
fun GenreChart(dbHandler: DBHandler, modifier: Modifier) {
    val data = countAllGenres(dbHandler.readAlbums()!!).toList().sortedByDescending { (_, value) -> value}.toMap()

    val totalSum = data.values.sum()
    val floatValue = mutableListOf<Float>()

    data.values.forEachIndexed { index, values ->
        floatValue.add(index, 360 * values.toFloat() / totalSum.toFloat())
    }

    val colors = mutableListOf<Color>()
    val colorInts = mutableListOf<Long>()

    for(genre in data) {
        colorInts.add(0xFF000000 + (Math.random() * 16777216).toInt())
    }

    for(color in colorInts) {
        colors.add(Color(color))
    }

    /*TODO Sort the red values first, then the Green Values, then the Blue values, in order of shade.
    If red is highest then -> and if both have red highest who has the highest number*/
//    val colorComparator =  Comparator<Color> { a, b ->
//        if(a == b) return@Comparator 0
//
//        var aMS = findMainShade(a)
//        var bMS = findMainShade(b)
//
//
//        if(aMS == "red" && (bMS == "green" || bMS == "blue")) return@Comparator 1
//        else if(aMS == "green" && bMS == "blue") return@Comparator 1
//        else return@Comparator 0
//
//
//    }

    var lastValue = 0f

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
            text = "Your Genres Are...",
            modifier = modifier.align(Alignment.TopCenter)
        )

        Spacer(modifier = modifier.padding(8.dp))


        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .background(color = MaterialTheme.colors.surface)
                .padding(5.dp)
                .fillMaxHeight(0.85f)
                .align(Alignment.Center)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(120.dp * 2f)
                        .fillMaxSize()
                        .padding(30.dp)
                        .align(Alignment.Center)
                ) {

                    floatValue.forEachIndexed { index, value ->
                        drawArc(
                            color = colors[index],
                            lastValue,
                            value,
                            useCenter = false,
                            style = Stroke(25.dp.toPx(), cap = StrokeCap.Butt)
                        )
                        lastValue += value
                    }
                }

                Text(
                    fontSize = 27.sp,
                    fontFamily = FontFamily(
                        Font(R.font.righteous)
                    ),
                    text = dbHandler.readAlbums()!!.size.toString(),
                    modifier = modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = modifier.padding(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                data.values.forEachIndexed { index, value ->
                    GenreItem(
                        data = Pair(data.keys.elementAt(index), value),
                        color = colors[index]
                    )
                }
            }
        }
    }

}

fun findMainShade(a: Color): String {
    return if((a.red > a.green && a.red > a.blue) || (a.red == a.green && a.red > a.blue)) "red"
    else if ((a.green > a.red && a.green > a.blue) || (a.green == a.blue && a.green > a.red)) "green"
    else "blue"
}

@Composable
fun GenreItem(
    data: Pair<String, Int>,
    color: Color
) {
    Spacer(modifier = Modifier.padding(5.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .background(
                    color = color,
                    shape = RoundedCornerShape(50.dp)
                )
                .fillMaxHeight()
                .aspectRatio(1f)
        )

        Text(
            modifier = Modifier
                .padding(start = 15.dp)
                .fillMaxHeight()
                .align(Alignment.CenterVertically),
            text = data.first + " / " + data.second.toString(),
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            color = colors.onPrimary
        )
    }

}

fun countAllGenres(readAlbums: ArrayList<AlbumModel>): MutableMap<String, Int> {
    val genreList = mutableMapOf<String, Int>()

    for(album in readAlbums) {
        if(album.genre.contains("+")) {
            var genre1 = album.genre.split(" + ")[0]
            var genre2 = album.genre.split(" + ")[1]

            if(!genreList.contains(genre1)) genreList[genre1] = 0
            else if(genreList.contains(genre1)) genreList[genre1] = genreList[genre1]?.plus(1) ?: 0

            if(!genreList.contains(genre2)) genreList[genre2] = 0
            else if(genreList.contains(genre2)) genreList[genre2] = genreList[genre2]?.plus(1) ?: 0
        }

        else {
            if(!genreList.contains(album.genre)) genreList[album.genre] = 0
            if(genreList.contains(album.genre)) genreList[album.genre] = genreList[album.genre]?.plus(1) ?: 0

        }
    }

    return genreList
}

fun countAllArtists(readAlbums: List<AlbumModel>): MutableMap<String, Pair<String, Int>> {
    var artistList = mutableMapOf<String, Pair<String, Int>>()


    for(album in readAlbums) {
        if(!artistList.contains(album.artist)) {
            artistList[album.artist] = Pair(album.cover, 1)
        }
        else {
            artistList[album.artist] = Pair(artistList[album.artist]?.first, artistList[album.artist]?.second?.plus(1))
                    as Pair<String, Int>
        }
    }

    return artistList
}

fun countLisFav(readAlbums: List<AlbumModel>): List<Int> {
    var lisFavList = mutableListOf(readAlbums.size, 0, 0)

    for(album in readAlbums) {
        if(album.listened == 1 && album.favorite == 0) {
            lisFavList[1] += 1
        }

        else if (album.listened == 1 && album.favorite == 1) {
            lisFavList[2] +=1
        }
    }

    return lisFavList.toList()
}

@Composable
fun ArtistChart(dbHandler: DBHandler, modifier: Modifier) {
    val artists = countAllArtists(dbHandler.readAlbums()!!.sortedBy { it.artist })

    val data = artists.toList().sortedWith(compareByDescending<Pair<String, Pair<String, Int>>> { it.second.second }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.first }).toMap()

    var top = 0;

    val totalArtists = data.size

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
            text = "Your Artists Are...",
            modifier = modifier.align(Alignment.TopCenter)
        )

        Spacer(modifier = modifier.padding(8.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .background(color = colors.surface)
                .padding(5.dp)
                .align(Alignment.Center)
                .fillMaxHeight(0.85f)
                .verticalScroll(rememberScrollState())
        ) {
            data.values.forEachIndexed { index, value ->

                /*TODO Consider making a signifier for how many albums each artist has*/
                if(index == top) {
                    top += 10
                    Row(modifier = modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center) {
                        Text(
                            modifier = Modifier
                                .padding(15.dp),
                            text = "TOP $top" + if(top <= 10) " OF $totalArtists" else "",
                            fontFamily = FontFamily(
                                Font(R.font.righteous)
                            ),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = colors.onPrimary
                        )
                    }

                    top = (top*2) - 10
                }

                ArtistItem(
                    data = Pair(data.keys.elementAt(index), value)
                )
            }
        }
    }
}

@Composable
fun ArtistItem(data: Pair<String, Pair<String, Int>>, modifier: Modifier = Modifier) {

    Spacer(modifier = Modifier.padding(5.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
        ) {
            Image(
                modifier = modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop,
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = data.second.first)
                        .allowHardware(false)
                        .build()
                ),
                contentDescription = null
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                modifier = Modifier
                    .padding(start = 15.dp),
                text = data.first,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = colors.onPrimary
            )

            Text(
                modifier = Modifier
                    .padding(start = 15.dp),
                text = data.second.second.toString() + " Albums",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = colors.onPrimary
            )
        }

    }
}

@Composable
fun LisFavChart(dbHandler: DBHandler, modifier: Modifier) {
    val data = countLisFav(dbHandler.readAlbums()!!)

    val totalSum = data[0].toFloat()
    val floatValue = listOf(
        data[2].toFloat()/totalSum,
        data[1].toFloat()/totalSum,
        (data[0] - (data[1] + data[2])).toFloat()/totalSum
    )

    val colors = listOf(colors.primary, colors.secondary, colors.secondaryVariant)

    /*TODO Maybe make this a pie chart*/

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
            text = "Your Faves Are...",
            modifier = modifier.align(Alignment.TopCenter)
        )

        Spacer(modifier = modifier.padding(8.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .background(color = MaterialTheme.colors.surface)
                .padding(5.dp)
                .fillMaxHeight(0.85f)
                .align(Alignment.Center)
        ) {
            Row(modifier = modifier.fillMaxWidth()) {

               floatValue.forEachIndexed { index, _ ->
                   Box(modifier = modifier.background(colors[index]).height(50.dp).weight(floatValue[index]))
                }
            }
        }
    }
}

@Preview
@Composable
fun StatsPreview() {
    StatsScreen()
}
