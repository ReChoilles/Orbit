package com.qx.orbit.bili.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.qx.orbit.bili.R
import com.qx.orbit.bili.data.model.VideoCard
import com.qx.orbit.bili.presentation.ui.components.RecommendVideoCard
import com.qx.orbit.bili.presentation.ui.components.WysTimeText
import com.qx.orbit.bili.presentation.util.rememberSafeRotaryScrollableBehavior
import com.qx.orbit.bili.presentation.viewmodel.FavoriteFolderViewModel
import com.qx.orbit.bili.data.remote.CookieManager

@Composable
fun FavoriteFoldersScreen(
    viewModel: FavoriteFolderViewModel,
    navController: NavController
) {
    val folderList by viewModel.folderList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    
    // We get mid to pass to the detail screen since some APIs might need it.
    // Wait, let's parse inside a remember block or just read it.
    val mid = remember {
        val midStr = CookieManager.getInfoFromCookie("DedeUserID")
        midStr.toLongOrNull() ?: 0L
    }

    ScreenScaffold(
        timeText = { WysTimeText() },
        scrollState = listState,
        modifier = Modifier.fillMaxSize()
    ) { contentPadding ->
        TransformingLazyColumn(
            state = listState,
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize(),
            rotaryScrollableBehavior = rememberSafeRotaryScrollableBehavior(listState)
        ) {
            item {
                ListHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec)
                ) {
                    Text("我的收藏夹", color = MaterialTheme.colorScheme.primary)
                }
            }

            if (errorMessage != null && folderList.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_empty22),
                            contentDescription = "Error",
                            modifier = Modifier.height(96.dp)
                        )
                        Text(
                            text = errorMessage ?: "",
                            modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (folderList.isEmpty() && !isLoading) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_empty22),
                            contentDescription = "Error",
                            modifier = Modifier.height(96.dp)
                        )
                        Text(
                            text = "暂无收藏夹",
                            modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            itemsIndexed(folderList, key = { _, folder -> folder.id }) { index, folder ->
                val videoCard = VideoCard(
                    title = folder.name,
                    upName = "${folder.videoCount}个内容",
                    view = "",
                    cover = folder.cover,
                    type = "folder",
                    aid = folder.mediaId,
                    bvid = ""
                )
                
                RecommendVideoCard(
                    item = videoCard,
                    onClick = {
                        navController.navigate("favorite_detail/${folder.mediaId}/$mid")
                    },
                    transformation = SurfaceTransformation(transformationSpec),
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec)
                        .animateItem()
                )
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }

            if (folderList.isNotEmpty()) {
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}
