package com.decagon.n26_p3_usecase.features.wallpaper.model

data class WallPaperData(
    val blur_hash: String,
    val color: String,
    val created_at: String,
    val current_user_collections: List<Any>,
    val description: String,
    val height: Int,
    val id: String,
    val liked_by_user: Boolean,
    val likes: Int,
    val links: Links,
    val urls: Urls,
    val user: User,
    val width: Int
)

fun WallPaperData.toWallPaperDataSafe() = WallPaperDataSafe(
    id = id,
    color = color,
    description = description,
    url = urls.small,
)