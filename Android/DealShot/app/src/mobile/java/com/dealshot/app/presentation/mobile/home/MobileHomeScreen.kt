package com.dealshot.app.presentation.mobile.home

import android.util.Log
import android.widget.Toast
import android.view.LayoutInflater
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dealshot.app.data.remote.ApiClient
import com.dealshot.app.data.remote.repository.SearchRepositoryImpl
import com.dealshot.app.presentation.mobile.search.ExpandableSearchBar
import com.dealshot.app.presentation.mobile.search.SearchEffect
import com.dealshot.app.presentation.mobile.search.SearchIntent
import com.dealshot.app.presentation.mobile.search.SearchScreen
import com.dealshot.app.presentation.mobile.search.SearchViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.dealshot.app.R
import com.dealshot.app.data.preferences.UserPreferences
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.presentation.shared.home.HomeIntent
import com.dealshot.app.presentation.shared.home.HomeState
import com.dealshot.app.ui.theme.GoldPrimary
import com.dealshot.app.ui.theme.GoldTextLight
import com.dealshot.app.ui.theme.HintText
import com.dealshot.app.ui.theme.TextMuted
import com.dealshot.app.ui.theme.orange
import kotlinx.coroutines.delay
import org.junit.Test

@Composable
fun HomeScreen(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    onAddProfile: () -> Unit = {}
) {


    val context = LocalContext.current
    val sections = state.sections
    // 1. Single shared ExoPlayer instance initialized once for the whole screen
    val sharedExoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            volume = 0f
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    val featuredExoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            volume = 0f
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }
    var showProfileDialog by remember {
        mutableStateOf(false)
    }

    // Tracks the globally playing video across all rows and sections
    var activeVideoUrl by remember { mutableStateOf<String?>(null) }

    // 2. Lifecycle cleanup: Releases hardware codecs when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            sharedExoPlayer.release()
            featuredExoPlayer.release()
        }
    }
    val userPreferences = remember { UserPreferences(context.applicationContext) }
    val mobileNumber by userPreferences.mobileNumber.collectAsStateWithLifecycle(initialValue = "")
    val userName by userPreferences.userName.collectAsStateWithLifecycle(initialValue = "")

    if (showProfileDialog) {
        ProfileDialog(
            userName = userName.orEmpty(),
            mobileNumber = mobileNumber.orEmpty(),
            onDismiss = {
                showProfileDialog = false
            },
            onLogout = {
                showProfileDialog = false

                onIntent(
                    HomeIntent.LogoutClicked
                )
            },
            onAddProfile = {
                showProfileDialog = false
                onAddProfile()
            }
        )
    }
    val allVideos = state.allVideos

    val searchViewModel: SearchViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SearchViewModel(
                    searchRepository = SearchRepositoryImpl(ApiClient.videoApi)
                ) as T
            }
        }
    )
    val searchState by searchViewModel.uiState.collectAsStateWithLifecycle()
    var isSearchActive by remember { mutableStateOf(false) }

    LaunchedEffect(searchViewModel) {
        searchViewModel.effect.collect { effect ->
            when (effect) {
                is SearchEffect.OpenVideoDetails -> {
                    onIntent(HomeIntent.VideoClicked(effect.video))
                }
                SearchEffect.DismissKeyboard -> {}
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isSearchActive) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Deal",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Shot",
                        color = orange,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = {
                        onIntent(HomeIntent.LikedVideosClicked)
                    }) {
                    Icon(
                        painter = painterResource(id = R.drawable.heart),
                        contentDescription = "Liked Videos",
                        tint = orange,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(
                    onClick = {
                        onIntent(HomeIntent.MyListClicked)
                    }) {
                    Icon(
                        painter = painterResource(id = R.drawable.bookmark),
                        contentDescription = "My List",
                        tint = orange,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            ExpandableSearchBar(
                isExpanded = isSearchActive,
                query = searchState.query,
                onQueryChange = { searchViewModel.onIntent(SearchIntent.QueryChanged(it)) },
                onSearchSubmit = { searchViewModel.onIntent(SearchIntent.SearchSubmitted(it)) },
                onClearQuery = { searchViewModel.onIntent(SearchIntent.ClearQuery) },
                onOpenSearch = { isSearchActive = true },
                onCloseSearch = {
                    isSearchActive = false
                    searchViewModel.onIntent(SearchIntent.ClearQuery)
                },
                modifier = if (isSearchActive) Modifier.weight(1f) else Modifier
            )

            if (!isSearchActive) {
                IconButton(
                    onClick = {
                        onIntent(HomeIntent.ProfileClicked)
                    }) {
                    Icon(
                        painter = painterResource(id = R.drawable.person_crop_circle),
                        contentDescription = "Profile",
                        tint = orange,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        if (isSearchActive) {
            SearchScreen(
                state = searchState,
                onIntent = searchViewModel::onIntent,
                onBack = {
                    isSearchActive = false
                    searchViewModel.onIntent(SearchIntent.ClearQuery)
                },
                modifier = Modifier.weight(1f)
            )
        } else {


        // Category Header
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {

            items(sections) { section ->

                val isSelected = state.selectedSection.equals(
                    section, ignoreCase = true
                )

                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .clickable(
                            indication = null, interactionSource = remember {
                                MutableInteractionSource()
                            }) {
                            onIntent(
                                HomeIntent.SectionClicked(section)
                            )
                        }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = section,
                        color = if (isSelected) {
                            GoldTextLight
                        } else {
                            TextMuted
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Medium
                        }
                    )

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .height(3.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(GoldPrimary)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .height(3.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Video Categories
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

        } else if (state.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.error, color = Color.White
                )
            }
        } else {
            val availableCategories =
                state.categories.map { it.name.trim() }.filter { it.isNotBlank() }.distinct()
            when {
                state.selectedSection.equals("Home", ignoreCase = true) -> {
                    HomeContent(
                        bannerVideos = state.bannerVideos,
                        allVideos = allVideos,
                        continueWatching = state.continueWatching,
                        hasPremiumAccess = com.dealshot.app.data.subscription.hasPremiumAccess(state.subscriptionState),
                        availableCategories = availableCategories,
                        sharedExoPlayer = sharedExoPlayer,
                        featuredExoPlayer = featuredExoPlayer,
                        activeVideoUrl = activeVideoUrl,
                        myWatchLaterVideoIds = state.myWatchLaterVideoIds,
                        onActiveVideoUrlChanged = {
                            activeVideoUrl = it
                        },
                        onIntent = onIntent
                    )
                }

                state.selectedSection.equals("Premium", ignoreCase = true) -> {

                    val premiumVideos = allVideos.filter {
                        it.isPremium && !it.videoUrl.isNullOrBlank()
                    }

                    PremiumContent(
                        premiumVideos = premiumVideos,
                        sharedExoPlayer = sharedExoPlayer,
                        featuredExoPlayer = featuredExoPlayer,
                        activeVideoUrl = activeVideoUrl,
                        myWatchLaterVideoIds = state.myWatchLaterVideoIds,
                        onActiveVideoUrlChanged = {
                            activeVideoUrl = it
                        },
                        onIntent = onIntent
                    )
                }

                else -> {

                    val selectedCategory = state.selectedSection

                    val categoryVideos = allVideos.filter { video ->
                        video.category?.trim()?.equals(
                                selectedCategory, ignoreCase = true
                            ) == true
                    }

                    CategoryContent(
                        selectedCategory = selectedCategory,
                        categoryVideos = categoryVideos,
                        sharedExoPlayer = sharedExoPlayer,
                        featuredExoPlayer = featuredExoPlayer,
                        activeVideoUrl = activeVideoUrl,
                        myWatchLaterVideoIds = state.myWatchLaterVideoIds,
                        onActiveVideoUrlChanged = {
                            activeVideoUrl = it
                        },
                        onIntent = onIntent
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun PremiumContent(
    premiumVideos: List<VideoDto>,
    sharedExoPlayer: ExoPlayer,
    featuredExoPlayer: ExoPlayer,
    activeVideoUrl: String?,
    myWatchLaterVideoIds: Set<String> = emptySet(),
    onActiveVideoUrlChanged: (String?) -> Unit,
    onIntent: (HomeIntent) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            if (premiumVideos.isNotEmpty()) {
                FeaturedVideoSection(
                    videos = premiumVideos,
                    exoPlayer = featuredExoPlayer,
                    myWatchLaterVideoIds = myWatchLaterVideoIds,
                    onWatchNow = { video ->
                        onIntent(HomeIntent.VideoClicked(video))
                    },
                    onMyList = { video ->
                        val isSaved = video.id in myWatchLaterVideoIds
                        if (!isSaved) {
                            val title = video.title?.ifBlank { "Video" } ?: "Video"
                            Toast.makeText(context, "$title is added to the list", Toast.LENGTH_SHORT).show()
                        }
                        onIntent(HomeIntent.ToggleMyList(video))
                    })
            }
        }

        item {
            if (premiumVideos.isNotEmpty()) {
                VideoCategorySection(
                    title = "Premium",
                    videos = premiumVideos,
                    sharedExoPlayer = sharedExoPlayer,
                    activeVideoUrl = activeVideoUrl,
                    onPlayVideo = { video ->
                        playVideo(
                            video = video,
                            activeVideoUrl = activeVideoUrl,
                            onActiveUrlChanged = onActiveVideoUrlChanged,
                            player = sharedExoPlayer
                        )
                    },
                    onViewAll = { categoryName ->
                        onIntent(HomeIntent.SeeAllClicked(categoryName))
                    },
                    onVideoClick = { video ->
                        onIntent(HomeIntent.VideoClicked(video))
                    })
            }
        }
    }
}


private fun playVideo(
    video: VideoDto,
    activeVideoUrl: String?,
    onActiveUrlChanged: (String?) -> Unit,
    player: ExoPlayer
) {
    val url = video.videoUrl?.trim()

    if (url.isNullOrBlank()) {
        return
    }

    if (activeVideoUrl == url) {
        return
    }

    onActiveUrlChanged(url)

    player.stop()
    player.clearMediaItems()

    player.setMediaItem(
        MediaItem.fromUri(url)
    )

    player.prepare()
    player.playWhenReady = true
}

@Composable
private fun CategoryContent(
    selectedCategory: String,
    categoryVideos: List<VideoDto>,
    sharedExoPlayer: ExoPlayer,
    featuredExoPlayer: ExoPlayer,
    activeVideoUrl: String?,
    myWatchLaterVideoIds: Set<String> = emptySet(),
    onActiveVideoUrlChanged: (String?) -> Unit,
    onIntent: (HomeIntent) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        item {
            if (categoryVideos.isNotEmpty()) {

                FeaturedVideoSection(
                    videos = categoryVideos,
                    exoPlayer = featuredExoPlayer,
                    myWatchLaterVideoIds = myWatchLaterVideoIds,

                    onWatchNow = { video ->
                        onIntent(
                            HomeIntent.VideoClicked(video)
                        )
                    },

                    onMyList = { video ->
                        val isSaved = video.id in myWatchLaterVideoIds
                        if (!isSaved) {
                            val title = video.title?.ifBlank { "Video" } ?: "Video"
                            Toast.makeText(context, "$title is added to the list", Toast.LENGTH_SHORT).show()
                        }
                        onIntent(
                            HomeIntent.ToggleMyList(video)
                        )
                    })
            }
        }

        item {
            if (categoryVideos.isNotEmpty()) {
                VideoCategorySection(
                    title = selectedCategory,
                    videos = categoryVideos,
                    sharedExoPlayer = sharedExoPlayer,
                    activeVideoUrl = activeVideoUrl,

                    onPlayVideo = { video ->
                        playVideo(
                            video = video,
                            activeVideoUrl = activeVideoUrl,
                            onActiveUrlChanged = onActiveVideoUrlChanged,
                            player = sharedExoPlayer
                        )
                    },

                    onViewAll = { categoryName ->
                        onIntent(
                            HomeIntent.SeeAllClicked(categoryName)
                        )
                    },

                    onVideoClick = { video ->
                        onIntent(
                            HomeIntent.VideoClicked(video)
                        )
                    })
            }
        }
    }
}

@Composable
fun VideoCategorySection(
    title: String,
    videos: List<VideoDto>,
    sharedExoPlayer: ExoPlayer,
    activeVideoUrl: String?,
    onPlayVideo: (VideoDto) -> Unit,
    onVideoClick: (VideoDto) -> Unit,
    onViewAll: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                    onViewAll(title)
                }) {
                Text(
                    text = "view all",
                    color = HintText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    painter = painterResource(id = R.drawable.chevron_right),
                    contentDescription = "view all",
                    tint = orange,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = videos, key = { video -> video.id }) { video ->
                val isPlaying = video.videoUrl != null && activeVideoUrl == video.videoUrl

                VideoCard(
                    video = video,
                    isPlaying = isPlaying,
                    exoPlayer = sharedExoPlayer,
                    onActive = { onPlayVideo(video) },
                    onClick = { onVideoClick(video) })
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoCard(
    video: VideoDto,
    isPlaying: Boolean,
    exoPlayer: ExoPlayer,
    onActive: () -> Unit,
    onClick: () -> Unit
) {
    // Tell the parent system to switch to this video file when selected

    Column(
        modifier = Modifier
            .width(190.dp)
            .clickable(onClick = onClick)
    ) {
        if (isPlaying) {
            AndroidView(
                factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    player = exoPlayer
                }
            },
                update = { playerView ->
                    if (playerView.player != exoPlayer) {
                        playerView.player = exoPlayer
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(14.dp)),
                onRelease = {
                    it.player = null
                })
        } else {
            // Lightweight placeholder surface prevents OOM exceptions
            AsyncImage(
                model = video.thumbnailUrl ?: video.thumbnail,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(14.dp))
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        video.title?.let {
            Text(
                text = it,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (!video.duration.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = video.duration,
                color = Color(0xFF9E9E9E),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/*@Composable
fun ProfileDialog(
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val userPreferences = UserPreferences(context)
    val mobileNumber: String? by userPreferences.mobileNumber
        .collectAsStateWithLifecycle(initialValue = null)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    onDismiss()
                }
                .padding(
                    top = 70.dp,
                    end = 16.dp
                ),
            contentAlignment = Alignment.TopEnd
        ) {

            Column(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(32.dp)
                    )
                    .background(
                        Color(0xFF1E1E1E)
                    )
                    .clickable {
                        // Consume click so it does not dismiss
                    }
                    .width(
                        250.dp
                    )
                    .padding(
                        horizontal = 24.dp,
                        vertical = 22.dp
                    )
            ) {
                Text(
                    text = mobileNumber ?: "guest",
                    color = Color(0xFFBDBDBD),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(
                    modifier = Modifier.height(22.dp)
                )
//                val isGuest = mobileNumber.equals("guest",false)
                val isGuest = mobileNumber.isNullOrBlank()
                Text(
                    text = if (isGuest) "Log In" else "Log out",
                    color = if (isGuest) {
                        Color(0xFF1E6705)
                    } else {
                        Color(0xFFFF5252)
                    },
                    fontSize = 20.sp,
                    modifier = Modifier
                        .clickable {
                            showLogoutConfirmation = true
                        }
                )
            }
        }
    }
}*/
@Preview(showBackground = true, name = "Profile Dialog Light Mode")
@Composable
fun ProfileDialogPreview() {
    // Wrapped in a Box to simulate the screen background under the dialog
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        ProfileDialog(
            userName = "Alex Mercer",
            mobileNumber = "+1 (555) 019-2834",
            onDismiss = {},
            onLogout = {},
            onAddProfile = {}
        )
    }
}
@Composable
fun ProfileDialog(
    userName: String,
    mobileNumber: String,
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
    onAddProfile: () -> Unit = {},
    email: String = "",
    gender: String = "",
    language: String = "",
    subscriptionLabel: String = "Free",
    profiles: List<com.dealshot.app.presentation.mobile.profile.ProfileListItem> = emptyList(),
    selectedProfileId: String? = null,
    onProfileSelected: (String) -> Unit = {},
    onRemoveProfile: (String) -> Unit = {},
    onViewSubscription: () -> Unit = {},
    onUnlockPremium: () -> Unit = {},
    isLoggedIn: Boolean = mobileNumber.isNotBlank() || (userName.isNotBlank() && userName != "Guest")
) {
    androidx.activity.compose.BackHandler {
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() }, contentAlignment = Alignment.Center
        ) {
            // Animated golden background on profile popup
            com.dealshot.app.ui.AnimatedGoldenBackground(
                modifier = Modifier.matchParentSize()
            )

            // Dim overlay for contrast and click dismissal
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.65f))
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { /* Consume click */ }
                    .padding(20.dp)) {
                // Header Block
                Text(
                    text = "ACCOUNT",
                    color = com.dealshot.app.ui.theme.GoldPrimary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "My Profile",
                    color = com.dealshot.app.ui.theme.GoldTextLight,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Manage your profile and account details",
                    color = com.dealshot.app.ui.theme.TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Profile Card Content
                com.dealshot.app.presentation.mobile.profile.ProfilePopupCard(
                    userName = userName,
                    mobileNumber = mobileNumber,
                    email = email,
                    gender = gender,
                    language = language,
                    subscriptionLabel = subscriptionLabel,
                    profiles = profiles,
                    selectedProfileId = selectedProfileId,
                    onProfileSelected = onProfileSelected,
                    onRemoveProfile = onRemoveProfile,
                    onViewSubscription = onViewSubscription,
                    onUnlockPremium = onUnlockPremium,
                    onLogout = {
                        onDismiss()
                        onLogout()
                    },
                    onAddProfile = {
                        onDismiss()
                        onAddProfile()
                    },
                    isLoggedIn = isLoggedIn
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    bannerVideos: List<VideoDto>,
    allVideos: List<VideoDto>,
    continueWatching: List<VideoDto> = emptyList(),
    hasPremiumAccess: Boolean = false,
    availableCategories: List<String>,
    sharedExoPlayer: ExoPlayer,
    featuredExoPlayer: ExoPlayer,
    activeVideoUrl: String?,
    myWatchLaterVideoIds: Set<String> = emptySet(),
    onActiveVideoUrlChanged: (String?) -> Unit,
    onIntent: (HomeIntent) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        // Featured Banner
        item {
            if (bannerVideos.isNotEmpty()) {
                FeaturedVideoSection(
                    videos = bannerVideos,
                    exoPlayer = featuredExoPlayer,
                    myWatchLaterVideoIds = myWatchLaterVideoIds,

                    onWatchNow = { video ->
                        onIntent(
                            HomeIntent.VideoClicked(video)
                        )
                    },

                    onMyList = { video ->
                        val isSaved = video.id in myWatchLaterVideoIds
                        if (!isSaved) {
                            val title = video.title?.ifBlank { "Video" } ?: "Video"
                            Toast.makeText(context, "$title is added to the list", Toast.LENGTH_SHORT).show()
                        }
                        onIntent(
                            HomeIntent.ToggleMyList(video)
                        )
                    })
            }
        }

        // Continue Watching Section (ONLY shown for non-premium users when non-empty)
        if (!hasPremiumAccess && continueWatching.isNotEmpty()) {
            item {
                VideoCategorySection(
                    title = "Continue Watching",
                    videos = continueWatching,
                    sharedExoPlayer = sharedExoPlayer,
                    activeVideoUrl = activeVideoUrl,
                    onPlayVideo = { video ->
                        playVideo(
                            video = video,
                            activeVideoUrl = activeVideoUrl,
                            onActiveUrlChanged = onActiveVideoUrlChanged,
                            player = sharedExoPlayer
                        )
                    },
                    onViewAll = { },
                    onVideoClick = { video ->
                        onIntent(HomeIntent.VideoClicked(video))
                    }
                )
            }
        }

        // Dynamic Categories
        items(
            items = availableCategories
        ) { category ->
            val categoryVideos = allVideos.filter { video ->
                video.category?.trim()?.equals(
                        category, ignoreCase = true
                    ) == true
            }
//            var categoryVideos = categories

            if (categoryVideos.isNotEmpty()) {

                VideoCategorySection(
                    title = category,
                    videos = categoryVideos,
                    sharedExoPlayer = sharedExoPlayer,
                    activeVideoUrl = activeVideoUrl,

                    onPlayVideo = { video ->
                        playVideo(
                            video = video,
                            activeVideoUrl = activeVideoUrl,
                            onActiveUrlChanged = onActiveVideoUrlChanged,
                            player = sharedExoPlayer
                        )
                    },
                    onViewAll = { categoryName ->
                        onIntent(
                            HomeIntent.SeeAllClicked(categoryName)
                        )
                    },
                    onVideoClick = { video ->
                        onIntent(
                            HomeIntent.VideoClicked(video)
                        )
                    })
            }
        }
    }
}

private const val BANNER_VIDEO_DELAY_MS = 5_000L

@OptIn(UnstableApi::class)
@Composable
private fun FeaturedVideoSection(
    videos: List<VideoDto>,
    exoPlayer: ExoPlayer,
    myWatchLaterVideoIds: Set<String> = emptySet(),
    onWatchNow: (VideoDto) -> Unit,
    onMyList: (VideoDto) -> Unit
) {
    if (videos.isEmpty()) return

    var isBuffering by remember {
        mutableStateOf(false)
    }

    var hasPlayerError by remember {
        mutableStateOf(false)
    }

    var isVideoReady by remember {
        mutableStateOf(false)
    }

    val pagerState = rememberPagerState(
        initialPage = 0, pageCount = { Int.MAX_VALUE })

    /*
     * IMPORTANT:
     * Use settledPage for playback.
     */
    val currentIndex = pagerState.settledPage % videos.size

    val currentVideo = videos[currentIndex]

    /*
     * --------------------------------
     * PLAYER LISTENER
     * --------------------------------
     */

    DisposableEffect(exoPlayer) {

        val listener = object : Player.Listener {

            override fun onPlaybackStateChanged(
                playbackState: Int
            ) {

                isBuffering = playbackState == Player.STATE_BUFFERING

            }

            override fun onRenderedFirstFrame() {

                isVideoReady = true
                isBuffering = false
            }

            override fun onIsPlayingChanged(
                isPlaying: Boolean
            ) {
            }

            override fun onPlayerError(
                error: PlaybackException
            ) {

                Log.e(
                    "FeaturedVideo", "Banner player error", error
                )

                hasPlayerError = true
                isBuffering = false
                isVideoReady = false
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    /*
     * --------------------------------
     * CHANGE BANNER
     * --------------------------------
     *
     * IMPORTANT:
     *
     */

    LaunchedEffect(pagerState.settledPage) {
        val currentIndex = pagerState.settledPage % videos.size
        val currentVideo = videos[currentIndex]

        // Immediately hide previous video frame
        isVideoReady = false
        hasPlayerError = false
        isBuffering = false

        exoPlayer.stop()
        exoPlayer.clearMediaItems()

        delay(BANNER_VIDEO_DELAY_MS)

        val videoUrl = currentVideo.promoVideoUrl

        if (!videoUrl.isNullOrBlank()) {
            try {
                exoPlayer.setMediaItem(
                    MediaItem.fromUri(videoUrl)
                )

                exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
                exoPlayer.volume = 0f
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            } catch (e: Exception) {
                hasPlayerError = true
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {

            // 1. THUMBNAIL / PAGER
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentPadding = PaddingValues(0.dp),
                pageSpacing = 0.dp,
                pageSize = PageSize.Fill
            ) { page ->
                val video = videos[page % videos.size]

                AsyncImage(
                    model = video.thumbnailUrl ?: video.thumbnail,
                    contentDescription = video.title,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 2. VIDEO
            if (!hasPlayerError) {
                AndroidView(
                    factory = { context ->
                    LayoutInflater.from(context).inflate(
                            R.layout.view_banner_player, null
                        ) as PlayerView
                }, update = { playerView ->
                    playerView.player = exoPlayer
                }, onRelease = { playerView ->
                    playerView.player = null
                }, modifier = Modifier
                        .fillMaxSize()
                        .alpha(
                            if (isVideoReady) 1f else 0f
                        )
                )
            }

            // 3. GRADIENT
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent, Color(0x99000000), Color(0xFF000000)
                            )
                        )
                    )
            )

            // TOP LEFT LABEL
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 16.dp, start = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DEALSHOT ORIGINAL",
                    color = Color(0xFFE5C058),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = R.drawable.sparkles),
                    contentDescription = null,
                    tint = Color(0xFFE5C058),
                    modifier = Modifier.size(14.dp)
                )
            }

            // 4. PAGER INDICATOR DOTS
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val actualPage = pagerState.currentPage % videos.size
                for (i in videos.indices) {
                    val isSelected = i == actualPage
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (isSelected) 18.dp else 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isSelected) GoldPrimary else Color.White.copy(alpha = 0.5f))
                    )
                }
            }


            // 5. TEXT + BUTTONS
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                currentVideo.title?.let { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFFFFF9C4), Color(0xFFD4AF37))
                            )
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Description
                currentVideo.description?.takeIf { it.isNotBlank() }?.let { desc ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        color = Color(0xFFE0E0E0),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Meta row: 2026 • 1 min • Premium • [HD]
                val metaItems = listOfNotNull(
                    currentVideo.createdAt?.take(4),
                    currentVideo.duration?.takeIf { it.isNotBlank() },
                    if (currentVideo.isPremium) "Premium" else null
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    metaItems.forEachIndexed { index, item ->
                        if (index > 0) {
                            Text(
                                text = " • ",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                        Text(
                            text = item,
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = " • ",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    // HD badge
                    Box(
                        modifier = Modifier
                            .border(1.dp, Color.White, RoundedCornerShape(3.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "HD",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Watch Now Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFEAE2B7), Color(0xFFD4AF37)
                                    )
                                )
                            )
                            .clickable { onWatchNow(currentVideo) }
                            .padding(horizontal = 16.dp, vertical = 6.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.play_fill),
                            contentDescription = "Watch Now",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Watch Now",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    // My List Circular Button
                    val isInMyWatchLater = currentVideo.id in myWatchLaterVideoIds

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.5f),
                                RoundedCornerShape(percent = 50)
                            )
                            .clickable { onMyList(currentVideo) }) {
                        Icon(
                            painter = painterResource(id = if (isInMyWatchLater) R.drawable.checkmark else R.drawable.plus),
                            contentDescription = if (isInMyWatchLater) "In My List" else "My List",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun ProfileDialog(
        onDismiss: () -> Unit, onLogout: () -> Unit
    ) {
        val context = LocalContext.current
        val userPreferences = UserPreferences(context)
        val mobileNumber: String? by userPreferences.mobileNumber.collectAsStateWithLifecycle(
                initialValue = null
            )

        var showLogoutConfirmation by remember { mutableStateOf(false) }
        val isGuest = mobileNumber.isNullOrBlank()

        // Confirmation dialog
        if (showLogoutConfirmation) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirmation = false },
                containerColor = Color(0xFF1E1E1E),
                title = {
                    Text(
                        text = "Log out", color = Color.White, fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to log out?", color = Color(0xFFBDBDBD)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutConfirmation = false
                            onLogout()
                        }) {
                        Text(text = "Yes", color = Color(0xFFFF5252))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showLogoutConfirmation = false }) {
                        Text(text = "No", color = Color.White)
                    }
                })
        }

        Dialog(
            onDismissRequest = onDismiss, properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() }
                .padding(top = 70.dp, end = 16.dp), contentAlignment = Alignment.TopEnd) {

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1E1E1E))
                        .clickable { /* Consume click so it does not dismiss */ }
                        .width(250.dp)
                        .padding(16.dp)) {
                    Text(
                        text = mobileNumber ?: "guest",
                        color = Color(0xFFBDBDBD),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isGuest) "Log In" else "Log out",
                        color = if (isGuest) Color(0xFF1E6705) else Color(0xFFFF5252),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.clickable {
                            if (isGuest) {
                                // Guests go straight to login, no confirmation needed
                                onLogout()
                            } else {
                                showLogoutConfirmation = true
                            }
                        })
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun HomeScreenPreview() {
    val dummyVideos = listOf(
        VideoDto(
            id = "1",
            title = "Big Buck Bunny",
            description = "A short computer-animated comedy film.",
            duration = "10:00",
            category = "Comedy",
            isPremium = true,
            thumbnailUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Big_buck_bunny_poster_big.jpg/800px-Big_buck_bunny_poster_big.jpg",
            videoUrl = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        ), VideoDto(
            id = "2",
            title = "Elephant Dream",
            description = "The first computer-generated animated short film produced entirely with free software.",
            duration = "10:53",
            category = "Action",
            thumbnailUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/Elephants_Dream_s5_both.jpg/800px-Elephants_Dream_s5_both.jpg",
            videoUrl = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        )
    )

    val dummyState = HomeState(
        isLoading = false,
        allVideos = dummyVideos,
        bannerVideos = dummyVideos,
        sections = listOf("Home", "Premium", "Action", "Comedy"),
        selectedSection = "Home",
        categories = listOf(
            com.dealshot.app.presentation.shared.home.VideoCategory("Action", 1),
            com.dealshot.app.presentation.shared.home.VideoCategory("Comedy", 1)
        )
    )

    HomeScreen(
        state = dummyState, onIntent = {})
}
