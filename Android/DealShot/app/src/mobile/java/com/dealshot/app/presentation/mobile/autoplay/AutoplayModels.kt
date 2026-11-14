package com.dealshot.app.presentation.mobile.autoplay

import androidx.compose.ui.graphics.Color

data class AutoplayCategory(
    val id: String,
    val name: String
)

data class AutoplayVideo(
    val id: String,
    val title: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val duration: String,
    val publishedTime: String,
    val categoryId: String,
    val gradientColors: List<Color> = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
)

object AutoplayDemoRepository {
    val categories = listOf(
        AutoplayCategory(id = "national", name = "National"),
        AutoplayCategory(id = "sports", name = "Sports"),
        AutoplayCategory(id = "entertainment", name = "Entertainment")
    )

    private const val DEMO_VIDEO_URL_1 = "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    private const val DEMO_VIDEO_URL_2 = "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
    private const val DEMO_VIDEO_URL_3 = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"

    val videosByCategory: Map<String, List<AutoplayVideo>> = mapOf(
        "national" to listOf(
            AutoplayVideo(
                id = "nat_1",
                title = "Parliament Session Highlights",
                videoUrl = DEMO_VIDEO_URL_1,
                thumbnailUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=500",
                duration = "4:32",
                publishedTime = "2 hours ago",
                categoryId = "national",
                gradientColors = listOf(Color(0xFF3D2A1D), Color(0xFF1A120B))
            ),
            AutoplayVideo(
                id = "nat_2",
                title = "Budget 2026: Key Announcements",
                videoUrl = DEMO_VIDEO_URL_2,
                thumbnailUrl = "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=500",
                duration = "6:10",
                publishedTime = "4 hours ago",
                categoryId = "national",
                gradientColors = listOf(Color(0xFF1C2D42), Color(0xFF0D1724))
            ),
            AutoplayVideo(
                id = "nat_3",
                title = "Monsoon Update Across States",
                videoUrl = DEMO_VIDEO_URL_3,
                thumbnailUrl = "https://images.unsplash.com/photo-1515694346937-94d85e41e6f0?w=500",
                duration = "3:45",
                publishedTime = "6 hours ago",
                categoryId = "national",
                gradientColors = listOf(Color(0xFF3E3218), Color(0xFF1B160A))
            ),
            AutoplayVideo(
                id = "nat_4",
                title = "New Metro Line Inaugurated",
                videoUrl = DEMO_VIDEO_URL_1,
                thumbnailUrl = "https://images.unsplash.com/photo-1517649763962-0c623266010b?w=500",
                duration = "2:58",
                publishedTime = "8 hours ago",
                categoryId = "national",
                gradientColors = listOf(Color(0xFF2B3038), Color(0xFF12151A))
            ),
            AutoplayVideo(
                id = "nat_5",
                title = "Supreme Court Verdict Explained",
                videoUrl = DEMO_VIDEO_URL_2,
                thumbnailUrl = "https://images.unsplash.com/photo-1589829545856-d10d557cf95f?w=500",
                duration = "5:20",
                publishedTime = "10 hours ago",
                categoryId = "national",
                gradientColors = listOf(Color(0xFF2A1C38), Color(0xFF110B18))
            ),
            AutoplayVideo(
                id = "nat_6",
                title = "Independence Day Preparations",
                videoUrl = DEMO_VIDEO_URL_3,
                thumbnailUrl = "https://images.unsplash.com/photo-1532375810709-75b1da00537c?w=500",
                duration = "3:15",
                publishedTime = "12 hours ago",
                categoryId = "national",
                gradientColors = listOf(Color(0xFF1F3A29), Color(0xFF0A180F))
            )
        ),
        "sports" to listOf(
            AutoplayVideo(
                id = "sp_1",
                title = "India vs Australia T20 Thriller Highlights",
                videoUrl = DEMO_VIDEO_URL_1,
                thumbnailUrl = "https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?w=500",
                duration = "5:45",
                publishedTime = "1 hour ago",
                categoryId = "sports"
            ),
            AutoplayVideo(
                id = "sp_2",
                title = "Neeraj Chopra Wins Gold in Diamond League",
                videoUrl = DEMO_VIDEO_URL_2,
                thumbnailUrl = "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=500",
                duration = "4:12",
                publishedTime = "3 hours ago",
                categoryId = "sports"
            ),
            AutoplayVideo(
                id = "sp_3",
                title = "ISL Final: Dramatic Extra Time Goal",
                videoUrl = DEMO_VIDEO_URL_3,
                thumbnailUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=500",
                duration = "7:00",
                publishedTime = "5 hours ago",
                categoryId = "sports"
            ),
            AutoplayVideo(
                id = "sp_4",
                title = "Badminton World Championship Semi-Finals",
                videoUrl = DEMO_VIDEO_URL_1,
                thumbnailUrl = "https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=500",
                duration = "3:30",
                publishedTime = "7 hours ago",
                categoryId = "sports"
            ),
            AutoplayVideo(
                id = "sp_5",
                title = "Chess World Cup: Pragg's Masterclass",
                videoUrl = DEMO_VIDEO_URL_2,
                thumbnailUrl = "https://images.unsplash.com/photo-1529699211952-734e80c4d42b?w=500",
                duration = "8:15",
                publishedTime = "9 hours ago",
                categoryId = "sports"
            ),
            AutoplayVideo(
                id = "sp_6",
                title = "Olympic Training Camp Inside Look",
                videoUrl = DEMO_VIDEO_URL_3,
                thumbnailUrl = "https://images.unsplash.com/photo-1517649763962-0c623266010b?w=500",
                duration = "4:50",
                publishedTime = "11 hours ago",
                categoryId = "sports"
            )
        ),
        "entertainment" to listOf(
            AutoplayVideo(
                id = "ent_1",
                title = "Blockbuster Movie Trailer Reaction",
                videoUrl = DEMO_VIDEO_URL_1,
                thumbnailUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500",
                duration = "3:40",
                publishedTime = "30 mins ago",
                categoryId = "entertainment"
            ),
            AutoplayVideo(
                id = "ent_2",
                title = "National Film Awards Red Carpet Highlights",
                videoUrl = DEMO_VIDEO_URL_2,
                thumbnailUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500",
                duration = "6:25",
                publishedTime = "2 hours ago",
                categoryId = "entertainment"
            ),
            AutoplayVideo(
                id = "ent_3",
                title = "Behind The Scenes: Stunt Sequences",
                videoUrl = DEMO_VIDEO_URL_3,
                thumbnailUrl = "https://images.unsplash.com/photo-1478720568477-152d9b164e26?w=500",
                duration = "5:10",
                publishedTime = "4 hours ago",
                categoryId = "entertainment"
            ),
            AutoplayVideo(
                id = "ent_4",
                title = "Top 10 Web Series to Binge This Weekend",
                videoUrl = DEMO_VIDEO_URL_1,
                thumbnailUrl = "https://images.unsplash.com/photo-1522869635100-9f4c5e86aa37?w=500",
                duration = "4:45",
                publishedTime = "6 hours ago",
                categoryId = "entertainment"
            ),
            AutoplayVideo(
                id = "ent_5",
                title = "Exclusive Interview with Film Directors",
                videoUrl = DEMO_VIDEO_URL_2,
                thumbnailUrl = "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?w=500",
                duration = "9:30",
                publishedTime = "8 hours ago",
                categoryId = "entertainment"
            ),
            AutoplayVideo(
                id = "ent_6",
                title = "Music Concert Live Performance",
                videoUrl = DEMO_VIDEO_URL_3,
                thumbnailUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500",
                duration = "7:15",
                publishedTime = "10 hours ago",
                categoryId = "entertainment"
            )
        )
    )
}
