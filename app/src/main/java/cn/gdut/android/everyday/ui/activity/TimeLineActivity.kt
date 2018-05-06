package cn.gdut.android.everyday.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.BmobUser
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import cn.gdut.android.everyday.R
import cn.gdut.android.everyday.adapter.TimeLineAdapter
import cn.gdut.android.everyday.models.Note
import cn.gdut.android.everyday.utils.TimeLineItemDecoration
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieComposition
import com.github.nuptboyzhb.lib.SuperSwipeRefreshLayout
import kotlinx.android.synthetic.main.activity_time_line.*
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.toast


/**
 * Created by denghewen on 2018/5/1 0001.
 */
class TimeLineActivity : AppCompatActivity() {

    private lateinit var mTimeLineAdapter: TimeLineAdapter
    private lateinit var lottieAnimationViewRefresh: LottieAnimationView
    private lateinit var lottieAnimationViewMore: LottieAnimationView
    private var skip: Int = 0
    private var sortType = CREATEORDER
    private var folderId = "*"

    companion object {
        val ACTION_SHOW_LOADING_ITEM = "action_show_loading_item"
        val FOLDER_ID = "folderId"
        val CREATEORDER = "createdAt"
        val CREATEORDERDESC = "-createdAt"
        val UPDATEORDER = "updatedAt"
        val UPDATEORDERDESC = "-updatedAt"
        val ONE_REQUEST_LIMIT = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_line)

        folderId = intent.getStringExtra(FOLDER_ID)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        recyclerViewTimeLine.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        lottieAnimationViewRefresh = layoutInflater.inflate(R.layout.lottie_animation_view, null) as LottieAnimationView
        lottieAnimationViewMore = layoutInflater.inflate(R.layout.lottie_animation_view, null) as LottieAnimationView
        initSwipeRefreshLayout()
        recyclerViewTimeLine.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        initViews()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_time_line, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        //When home is clicked
            android.R.id.home -> {
                onBackPressed()
                return true
            }

            R.id.menu_sort_create_far -> {
//                mAlbumPresenter.setAlbumSort(1)
//                mAlbumPresenter.sortData()
                sortType = CREATEORDER
                item.isChecked = true
            }
            R.id.menu_sort_create_close -> {
                sortType = CREATEORDERDESC
                item.isChecked = true
            }
            R.id.menu_sort_edit_far -> {
                sortType = UPDATEORDER
                item.isChecked = true
            }
            R.id.menu_sort_edit_close -> {
                sortType = UPDATEORDERDESC
                item.isChecked = true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initSwipeRefreshLayout() {
        swipeRefreshLayout.setHeaderView(createHeaderView())
        swipeRefreshLayout.setOnPullRefreshListener(object : SuperSwipeRefreshLayout.OnPullRefreshListener {
            override fun onPullEnable(enable: Boolean) {
                lottieAnimationViewRefresh.visibility = View.VISIBLE
                lottieAnimationViewRefresh.repeatCount = Integer.MAX_VALUE
                lottieAnimationViewRefresh.playAnimation()
            }

            override fun onPullDistance(distance: Int) {

            }

            override fun onRefresh() {
                refreshData(object : FindListener<Note>() {
                    override fun done(noteList: MutableList<Note>, e: BmobException?) {
                        swipeRefreshLayout.isRefreshing = false
                        lottieAnimationViewRefresh.progress = 1f
                        lottieAnimationViewRefresh.cancelAnimation()
                        lottieAnimationViewRefresh.visibility = View.GONE
                        if (e == null) {
                            toast("查询成功：共" + noteList.size + "条数据。")
                            skip = noteList.size
                            mTimeLineAdapter.setNoteList(noteList)
                        } else {
                            Log.i("info", "失败：" + e.message + "," + e.errorCode)
                        }
                    }
                })
            }
        })

        swipeRefreshLayout.setFooterView(createFootView())
        swipeRefreshLayout.setOnPushLoadMoreListener(object : SuperSwipeRefreshLayout.OnPushLoadMoreListener {
            override fun onPushDistance(distance: Int) {
                lottieAnimationViewMore.visibility = View.VISIBLE
                lottieAnimationViewMore.repeatCount = Integer.MAX_VALUE
                lottieAnimationViewMore.playAnimation()
            }

            override fun onPushEnable(enable: Boolean) {
            }

            override fun onLoadMore() {
                loadMoreData(object : FindListener<Note>() {
                    override fun done(noteList: MutableList<Note>, e: BmobException?) {
                        swipeRefreshLayout.setLoadMore(false)
                        lottieAnimationViewMore.progress = 1f
                        lottieAnimationViewMore.cancelAnimation()
                        lottieAnimationViewMore.visibility = View.GONE
                        if (e == null) {
                            toast("查询成功：共" + noteList.size + "条数据。")
                            skip += noteList.size
                            mTimeLineAdapter.addNoteList(noteList)
                        } else {
                            Log.i("info", "失败：" + e.message + "," + e.errorCode)
                        }
                    }
                })
            }
        })
    }

    private fun createHeaderView(): View? {
        LottieComposition.Factory.fromRawFile(this, R.raw.refresh) { composition ->
            if (composition != null) {
                lottieAnimationViewRefresh.setComposition(composition)
            }
        }
        return lottieAnimationViewRefresh
    }

    private fun createFootView(): View? {
        LottieComposition.Factory.fromRawFile(this, R.raw.loading_more) { composition ->
            if (composition != null) {
                lottieAnimationViewMore.setComposition(composition)
            }
        }
        return lottieAnimationViewMore
    }

    private fun initViews() {
        refreshData(object : FindListener<Note>() {
            override fun done(noteList: MutableList<Note>, e: BmobException?) {
                if (e == null) {
                    toast("查询成功：共" + noteList.size + "条数据。")
                    skip = noteList.size
                    mTimeLineAdapter.setNoteList(noteList)
                } else {
                    Log.i("info", "失败：" + e.message + "," + e.errorCode)
                }
            }

        })
        val dataList = ArrayList<Note>()
        mTimeLineAdapter = TimeLineAdapter(this, dataList) { note, imageView ->
            //            toast("点击" + note)
            transition(note, imageView)
        }
        recyclerViewTimeLine.adapter = mTimeLineAdapter
        recyclerViewTimeLine.addItemDecoration(TimeLineItemDecoration(this))
    }

    private fun loadMoreData(findListener: FindListener<Note>) {
        val query = BmobQuery<Note>()
        query.addWhereEqualTo("userId", BmobUser.getCurrentUser().objectId)
        if("*" != folderId) {
            query.addWhereEqualTo("folderId", folderId)
        }
        query.order(sortType)
        query.setSkip(skip)
        query.setLimit(ONE_REQUEST_LIMIT)
        query.findObjects(findListener)
    }

    private fun refreshData(findListener: FindListener<Note>) {
        val query = BmobQuery<Note>()
        query.addWhereEqualTo("userId", BmobUser.getCurrentUser().objectId)
        if("*" != folderId) {
            query.addWhereEqualTo("folderId", folderId)
        }
        query.order(sortType)
        query.setLimit(ONE_REQUEST_LIMIT)
        query.findObjects(findListener)
    }

    private fun transition(note: Note, view: View) {
        val intent = Intent(this@TimeLineActivity, ActivityTransitionToActivity::class.java)
        intent.putExtra(ActivityTransitionToActivity.PHOTO_URL, note.url)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@TimeLineActivity, view, getString(R.string.transition_test))
        startActivity(intent, options.toBundle())
    }

}