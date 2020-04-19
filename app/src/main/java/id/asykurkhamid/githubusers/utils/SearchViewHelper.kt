package id.asykurkhamid.githubusers.utils

import id.asykurkhamid.githubusers.model.SearchModel

interface SearchViewHelper {
    fun showLoading()
    fun hideLoading()
    fun sendDataUser(user: SearchModel?)
    fun overLimit(message: String)
}