package com.invi.finerc

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun CardStack(
    cardColors: List<Color>,
    modifier: Modifier = Modifier,
    visibleCount: Int = 3,
    cardWidth: Dp = 280.dp,
    cardHeight: Dp = 180.dp,
    cardOffset: Dp = 16.dp
) {
    var cards by remember { mutableStateOf(cardColors) }
    val scope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(0f) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val cardsToShow = cards.take(visibleCount)
        cardsToShow.reversed().forEachIndexed { i, color ->
            val offset = cardOffset * i
            val isTop = i == cardsToShow.lastIndex
            val yOffset = if (isTop) dragOffset.value.dp else 0.dp
            Card(
                modifier = Modifier
                    .size(cardWidth, cardHeight)
                    .offset(y = offset + yOffset)
                    .then(
                        if (isTop) Modifier.pointerInput(cards) {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    scope.launch {
                                        if (dragOffset.value < -60) {
                                            // Dragged up: move top card to end
                                            dragOffset.animateTo(-cardHeight.value, tween(200))
                                            cards = cards.drop(1) + cards.first()
                                        } else if (dragOffset.value > 60) {
                                            // Dragged down: move last card to top
                                            dragOffset.animateTo(cardHeight.value, tween(200))
                                            cards = listOf(cards.last()) + cards.dropLast(1)
                                        }
                                        dragOffset.animateTo(0f, tween(200))
                                    }
                                },
                                onVerticalDrag = { _, dragAmount ->
                                    scope.launch {
                                        dragOffset.snapTo(dragOffset.value + dragAmount)
                                    }
                                }
                            )
                        } else Modifier
                    ),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color)
                )
            }
        }
    }
}