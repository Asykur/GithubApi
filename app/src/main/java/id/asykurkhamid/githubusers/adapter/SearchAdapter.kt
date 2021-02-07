package id.asykurkhamid.githubusers.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import id.asykurkhamid.githubusers.R
import id.asykurkhamid.githubusers.model.Item
import kotlinx.android.synthetic.main.item_loading.view.*
import kotlinx.android.synthetic.main.item_search.view.*

class SearchAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var errMsg = ""
    private val viewTypeItem = 1
    private val viewTypeLoading = 2
    private var isOverLimitRequest = false
    private var itemList = ArrayList<Item>()

    override fun getItemViewType(position: Int): Int {
        return if (position == itemList.size) {
            viewTypeLoading
        } else {
            viewTypeItem
        }
    }

    fun setItemData(newItemList: ArrayList<Item>) {
        this.itemList = newItemList
    }

    fun isOverLimit(over : Boolean, msg: String){
        this.errMsg = msg
        isOverLimitRequest = over
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == viewTypeItem) {
            val viewItem =
                LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
            VH(viewItem)
        } else {
            val viewLoading =
                LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
            VHLoading(viewLoading)
        }
    }

    override fun getItemCount(): Int {
        return if (itemList.isEmpty()){
            0
        }else{
            itemList.size + 1
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is VH){
            holder.onBind(itemList[position])

        }else if (holder is VHLoading){
            holder.onBindLoading(isOverLimitRequest, errMsg)
        }
    }


    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun onBind(item: Item) {
            val circleTransform = RequestOptions.circleCropTransform()
            Glide.with(itemView.imgUser.context)
                .load(item.avatar_url)
                .apply(circleTransform)
                .into(itemView.imgUser)
            itemView.tvName.text = item.login
        }
    }

    class VHLoading(itemView: View):RecyclerView.ViewHolder(itemView){
        fun onBindLoading(overLimit: Boolean, msg: String){
            if (overLimit){
                itemView.pgLoadMore.visibility = View.GONE
                itemView.tvWarning.visibility = View.VISIBLE
                itemView.tvWarning.text = msg
            }else{
                itemView.pgLoadMore.visibility = View.VISIBLE
                itemView.tvWarning.visibility = View.GONE
            }
        }
    }

}