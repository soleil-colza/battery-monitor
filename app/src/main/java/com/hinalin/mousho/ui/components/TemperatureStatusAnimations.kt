package com.hinalin.mousho.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.hinalin.mousho.R

@Composable
fun LottieAnimationView(isOverheated: Boolean) {
    if (isOverheated) {
        IsOverheatedAnimation()
    } else {
        IsCoolEnoughAnimation()
    }
}

@Composable
fun IsOverheatedAnimation() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.is_overheated))
    LottieAnimation(
        composition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier.size(400.dp),
    )
}

@Composable
fun IsCoolEnoughAnimation() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.is_cool_enough))
    LottieAnimation(
        composition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier.size(500.dp),
    )
}
