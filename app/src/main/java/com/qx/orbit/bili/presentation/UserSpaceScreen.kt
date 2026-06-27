package com.qx.orbit.bili.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.togetherWith
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.wear.compose.material3.HorizontalPageIndicator
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextLayoutResult
import com.qx.orbit.bili.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.qx.orbit.bili.presentation.ui.components.LevelIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import com.qx.orbit.bili.presentation.component.WysTimeText
import androidx.wear.compose.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.qx.orbit.bili.data.model.Dynamic
import com.qx.orbit.bili.data.model.UserInfo
import com.qx.orbit.bili.data.model.VideoCard
import com.qx.orbit.bili.data.model.ArticleCard
import com.qx.orbit.bili.presentation.ArticleCardItem
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.focusable
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.qx.orbit.bili.presentation.ui.components.RecommendVideoCard
import com.qx.orbit.bili.presentation.viewmodel.UserSpaceViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserSpaceScreen(
    mid: Long,
    viewModel: UserSpaceViewModel,
    navController: NavHostController
) {
    LaunchedEffect(mid) {
        viewModel.initUser(mid)
    }

    val userInfo by viewModel.userInfo.collectAsState()
    val dynamics by viewModel.dynamics.collectAsState()
    val videos by viewModel.videos.collectAsState()
    val articles by viewModel.articles.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 3 })
    val focusRequesters = remember { List(3) { FocusRequester() } }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AnimatedContent(
            targetState = userInfo == null,
            transitionSpec = { fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300)) },
            label = "LoadingAnimation"
        ) { isInitialLoading ->
            if (isInitialLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val isFocusedPage = pagerState.currentPage == page
                        LaunchedEffect(isFocusedPage) {
                            if (isFocusedPage) {
                                try { focusRequesters[page].requestFocus() } catch (e: Exception) {}
                            }
                        }

                        when (page) {
                            0 -> UserDynamicsPage(userInfo, dynamics, focusRequesters[0], navController, viewModel)
                            1 -> UserVideosPage(videos, focusRequesters[1], navController, viewModel)
                            2 -> UserArticlesPage(articles, focusRequesters[2], navController, viewModel)
                        }
                    }
                    HorizontalPageIndicator(
                        pagerState = pagerState,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UserDynamicsPage(
    userInfo: UserInfo?,
    dynamics: List<Dynamic>,
    focusRequester: FocusRequester,
    navController: NavHostController,
    viewModel: UserSpaceViewModel
) {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    ScreenScaffold(
        timeText = { WysTimeText() },
        scrollState = listState, 
        modifier = Modifier.fillMaxSize().focusRequester(focusRequester)
    ) { contentPadding ->
        TransformingLazyColumn(
            state = listState,
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                userInfo?.let { info ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .transformedHeight(this, transformationSpec)
                            .graphicsLayer {
                                with(transformationSpec) {
                                    applyContainerTransformation(scrollProgress)
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val isLive = info.live_room?.live_status == 1
                        val avatarModifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .let {
                                if (isLive) {
                                    it.border(2.dp, Color(0xFFFF69B4), CircleShape).clickable {
                                        navController.navigate("live_room/${info.live_room?.roomid}")
                                    }
                                } else {
                                    it
                                }
                            }

                        AsyncImage(
                            model = info.avatar.let {
                                when {
                                    it.startsWith("//") -> "https:$it"
                                    it.startsWith("http://") -> it.replaceFirst("http://", "https://")
                                    else -> it
                                }
                            },
                            contentDescription = info.name,
                            modifier = avatarModifier,
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = info.name, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            LevelIcon(level = info.level, isSenior = info.is_senior_member == 1)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "粉丝: ${info.fans} · 关注: ${info.following}", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = info.sign, fontSize = 12.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            itemsIndexed(dynamics) { index, item ->
                if (index >= dynamics.size - 3) {
                    LaunchedEffect(index) {
                        viewModel.loadMoreDynamics()
                    }
                }
                Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp).transformedHeight(this@itemsIndexed, transformationSpec)) {
                    DynamicCard(
                        item = item,
                        transformation = SurfaceTransformation(transformationSpec),
                        onClick = { if (item.major_type == "MAJOR_TYPE_OPUS" || item.major_type == "MAJOR_TYPE_ARTICLE") navController.navigate("opus_detail/${item.dynamicId}") else navController.navigate("dynamic_detail/${item.dynamicId}") },
                        onUserClick = { mid -> navController.navigate("user_space/$mid") },
                        onArchiveClick = { bvid, aid -> navController.navigate("detail/$bvid/$aid") }
                    )
                }
            }
        }
    }
}

@Composable
fun UserVideosPage(
    videos: List<VideoCard>,
    focusRequester: FocusRequester,
    navController: NavHostController,
    viewModel: UserSpaceViewModel
) {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    ScreenScaffold(
        timeText = { WysTimeText() },
        scrollState = listState, 
        modifier = Modifier.fillMaxSize().focusRequester(focusRequester)
    ) { contentPadding ->
        TransformingLazyColumn(
            state = listState,
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                ListHeader {
                    Text(text = "发布的视频")
                }
            }
            itemsIndexed(videos) { index, item ->
                if (index >= videos.size - 3) {
                    LaunchedEffect(index) {
                        viewModel.loadMoreVideos()
                    }
                }
                Box(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp).transformedHeight(this@itemsIndexed, transformationSpec)) {
                    RecommendVideoCard(
                        item = item, 
                        onClick = {
                            navController.navigate("detail/${item.bvid}/${item.aid}")
                        },
                        transformation = SurfaceTransformation(transformationSpec)
                    )
                }
            }
        }
    }
}

@Composable
fun UserArticlesPage(
    articles: List<ArticleCard>,
    focusRequester: FocusRequester,
    navController: NavHostController,
    viewModel: UserSpaceViewModel
) {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    ScreenScaffold(
        timeText = { WysTimeText() },
        scrollState = listState, 
        modifier = Modifier.fillMaxSize().focusRequester(focusRequester)
    ) { contentPadding ->
        TransformingLazyColumn(
            state = listState,
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                ListHeader {
                    Text(text = "发布的图文")
                }
            }
            itemsIndexed(articles) { index, item ->
                if (index >= articles.size - 3) {
                    LaunchedEffect(index) {
                        viewModel.loadMoreArticles()
                    }
                }
                Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp).transformedHeight(this@itemsIndexed, transformationSpec)) {
                    ArticleCardItem(
                        item = item, 
                        onClick = {
                            // navController.navigate("article/${item.id}")
                        },
                        transformation = SurfaceTransformation(transformationSpec)
                    )
                }
            }
        }
    }
}

