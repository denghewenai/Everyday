package cn.gdut.android.everyday.ui.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import android.view.View
import cn.bmob.v3.BmobUser
import cn.gdut.android.everyday.R
import kotlinx.android.synthetic.main.activity_splash.*

/**
 * Created by denghewen on 2018/4/30 0030.
 */
class SplashActivity : AppCompatActivity() {

    private val countDownTimer = object : CountDownTimer(3400, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            btnSplashSkip.text = "跳过(${millisUntilFinished / 1000}s)"
        }

        override fun onFinish() {
            btnSplashSkip.text = "跳过(0s)"
            gotoLoginOrMainActivity()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        showSplash()
        btnSplashSkip.setOnClickListener {
            gotoLoginOrMainActivity()
        }
    }

    private fun showSplash() {
        lottieAnimationViewSplash.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                startClock()
                lottieAnimationViewSplash.progress = 0f
            }
        })
        lottieAnimationViewSplash.playAnimation()
    }

    private fun startClock() {
        btnSplashSkip.visibility = View.VISIBLE
        countDownTimer.start()
    }

    private fun gotoLoginOrMainActivity() {
        countDownTimer.cancel()
        if (BmobUser.getCurrentUser() == null) {
            gotoLoginActivity()
        } else {
            gotoMainActivity()
        }
    }

    private fun gotoMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun gotoLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }
}
