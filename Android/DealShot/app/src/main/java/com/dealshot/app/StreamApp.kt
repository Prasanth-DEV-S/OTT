package com.dealshot.app

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dealshot.app.presentation.mobile.login.LoginRoute
import com.dealshot.app.presentation.mobile.otp.OtpRoute
import com.dealshot.app.ui.theme.StreamTheme
import com.dealshot.app.data.video.Video
import com.dealshot.app.data.video.VideoRepository
import com.dealshot.app.presentation.mobile.home.HomeRoute
import com.dealshot.app.presentation.mobile.details.MobileDetailsRoute
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.dealshot.app.data.preferences.UserPreferences
import com.dealshot.app.presentation.mobile.player.MobilePlayerRoute
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.media3.common.util.Log
import com.dealshot.app.data.remote.ApiClient
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.remote.repository.VideoRepositoryImpl
import com.dealshot.app.presentation.mobile.subscription.SubscriptionActivity
import com.dealshot.app.presentation.mobile.category.CategoryVideosRoute
import com.dealshot.app.presentation.mobile.likeAndList.VideoCollectionScreen
import kotlinx.coroutines.flow.first

private object StreamRoutes {

    const val LOGIN = "login"
    const val SUBSCRIPTION = "subscription"

    const val PLAYER = "player/{videoId}"
    const val DETAILS = "details/{videoId}"

    const val LIKED_VIDEOS = "liked_videos"
    const val MY_LIST = "my_list"

