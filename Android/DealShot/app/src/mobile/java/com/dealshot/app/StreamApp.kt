package com.dealshot.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dealshot.app.data.preferences.PreferenceManager
import com.dealshot.app.data.preferences.UserPreferences
import com.dealshot.app.data.preferences.UserPreferences.Companion.MOBILE_NUMBER
import com.dealshot.app.data.remote.ApiClient
import com.dealshot.app.data.remote.ApiClient.watchProgressApiService
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.remote.repository.AuthRepositoryImpl
import com.dealshot.app.data.remote.repository.SavedVideoCollection
import com.dealshot.app.data.remote.repository.SavedVideosRepositoryImpl
import com.dealshot.app.data.remote.repository.VideoRepositoryImpl
import com.dealshot.app.data.remote.repository.WatchProgressRepositoryImpl
import com.dealshot.app.data.remote.repository.like.LikedVideosRepositoryImpl
import com.dealshot.app.data.video.Video
import com.dealshot.app.presentation.mobile.category.CategoryVideosRoute
import com.dealshot.app.presentation.mobile.details.MobileDetailsRoute
import com.dealshot.app.presentation.mobile.home.HomeRoute
import com.dealshot.app.presentation.mobile.home.SessionRoute
import com.dealshot.app.presentation.mobile.myList.LikedVideosRoute
import com.dealshot.app.presentation.mobile.myList.VideoCollectionRoute
import com.dealshot.app.presentation.mobile.login.LoginRoute
import com.dealshot.app.presentation.mobile.otp.OtpRoute
import com.dealshot.app.presentation.mobile.player.MobilePlayerRoute
import com.dealshot.app.presentation.mobile.profile.ProfileRoute
import com.dealshot.app.presentation.mobile.report.ReportRoute
import com.dealshot.app.presentation.mobile.subscription.MobileSubscriptionRoute
import com.dealshot.app.presentation.mobile.subscription.SubscriptionActivity
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import com.dealshot.app.presentation.mobile.profile.CreateProfileRoute
import com.dealshot.app.presentation.mobile.profile.CreateProfileScreen
import com.dealshot.app.data.remote.repository.report.MockReportOutcome
import com.dealshot.app.data.remote.repository.report.MockReportRepository
import com.dealshot.app.presentation.shared.auth.SessionViewModelFactory
import com.dealshot.app.presentation.shared.like.LikedVideosViewModel
import com.dealshot.app.presentation.shared.like.LikedVideosViewModelFactory
import com.dealshot.app.presentation.shared.myList.SavedVideosViewModel
import com.dealshot.app.presentation.shared.player.VideoPlayerViewModelFactory
import com.dealshot.app.presentation.shared.profile.ProfileIntent
import com.dealshot.app.presentation.shared.subscription.SubscriptionViewModel
import com.dealshot.app.ui.theme.StreamTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

private val Context.dataStore by preferencesDataStore(
    name = "streambox_preferences"
)

private object StreamRoutes {

    const val LOGIN = "login?redirect={redirect}"
    fun login(redirect: String? = null): String {
        return if (redirect != null) "login?redirect=$redirect" else "login"
    }
    const val SUBSCRIPTION = "subscription/plans"

    const val PLAYER = "player/{videoId}"
    const val DETAILS = "details/{videoId}"
    const val REPORT = "report/{videoId}?title={title}&thumbnail={thumbnail}"

    const val CREATE_PROFILE = "create_profile"
    fun report(videoId: String, title: String, thumbnail: String): String {
        return "report/${Uri.encode(videoId)}?title=${Uri.encode(title)}&thumbnail=${Uri.encode(thumbnail)}"
    }

    const val LIKED_VIDEOS = "liked_videos"
    const val MY_LIST = "my_list"

    const val CATEGORY = "category/{category}"
    const val OTP = "otp/{phoneNumber}/{mobileHint}?redirect={redirect}"
    fun player(videoId: String): String {
        return "player/$videoId"
    }
    const val HOME = "home/{phoneNumber}"

