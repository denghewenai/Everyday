package cn.gdut.android.everyday.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.BmobUser
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import cn.gdut.android.everyday.R
import cn.gdut.android.everyday.adapter.TimeLineAdapter
import cn.gdut.android.everyday.models.Note
import cn.gdut.android.everyday.utils.TimeLineItemDecoration
import com.airbnb.lottie.LottieComposition
import kotlinx.android.synthetic.main.activity_time_line.*
import kotlinx.android.synthetic.main.lottie_animation_view.*
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.toast


/**
 * Created by denghewen on 2018/5/1 0001.
 */
class TimeLineActivity : AppCompatActivity() {

    private lateinit var mTimeLineAdapter: TimeLineAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_line)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        recyclerViewTimeLine.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        swipeRefreshLayout.setHeaderView(createHeaderView())
        recyclerViewTimeLine.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        initViews()
    }

    private fun createHeaderView(): View? {
        LottieComposition.Factory.fromRawFile(this, R.raw.loading) { composition ->
            if (composition != null) {
                lottieAnimationView.setComposition(composition)
            }
        }
        return lottieAnimationView
    }


    private fun initViews() {
        initData()
        val dataList = ArrayList<Note>()
        mTimeLineAdapter = TimeLineAdapter(this, dataList) { note ->
            toast("点击" + note)
        }
        recyclerViewTimeLine.adapter = mTimeLineAdapter
        recyclerViewTimeLine.addItemDecoration(TimeLineItemDecoration(this))
    }

    private fun initData() {
        val query = BmobQuery<Note>()
        query.addWhereEqualTo("userId", BmobUser.getCurrentUser().objectId)
        query.findObjects(object : FindListener<Note>() {
            override fun done(noteList: List<Note>, e: BmobException?) {
                if (e == null) {
                    toast("查询成功：共" + noteList.size + "条数据。")
                    mTimeLineAdapter.setNoteList(noteList)
                } else {
                    Log.i("info", "失败：" + e.message + "," + e.errorCode)
                }
            }

        })
    }

}