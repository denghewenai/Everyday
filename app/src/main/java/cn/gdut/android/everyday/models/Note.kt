package cn.gdut.android.everyday.models

import cn.bmob.v3.BmobObject

/**
 * Created by denghewen on 2018/4/30 0030.
 */
data class Note(var fileName: String = "",
                var folderId: String = "", var userId: String = "",
                var url: String = "", var describe: String = "") : BmobObject()