    fun home(phoneNumber: String): String {
        return "home/$phoneNumber"
    }
    fun details(videoId: String): String {
        return "details/$videoId"
    }

    fun category(category: String): String {
        return "category/${Uri.encode(category)}"
    }

    fun otp(
        phoneNumber: String,
        mobileHint: String,
        redirect: String? = null
    ): String {
        return if (redirect != null) "otp/$phoneNumber/$mobileHint?redirect=$redirect" else "otp/$phoneNumber/$mobileHint"
    }
    const val START = "start"

    const val PROFILE = "profile"
    const val WATCH_LATER = "watch_later"
}
suspend fun saveMobileNumber(number: String, context: Context ) {
    context.dataStore.edit { preferences ->
        preferences[MOBILE_NUMBER] = number
    }
}
fun String.isValidIndianNumber(): Boolean {
    // Allows the first digit to be ANY number (0-9), making your test number TRUE
    val pattern = Regex("^(\\+?91)?\\d{10}$")
    return this.matches(pattern)
}

@OptIn(UnstableApi::class)
@Composable
fun StreamApp(initialDeepLinkId: String? = null) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userPreferences = remember {
        UserPreferences(context.applicationContext)
    }
    val watchProgressRepository = WatchProgressRepositoryImpl(
        api = watchProgressApiService
    )
    
    val guestPreviewRepository = com.dealshot.app.data.preferences.GuestPreviewRepositoryImpl(context)


    val authRepository = remember {
        AuthRepositoryImpl(ApiClient.authApi)
    }

    val sessionFactory = remember {
        SessionViewModelFactory(
            authRepository = authRepository,
            hasSavedToken = {
                userPreferences.authToken.first()
                    .orEmpty()
                    .isNotBlank()
            },
            clearSession = {
                userPreferences.clearUser()
            }
        )
    }


    val navController = rememberNavController()

    LaunchedEffect(initialDeepLinkId) {
        if (!initialDeepLinkId.isNullOrBlank()) {
            navController.navigate(StreamRoutes.details(initialDeepLinkId)) {
                launchSingleTop = true
            }
        }
    }

    StreamTheme {

        NavHost(
            navController = navController,
            startDestination = StreamRoutes.START,
            modifier = Modifier.fillMaxSize()
        ) {

            composable(StreamRoutes.START) {
                SessionRoute(
                    factory = sessionFactory,

                    onNavigateToLogin = {
                        navController.navigate(StreamRoutes.HOME) {
                            popUpTo(StreamRoutes.START) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },

                    onNavigateToHome = { phoneNumber ->
                        coroutineScope.launch {
                            userPreferences.updateLastActivity()
                        }

                        navController.navigate(
                            StreamRoutes.home(phoneNumber)
                        ) {
                            popUpTo(StreamRoutes.START) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // LOGIN
            composable(
                route = StreamRoutes.LOGIN,
                arguments = listOf(
                    navArgument("redirect") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val redirect = backStackEntry.arguments?.getString("redirect")
                LoginRoute(
                    onNavigateToOtp = { phoneNumber, mobileHint  ->

                        navController.navigate(
                            StreamRoutes.otp(
                                phoneNumber = phoneNumber,
                                mobileHint = mobileHint,
                                redirect = redirect
                            )
                        )
                    }
                )
            }

            // PROFILE
            composable(StreamRoutes.PROFILE) {

                ProfileRoute(
                    userPreferences = userPreferences,

                    onBack = {
                        navController.popBackStack()
                    },

                    onNavigateToLogin = {
                        navController.navigate(StreamRoutes.login(redirect = "profile"))
                    },

                    onOpenDownloadQuality = {
                        // We will implement this next.
                    },

                    onOpenDeviceManagement = {
                        // We will implement this next.
                    },

                    onOpenHelpCenter = {
                        // We will implement this next.
                    },

                    onLogout = {
                        coroutineScope.launch {
                            userPreferences.clearUser()
                        }
                        PreferenceManager(context).clearAll()
                        guestPreviewRepository.clearPendingResumeState()
                        navController.navigate(StreamRoutes.home("")) {
                            popUpTo(0) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },

                    onDeleteAccount = {
                        coroutineScope.launch {
                            userPreferences.clearUser()
                        }
                        PreferenceManager(context).clearAll()
                        guestPreviewRepository.clearPendingResumeState()
                        navController.navigate(StreamRoutes.home("")) {
                            popUpTo(0) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onAddProfile = {
                        navController.navigate(StreamRoutes.CREATE_PROFILE) { launchSingleTop = true }
                    },
                    onOpenSubscription = {
                        navController.navigate(StreamRoutes.SUBSCRIPTION) { launchSingleTop = true }
                    }
                )
            }

            // HOME
            composable(
                route = StreamRoutes.HOME
            )
            { backStackEntry ->

                var phoneNumber =
                    backStackEntry.arguments
                        ?.getString("phoneNumber")
                        .orEmpty()
                val preferenceManager = PreferenceManager(context)
                phoneNumber = if (phoneNumber.isValidIndianNumber()) {
                    phoneNumber
                }else{
                    ""
                }

                preferenceManager.saveString("Mobile_No",phoneNumber)


                HomeRoute(
                    mobileNumber = phoneNumber,

                    onOpenPlayer = { video: VideoDto ->

                        navController.navigate(
                            StreamRoutes.details(video.id)
                        )
                    },
                    onOpenDetails = { videoId ->
                        navController.navigate(
                            StreamRoutes.details(videoId)
                        )
                    },

                    onOpenProfile = {
                        navController.navigate(StreamRoutes.PROFILE)
                    },

                    onOpenSubscription = { selectedVideo ->

                        context.startActivity(
                            Intent(
                                context,
                                SubscriptionActivity::class.java
                            ).apply {
                                putExtra("title", selectedVideo.title)
                                putExtra("description", selectedVideo.description)
                                putExtra("url", selectedVideo.videoUrl)
                            }
                        )
                    },


                    onNavigateToLogin = {
                        navController.navigate(
                            StreamRoutes.login(redirect = "home")
                        )
                    },

                    onOpenLiked = {
                        /*if(phoneNumber.isBlank()){
                            navController.navigate(StreamRoutes.login()) {
                                popUpTo(StreamRoutes.HOME) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }else {*/
                            navController.navigate(
                                StreamRoutes.LIKED_VIDEOS
                            )
//                        }

                    },

                    onOpenMyList = {
                        navController.navigate(
                            StreamRoutes.MY_LIST
                        )
                    },
                    onOpenCategory = { category ->
                        navController.navigate(
                            StreamRoutes.category(category)
                        )
                    },
                    onAddProfile = {
                        navController.navigate(StreamRoutes.CREATE_PROFILE) { launchSingleTop = true }
                    }
                )
            }

            // OTP
            composable(
                route = StreamRoutes.OTP,
                arguments = listOf(
                    navArgument("phoneNumber") { type = NavType.StringType },
                    navArgument("mobileHint") { type = NavType.StringType },
                    navArgument("redirect") { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            )
            { backStackEntry ->

                val phoneNumber =
                    backStackEntry.arguments
                        ?.getString("phoneNumber")
                        .orEmpty()

                val mobileHint =
                    backStackEntry.arguments
                        ?.getString("mobileHint")
                        .orEmpty()
                        
                val redirect = backStackEntry.arguments?.getString("redirect")

                OtpRoute(
                    phoneNumber = phoneNumber,
                    mobileHint = mobileHint,

                    onNavigateToHome = {
                        val pendingState = guestPreviewRepository.getPendingResumeState()
                        val targetScreen = pendingState?.screen ?: redirect

                        if (targetScreen != null && (targetScreen.startsWith("player") || targetScreen.startsWith("details"))) {
                            guestPreviewRepository.clearPendingResumeState()
                            
                            // Pop both OTP and Login off the stack to return to the player/details
                            navController.popBackStack(StreamRoutes.LOGIN, inclusive = true)
                        } else {
                            guestPreviewRepository.clearPendingResumeState()
                            navController.navigate(StreamRoutes.home(phoneNumber)) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },

                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = StreamRoutes.CATEGORY,
                arguments = listOf(
                    navArgument("category") {
                        type = NavType.StringType
                    }
                )
            ) {
                    backStackEntry ->

                val category =
                    backStackEntry.arguments
                        ?.getString("category")
                        .orEmpty()

                CategoryVideosRoute(
                    category = category,

                    onBack = {
                        navController.popBackStack()
                    },

                    onVideoClick = { video ->
                        navController.navigate(
                            StreamRoutes.player(video.id)
                        )
                    }
                )
            }

            // LIKED VIDEOS
            composable(StreamRoutes.LIKED_VIDEOS) {

                val repository = remember {
                    LikedVideosRepositoryImpl(
                        api = ApiClient.likedVideosApiService
                    )
                }

                val likedVideosViewModel: LikedVideosViewModel = viewModel(
                    factory = LikedVideosViewModelFactory(repository)
                )

                LikedVideosRoute(
                    viewModel = likedVideosViewModel,
                    title = "Liked Videos",
                    onHomeClick = {
                        navController.popBackStack()
                    },
                    onVideoClick = { video ->
//                        navController.navigate(
//                            StreamRoutes.player(video.id)
//                        )
                        navController.navigate(
                            StreamRoutes.details(video.id)
                        )
                    },
                    onLoginClick = {
                        navController.navigate(
                            StreamRoutes.login(redirect = "liked_videos")
                        ) {
                            launchSingleTop = true
                        }
                    }
                )
            }
/*            composable(StreamRoutes.LIKED_VIDEOS) {
                val repository = remember {
                    SavedVideosRepositoryImpl(ApiClient.savedVideosApiService)
                }
//111111111111111111111111
                val likedVideosViewModel: SavedVideosViewModel = viewModel {
                    SavedVideosViewModel(
                        repository = repository,
                        collection = SavedVideoCollection.MY_LIST
                    )
                }

                VideoCollectionRoute(
                    viewModel = likedVideosViewModel,
                    title = "Liked Videos",
                    onHomeClick = { navController.popBackStack() },
                    onVideoClick = { video ->
                        navController.navigate(StreamRoutes.player(video.id))
                    }
                )
            }*/

            // MY LIST
            composable(StreamRoutes.MY_LIST) {
                val repository = remember {
                    SavedVideosRepositoryImpl(ApiClient.savedVideosApiService)
                }

                val myListViewModel: SavedVideosViewModel = viewModel {
                    SavedVideosViewModel(
                        repository = repository,
                        collection = SavedVideoCollection.MY_LIST
                    )
                }

                VideoCollectionRoute(
                    viewModel = myListViewModel,
                    title = "My List",
                    onHomeClick = { navController.popBackStack() },
                    onVideoClick = { video ->
//                        navController.navigate(StreamRoutes.player(video.id))

                        navController.navigate(
                            StreamRoutes.details(video.id)
                        )
                    }
                )
            }

            // SUBSCRIPTION
            composable(
                StreamRoutes.SUBSCRIPTION
            ) {
                val subscriptionViewModel: SubscriptionViewModel = viewModel()

                MobileSubscriptionRoute(
                    onBack = {
                        navController.popBackStack()
                    },
                    onOpenPlayer = { _, _, _ ->
                        navController.popBackStack()
                    },
                    viewModel = subscriptionViewModel
                )
            }

            // DETAILS
            composable(
                route = StreamRoutes.DETAILS,
                arguments = listOf(
                    navArgument("videoId") {
                        type = NavType.StringType
                    }
                ),
                deepLinks = listOf(
                    androidx.navigation.navDeepLink { uriPattern = "dealshot://video/{videoId}" },
                    androidx.navigation.navDeepLink { uriPattern = "dealshot://details/{videoId}" },
                    androidx.navigation.navDeepLink { uriPattern = "https://dealshot.in/video/{videoId}" },
                    androidx.navigation.navDeepLink { uriPattern = "https://dealshot.in/details/{videoId}" },
                    androidx.navigation.navDeepLink { uriPattern = "http://dealshot.in/video/{videoId}" },
                    androidx.navigation.navDeepLink { uriPattern = "http://dealshot.in/details/{videoId}" },
                    androidx.navigation.navDeepLink { uriPattern = "dealshot://share/{videoId}" },
                    androidx.navigation.navDeepLink { uriPattern = "https://dealshot.in/share/{videoId}" },
                    androidx.navigation.navDeepLink { uriPattern = "http://dealshot.in/share/{videoId}" }
                )
            )
            { backStackEntry ->

                val videoId =
                    backStackEntry.arguments
                        ?.getString("videoId")

                val videoRepository = remember {
                    VideoRepositoryImpl(ApiClient.videoApi)
                }

                var video by remember {
                    mutableStateOf<Video?>(null)
                }

                var isLoading by remember {
                    mutableStateOf(true)
                }

                var errorMessage by remember {
                    mutableStateOf<String?>(null)
                }

                LaunchedEffect(videoId) {

                    if (videoId.isNullOrBlank()) {
                        errorMessage = "Invalid video ID"
                        isLoading = false
                        return@LaunchedEffect
                    }

                    try {

                        val response =
                            videoRepository.getVideos()

                        val dto =
                            response.firstOrNull {
                                it.id == videoId || it.altId == videoId
                            }

                        if (dto != null) {

                            video = Video(
                                id = dto.effectiveId,
                                title = dto.title.orEmpty(),
                                description = dto.description.orEmpty(),
                                duration = dto.duration.orEmpty(),
                                thumbnail = dto.effectiveThumbnail,
                                url = dto.videoUrl.orEmpty(),
                                category = dto.category.orEmpty(),
                                apiId = dto.effectiveId,
                                thumbnailUrl = dto.effectiveThumbnail,
                                isPremium = dto.isPremium,
                                promoVideoUrl = dto.promoVideoUrl.orEmpty(),
                                featured = dto.featured
                            )

                        } else {
                            // Video not found in memory/list, resolve public share link: GET /videos/share/resolve/{videoId}
                            val resolveResult = videoRepository.resolveSharedVideo(videoId)
                            resolveResult.fold(
                                onSuccess = { resolvedDto ->
                                    val videoDto = resolvedDto.toVideoDto()
                                    video = Video(
                                        id = videoDto.effectiveId,
                                        title = videoDto.title.orEmpty(),
                                        description = videoDto.description.orEmpty(),
                                        duration = videoDto.duration.orEmpty(),
                                        thumbnail = videoDto.effectiveThumbnail,
                                        url = videoDto.videoUrl.orEmpty(),
                                        category = videoDto.category.orEmpty(),
                                        apiId = videoDto.effectiveId,
                                        thumbnailUrl = videoDto.effectiveThumbnail,
                                        isPremium = videoDto.isPremium,
                                        promoVideoUrl = videoDto.promoVideoUrl.orEmpty(),
                                        featured = videoDto.featured
                                    )
                                },
                                onFailure = { error ->
                                    errorMessage = error.message ?: "This video is not available"
                                }
                            )
                        }

                    } catch (e: Exception) {

                        android.util.Log.e(
                            "DETAILS",
                            "Failed to load video: $videoId",
                            e
                        )
                        errorMessage = e.message ?: "Failed to load video"

                    } finally {
                        isLoading = false
                    }
                }

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF120E0A)),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = com.dealshot.app.ui.theme.GoldPrimary
                            )
                        }
                    }

                    video != null -> {

                        MobileDetailsRoute(
                            video = video!!,
                            onBack = {
                                navController.popBackStack()
                            },
                            onPlayVideo = {
                                navController.navigate(
                                    StreamRoutes.player(video!!.id)
                                )
                            },
                            onShareClick = {
                                val currentVideo = video ?: return@MobileDetailsRoute
                                val shareText = buildString {
                                    append(currentVideo.title)

                                    if (currentVideo.description.isNotBlank()) {
                                        append("\n\n")
                                        append(currentVideo.description)
                                    }

                                    if (currentVideo.url.isNotBlank()) {
                                        append("\n\n")
                                        append(currentVideo.url)
                                    }
                                }

                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, currentVideo.title)
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }

                                context.startActivity(
                                    Intent.createChooser(shareIntent, "Share video")
                                )
                            },
                            onLikeClick = {
//                                navController.navigate(
//                                    StreamRoutes.LOGIN
//                                ) {
//                                    popUpTo(
//                                        StreamRoutes.HOME
//                                    ) {
//                                        inclusive = true
//                                    }
//                                }
                            },
                            onMyListClick = {
//                                navController.navigate(
//                                    StreamRoutes.LOGIN
//                                ) {
//                                    popUpTo(
//                                        StreamRoutes.HOME
//                                    ) {
//                                        inclusive = true
//                                    }
//                                }
                                  },
                            onLoginClick = {
                                navController.navigate(
                                    StreamRoutes.login(redirect = "details")
                                )
                            },
                            isPremiumUser = video!!.isPremium && false, // see note below
                            onSubscribeClick = {
                                navController.navigate(StreamRoutes.SUBSCRIPTION)
                                //                                navController.navigate(StreamRoutes.SUBSCRIPTION)
                            },
                            onReportClick = {
                                video?.let { v ->
                                    navController.navigate(
                                        StreamRoutes.report(
                                            videoId = v.id,
                                            title = v.title,
                                            thumbnail = (v.thumbnailUrl ?: v.thumbnail).orEmpty()
                                        )
                                    )
                                }
                            }
                        )
                    }

                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF120E0A))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.layout.Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = com.dealshot.app.ui.theme.GoldPrimary,
                                    modifier = Modifier.size(56.dp)
                                )
                                Text(
                                    text = errorMessage ?: "This video is not available",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                androidx.compose.material3.Button(
                                    onClick = { navController.popBackStack() },
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                        containerColor = com.dealshot.app.ui.theme.GoldPrimary,
                                        contentColor = Color(0xFF161108)
                                    ),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                ) {
                                    Text("Go Back", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }


            // PLAYER
            composable(
                route = StreamRoutes.PLAYER,
                arguments = listOf(
                    navArgument("videoId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->

                val videoId =
                    backStackEntry.arguments?.getString("videoId")

                val videoRepository = remember {
                    VideoRepositoryImpl(ApiClient.videoApi)
                }

                var video by remember {
                    mutableStateOf<Video?>(null)
                }

                var isLoading by remember {
                    mutableStateOf(true)
                }

                LaunchedEffect(videoId) {

                    if (videoId.isNullOrBlank()) {
                        navController.popBackStack()
                        return@LaunchedEffect
                    }

                    try {

                        val videos =
                            videoRepository.getVideos()

                        val dto =
                            videos.firstOrNull {
                                it.id == videoId || it.altId == videoId
                            }

                        if (dto != null) {

                            video = Video(
                                id = dto.effectiveId,
                                title = dto.title.orEmpty(),
                                description = dto.description.orEmpty(),
                                duration = dto.duration.orEmpty(),
                                thumbnail = dto.effectiveThumbnail,
                                url = dto.videoUrl.orEmpty(),
                                category = dto.category.orEmpty(),
                                apiId = dto.effectiveId,
                                thumbnailUrl = dto.effectiveThumbnail,
                                isPremium = dto.isPremium,
                                featured = dto.featured,
                                promoVideoUrl = dto.promoVideoUrl.orEmpty()
                            )

                        } else {
                            val resolveResult = videoRepository.resolveSharedVideo(videoId)
                            resolveResult.fold(
                                onSuccess = { resolvedDto ->
                                    val videoDto = resolvedDto.toVideoDto()
                                    video = Video(
                                        id = videoDto.effectiveId,
                                        title = videoDto.title.orEmpty(),
                                        description = videoDto.description.orEmpty(),
                                        duration = videoDto.duration.orEmpty(),
                                        thumbnail = videoDto.effectiveThumbnail,
                                        url = videoDto.videoUrl.orEmpty(),
                                        category = videoDto.category.orEmpty(),
                                        apiId = videoDto.effectiveId,
                                        thumbnailUrl = videoDto.effectiveThumbnail,
                                        isPremium = videoDto.isPremium,
                                        featured = videoDto.featured,
                                        promoVideoUrl = videoDto.promoVideoUrl.orEmpty()
                                    )
                                },
                                onFailure = {
                                    Log.e(
                                        "PLAYER_ROUTE",
                                        "Video not found: $videoId"
                                    )
                                }
                            )
                        }

                    } catch (e: Exception) {

                        Log.e(
                            "PLAYER_ROUTE",
                            "Failed to load video: $videoId",
                            e
                        )

                    } finally {

                        isLoading = false
                    }
                }

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Loading...",
                                color = Color.White
                            )
                        }
                    }

                    video != null -> {
                        MobilePlayerRoute(
                            video = video!!,
                            onBack = {
                                navController.popBackStack()
                            },
                            viewModelFactory = VideoPlayerViewModelFactory(
                                watchProgressRepository = watchProgressRepository,
                                guestPreviewRepository = guestPreviewRepository,
                                userPreferences = userPreferences
                            ),
                            onOpenSubscription = { selectedVideo ->
                                context.startActivity(
                                    Intent(context, SubscriptionActivity::class.java).apply {
                                        putExtra("title", selectedVideo.title)
                                        putExtra("description", selectedVideo.description)
                                        putExtra("url", selectedVideo.url)
                                    }
                                )
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

            // CREATE PROFILE
            composable(
                route = StreamRoutes.CREATE_PROFILE,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut() },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn() },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
            ) {
                CreateProfileRoute(
                    userPreferences = userPreferences,
                    onBack = { navController.popBackStack() }
                )
            }

            // REPORT
            composable(
                route = StreamRoutes.REPORT,
                arguments = listOf(
                    navArgument("videoId") { type = NavType.StringType },
                    navArgument("title") { type = NavType.StringType; defaultValue = "" },
                    navArgument("thumbnail") { type = NavType.StringType; defaultValue = "" }
                ),
                enterTransition = { slideInVertically(initialOffsetY = { it / 3 }) + fadeIn() },
                exitTransition = { fadeOut() },
                popEnterTransition = { fadeIn() },
                popExitTransition = { slideOutVertically(targetOffsetY = { it / 3 }) + fadeOut() }
            ) { backStackEntry ->

                val videoId = backStackEntry.arguments?.getString("videoId").orEmpty()
                val title = backStackEntry.arguments?.getString("title").orEmpty()
                val thumbnail = backStackEntry.arguments?.getString("thumbnail").orEmpty()

                // TODO: swap for ReportRepositoryImpl(<your ReportApiService>) when the backend is ready
                val reportRepository = remember {
                    MockReportRepository(MockReportOutcome.SIMULATED_SUCCESS)
                }

                ReportRoute(
                    videoId = videoId,
                    videoTitle = title,
                    thumbnailUrl = thumbnail,
                    repository = reportRepository,
                    isLoggedIn = { !PreferenceManager(context).getString("Mobile_No").isNullOrBlank() },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
