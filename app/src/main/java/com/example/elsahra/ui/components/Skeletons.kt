package com.example.elsahra.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.elsahra.util.shimmer

@Composable
fun MovieItemSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
        ) {
            Box(modifier = Modifier.fillMaxSize().shimmer())
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmer()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .width(30.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )
        }
    }
}

@Composable
fun MovieRowSkeleton(
    modifier: Modifier = Modifier,
    showTitle: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (showTitle) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .shimmer()
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmer()
                    )
                }
            }
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            userScrollEnabled = false
        ) {
            items(5) {
                MovieItemSkeleton(modifier = Modifier.width(140.dp))
            }
        }
    }
}

@Composable
fun HeroBannerSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().shimmer()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.height(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .width(if (it == 0) 24.dp else 8.dp)
                        .height(8.dp)
                        .clip(CircleShape)
                        .shimmer()
                )
            }
        }
    }
}

@Composable
fun MoviesGridSkeleton(
    modifier: Modifier = Modifier,
    columns: GridCells = GridCells.Fixed(2)
) {
    LazyVerticalGrid(
        columns = columns,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false
    ) {
        items(6) {
            MovieItemSkeleton(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun MovieDetailSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
            Box(modifier = Modifier.fillMaxSize().shimmer())
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.3f to Color.Transparent,
                            0.7f to Color.Black.copy(alpha = 0.1f),
                            1f to Color.Black.copy(0.3f)
                        )
                    )
            )
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(128.dp)
                        .height(28.dp)
                        .clip(CircleShape)
                        .shimmer()
                )
                Spacer(modifier = Modifier.height(8.dp))
                DetailTextLineSkeleton(widthFraction = 0.86f)
                Spacer(modifier = Modifier.height(5.dp))
                DetailTextLineSkeleton(widthFraction = 0.62f)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    listOf(52.dp, 84.dp, 76.dp).forEachIndexed { index, width ->
                        Box(
                            modifier = Modifier
                                .width(width)
                                .height(24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .shimmer()
                        )
                        if (index < 2) Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .shimmer()
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .shimmer()
                    )
                }
            }
        }

        // Movies initially expose About, Suggested, and Review. The content below is
        // deliberately the selected About tab only.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            DetailSectionTitleSkeleton(width = 100.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth().height(172.dp), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailTextLineSkeleton(1f)
                    Spacer(modifier = Modifier.height(10.dp))
                    DetailTextLineSkeleton(0.92f)
                    Spacer(modifier = Modifier.height(10.dp))
                    DetailTextLineSkeleton(0.78f)
                    Spacer(modifier = Modifier.height(10.dp))
                    DetailTextLineSkeleton(0.56f)
                    Spacer(modifier = Modifier.height(10.dp))
                    DetailTextLineSkeleton(0.30f)
                    Spacer(modifier = Modifier.height(10.dp))
                    DetailTextLineSkeleton(0.60f)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailCrewCardSkeleton(modifier = Modifier.weight(1f))
                DetailCrewCardSkeleton(modifier = Modifier.weight(1f))
            }


        }
    }
}

@Composable
private fun DetailTextLineSkeleton(widthFraction: Float) {
    Box(
        modifier = Modifier.fillMaxWidth(widthFraction).height(14.dp)
            .clip(RoundedCornerShape(4.dp)).shimmer()
    )
}

@Composable
private fun DetailSectionTitleSkeleton(width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier.width(width).height(28.dp)
            .clip(RoundedCornerShape(4.dp)).shimmer()
    )
}

@Composable
private fun DetailCrewCardSkeleton(modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(80.dp), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.width(52.dp).height(12.dp)
                    .clip(RoundedCornerShape(4.dp)).shimmer()
            )
            Spacer(modifier = Modifier.height(10.dp))
            DetailTextLineSkeleton(0.9f)
        }
    }
}
