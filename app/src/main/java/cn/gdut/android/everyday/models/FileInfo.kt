package cn.gdut.android.everyday.models

import cn.bmob.v3.BmobObject
import cn.bmob.v3.BmobUser

/**
 * Created by denghewen on 2018/4/30 0030.
 */
data class FileInfo(var name: String,
                    var userId: String = BmobUser.getCurrentUser().objectId)
    : BmobObject()