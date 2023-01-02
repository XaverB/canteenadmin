package com.example.canteenchecker.adminapp.core

import java.util.Date

data class Canteen(
    val id: String,
    val name: String,
    val address: String,
    val phoneNumber: String,
    val website: String,
    val dish: String,
    val dishPrice: Double,
    val waitingTime: Int
) : java.io.Serializable

class EditCanteen(
    val name: String,
    val address: String,
    val website: String,
    val phoneNumber: String
) : java.io.Serializable

class EditDish(
    val name: String,
    val price: Double
) : java.io.Serializable

class ReviewStatistics(
    val countOneStar: Int,
    val countTwoStars: Int,
    val countThreeStars: Int,
    val countFourStars: Int,
    val countFiveStars: Int,
) {
    val totalRatings = countOneStar + countTwoStars + countThreeStars + countFourStars + countFiveStars
    val averageRating =
        if (totalRatings == 0) 0F
        else (countOneStar * 1 + countTwoStars * 2 + countThreeStars * 3 + countFourStars * 4 + countFiveStars * 5) / totalRatings.toFloat()
}

class Review(
    val id: String,
    val creationDate: Date,
    val creator: String,
    val rating: Float,
    val remark: String
)