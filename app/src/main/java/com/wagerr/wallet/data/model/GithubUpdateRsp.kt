package com.wagerr.wallet.data.model
import com.google.gson.annotations.SerializedName

data class Asset(
        @SerializedName("url") val url: String,
        @SerializedName("id") val id: Int,
        @SerializedName("node_id") val nodeId: String,
        @SerializedName("name") val name: String,
        @SerializedName("label") val label: Any,
        @SerializedName("uploader") val uploader: Uploader,
        @SerializedName("content_type") val contentType: String,
        @SerializedName("state") val state: String,
        @SerializedName("size") val size: Int,
        @SerializedName("download_count") val downloadCount: Int,
        @SerializedName("created_at") val createdAt: String,
        @SerializedName("updated_at") val updatedAt: String,
        @SerializedName("browser_download_url") val browserDownloadUrl: String
)
data class Author(
        @SerializedName("login") val login: String,
        @SerializedName("id") val id: Int,
        @SerializedName("node_id") val nodeId: String,
        @SerializedName("avatar_url") val avatarUrl: String,
        @SerializedName("gravatar_id") val gravatarId: String,
        @SerializedName("url") val url: String,
        @SerializedName("html_url") val htmlUrl: String,
        @SerializedName("followers_url") val followersUrl: String,
        @SerializedName("following_url") val followingUrl: String,
        @SerializedName("gists_url") val gistsUrl: String,
        @SerializedName("starred_url") val starredUrl: String,
        @SerializedName("subscriptions_url") val subscriptionsUrl: String,
        @SerializedName("organizations_url") val organizationsUrl: String,
        @SerializedName("repos_url") val reposUrl: String,
        @SerializedName("events_url") val eventsUrl: String,
        @SerializedName("received_events_url") val receivedEventsUrl: String,
        @SerializedName("type") val type: String,
        @SerializedName("site_admin") val siteAdmin: Boolean
)
data class GithubUpdateRsp(
        @SerializedName("url") val url: String,
        @SerializedName("assets_url") val assetsUrl: String,
        @SerializedName("upload_url") val uploadUrl: String,
        @SerializedName("html_url") val htmlUrl: String,
        @SerializedName("id") val id: Int,
        @SerializedName("node_id") val nodeId: String,
        @SerializedName("tag_name") val tagName: String,
        @SerializedName("target_commitish") val targetCommitish: String,
        @SerializedName("name") val name: String,
        @SerializedName("draft") val draft: Boolean,
        @SerializedName("author") val author: Author,
        @SerializedName("prerelease") val prerelease: Boolean,
        @SerializedName("created_at") val createdAt: String,
        @SerializedName("published_at") val publishedAt: String,
        @SerializedName("assets") val assets: List<Asset>,
        @SerializedName("tarball_url") val tarballUrl: String,
        @SerializedName("zipball_url") val zipballUrl: String,
        @SerializedName("body") val body: String
)

data class Uploader(
        @SerializedName("login") val login: String,
        @SerializedName("id") val id: Int,
        @SerializedName("node_id") val nodeId: String,
        @SerializedName("avatar_url") val avatarUrl: String,
        @SerializedName("gravatar_id") val gravatarId: String,
        @SerializedName("url") val url: String,
        @SerializedName("html_url") val htmlUrl: String,
        @SerializedName("followers_url") val followersUrl: String,
        @SerializedName("following_url") val followingUrl: String,
        @SerializedName("gists_url") val gistsUrl: String,
        @SerializedName("starred_url") val starredUrl: String,
        @SerializedName("subscriptions_url") val subscriptionsUrl: String,
        @SerializedName("organizations_url") val organizationsUrl: String,
        @SerializedName("repos_url") val reposUrl: String,
        @SerializedName("events_url") val eventsUrl: String,
        @SerializedName("received_events_url") val receivedEventsUrl: String,
        @SerializedName("type") val type: String,
        @SerializedName("site_admin") val siteAdmin: Boolean
)