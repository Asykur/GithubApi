package id.asykurkhamid.githubusers.presenter

import id.asykurkhamid.githubusers.http.ServiceFactory
import id.asykurkhamid.githubusers.model.SearchModel
import id.asykurkhamid.githubusers.utils.SearchViewHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchPresenter(private val view: SearchViewHelper) {

    fun getUser(keyword: String,page: Int, limit:Int){
        val api = ServiceFactory().instanceServices()
        val call = api.callSearch(keyword,page,limit)
        view.showLoading()
        call.enqueue(object :Callback<SearchModel>{
            override fun onFailure(call: Call<SearchModel>, t: Throwable) {
                view.hideLoading()
            }

            override fun onResponse(call: Call<SearchModel>, response: Response<SearchModel>) {
                view.hideLoading()
                if (response.isSuccessful){
                    view.sendDataUser(response.body())
                }else if (response.code() == 403){
                    view.overLimit(response.message())
                }
            }

        })
    }
}