    const val CATEGORY = "category/{category}"
    const val OTP = "otp/{phoneNumber}/{mobileHint}"
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
        mobileHint: String
    ): String {
        return "otp/$phoneNumber/$mobileHint"
    }
    const val START = "start"
}
@OptIn(UnstableApi::class)
@Composable
fun StreamApp() {
    val context = LocalContext.current
    val userPreferences = remember {
        UserPreferences(context.applicationContext)
    }
    val navController = rememberNavController()

    StreamTheme {

        NavHost(
            navController = navController,
            startDestination = StreamRoutes.START
        ) {

            composable(StreamRoutes.START) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading...")
                }

                LaunchedEffect(Unit) {

                    val mobileNumber =
                        userPreferences.mobileNumber.first()

                    android.util.Log.d(
                        "STREAM_APP",
                        "START -> SAVED MOBILE = $mobileNumber"
                    )

                    // No logged-in user
                    if (mobileNumber.isNullOrBlank()) {

                        android.util.Log.d(
                            "STREAM_APP",
                            "NO SAVED USER -> LOGIN"
                        )

                        navController.navigate(StreamRoutes.LOGIN) {
                            popUpTo(StreamRoutes.START) {
                                inclusive = true
                            }
                        }

                        return@LaunchedEffect
                    }

                    // Check 30-day inactivity
                    val lastActivity =
                        userPreferences.lastActivity.first()

                    val now =
                        System.currentTimeMillis()

                    val thirtyDays =
                        60L * 24L * 60L * 60L * 1000L

                    if (
                        lastActivity != null &&
                        now - lastActivity >= thirtyDays
                    ) {

                        android.util.Log.d(
                            "STREAM_APP",
                            "SESSION EXPIRED -> LOGIN"
                        )

                        userPreferences.clearUser()

                        navController.navigate(StreamRoutes.LOGIN) {
                            popUpTo(StreamRoutes.START) {
                                inclusive = true
                            }
                        }

                        return@LaunchedEffect
                    }

                    // User is still within 30 days.
                    // Reset the timer because the app was opened.
                    userPreferences.updateLastActivity()

                    android.util.Log.d(
                        "STREAM_APP",
                        "USER ACTIVE -> RESET 30-DAY TIMER"
                    )

                    navController.navigate(
                        StreamRoutes.home(mobileNumber)
                    ) {
                        popUpTo(StreamRoutes.START) {
                            inclusive = true
                        }
                    }
                }
            }

            // LOGIN
            composable(StreamRoutes.LOGIN) {

                android.util.Log.d(
                    "STREAM_APP",
                    "LOGIN DESTINATION ENTERED"
                )

                LoginRoute(
                    onNavigateToOtp = { phoneNumber, mobileHint  ->

                        navController.navigate(
                            StreamRoutes.otp(
                                phoneNumber = phoneNumber,
                                mobileHint = mobileHint
                            )
                        )
                    }
                )
            }

            // HOME
            composable(
                route = StreamRoutes.HOME
            )
            { backStackEntry ->

                val phoneNumber =
                    backStackEntry.arguments
                        ?.getString("phoneNumber")
                        .orEmpty()

                HomeRoute(
                    mobileNumber = phoneNumber,

                    onOpenPlayer = { video: VideoDto ->

                        android.util.Log.d(
                            "STREAM_APP",
                            "OPEN PLAYER: ${video.title}"
                        )

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
                        android.util.Log.d(
                            "STREAM_APP",
                            "OPEN PROFILE"
                        )
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

                        android.util.Log.d(
                            "STREAM_APP",
                            "NAVIGATE TO LOGIN"
                        )

                        navController.navigate(
                            StreamRoutes.LOGIN
                        ) {
                            popUpTo(
                                StreamRoutes.HOME
                            ) {
                                inclusive = true
                            }
                        }
                    },

                    onOpenLiked = {
                        navController.navigate(
                            StreamRoutes.LIKED_VIDEOS
                        )
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
                )
            }

            // OTP
            composable(
                route = StreamRoutes.OTP
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

                OtpRoute(
                    phoneNumber = phoneNumber,
                    mobileHint = mobileHint,

                    onNavigateToHome = {
                        navController.navigate(
                            StreamRoutes.home(phoneNumber)
                        ) {
                            popUpTo(StreamRoutes.LOGIN) {
                                inclusive = true
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
            ) { backStackEntry ->

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
            composable(
                StreamRoutes.LIKED_VIDEOS
            ) {

                val videos = getCollectionVideos().take(3)

                VideoCollectionScreen(
                    title = "Liked Videos",
                    videos = videos,

                    onHomeClick = {
                        navController.popBackStack()
                    },

                    onVideoClick = { video ->
                        navController.navigate(
                            StreamRoutes.player(video.id)
                        )
                    }
                )
            }

            // MY LIST
            composable(
                StreamRoutes.MY_LIST
            ) {

                val videos = getCollectionVideos().take(5)

                VideoCollectionScreen(
                    title = "My List",
                    videos = videos,

                    onHomeClick = {
                        navController.popBackStack()
                    },

                    onVideoClick = { video ->
                        navController.navigate(
                            StreamRoutes.player(video.id)
                        )
                    }
                )
            }

            // SUBSCRIPTION
            composable(
                StreamRoutes.SUBSCRIPTION
            ) {
                // Subscription screen
            }
            // DETAILS
            // DETAILS
            composable(
                route = StreamRoutes.DETAILS,
                arguments = listOf(
                    navArgument("videoId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->

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

                LaunchedEffect(videoId) {

                    if (videoId.isNullOrBlank()) {
                        navController.popBackStack()
                        return@LaunchedEffect
                    }

                    try {

                        val response =
                            videoRepository.getVideos()

                        val dto =
                            response.firstOrNull {
                                it.id == videoId
                            }

                        if (dto != null) {

                            video = Video(
                                id = dto.id,
                                title = dto.title.orEmpty(),
                                description = dto.description.orEmpty(),
                                duration = dto.duration.orEmpty(),
                                thumbnail = dto.thumbnailUrl.orEmpty(),
                                url = dto.videoUrl.orEmpty(),
                                category = dto.category.orEmpty(),
                                apiId = dto.id,
                                thumbnailUrl = dto.thumbnailUrl.orEmpty(),
                                isPremium = dto.isPremium,
                                featured = dto.featured
                            )

                        } else {

                            android.util.Log.e(
                                "DETAILS",
                                "Video not found: $videoId"
                            )
                        }

                    } catch (e: Exception) {

                        android.util.Log.e(
                            "DETAILS",
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
                            Text("Loading...")
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

                        Log.d(
                            "PLAYER_ROUTE",
                            "Loading video ID: $videoId"
                        )

                        val videos =
                            videoRepository.getVideos()

                        val dto =
                            videos.firstOrNull {
                                it.id == videoId
                            }

                        if (dto != null) {

                            video = Video(
                                id = dto.id,
                                title = dto.title.orEmpty(),
                                description = dto.description.orEmpty(),
                                duration = dto.duration.orEmpty(),
                                thumbnail = dto.thumbnailUrl.orEmpty(),
                                url = dto.videoUrl.orEmpty(),
                                category = dto.category.orEmpty(),
                                apiId = dto.id,
                                thumbnailUrl = dto.thumbnailUrl.orEmpty(),
                                isPremium = dto.isPremium,
                                featured = dto.featured
                            )

                            Log.d(
                                "PLAYER_ROUTE",
                                "Video found: ${dto.title}"
                            )

                        } else {

                            Log.e(
                                "PLAYER_ROUTE",
                                "Video not found: $videoId"
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

                            onOpenSubscription = { selectedVideo ->

                                context.startActivity(
                                    Intent(
                                        context,
                                        SubscriptionActivity::class.java
                                    ).apply {

                                        putExtra(
                                            "title",
                                            selectedVideo.title
                                        )

                                        putExtra(
                                            "description",
                                            selectedVideo.description
                                        )

                                        putExtra(
                                            "url",
                                            selectedVideo.url
                                        )
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
        }
    }
}

@OptIn(UnstableApi::class)
private fun getCollectionVideos(): List<Video> {
    return VideoRepository.getVideos()
}

