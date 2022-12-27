package com.example.canteenchecker.adminapp.core

import java.util.Date

class Canteen(
    val id: String,
    val name: String,
    val address: String,
    val phoneNumber: String,
    val website: String,
    val dish: String,
    val dishPrice: Float,
    val waitingTime: Int
)

class EditCanteen(
    val name: String,
    val address: String,
    val website: String,
    val phoneNumber: String
)

class EditDish(
    val name: String,
    val price: Float
)

class ReviewStatistics(
    val ratingsOne: Int,
    val ratingsTwo: Int,
    val ratingsThree: Int,
    val ratingsFour: Int,
    val ratingsFive: Int,
) {
    val totalRatings = ratingsOne + ratingsTwo + ratingsThree + ratingsFour + ratingsFive
    val averageRating =
        if (totalRatings == 0) 0F
        else (ratingsOne * 1 + ratingsTwo * 2 + ratingsThree * 3 + ratingsFour * 4 + ratingsFive * 5) / totalRatings.toFloat()
}

class Review(
    val id: String,
    val creationDate: Date,
    val creator: String,
    val rating: Float,
    val remark: String
)