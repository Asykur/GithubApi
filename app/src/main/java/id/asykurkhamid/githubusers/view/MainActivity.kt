package id.asykurkhamid.githubusers.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import id.asykurkhamid.githubusers.R
import id.asykurkhamid.githubusers.adapter.SearchAdapter
import id.asykurkhamid.githubusers.model.Item
import id.asykurkhamid.githubusers.viewmodel.UserViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var textQuery = ""
    private lateinit var adapter: SearchAdapter
    private var itemList = ArrayList<Item>()
    private var page = 1
    private lateinit var layoutManager: LinearLayoutManager
    private var isLoadingSearch = false
    private var isOverLimit = false
    private var viewModel = UserViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        ).get(UserViewModel::class.java)

        searchUser.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    isLoadingSearch = true
                    textQuery = query
                    itemList.clear()
                    if (isOverLimit) {
                        Snackbar.make(rvSearch, R.string.over_limit, Snackbar.LENGTH_LONG)
                            .show()
                    } else {
                        callItem(query)
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })
        initAdapter()
        initScrollListener()

        viewModel.observeUser().observe(this, Observer { userList ->
            showLoading(false)
            if (userList != null && userList.items.isNotEmpty()) {
                rvSearch.visibility = View.VISIBLE
                emptyState.visibility = View.GONE

                itemList.addAll(userList.items)
                adapter.setItemData(itemList)
                adapter.notifyDataSetChanged()
            } else {
                emptyState.visibility = View.VISIBLE
                tvEmpty.text = resources.getString(R.string.empty_search)
                rvSearch.visibility = View.GONE
            }
        })

        viewModel.observeErrMessage().observe(this, Observer { errMsg ->
                adapter.isOverLimit(true, errMsg)
                adapter.notifyDataSetChanged()
                isOverLimit = true

        })
    }

    private fun initAdapter() {
        adapter = SearchAdapter();
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
        showLoading(true)
        isLoadingSearch = false
        viewModel.callUser(text, page, 10)
        page++
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoadingSearch && isLoading) {
            pgMain.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
        } else {
            pgMain.visibility = View.GONE
        }
    }

}
