package id.asykurkhamid.githubusers.view

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.asykurkhamid.githubusers.R
import id.asykurkhamid.githubusers.adapter.SearchAdapter
import id.asykurkhamid.githubusers.model.Item
import id.asykurkhamid.githubusers.viewmodel.UserViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var isFirstCall = true
    private var textQuery = ""
    private lateinit var adapter: SearchAdapter
    private var itemList = ArrayList<Item>()
    private var page = 1
    private lateinit var layoutManager: LinearLayoutManager
    private var isOverLimit = false
    private var viewModel = UserViewModel()
    private var newQuery = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        ).get(UserViewModel::class.java)

        initSearchListener()
        initAdapter()
        initScrollListener()

        viewModel.observeUser().observe(this, Observer { userList ->
            if (userList != null && userList.isNotEmpty()) {
                hideEmptyState()
                adapter.setItemData(userList)
                adapter.notifyDataSetChanged()
            } else {
                val errMsg = "System can't find \"${searchUser.text}\" in database"
                showEmptyState(errMsg)
            }
        })

        viewModel.observeErrMessage().observe(this, Observer { errMsg ->
            adapter.isOverLimit(true, errMsg)
            adapter.notifyDataSetChanged()
            isOverLimit = true

        })

        viewModel.isLoading().observe(this, Observer { loading ->
            if (loading) {
                showLoading(true)
            } else {
                showLoading(false)
            }
        })
    }

    private fun initSearchListener() {
        searchUser.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                viewModel.clearItemList()
                page = 1
                newQuery = true
                doQuery()
                true
            } else {
                false
            }
        }
        searchUser.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                viewModel.clearItemList()
                page = 1
                newQuery = true
                doQuery()
                true
            } else {
                false
            }
        }
    }

    private fun doQuery() {
        searchUser.text.trim().let { query ->
            textQuery = query.toString()
            itemList.clear()
            if (isOverLimit) {
                val errMsg = resources.getString(R.string.over_limit)
                showEmptyState(errMsg)
            } else {
                viewModel.clearItemList()
                callItem(query.toString())
                isFirstCall = false

            }
        }

    }

    private fun showLoading(isLoading: Boolean) = if (newQuery && isLoading) {
        this.pgMain.visibility = View.VISIBLE
        this.rvSearch.visibility = View.GONE
    } else {
        this.pgMain.visibility = View.GONE
        this.rvSearch.visibility = View.VISIBLE
    }

    private fun showEmptyState(message: String) {
        this.pgMain.visibility = View.GONE
        this.rvSearch.visibility = View.GONE
        this.emptyState.visibility = View.VISIBLE
        this.tvEmpty.text = message
    }

    private fun hideEmptyState() {
        pgMain.visibility = View.GONE
        rvSearch.visibility = View.VISIBLE
        emptyState.visibility = View.GONE
    }

    private fun initAdapter() {
        adapter = SearchAdapter()
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
                val layoutMgr = rvSearch.layoutManager as LinearLayoutManager

                val visibleItemCount = layoutMgr.childCount
                val totalItemCount = layoutMgr.itemCount
                val firstVisibleItem = layoutMgr.findFirstVisibleItemPosition()

                if (isLoading) {
                    if (newQuery){
                        mPreviousTotal = 0
                    }
                    if (totalItemCount > mPreviousTotal) {
                        isLoading = false
                        mPreviousTotal = totalItemCount
                    }
                }

                val visibleThreshold = 5
                val res1 = totalItemCount - visibleItemCount
                val res2 = firstVisibleItem + visibleThreshold
                if (!isLoading && res1 <= res2) {
                    newQuery = false
                    loadMoreItems()
                    isLoading = true
                }

            }
        })
    }


    private fun loadMoreItems() {
        page++
        itemList.addAll(emptyList())
        adapter.notifyItemInserted(itemList.size - 1)
        val scrollPosition = itemList.size
        adapter.notifyItemRemoved(scrollPosition)
//        isNewKeyword = false
        callItem(textQuery)
        adapter.notifyDataSetChanged()
    }

    private fun callItem(text: String) {
//        isLoadingSearch = false
        viewModel.callUser(text, page, 10, newQuery)
    }



}
