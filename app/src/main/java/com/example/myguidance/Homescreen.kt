package com.example.myguidance

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun HomeScreen(
    onScheduleClick: () -> Unit,
    onReportClick: () -> Unit,
    authViewModel: AuthViewModel,
    postViewModel: PostViewModel,
    onCreatePostClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingClick: () -> Unit,
    onNotificationsClick: () -> Unit = {},
) {

    val userName by authViewModel.userName.collectAsState()
    val userEmail by authViewModel.userEmail.collectAsState()
    val postsList by postViewModel.postsList.collectAsState()
    val isViewModelRefreshing by postViewModel.isRefreshing.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val userRole by authViewModel.userRole.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    HomeScreenContent(
        isRefreshing = isRefreshing || isViewModelRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                postViewModel.fetchPosts()
                authViewModel.fetchUserProfile()
                delay(1000.milliseconds)
                isRefreshing = false
            }
        },

        userName = userName,
        userEmail = userEmail,
        postsList = postsList,
        currentUserId = currentUserId,
        onScheduleClick = onScheduleClick,
        onLikeClick = { postId -> postViewModel.toggleLike(postId) },
        onCreatePostClick = onCreatePostClick,
        onProfileClick = onProfileClick,
        onNotificationsClick = onNotificationsClick,
        onSettingClick = onSettingClick,
        userRole = userRole,
        onReportClick = onReportClick,
    )
}

@Composable
fun HomeScreenContent(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    userRole: UserRole,
    userName: String,
    userEmail: String,
    postsList: List<Post> = emptyList(),
    currentUserId: String = "",
    onLikeClick: (String) -> Unit = {},
    onScheduleClick: () -> Unit,
    onCreatePostClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingClick: () -> Unit,
    onNotificationsClick: () -> Unit = {},
    onReportClick: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var isFabExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(1) }
    var searchText by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    val searchedPosts = remember(postsList, searchText) {
        if (searchText.isBlank()) {
            postsList
        } else {
            postsList.filter {
                it.textContent.contains(searchText.trim(), ignoreCase = true) ||
                        it.name.contains(searchText.trim(), ignoreCase = true)
            }
        }
    }

    if (userRole != UserRole.UNKNOWN && userRole.canCreatePost) {
        Scaffold(
            floatingActionButton = {
                if (userRole.canCreatePost) {
                    ExpandableFloatingActionButton(
                        items = listOf(
                            FabSubItem(
                                label = "Create Post",
                                icon = Icons.Default.Edit,
                                onClick = onCreatePostClick,
                            )
                        ),
                        isExpanded = isFabExpanded,
                        onExpandedChange = { isFabExpanded = it },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            },


            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {

                    NavigationBarItem(
                        selected = false,
                        onClick = onScheduleClick,
                        icon = {
                            Icon(
                                Icons.Outlined.CalendarMonth,
                                contentDescription = "Schedule"
                            )
                        }
                    )

                    NavigationBarItem(
                        selected = true,
                        onClick = { },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") }
                    )
                    if (userRole.isCounselorOrAdmin) {
                        NavigationBarItem(
                            selected = false,
                            onClick = onReportClick,
                            icon = {
                                Icon(
                                    Icons.Outlined.Report,
                                    contentDescription = "Reports"
                                )
                            }
                        )
                    }
                    NavigationBarItem(
                        selected = false,
                        onClick = onSettingClick,
                        icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") }
                    )
                }
            }
        )


        { innerPadding ->
            val scrimAlpha by animateFloatAsState(
                targetValue = if (isFabExpanded) 0.6f else 0f,
                animationSpec = tween(durationMillis = 300),
                label = "ScrimAnimation"
            )

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // Profile Avatar with Click Listener
                        Box(
                            modifier = Modifier
                                .size(55.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    onProfileClick()
                                    android.util.Log.d("PROFILE_UI", "User avatar clicked")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "User Avatar",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        TextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            placeholder = {
                                Text(
                                    "Search",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },

                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Search

                            ),

                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    scope.launch {
                                        keyboardController?.hide()
                                    }
                                }
                            ),

                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        FloatingActionButton(
                            onClick = onNotificationsClick,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                    }
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = onRefresh,
                        modifier = Modifier.weight(1f)
                    ) {

                        val listAlpha by animateFloatAsState(
                            targetValue = if (isRefreshing) 0.3f else 1.0f,
                            animationSpec = tween(durationMillis = 400),
                            label = "ListFadeAnimation"
                        )
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                                .graphicsLayer { alpha = listAlpha },
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 12.dp
                            )
                        ) {
                            // Top Tab Navigation
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    val tabs = listOf("Notice", "For you", "Favorites")
                                    tabs.forEachIndexed { index, title ->
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            TextButton(onClick = { selectedTab = index }) {
                                                Text(
                                                    text = title,
                                                    fontSize = 16.sp,
                                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (selectedTab == index) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (selectedTab == index) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(28.dp)
                                                        .height(3.dp)
                                                        .background(
                                                            MaterialTheme.colorScheme.primary,
                                                            shape = RoundedCornerShape(2.dp)
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }


                            when (selectedTab) {
                                0 -> { // Announcements Tab
                                    val notice = searchedPosts.filter {
                                        it.category.equals("Notice", ignoreCase = true)
                                    }
                                    if (notice.isEmpty()) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 40.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "No Notice yet.",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    } else {
                                        items(items = notice, key = { it.postId }) { post ->
                                            val isLiked = post.likedBy.contains(currentUserId)
                                            PostItemView(
                                                post = post,
                                                isLiked = isLiked,
                                                onLikeClick = { onLikeClick(post.postId) }
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }
                                    }
                                }

                                1 -> { // For You Tab
                                    if (searchedPosts.isEmpty()) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 40.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "No general posts yet.",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    } else {
                                        items(
                                            items = searchedPosts,
                                            key = { it.postId }) { post ->
                                            val isLiked = post.likedBy.contains(currentUserId)
                                            PostItemView(
                                                post = post,
                                                isLiked = isLiked,
                                                onLikeClick = { onLikeClick(post.postId) }
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }
                                    }
                                }

                                2 -> { // Favorites Tab
                                    val favorites = searchedPosts.filter {
                                        it.likedBy.contains(currentUserId)
                                    }
                                    if (favorites.isEmpty()) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 40.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "No favorites yet.",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    } else {
                                        items(items = favorites, key = { it.postId }) { post ->
                                            PostItemView(
                                                post = post,
                                                isLiked = true,
                                                onLikeClick = { onLikeClick(post.postId) }
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (scrimAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = scrimAlpha }
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { isFabExpanded = false }
                )
            }
        }
    }
}
@Composable
fun PostItemView(
    post: Post,
    isLiked: Boolean,
    onLikeClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Author Info Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.name.ifBlank { "Anonymous" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Posted on: ${formatTimestamp(post.timestamp)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = "More")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Text Content
            if (post.textContent.isNotBlank()) {
                Text(
                    text = post.textContent,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Attached Image Preview
            post.mediaUrl?.let { url ->
                if (url.isNotBlank()) {
                    AsyncImage(
                        model = optimizedUrl(url),
                        contentDescription = "Post Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }


            // Like & Comment Action Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLikeClick, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${post.likesCount} likes",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
}






fun optimizedUrl(url: String): String =
    if ("/upload/" in url) url.replace("/upload/", "/upload/w_800,q_auto,f_auto/") else url