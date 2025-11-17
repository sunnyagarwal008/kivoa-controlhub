package com.kivoa.controlhub.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kivoa.controlhub.AppBarState
import com.kivoa.controlhub.AppBarViewModel
import com.kivoa.controlhub.R
import com.kivoa.controlhub.Screen


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = viewModel(),
    navController: NavController,
    appBarViewModel: AppBarViewModel
) {
    LaunchedEffect(Unit) {
        appBarViewModel.setAppBarState(
            AppBarState(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.kivoa_logo),
                            contentDescription = "Kivoa Logo",
                            modifier = Modifier.size(32.dp)
                        )
                        Text(Screen.Search.route, modifier = Modifier.padding(start = 8.dp))
                    }
                },
                navigationIcon = { },
                actions = { }
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.kivoa_logo),
            contentDescription = "Kivoa Logo",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(16.dp))

        SearchContent(viewModel = viewModel, navController = navController, onProductClick = {})
    }
}