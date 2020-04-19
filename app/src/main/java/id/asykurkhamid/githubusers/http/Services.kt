package id.asykurkhamid.githubusers.http

import id.asykurkhamid.githubusers.model.SearchModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface Services {
    @GET("users")
    fun callSearch(
        @Query("q") keyWord: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Call<SearchModel>

}