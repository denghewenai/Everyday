package cn.gdut.android.everyday.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cn.gdut.android.everyday.R
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_transition_to.*

/**
 * Created by denghewen on 2018/5/3 0003.
 */
class ActivityTransitionToActivity : AppCompatActivity() {
    companion object {
        val PHOTO_URL = "photo_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra(PHOTO_URL)

        setContentView(R.layout.activity_transition_to)
        Picasso.Builder(this)
                .build()
                .load(url)
                .into(photoView, object : Callback {
                    override fun onSuccess() {
//                        photoView.animate()
//                                .scaleX(1f)
//                                .scaleY(1f)
//                                .setInterpolator(OvershootInterpolator())
//                                .setDuration(400)
//                                .setStartDelay(200)
//                                .start()
                    }

                    override fun onError() {
                        photoView.setImageResource(R.mipmap.ic_launcher)
                    }
                })
    }

}