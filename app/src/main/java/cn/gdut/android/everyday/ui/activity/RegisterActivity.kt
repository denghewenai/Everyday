package cn.gdut.android.everyday.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import cn.bmob.v3.BmobUser
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.SaveListener
import cn.gdut.android.everyday.R
import kotlinx.android.synthetic.main.activity_register.*


/**
 * Created by denghewen on 2018/3/21 0021.
 */
class RegisterActivity : AppCompatActivity() {

    private var email: String? = null
    private var username: String? = null
    private var password: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        init()
    }

    private fun init() {
        btnRegister.setOnClickListener {
            email = etRegisterInputEmail.text.toString()
            username = etRegisterInputUsername.text.toString()
            password = etRegisterInputPassword.text.toString()

            if (checkInputs(email, username, password)) {
                progressBarRegister.visibility = View.VISIBLE
                loadingPleaseWait.visibility = View.VISIBLE
                val user = BmobUser()
                user.username = username
                user.setPassword(password)
                user.email = email
                user.signUp(object : SaveListener<BmobUser>() {
                    override fun done(s: BmobUser, e: BmobException?) {
                        if (e == null) {
                            progressBarRegister.visibility = View.GONE
                            loadingPleaseWait.visibility = View.GONE
                            Toast.makeText(applicationContext, "注册成功:" + s.toString(), Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            startActivity(intent)
                            finish()
                        } else {
                            progressBarRegister.visibility = View.GONE
                            loadingPleaseWait.visibility = View.GONE
                            Toast.makeText(applicationContext, "注册失败:" + e.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }
        }
    }

    private fun checkInputs(email: String?, username: String?, password: String?): Boolean {
        Log.d(TAG, "checkInputs: checking inputs for null values.")
        if (email == "" || username == "" || password == "") {
            Toast.makeText(applicationContext, "所有信息都必须填写！", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    companion object {
        private val TAG = "RegisterActivity"
    }
}