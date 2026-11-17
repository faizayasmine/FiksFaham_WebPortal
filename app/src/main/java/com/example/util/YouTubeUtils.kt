package com.example.util

/**
 * Utility functions for extracting YouTube video ID and building thumbnail URLs.
 */
object YouTubeUtils {

    /**
     * Extracts an 11-character YouTube video ID from various YouTube URL formats:
     * - https://www.youtube.com/watch?v=VIDEO_ID
     * - https://youtube.com/watch?v=VIDEO_ID&feature=share
     * - https://youtu.be/VIDEO_ID
     * - https://youtu.be/VIDEO_ID?si=xyz
     * - https://www.youtube.com/embed/VIDEO_ID
     * - Plain 11-char ID
     */
    fun extractVideoId(url: String?): String? {
        if (url.isNullOrBlank()) return null
        val trimmed = url.trim()

        // Direct 11-character ID
        if (trimmed.length == 11 && trimmed.matches(Regex("^[a-zA-Z0-9_-]{11}$"))) {
            return trimmed
        }

        // Standard regex pattern covering youtube.com and youtu.be
        val patterns = listOf(
            Regex("(?:https?:\\/\\/)?(?:www\\.)?youtube\\.com\\/(?:watch\\?(?:.*&)?v=|(?:embed|v)\\/)([a-zA-Z0-9_-]{11})"),
            Regex("(?:https?:\\/\\/)?(?:www\\.)?youtu\\.be\\/([a-zA-Z0-9_-]{11})")
        )

        for (pattern in patterns) {
            val match = pattern.find(trimmed)
            if (match != null && match.groupValues.size > 1) {
                return match.groupValues[1]
            }
        }

        return null
    }

    /**
     * Returns high quality YouTube thumbnail URL format:
     * https://img.youtube.com/vi/{VIDEO_ID}/hqdefault.jpg
     */
    fun getThumbnailUrl(videoId: String?): String? {
        if (videoId.isNullOrBlank()) return null
        return "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
    }

    /**
     * Helper to resolve thumbnail URL directly from URL or fallback ID
     */
    fun resolveThumbnailUrl(url: String?, fallbackId: String? = null): String? {
        val extractedId = extractVideoId(url) ?: extractVideoId(fallbackId)
        return getThumbnailUrl(extractedId)
    }
}
