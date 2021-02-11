package id.asykurkhamid.githubusers.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.asykurkhamid.githubusers.http.ServiceFactory
import id.asykurkhamid.githubusers.model.Item
import id.asykurkhamid.githubusers.model.SearchModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserViewModel : ViewModel(){

    private var errMessage = MutableLiveData<String>()

    private var itemListLiveData = MutableLiveData<ArrayList<Item>>()
    private var itemList = ArrayList<Item>()
    private var isLoading = MutableLiveData<Boolean>()

    fun callUser(keyword: String, page: Int, limit:Int, isNewKeyword: Boolean){
        val api = ServiceFactory.instanceServices
        val call = api.callSearch(keyword,page,limit)

        isLoading.value = true
        call.enqueue(object : Callback<SearchModel> {
            override fun onFailure(call: Call<SearchModel>, t: Throwable) {
                isLoading.postValue(false)
                errMessage.postValue(t.message.toString())
            }

            override fun onResponse(call: Call<SearchModel>, response: Response<SearchModel>) {
                isLoading.postValue(false)
                if (response.isSuccessful && response.body() != null){

                    if(itemListLiveData.value != null && isNewKeyword){
                        itemListLiveData.postValue(null)
                    } else{
                        itemList.addAll(response.body()!!.items)
                        itemListLiveData.postValue(itemList)
                    }


                }else if (response.code() == 403){
                    errMessage.postValue("API rate limit exceeded")
                }
            }
        })
    }

    fun observeUser(): LiveData<ArrayList<Item>> {
        return itemListLiveData
    }

    fun observeErrMessage(): LiveData<String>{
        return errMessage
    }

    fun clearItemList() {
        itemListLiveData.value?.clear()
    }

    fun isLoading(): LiveData<Boolean>{
        return isLoading
    }
}