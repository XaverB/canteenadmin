package com.example.canteenchecker.adminapp.api

import com.example.canteenchecker.adminapp.core.*

interface AdminApi {
    suspend fun authenticate(userName: String, password: String): Result<String>
    suspend fun getCanteen(authenticationToken: String): Result<Canteen>
    suspend fun getReviewStatistics(authenticationToken: String): Result<ReviewStatistics>
    suspend fun updateCanteen(authenticationToken: String, editCanteen: EditCanteen): Result<Unit>
    suspend fun updateDish(authenticationToken: String, dish: EditDish): Result<Unit>
    suspend fun updateWaitingTime(authenticationToken: String, timeMinutes: Int): Result<Unit>
    suspend fun getReviews(authenticationToken: String): Result<List<Review>>
    suspend fun deleteReview(authenticationToken: String, reviewId: String): Result<Unit>
}
