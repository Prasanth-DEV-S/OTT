package com.dealshot.app

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dealshot.app.data.preferences.UserPreferences
import com.dealshot.app.data.remote.ApiClient
import com.dealshot.app.data.remote.ApiClient.watchProgressApiService
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.remote.repository.AuthRepositoryImpl
import com.dealshot.app.data.remote.repository.SavedVideoCollection
import com.dealshot.app.data.remote.repository.SavedVideosRepositoryImpl
import com.dealshot.app.data.remote.repository.VideoRepositoryImpl
import com.dealshot.app.data.remote.repository.WatchProgressRepositoryImpl
import com.dealshot.app.data.video.Video
import com.dealshot.app.presentation.shared.auth.SessionViewModelFactory
import com.dealshot.app.presentation.shared.myList.SavedVideosViewModel
import com.dealshot.app.presentation.shared.player.VideoPlayerViewModelFactory
import com.dealshot.app.presentation.tv.category.TvCategoryRoute
import com.dealshot.app.presentation.tv.details.TvDetailsRoute
import com.dealshot.app.presentation.tv.home.TvHomeRoute
import com.dealshot.app.presentation.tv.home.TvSessionRoute
import com.dealshot.app.presentation.tv.myList.TvVideoCollectionRoute
import com.dealshot.app.presentation.tv.login.TvLoginRoute
import com.dealshot.app.presentation.tv.otp.TvOtpRoute
import com.dealshot.app.presentation.tv.player.TvPlayerRoute
import com.dealshot.app.presentation.tv.profile.TvProfileRoute
import com.dealshot.app.presentation.tv.subscription.TvSubscriptionActivity
import com.dealshot.app.ui.theme.StreamTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private object TvRoutes {

    const val START = "start"
    const val LOGIN = "login?redirect={redirect}"
    fun login(redirect: String? = null): String {
        return if (redirect != null) "login?redirect=$redirect" else "login"
    }
    const val OTP = "otp/{phoneNumber}/{mobileHint}?redirect={redirect}"
    const val HOME = "home/{phoneNumber}"
    const val PROFILE = "profile"
    const val LIKED_VIDEOS = "liked_videos"
    const val MY_LIST = "my_list"
    const val CATEGORY = "category/{category}"
    const val DETAILS = "details/{videoId}"
    const val PLAYER = "player/{videoId}"

    fun otp(phoneNumber: String, mobileHint: String, redirect: String? = null): String =
        if (redirect != null) "otp/${Uri.encode(phoneNumber)}/${Uri.encode(mobileHint)}?redirect=$redirect" else "otp/${Uri.encode(phoneNumber)}/${Uri.encode(mobileHint)}"

    fun home(phoneNumber: String): String = "home/${Uri.encode(phoneNumber)}"

    fun category(category: String): String = "category/${Uri.encode(category)}"

    fun details(videoId: String): String = "details/${Uri.encode(videoId)}"

    fun player(videoId: String): String = "player/${Uri.encode(videoId)}"
}

private fun VideoDto.toVideo(): Video = Video(
    id = effectiveId,
    title = title.orEmpty(),
    description = description.orEmpty(),
    duration = duration.orEmpty(),
    thumbnail = effectiveThumbnail.orEmpty(),
    url = videoUrl.orEmpty(),
    category = category.orEmpty(),
    apiId = effectiveId,
    thumbnailUrl = effectiveThumbnail.orEmpty(),
    isPremium = isPremium,
    featured = featured
)

/**
 * Loads a single video by id using the same approach as mobile
 * (VideoRepositoryImpl.getVideos() then match on id, falling back to resolveSharedVideo).
 * Returns null if not found or on error.
 */
private suspend fun loadVideo(
    repository: VideoRepositoryImpl,
    videoId: String,
    tag: String
): Video? {
    return try {
        val dto = repository.getVideos().firstOrNull { it.id == videoId || it.altId == videoId }
        if (dto != null) {
            dto.toVideo()
        } else {
            repository.resolveSharedVideo(videoId).getOrNull()?.toVideoDto()?.toVideo()
        }
    } catch (e: Exception) {
        Log.e(tag, "Failed to load video: $videoId", e)
        null
    }
}

