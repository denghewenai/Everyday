package cn.gdut.android.everyday.ui.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.ViewTreeObserver
import android.view.animation.OvershootInterpolator
import cn.bmob.v3.BmobUser
import cn.bmob.v3.datatype.BmobFile
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.SaveListener
import cn.bmob.v3.listener.UploadFileListener
import cn.gdut.android.everyday.R
import cn.gdut.android.everyday.models.Note
import cn.gdut.android.everyday.utils.Utils
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_publish.*
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish)
        setSupportActionBar(toolbar)

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_grey600_24dp)
        photoSize = resources.getDimensionPixelSize(R.dimen.publish_photo_thumbnail_size)

        photoUri = if (savedInstanceState == null) {
            intent.getParcelableExtra(ARG_TAKEN_PHOTO_URI)
        } else {
            savedInstanceState.getParcelable(ARG_TAKEN_PHOTO_URI)
        }
        updateStatusBarColor()

        ivPhoto.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                ivPhoto.viewTreeObserver.removeOnPreDrawListener(this)
                loadThumbnailPhoto()
                return true
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_publish, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_publish) {
//            bringMainActivityToTop()
            if (TextUtils.isEmpty(etDescription.text)) {
                toast("请添加文字描述，留下文字记忆！")
            } else {
                postImage()
            }
            true
        } else {
            super.onOptionsItemSelected(item)
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

    private fun updateStatusBarColor() {
        if (Utils.isAndroid5()) {
            window.statusBarColor = 0x888888
        }
    }

    private fun bringMainActivityToTop() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
//        intent.action = MainActivity.ACTION_SHOW_LOADING_ITEM
        startActivity(intent)
    }

}