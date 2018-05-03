package cn.gdut.android.everyday.models

import cn.bmob.v3.BmobObject

/**
 * Created by denghewen on 2018/4/30 0030.
 */
data class FileInfo(var path: String, var name: String, var downloadSize: Int, var userId: String)
    : BmobObject()