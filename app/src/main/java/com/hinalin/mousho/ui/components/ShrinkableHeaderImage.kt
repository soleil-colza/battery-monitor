package com.hinalin.mousho.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ShrinkableHeaderImage(
    imageRes: Int,
    scrollState: ScrollState,
    maxHeight: Dp,
    minHeight: Dp,
) {
    val height by remember {
        derivedStateOf {
            maxHeight - (scrollState.value / 2f).dp.coerceAtMost(maxHeight - minHeight)
        }
    }

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = null,
        modifier =
        Modifier
            .fillMaxWidth()
            .height(height),
        contentScale = ContentScale.Crop,
    )
}
