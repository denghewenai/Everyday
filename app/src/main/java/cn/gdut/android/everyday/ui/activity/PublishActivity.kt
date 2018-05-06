package cn.gdut.android.everyday.ui.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewTreeObserver
import android.view.animation.OvershootInterpolator
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.BmobUser
import cn.bmob.v3.datatype.BmobFile
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import cn.bmob.v3.listener.SaveListener
import cn.bmob.v3.listener.UploadFileListener
import cn.gdut.android.everyday.R
import cn.gdut.android.everyday.models.FileInfo
import cn.gdut.android.everyday.models.Note
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_publish.*
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.toast
import java.io.File
import java.net.URI


/**
 * Created by denghewen on 2018/4/27 0027.
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

    private var photoSize: Int = 0
    private var photoUri: Uri? = null
    private var photoUrl: String? = null
    private var selectedFolderId: String = "8df4704cdc"
    private lateinit var fileInfoList: List<FileInfo>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "后台保存"
//        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_grey600_24dp)
        photoSize = resources.getDimensionPixelSize(R.dimen.publish_photo_thumbnail_size)

        getDefaultFolderId()
        photoUri = if (savedInstanceState == null) {
            intent.getParcelableExtra(ARG_TAKEN_PHOTO_URI)
        } else {
            savedInstanceState.getParcelable(ARG_TAKEN_PHOTO_URI)
        }

        ivPhoto.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                ivPhoto.viewTreeObserver.removeOnPreDrawListener(this)
                loadThumbnailPhoto()
                return true
            }
        })

        tbChooseFolder.setOnClickListener {
            showFolderList()
        }
    }

    private fun getDefaultFolderId() {
        val query = BmobQuery<FileInfo>()
        query.addWhereEqualTo("userId", BmobUser.getCurrentUser().objectId)
        query.addWhereEqualTo("name", "默认")
        query.order("createdAt")
        query.findObjects(object : FindListener<FileInfo>() {
            override fun done(fileInfoList: MutableList<FileInfo>, e: BmobException?) {
                if(e == null){
                    selectedFolderId = fileInfoList[0].objectId
                } else{

                }
            }
        })
    }

    private fun showFolderList() {
        val query = BmobQuery<FileInfo>()
        query.addWhereEqualTo("userId", BmobUser.getCurrentUser().objectId)
        query.order("createdAt")
        query.findObjects(object : FindListener<FileInfo>() {
            override fun done(fileInfoList: MutableList<FileInfo>, e: BmobException?) {
                if(e == null){
                    this@PublishActivity.fileInfoList = fileInfoList
                    Log.i("info", "size" + fileInfoList.size)
                    val fileNamesArray = Array<String>(fileInfoList.size){ i ->
                        fileInfoList[i].name
                    }
                    showSingleChoiceDialog(fileNamesArray)
                } else{
                    Log.i("info", "失败：" + e.message + "," + e.errorCode)
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_publish, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when {
            item.itemId == R.id.action_publish -> {
//            bringMainActivityToTop()
                if (TextUtils.isEmpty(etDescription.text)) {
                    toast("请添加文字描述，留下文字记忆！")
                } else {
                    postImage()
                }
                true
            }
            item.itemId == android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun postImage() {
        val bmobFile = BmobFile(File(URI(photoUri.toString())))
        bmobFile.uploadblock(object : UploadFileListener() {
            override fun done(e: BmobException?) {
                if (e == null) {
                    //bmobFile.getFileUrl()--返回的上传文件的完整地址
//                    toast("上传文件成功:" + bmobFile.fileUrl)
                    val note = Note()
                    note.describe = etDescription.text.toString()
                    note.url = bmobFile.fileUrl
                    note.folderId = selectedFolderId
                    note.userId = BmobUser.getCurrentUser().objectId
                    note.save(object : SaveListener<String>() {
                        override fun done(objectId: String?, e: BmobException?) {
                            if (e == null) {
                                toast("创建日记成功：" + objectId)
                                bringMainActivityToTop()
                            } else {
//                                Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                                toast("创建日记失败:" + e.message)
                            }
                        }
                    })
                } else {
                    toast("创建日记失败:" + e.message)
//                    toast("上传文件失败：" + e.message)
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putParcelable(ARG_TAKEN_PHOTO_URI, photoUri)
    }

    private fun loadThumbnailPhoto() {
        ivPhoto.scaleX = 0f
        ivPhoto.scaleY = 0f
        Picasso.Builder(this)
                .build()
                .load(photoUri)
                .centerCrop()
                .resize(photoSize, photoSize)
                .into(ivPhoto, object : Callback {
                    override fun onSuccess() {
                        ivPhoto.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setInterpolator(OvershootInterpolator())
                                .setDuration(400)
                                .setStartDelay(200)
                                .start()
                    }

                    override fun onError() {
                    }

                })
    }

    private fun showSingleChoiceDialog(items :Array<String>) {
        val builder = AlertDialog.Builder(this)
        var index = 0
        builder.setTitle("选择放置文件夹:")
//        builder.setIcon(R.mipmap.ic_launcher)
        builder.setSingleChoiceItems(items, 0) { dialog, which ->
            index = which
        }
        builder.setPositiveButton("确认") { dialog, which ->
            tbChooseFolder.textOn = items[index]
            tbChooseFolder.isChecked = true
            selectedFolderId = fileInfoList[index].objectId
            dialog.dismiss()
        }
        builder.setNegativeButton("取消") { dialog, which ->
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun bringMainActivityToTop() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
//        intent.action = MainActivity.ACTION_SHOW_LOADING_ITEM
        startActivity(intent)
    }

}