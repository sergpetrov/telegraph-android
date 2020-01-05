package com.telex.model.source.remote.api

import com.telex.model.source.remote.data.ImageData
import com.telex.model.source.remote.data.PageData
import com.telex.model.source.remote.data.PageListData
import com.telex.model.source.remote.data.PageViewsData
import com.telex.model.source.remote.data.ResponseData
import com.telex.model.source.remote.data.UserData
import com.telex.utils.Constants.PAGE_LIST_LIMIT
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface RestApi {

    @POST
    fun login(@Url url: String): Single<ResponseBody>

    @POST("/revokeAccessToken")
    fun revokeAccessToken(): Single<ResponseData<UserData>>

    @GET("/getAccountInfo")
    fun getAccountInfo(@Query("fields") fields: String = "[\"short_name\",\"author_name\",\"author_url\",\"page_count\"]"): Single<ResponseData<UserData>>

    @POST("/editAccountInfo")
    fun editAccountInfo(
        @Query("short_name") shortName: String,
        @Query("author_name") authorName: String?,
        @Query("author_url") authorUrl: String?
    ): Single<ResponseData<UserData>>

    @GET("/getPageList")
    fun getPages(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int = PAGE_LIST_LIMIT
    ): Single<ResponseData<PageListData>>

    @GET("/getPage/{path}")
    fun getPage(
        @Path("path") path: String,
        @Query("return_content") returnContent: Boolean = true
    ): Single<ResponseData<PageData>>

    @GET("/getViews/{path}")
    fun getPageViews(
        @Path("path") path: String,
        @Query("year") year: Int,
        @Query("month") month: Int?,
        @Query("day") day: Int?
    ): Single<ResponseData<PageViewsData>>

    @Multipart
    @POST
    fun uploadImage(@Url url: String, @Part body: MultipartBody.Part): Observable<List<ImageData>>

    @FormUrlEncoded
    @POST("/editPage/{path}")
    fun editPage(
        @Path("path") path: String,
        @Field("title") title: String,
        @Field("author_name") authorName: String?,
        @Field("author_url") authorUrl: String?,
        @Field("content", encoded = false) content: String,
        @Field("return_content") returnContent: Boolean = true
    ): Single<ResponseData<PageData>>

    @FormUrlEncoded
    @POST("/createPage")
    fun createPage(
        @Field("title") title: String,
        @Field("author_name") authorName: String?,
        @Field("author_url") authorUrl: String?,
        @Field("content", encoded = false) content: String,
        @Field("return_content") returnContent: Boolean = true
    ): Single<ResponseData<PageData>>

    @POST
    fun savePage(@Url url: String, @Body body: MultipartBody): Single<ResponseData<PageData>>
}
