package com.example.data.model

data class UserPointsBreakdown(
    val pretestPoints: Int = 0,
    val posttestPoints: Int = 0,
    val quizPoints: Map<Int, Int> = emptyMap(), // moduleId -> points (0, 60, or 100)
    val capstonePoints: Int = 0,
    val totalPoints: Int = 0,
    val starCount: Int = 0, // 0..5
    val starTierTitle: String = "Mulai Belajar",
    val maxPoints: Int = 300 + (quizPoints.size.coerceAtLeast(1) * 100)
) {
    val totalQuizPoints: Int
        get() = quizPoints.values.sum()

    val totalTestPoints: Int
        get() = pretestPoints + posttestPoints
}

data class LeaderboardEntry(
    val rank: Int,
    val userId: Int,
    val name: String,
    val institution: String,
    val points: Int,
    val stars: Int,
    val starTier: String,
    val isCurrentUser: Boolean = false,
    val badgeLabel: String? = null,
    val avatarUrl: String = ""
)

data class CoinLeaderboardEntryDoc(
    val uid: String = "",
    val username: String = "",
    val avatarUrl: String = "",
    val koin: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

enum class LeaderboardPeriod(val label: String, val subtitle: String) {
    DAILY("Harian", "Peringkat progres belajar hari ini"),
    WEEKLY("Mingguan", "Peringkat aktif minggu ini"),
    MONTHLY("Bulanan", "Peringkat akumulasi bulan ini"),
    ALL_TIME("Semua", "Peringkat keseluruhan")
}

data class FirestoreLeaderboardDoc(
    val uid: String = "",
    val name: String = "",
    val institution: String = "",
    val totalPoints: Int = 0,
    val dailyPoints: Int = 0,
    val weeklyPoints: Int = 0,
    val monthlyPoints: Int = 0,
    val stars: Int = 0,
    val starTier: String = "",
    val badgeLabel: String? = null,
    val lastDailyTimestamp: Long = 0L,
    val lastWeeklyTimestamp: Long = 0L,
    val lastMonthlyTimestamp: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)

data class PointEvent(
    val points: Int,
    val timestamp: Long
)

data class PeriodPointsSummary(
    val totalPoints: Int,
    val dailyPoints: Int,
    val weeklyPoints: Int,
    val monthlyPoints: Int,
    val lastDailyTimestamp: Long,
    val lastWeeklyTimestamp: Long,
    val lastMonthlyTimestamp: Long
)
