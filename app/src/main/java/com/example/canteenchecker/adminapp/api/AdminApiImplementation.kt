package com.example.canteenchecker.adminapp.api

import android.util.Log
import com.example.canteenchecker.adminapp.core.*
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import retrofit2.http.*
import java.io.IOException
import java.util.*

object AdminApiFactory {
    fun createAdminAPi(): AdminApi =
        AdminApiImplementation("https://moc5.projekte.fh-hagenberg.at/CanteenChecker/api/admin/")
}

private class AdminApiImplementation(apiBaseUrl: String) : AdminApi {
    private val retrofit =
        Retrofit.Builder().baseUrl(apiBaseUrl).addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create()).build()

    override suspend fun authenticate(userName: String, password: String): Result<String> =
        apiCall {
            postAuthenticate(userName, password)
        }

    override suspend fun getCanteen(authenticationToken: String): Result<Canteen> = apiCall {
        getCanteen(authenticationToken)
    }.convert { Canteen(id, name, address, phoneNumber, website, dish, dishPrice, waitingTime) }

    override suspend fun getReviewStatistics(authenticationToken: String): Result<ReviewStatistics> =
        apiCall {
            getReviewStatistics(authenticationToken)
        }.convert {
            ReviewStatistics(
                ratingsOne,
                ratingsTwo,
                ratingsThree,
                ratingsFour,
                ratingsFive
            )
        }

    override suspend fun updateCanteen(
        authenticationToken: String,
        editCanteen: EditCanteen
    ): Result<Unit> = apiCall {
        putCanteenData(
            authenticationToken,
            editCanteen.name,
            editCanteen.address,
            editCanteen.website,
            editCanteen.phoneNumber
        )
    }.convert { }

    override suspend fun updateDish(authenticationToken: String, dish: EditDish): Result<Unit> =
        apiCall {
            putCanteenDish(authenticationToken, dish.name, dish.price)
        }.convert { }

    override suspend fun updateWaitingTime(
        authenticationToken: String,
        timeMinutes: Int
    ): Result<Unit> = apiCall {
        putCanteenWaitingTime(authenticationToken, timeMinutes)
    }.convert { }

    override suspend fun getReviews(authenticationToken: String): Result<List<Review>> = apiCall {
        getCanteenReview(authenticationToken)
    }.convertEach {
        Review(
            id,
            creationDate,
            creator,
            rating,
            remark
        )
    }

    override suspend fun deleteReview(authenticationToken: String, reviewId: String): Result<Unit> =
        apiCall {
            deleteCanteenReview(authenticationToken, reviewId)
        }.convert { }

    private interface Api {
        @POST("authenticate")
        suspend fun postAuthenticate(
            @Query("userName") userName: String, @Query("password") password: String
        ): String

        @GET("canteen")
        suspend fun getCanteen(@Header("Authorization") authenticationToken: String): ApiCanteen

        @GET("canteen/review-statistics")
        suspend fun getReviewStatistics(@Header("Authorization") authenticationToken: String): ApiReviewStatistics

        @PUT("canteen/data")
        suspend fun putCanteenData(
            @Header("Authorization") authenticationToken: String,
            @Query("name") name: String,
            @Query("address") address: String,
            @Query("website") website: String,
            @Query("phoneNumber") phoneNumber: String
        ): Response<Unit>

        @PUT("canteen/dish")
        suspend fun putCanteenDish(
            @Header("Authorization") authenticationToken: String,
            @Query("dish") name: String,
            @Query("dishPrice") address: Float
        ): Response<Unit>

        @PUT("canteen/waiting-time")
        suspend fun putCanteenWaitingTime(
            @Header("Authorization") authenticationToken: String,
            @Query("waitingTime") waitingTime: Int
        ): Response<Unit>

        @GET("canteen/reviews")
        suspend fun getCanteenReview(
            @Header("Authorization") authenticationToken: String
        ): List<ApiReview>

        @DELETE("canteen/reviews/{reviewId}")
        suspend fun deleteCanteenReview(
            @Header("Authorization") authenticationToken: String,
            @Path("reviewId") reviewId: String
        )
    }

    private class ApiCanteen(
        val id: String,
        val name: String,
        val address: String,
        val phoneNumber: String,
        val website: String,
        val dish: String,
        val dishPrice: Float,
        val waitingTime: Int
    )

    private class ApiReviewStatistics(
        val ratingsOne: Int,
        val ratingsTwo: Int,
        val ratingsThree: Int,
        val ratingsFour: Int,
        val ratingsFive: Int,
    )
    
    private class ApiReview(
        val id: String,
        val creationDate: Date,
        val creator: String,
        val rating: Float,
        val remark: String
    )

    private inline fun <T> apiCall(call: Api.() -> T): Result<T> = try {
        Result.success(call(retrofit.create()))
    } catch (ex: HttpException) {
        Result.failure(ex)
    } catch (ex: IOException) {
        Result.failure(ex)
    }.onFailure {
        Log.e(TAG, "API call failed", it)
    }

    companion object {
        private val TAG = this::class.simpleName
    }
}

private inline fun <T, R> Result<List<T>>.convertEach(map: T.() -> R): Result<List<R>> =
    this.map { it.map(map) }


private inline fun <T, R> Result<T>.convert(map: T.() -> R): Result<R> = this.map(map)