package cn.gdut.android.everyday

import android.app.Application
import cn.bmob.v3.Bmob
import cn.bmob.v3.BmobUser

/**
 * Created by denghewen on 2018/3/21 0021.
 */
class App : Application() {

    companion object {
        var instance: App? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Bmob.initialize(this, "cc539eeddbf7d7d6fa4d0a1f17f53579")
    }

    fun getCurrentUser(): BmobUser?{
        return BmobUser.getCurrentUser()
    }

}