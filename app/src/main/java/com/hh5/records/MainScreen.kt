package com.hh5.records

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hh5.records.ui.CollectionScreen
import com.hh5.records.ui.ShuffleScreen
import com.hh5.records.ui.StartRecordsScreen
import com.hh5.records.ui.StatsScreen

enum class RecordsScreen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    Collection(title = R.string.collection),
    Stats(title = R.string.stats),
    Shuffle(title = R.string.shuffle)
}


@Composable
fun RecordsApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {

    Scaffold { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = RecordsScreen.Start.name,
            modifier = modifier.padding(innerPadding)
        ) {

            composable(route = RecordsScreen.Start.name) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    StartRecordsScreen(
                        onCollectionClicked = {
                            navController.navigate(RecordsScreen.Collection.name)
                        },
                        onStatsClicked = {
                            navController.navigate(RecordsScreen.Stats.name)
                        },
                        onShuffleClicked = {
                            navController.navigate(RecordsScreen.Shuffle.name)
                        }
                    )
                }

            }

            composable(route = RecordsScreen.Collection.name) {
                CollectionScreen()
            }

            composable(route = RecordsScreen.Stats.name) {
                StatsScreen()
            }

            composable(route = RecordsScreen.Shuffle.name) {
                ShuffleScreen()
            }

        }
    }

}