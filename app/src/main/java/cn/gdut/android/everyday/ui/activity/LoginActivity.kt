package cn.gdut.android.everyday.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import cn.bmob.v3.BmobUser
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.SaveListener
import cn.gdut.android.everyday.R
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

/**
 * Created by denghewen on 2018/3/22 0022.
 */
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        init()
    }

    private fun init() {
        btnLogin.setOnClickListener {
            Log.d(TAG, "onClick: attempting to log in.")

            val email = etLoginInputEmail.text.toString()
            val password = etLoginInputPassword.text.toString()

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(applicationContext, "所有信息都必须填写！", Toast.LENGTH_SHORT).show()
            } else {
                progressBarLogin.visibility = View.VISIBLE
                tvPleaseWait.visibility = View.VISIBLE

                val user = BmobUser()
                user.email = email
                user.setPassword(password)

                user.login(object : SaveListener<BmobUser>() {
                    override fun done(s: BmobUser, e: BmobException?) {
                        if (e == null) {
                            progressBarLogin.visibility = View.GONE
                            tvPleaseWait.visibility = View.GONE
                            val currentUser: BmobUser = BmobUser.getCurrentUser()
                            if (currentUser.emailVerified) {
                                Toast.makeText(applicationContext, "登录成功:" + s.toString(), Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(applicationContext, "邮箱校验失败，检查你的邮箱", Toast.LENGTH_SHORT).show()
                                progressBarLogin.visibility = View.GONE
                                tvPleaseWait.visibility = View.GONE
                                BmobUser.logOut()
                            }
                        } else {
                            progressBarLogin.visibility = View.GONE
                            loadingPleaseWait.visibility = View.GONE
                            Toast.makeText(applicationContext, "登录失败:" + e.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }
        }

        tvLinkSignUp.setOnClickListener {
            Log.d(TAG, "onClick: navigating to register screen")
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        /*
         If the user is logged in then navigate to HomeActivity and call 'finish()'
          */
        if (BmobUser.getCurrentUser() != null) {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    companion object {
        private val TAG = "LoginActiviyty"
    }
}