@OptIn(UnstableApi::class)
@Composable
fun StreamApp(initialDeepLinkId: String? = null) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val userPreferences = remember {
        UserPreferences(context.applicationContext)
    }

    val watchProgressRepository = remember {
        WatchProgressRepositoryImpl(api = watchProgressApiService)
    }
    
    val guestPreviewRepository = remember {
        com.dealshot.app.data.preferences.GuestPreviewRepositoryImpl(context)
    }

    val authRepository = remember {
        AuthRepositoryImpl(ApiClient.authApi)
    }

    val sessionFactory = remember {
        SessionViewModelFactory(
            authRepository = authRepository,
            hasSavedToken = {
                userPreferences.authToken.first().orEmpty().isNotBlank()
            },
            clearSession = {
                userPreferences.clearUser()
            }
        )
    }

    val navController = rememberNavController()

    fun openSubscription(title: String?, description: String?, url: String?) {
        context.startActivity(
            Intent(context, TvSubscriptionActivity::class.java).apply {
                putExtra("title", title)
                putExtra("description", description)
                putExtra("url", url)
            }
        )
    }

    StreamTheme {

        NavHost(
            navController = navController,
            startDestination = TvRoutes.START,
            modifier = Modifier.fillMaxSize()
        ) {

            // START (session check)
            composable(TvRoutes.START) {
                TvSessionRoute(
                    factory = sessionFactory,

                    onNavigateToLogin = {
                        navController.navigate(TvRoutes.LOGIN) {
                            popUpTo(TvRoutes.START) { inclusive = true }
                            launchSingleTop = true
                        }
                    },

                    onNavigateToHome = { phoneNumber ->
                        coroutineScope.launch {
                            userPreferences.updateLastActivity()
                        }

                        navController.navigate(TvRoutes.home(phoneNumber)) {
                            popUpTo(TvRoutes.START) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // LOGIN
            composable(
                route = TvRoutes.LOGIN,
                arguments = listOf(
                    navArgument("redirect") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val redirect = backStackEntry.arguments?.getString("redirect")
                TvLoginRoute(
                    onNavigateToOtp = { phoneNumber, mobileHint ->
                        navController.navigate(
                            TvRoutes.otp(
                                phoneNumber = phoneNumber,
                                mobileHint = mobileHint,
                                redirect = redirect
                            )
                        )
                    }
                )
            }

            // OTP
            composable(
                route = TvRoutes.OTP,
                arguments = listOf(
                    navArgument("phoneNumber") { type = NavType.StringType },
                    navArgument("mobileHint") { type = NavType.StringType },
                    navArgument("redirect") { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) { backStackEntry ->

                val phoneNumber =
                    backStackEntry.arguments?.getString("phoneNumber").orEmpty()

                val mobileHint =
                    backStackEntry.arguments?.getString("mobileHint").orEmpty()

                val redirect = backStackEntry.arguments?.getString("redirect")

                TvOtpRoute(
                    phoneNumber = phoneNumber,
                    mobileHint = mobileHint,

                    onNavigateToHome = {
                        val pendingState = guestPreviewRepository.getPendingResumeState()
                        val targetScreen = pendingState?.screen ?: redirect

                        if (targetScreen != null && (targetScreen.startsWith("player") || targetScreen.startsWith("details"))) {
                            guestPreviewRepository.clearPendingResumeState()
                            
                            // Pop both OTP and Login off the stack to return to the player/details
                            navController.popBackStack(TvRoutes.LOGIN, inclusive = true)
                        } else {
                            guestPreviewRepository.clearPendingResumeState()
                            navController.navigate(TvRoutes.home(phoneNumber)) {
                                popUpTo(TvRoutes.LOGIN) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },

                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // HOME
            composable(
                route = TvRoutes.HOME,
                arguments = listOf(
                    navArgument("phoneNumber") { type = NavType.StringType }
                )
            ) {
                TvHomeRoute(
                    onOpenPlayer = { video: VideoDto ->
                        navController.navigate(TvRoutes.details(video.id))
                    },

                    onOpenDetails = { videoId ->
                        navController.navigate(TvRoutes.details(videoId))
                    },

                    onOpenProfile = {
                        navController.navigate(TvRoutes.PROFILE)
                    },

                    onOpenSubscription = { selectedVideo ->
                        openSubscription(
                            selectedVideo.title,
                            selectedVideo.description,
                            selectedVideo.videoUrl
                        )
                    },

                    onNavigateToLogin = {
                        navController.navigate(TvRoutes.LOGIN) {
                            popUpTo(TvRoutes.HOME) { inclusive = true }
                            launchSingleTop = true
                        }
                    },

                    onOpenLiked = {
                        navController.navigate(TvRoutes.LIKED_VIDEOS)
                    },

                    onOpenMyList = {
                        navController.navigate(TvRoutes.MY_LIST)
                    },

                    onOpenCategory = { category ->
                        navController.navigate(TvRoutes.category(category))
                    }
                )
            }

            // PROFILE
            composable(TvRoutes.PROFILE) {
                TvProfileRoute(
                    userPreferences = userPreferences,

                    onBack = {
                        navController.popBackStack()
                    },

                    onOpenDownloadQuality = {
                        // Same as mobile: not implemented yet.
                    },

                    onOpenDeviceManagement = {
                        // Same as mobile: not implemented yet.
                    },

                    onOpenHelpCenter = {
                        // Same as mobile: not implemented yet.
                    },

                    onLogout = {
                        navController.navigate(TvRoutes.LOGIN) {
                            popUpTo(TvRoutes.HOME) { inclusive = true }
                            launchSingleTop = true
                        }
                    },

                    onDeleteAccount = {
                        // Same as mobile: pending API confirmation.
                    }
                )
            }

            // CATEGORY
            composable(
                route = TvRoutes.CATEGORY,
                arguments = listOf(
                    navArgument("category") { type = NavType.StringType }
                )
            ) { backStackEntry ->

                val category =
                    backStackEntry.arguments?.getString("category").orEmpty()

                TvCategoryRoute(
                    category = category,

                    onBack = {
                        navController.popBackStack()
                    },

                    onVideoClick = { video ->
                        navController.navigate(TvRoutes.player(video.id))
                    }
                )
            }

            // LIKED VIDEOS (mirrors mobile: uses SavedVideoCollection.MY_LIST)
            composable(TvRoutes.LIKED_VIDEOS) {
                val repository = remember {
                    SavedVideosRepositoryImpl(ApiClient.savedVideosApiService)
                }

                val likedVideosViewModel: SavedVideosViewModel = viewModel {
                    SavedVideosViewModel(
                        repository = repository,
                        collection = SavedVideoCollection.MY_LIST
                    )
                }

                TvVideoCollectionRoute(
                    viewModel = likedVideosViewModel,
                    title = "Liked Videos",
                    onHomeClick = { navController.popBackStack() },
                    onVideoClick = { video ->
                        navController.navigate(TvRoutes.player(video.id))
                    }
                )
            }

            // MY LIST (mirrors mobile: uses SavedVideoCollection.WATCH_LATER)
            composable(TvRoutes.MY_LIST) {
                val repository = remember {
                    SavedVideosRepositoryImpl(ApiClient.savedVideosApiService)
                }

                val myListViewModel: SavedVideosViewModel = viewModel {
                    SavedVideosViewModel(
                        repository = repository,
                        collection = SavedVideoCollection.MY_LIST
                    )
                }

                TvVideoCollectionRoute(
                    viewModel = myListViewModel,
                    title = "My List",
                    onHomeClick = { navController.popBackStack() },
                    onVideoClick = { video ->
                        navController.navigate(TvRoutes.player(video.id))
                    }
                )
            }

            // DETAILS
            composable(
                route = TvRoutes.DETAILS,
                arguments = listOf(
                    navArgument("videoId") { type = NavType.StringType }
                )
            ) { backStackEntry ->

                val videoId = backStackEntry.arguments?.getString("videoId")

                val videoRepository = remember {
                    VideoRepositoryImpl(ApiClient.videoApi)
                }

                var video by remember { mutableStateOf<Video?>(null) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(videoId) {
                    if (videoId.isNullOrBlank()) {
                        navController.popBackStack()
                        return@LaunchedEffect
                    }
                    video = loadVideo(videoRepository, videoId, "DETAILS")
                    isLoading = false
                }

                val loaded = video

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Loading...", color = Color.White)
                        }
                    }

                    loaded != null -> {
                        TvDetailsRoute(
                            video = loaded,
                            onBack = { navController.popBackStack() },
                            onPlayVideo = {
                                navController.navigate(TvRoutes.player(loaded.id))
                            },
                            onOpenLogin = {
                                navController.navigate(TvRoutes.login(redirect = "details")) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    else -> {
                        LaunchedEffect(Unit) {
                            navController.popBackStack()
                        }
                    }
                }
            }

            // PLAYER
            composable(
                route = TvRoutes.PLAYER,
                arguments = listOf(
                    navArgument("videoId") { type = NavType.StringType }
                )
            ) { backStackEntry ->

                val videoId = backStackEntry.arguments?.getString("videoId")

                val videoRepository = remember {
                    VideoRepositoryImpl(ApiClient.videoApi)
                }

                var video by remember { mutableStateOf<Video?>(null) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(videoId) {
                    if (videoId.isNullOrBlank()) {
                        navController.popBackStack()
                        return@LaunchedEffect
                    }
                    video = loadVideo(videoRepository, videoId, "PLAYER_ROUTE")
                    isLoading = false
                }

                val loaded = video

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Loading...", color = Color.White)
                        }
                    }

                    loaded != null -> {
                        TvPlayerRoute(
                            video = loaded,
                            onBack = {
                                navController.navigate(TvRoutes.home("")) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            viewModelFactory = VideoPlayerViewModelFactory(
                                watchProgressRepository = watchProgressRepository,
                                guestPreviewRepository = guestPreviewRepository
                            ),
                            onOpenSubscription = { selectedVideo ->
                                navController.navigate(TvRoutes.login(redirect = "player")) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    else -> {
                        LaunchedEffect(Unit) {
                            navController.popBackStack()
                        }
                    }
                }
            }
        }
    }
}
