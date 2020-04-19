package id.asykurkhamid.githubusers.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.asykurkhamid.githubusers.R
import id.asykurkhamid.githubusers.adapter.SearchAdapter
import id.asykurkhamid.githubusers.model.Item
import id.asykurkhamid.githubusers.model.SearchModel
import id.asykurkhamid.githubusers.presenter.SearchPresenter
import id.asykurkhamid.githubusers.utils.SearchViewHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SearchViewHelper {

    private lateinit var textQuery: String
    private lateinit var adapter: SearchAdapter
    private var itemList = ArrayList<Item>()
    private var page = 1
    private lateinit var layoutManager: LinearLayoutManager
    private var isFirstCall = false
    private var isOverLimit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchUser.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    textQuery = newText
                    if (newText.length >= 3) {
                        if (isOverLimit){
                            Toast.makeText(this@MainActivity,R.string.over_limit,Toast.LENGTH_SHORT).show()
                        }else{
                            callItem(textQuery)
                        }
                    }
                }
                return false
            }

        })
        initAdapter()
        initScrollListener()
    }

    private fun initAdapter() {
        adapter = SearchAdapter(itemList);
        layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvSearch.layoutManager = layoutManager
        rvSearch.adapter = adapter
        rvSearch.addItemDecoration(
            DividerItemDecoration(
                this,
                RecyclerView.VERTICAL
            )
        )
    }

    private fun initScrollListener() {
        rvSearch.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var mPreviousTotal = 0
            private var isLoading = true

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = rvSearch.childCount
                val totalItemCount = rvSearch.layoutManager?.itemCount
                val layoutMgr = rvSearch.layoutManager as LinearLayoutManager
                val firstVisibleItem = layoutMgr.findFirstVisibleItemPosition()

                if (isLoading && totalItemCount != null) {
                    if (totalItemCount > mPreviousTotal) {
                        isLoading = false
                        mPreviousTotal = totalItemCount
                    }
                }
                if (totalItemCount != null) {
                    val visibleThreshold = 5
                    val res1 = totalItemCount - visibleItemCount
                    val res2 = firstVisibleItem + visibleThreshold
                    if (!isLoading && res1 <= res2) {
                        loadMoreItems()
                        isLoading = true
                    }
                }
            }
        })
    }

    private fun loadMoreItems() {
        itemList.addAll(emptyList())
        adapter.notifyItemInserted(itemList.size - 1)

        val scrollPosition = itemList.size
        adapter.notifyItemRemoved(scrollPosition)
        callItem(textQuery)
        adapter.notifyDataSetChanged()
    }

    private fun callItem(text: String) {
        SearchPresenter(this).getUser(text, page, 10)

    }


    override fun showLoading() {
        if (!isFirstCall){
            pgMain.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
        }
    }

    override fun hideLoading() {
        pgMain.visibility = View.GONE
    }

    override fun sendDataUser(user: SearchModel?) {
        if (user != null && user.items.isNotEmpty()) {
            rvSearch.visibility = View.VISIBLE
            emptyState.visibility = View.GONE

            isFirstCall = true
            page++
            itemList.addAll(user.items)
            adapter.setItemData(itemList)
            adapter.notifyDataSetChanged()
        }else{
            emptyState.visibility = View.VISIBLE
            tvEmpty.text = "system cannot find data with keywords $textQuery"
            rvSearch.visibility = View.GONE
        }
    }

    override fun overLimit(message: String) {
        adapter.isOverLimit(true)
        adapter.notifyDataSetChanged()
        isOverLimit = true
    }
}
