package com.ediapp.a1routine

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpsFragment() {
    val images = listOf(
        R.drawable.fragment_home,
        R.drawable.fragment_list,
        R.drawable.activity_setting,
    )

    val pagerState = rememberPagerState(pageCount = { images.size })
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f) // Allows pager to take available space
                .padding(10.dp)
        ) { page ->
            Image(
                painter = painterResource(id = images[page]),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Button
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                enabled = pagerState.currentPage > 0 // Disable if it's the first page
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous Page")
            }

            // Page Indicator (optional)
            Text("${pagerState.currentPage + 1} / ${pagerState.pageCount}")

            // Next Button
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                enabled = pagerState.currentPage < pagerState.pageCount - 1 // Disable if it's the last page
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next Page")
            }
        }
    }
}