@Composable
fun DynamicCard(
    item: Dynamic, 
    modifier: Modifier = Modifier,
    transformation: SurfaceTransformation? = null,
    onClick: () -> Unit = {},
    onUserClick: (Long) -> Unit = {},
    onArchiveClick: (String, Long) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val getImageRequest = { url: String, isCover: Boolean ->
        val fixedUrl = when {
            url.startsWith("//") -> "https:$url"
            url.startsWith("http://") -> url.replaceFirst("http://", "https://")
            else -> url
        }
        val finalUrl = if (isCover && !fixedUrl.contains("@")) "$fixedUrl@480w_270h_1c.webp" else fixedUrl
        ImageRequest.Builder(context)
            .data(finalUrl)
            .crossfade(true)
            .build()
    }

    Card(
        onClick = onClick, 
        modifier = modifier.fillMaxWidth(), 
        shape = RoundedCornerShape(12.dp),
        transformation = transformation
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = item.userInfo?.avatar,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.akari),
                    modifier = Modifier.size(28.dp).clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.userInfo?.name ?: "Unknown",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (item.major_type == "MAJOR_TYPE_ARCHIVE") "投稿了视频" else "发布了动态",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }
            Text(
                text = item.pubTime, 
                fontSize = 10.sp, 
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (item.title.isNotEmpty()) {
                Text(text = item.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
            }
            if (item.content.isNotEmpty() && item.content != item.archiveTitle) {
                val (richText, inlineContent) = parseRichText(item.content, item.emotes, item.members, emptyMap())
                val textLayoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
                Text(
                    text = richText,
                    inlineContent = inlineContent,
                    fontSize = 12.sp,
                    color = Color.White,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified),
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val pos = textLayoutResult.value?.getOffsetForPosition(offset) ?: -1
                            var handled = false
                            if (pos >= 0) {
                                richText.getStringAnnotations("USER", pos, pos).firstOrNull()?.let {
                                    onUserClick(it.item.toLongOrNull() ?: 0L)
                                    handled = true
                                }
                            }
                            if (!handled) onClick()
                        }
                    },
                    onTextLayout = { textLayoutResult.value = it }
                )
            }
            
            if (item.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                if (item.images.size == 1) {
                    AsyncImage(
                        model = getImageRequest(item.images.first(), false),
                        contentDescription = null,
                        modifier = Modifier.height(100.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        item.images.take(3).forEach { imgUrl ->
                            AsyncImage(
                                model = getImageRequest(imgUrl, false),
                                contentDescription = null,
                                modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
            
            if (item.major_type == "MAJOR_TYPE_ARCHIVE" && item.cover.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(4.dp).clickable {
                        if (item.bvid.isNotEmpty() || item.comment_id > 0) {
                            onArchiveClick(item.bvid, item.comment_id)
                        }
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = getImageRequest(item.cover, true),
                        contentDescription = null,
                        modifier = Modifier.width(60.dp).height(40.dp).clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = item.archiveTitle, fontSize = 11.sp, color = Color.LightGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            
            item.dynamic_forward?.let { forward ->
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier.fillMaxWidth().background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(6.dp)
                ) {
                    Text(text = "@${forward.userInfo?.name ?: "已失效动态"}", fontSize = 10.sp, color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    if (forward.content.isNotEmpty() && forward.content != forward.archiveTitle) {
                        val (richText, inlineContent) = parseRichText(forward.content, forward.emotes, forward.members, emptyMap())
                        val textLayoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
                        Text(
                            text = richText,
                            inlineContent = inlineContent,
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified),
                            modifier = Modifier.pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val pos = textLayoutResult.value?.getOffsetForPosition(offset) ?: -1
                                    var handled = false
                                    if (pos >= 0) {
                                        richText.getStringAnnotations("USER", pos, pos).firstOrNull()?.let {
                                            onUserClick(it.item.toLongOrNull() ?: 0L)
                                            handled = true
                                        }
                                    }
                                    if (!handled) onClick()
                                }
                            },
                            onTextLayout = { textLayoutResult.value = it }
                        )
                    }
                    if (forward.images.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        if (forward.images.size == 1) {
                            AsyncImage(
                                model = getImageRequest(forward.images.first(), false),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                forward.images.take(3).forEach { imgUrl ->
                                    AsyncImage(
                                        model = getImageRequest(imgUrl, false),
                                        contentDescription = null,
                                        modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                    if (forward.major_type == "MAJOR_TYPE_ARCHIVE" && forward.cover.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = getImageRequest(forward.cover, true),
                                contentDescription = null,
                                modifier = Modifier.width(50.dp).height(30.dp).clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = forward.archiveTitle, fontSize = 10.sp, color = Color.LightGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}
