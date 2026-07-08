package com.qx.orbit.bili.presentation.settings

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.zIndex
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.PagerState
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.qx.orbit.bili.presentation.ui.components.WysTimeText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.wear.compose.material3.SuccessConfirmationDialog
import androidx.wear.compose.material3.curvedText

@Composable
fun OneFingerZoomTutorialScreen(navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    var showSuccessDialog by remember { mutableStateOf(false) }

    ScreenScaffold(
        timeText = { WysTimeText() }
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                0 -> TheoryPage(
                    onNext = {
                        // handled by Button click
                    },
                    pagerState = pagerState
                )
                1 -> PracticePage(
                    onSuccess = {
                        showSuccessDialog = true
                    }
                )
            }
        }
    }

    SuccessConfirmationDialog(
            visible = showSuccessDialog,
            curvedText = {curvedText("你太棒了！")},
            onDismissRequest = {
                showSuccessDialog = false
                navController.popBackStack()
            }
        )
}

@Composable
fun TheoryPage(onNext: () -> Unit, pagerState: PagerState) {
    val coroutineScope = rememberCoroutineScope()
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = "单指缩放",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "开启后，只需一根手指即可缩放视频，适合只有单点触控的手表。\n\n操作方法：\n先单击屏幕，紧接着按住屏幕快速上下滑动即可缩放。",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                icon = {Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)},
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("去实践")
            }
        }
    }
}

@Composable
fun PracticePage(onSuccess: () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val viewConfiguration = LocalViewConfiguration.current
    var currentStep by remember { mutableIntStateOf(0) } // 0: zoom in, 1: pan, 2: zoom out

    val infiniteTransition = rememberInfiniteTransition()
    val hintOffsetY by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "finger_swipe"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val stepText = when (currentStep) {
            0 -> "第1步：单指放大\n(双击并按住，向下滑动)"
            1 -> "第2步：单指平移\n(单指按住方块拖动)"
            2 -> "第3步：单指缩小\n(双击并按住，向上滑动)"
            else -> "完成！"
        }
        Text(
            text = stepText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(1f)
                .background(
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(4.dp)
        )

        Box(
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                }
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            Text("缩放我", style = MaterialTheme.typography.bodySmall)
        }

        if (currentStep != 1) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = "Swipe Hint",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer {
                        translationY = hintOffsetY
                    }
            )
        } else {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = "Pan Hint",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(48.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        if (currentStep >= 1) {
                            offsetX += pan.x * scale
                            offsetY += pan.y * scale
                            if (currentStep == 1 && (kotlin.math.abs(offsetX) > 100f || kotlin.math.abs(offsetY) > 100f)) {
                                currentStep = 2
                            }
                        }
                    }
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        var isUp = false
                        val down1 = awaitFirstDown(requireUnconsumed = false)
                        val up = withTimeoutOrNull(viewConfiguration.doubleTapTimeoutMillis) {
                            while (!isUp) {
                                val event = awaitPointerEvent(PointerEventPass.Main)
                                if (event.changes.any { it.changedToUp() }) {
                                    isUp = true
                                    return@withTimeoutOrNull event
                                }
                            }
                            null
                        }
                        if (up != null) {
                            val down2 = withTimeoutOrNull(viewConfiguration.doubleTapTimeoutMillis) {
                                awaitFirstDown(requireUnconsumed = false)
                            }
                            if (down2 != null) {
                                var isZooming = false
                                var lastY = down2.position.y
                                val startY = down2.position.y
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Main)
                                    val ptr = event.changes.firstOrNull { it.id == down2.id }
                                    if (ptr == null || !ptr.pressed) break

                                    if (!isZooming) {
                                        val dy = ptr.position.y - startY
                                        if (kotlin.math.abs(dy) > viewConfiguration.touchSlop) {
                                            isZooming = true
                                        }
                                    }
                                    if (isZooming) {
                                        val dy = ptr.position.y - lastY
                                        val zoomFactor = 1f + dy * 0.005f
                                        scale = (scale * zoomFactor).coerceIn(0.5f, 3f)
                                        lastY = ptr.position.y
                                        ptr.consume()

                                        if (currentStep == 0 && scale > 1.8f) {
                                            currentStep = 1
                                        } else if (currentStep == 2 && scale < 1.0f) {
                                            onSuccess()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        )
    }
}
