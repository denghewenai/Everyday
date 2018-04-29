package cn.gdut.android.everyday.ui.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cn.gdut.android.everyday.R

/**
 * Created by Administrator on 2018/4/27 0027.
 */
class PublishActivity : AppCompatActivity() {

    companion object {
        private val ARG_TAKEN_PHOTO_URI = "arg_taken_photo_uri"
        fun openWithPhotoUri(openingActivity: Activity, photoUri: Uri) {
            val intent = Intent(openingActivity, PublishActivity::class.java)
            intent.putExtra(ARG_TAKEN_PHOTO_URI, photoUri)
            openingActivity.startActivity(intent)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish)
    }

}