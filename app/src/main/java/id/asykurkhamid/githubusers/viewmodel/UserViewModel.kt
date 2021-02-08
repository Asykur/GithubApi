package id.asykurkhamid.githubusers.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import id.asykurkhamid.githubusers.http.ServiceFactory
import id.asykurkhamid.githubusers.model.ErrorModel
import id.asykurkhamid.githubusers.model.SearchModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserViewModel : ViewModel(){

    private var userLiveData = MutableLiveData<SearchModel>()
    private var errMessage = MutableLiveData<String>()

    fun callUser(keyword: String, page: Int, limit:Int){
        val api = ServiceFactory.instanceServices
        val call = api.callSearch(keyword,page,limit)

        call.enqueue(object : Callback<SearchModel> {
            override fun onFailure(call: Call<SearchModel>, t: Throwable) {
                errMessage.postValue(t.message.toString())
            }

            override fun onResponse(call: Call<SearchModel>, response: Response<SearchModel>) {
                if (response.isSuccessful && response.body() != null){
                    userLiveData.postValue(response.body())
                }else if (response.code() == 403){
                    errMessage.postValue("API rate limit exceeded")
                }
            }
        })
    }

    fun observeUser() : LiveData<SearchModel> {
        return userLiveData
    }

    fun observeErrMessage(): LiveData<String>{
        return errMessage
    }
}