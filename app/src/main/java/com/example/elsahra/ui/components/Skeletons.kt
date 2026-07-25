package com.example.elsahra.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
                    .clip(CircleShape)
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
        // Media Header Skeleton
        Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
            Box(modifier = Modifier.fillMaxSize().shimmer())
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.3f to Color.Transparent,
                            0.7f to Color.Black.copy(alpha = 0.5f),
                            1f to Color.Black
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
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(24.dp)
                        .clip(CircleShape)
                        .shimmer()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .shimmer()
                        )
                        if (it < 2) Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
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

        // Tabs Skeleton
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            repeat(4) {
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
            }
        }

        // Content Skeleton (Storyline)
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shimmer()
            )
        }
    }
}
