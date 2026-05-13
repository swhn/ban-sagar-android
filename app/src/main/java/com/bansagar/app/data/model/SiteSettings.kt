package com.bansagar.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SiteSettings(
    @SerialName("allow_registrations") val allowRegistrations: Boolean = true,
    @SerialName("require_approval") val requireApproval: Boolean = true,
    @SerialName("max_submissions_per_day") val maxSubmissionsPerDay: Int = 5,
    @SerialName("allow_nsfw") val allowNsfw: Boolean = true,
    @SerialName("show_ranking") val showRanking: Boolean = true,
    @SerialName("site_announcement") val siteAnnouncement: String? = null,
)
