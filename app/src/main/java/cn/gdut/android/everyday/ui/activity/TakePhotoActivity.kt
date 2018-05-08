package cn.gdut.android.everyday.ui.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import cn.gdut.android.everyday.R
import cn.gdut.android.everyday.adapter.PhotoFiltersAdapter
import cn.gdut.android.everyday.image.GLES20BackEnv
import cn.gdut.android.everyday.image.filter.ColorFilter
import cn.gdut.android.everyday.image.filter.ContrastColorFilter
import cn.gdut.android.everyday.models.FilterItem
import cn.gdut.android.everyday.ui.view.AnimationTopLayout
import cn.gdut.android.everyday.ui.view.RevealBackgroundView
import cn.gdut.android.everyday.utils.Constant
import cn.gdut.android.everyday.utils.Utils
import com.otaliastudios.cameraview.*
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.task.DefaultAlbumLoader
import kotlinx.android.synthetic.main.activity_take_photo.*
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream

/**
 * Created by denghewen on 2018/4/6
 */
class TakePhotoActivity : AppCompatActivity()
        , RevealBackgroundView.OnStateChangeListener
        , AnimationTopLayout.OnAnimationHalfFinishListener{

    companion object {
        private val ARG_REVEAL_START_LOCATION = "reveal_start_location"
        private val STATE_TAKE_PHOTO = 0
        private val STATE_SETUP_PHOTO = 1
        private val ACCELERATE_INTERPOLATOR = AccelerateInterpolator()
        private val DECELERATE_INTERPOLATOR = DecelerateInterpolator()

        fun startCameraFromLocation(startingLocation: IntArray, startingActivity: Activity) {
            val intent = Intent(startingActivity, TakePhotoActivity::class.java)
            intent.putExtra(ARG_REVEAL_START_LOCATION, startingLocation)
            startingActivity.startActivity(intent)
        }
    }

    private var mBackEnv: GLES20BackEnv? = null

    private var currentBitmap: Bitmap? = null
    private var pendingIntro: Boolean = false
    private var currentState: Int = 0
    private var mGridDrawableRes: IntArray = intArrayOf(R.drawable.ic_grid_on_white_24dp, R.drawable.ic_grid_off_white_24dp)
    private var mGridState: Int = 0
    private var mCameraIdState: Int = 0
    private val hueItems = listOf(FilterItem("灰白", ColorFilter.Filter.GRAY)
            , FilterItem("冷色调", ColorFilter.Filter.COOL)
            , FilterItem("暖色调", ColorFilter.Filter.WARM))

    private val outlineItems = listOf(FilterItem("模糊", ColorFilter.Filter.BLUR)
            , FilterItem("放大镜", ColorFilter.Filter.MAGN))


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_take_photo)
        updateStatusBarColor()
        updateState(STATE_TAKE_PHOTO)
        setupRevealBackground(savedInstanceState)
        setupPhotoFilters()

        vUpperPanel.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                vUpperPanel.viewTreeObserver.removeOnPreDrawListener(this)
                pendingIntro = true
                vUpperPanel.translationY = (-vUpperPanel.height).toFloat()
                vLowerPanel.translationY = vLowerPanel.height.toFloat()
                return true
            }
        })

        initEvent()
    }

    private fun initEvent() {
        layoutShowGrid.setOnAnimationHalfFinishListener(this)
        layoutChangeCamera.setOnAnimationHalfFinishListener(this)

        btnTakePhoto.setOnClickListener {
            btnTakePhoto.isEnabled = false
            cameraView.capturePicture()
            animateShutter()
        }

        btnOpenAlbum.setOnClickListener {
            Album.image(this)
                    .multipleChoice()
                    .columnCount(3)
                    .selectCount(1)
                    .camera(false)
                    .onResult { requestCode, result ->
                        Single.just(result[0].path)
                                .map { path ->
                                    DefaultAlbumLoader.readImageFromPath(path, ivTakenPhoto.width, ivTakenPhoto.height)
                                }
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe{ bitmap ->
                                    currentBitmap = bitmap
                                    showTakenPicture(bitmap)
                                }

                    }.start()
        }

        btnEffectHue.setOnClickListener {
            val photoFiltersAdapter = rvFilters.adapter as PhotoFiltersAdapter
            photoFiltersAdapter.setFilterList(hueItems)
        }

        btnEffectOutline.setOnClickListener {
            val photoFiltersAdapter = rvFilters.adapter as PhotoFiltersAdapter
            photoFiltersAdapter.setFilterList(outlineItems)
        }

        btnShowGrid.setOnClickListener {
            layoutShowGrid.doAnimation()
        }

        btnChangeCamera.setOnClickListener {
            layoutChangeCamera.doAnimation()
        }

        btnCancel.setOnClickListener {
            onBackPressed()
        }

        btnBack.setOnClickListener {
            onBackPressed()
        }

        btnAccept.setOnClickListener {
            Single.just(saveBitmap(mBackEnv!!.bitmap))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { file ->
                        PublishActivity.openWithPhotoUri(this, Uri.fromFile(file))
                    }
        }
    }

    private fun animateShutter() {
        vShutter.visibility = View.VISIBLE
        vShutter.alpha = 0f

        val alphaInAnim = ObjectAnimator.ofFloat(vShutter, "alpha", 0f, 0.8f)
        alphaInAnim.duration = 100
        alphaInAnim.startDelay = 100
        alphaInAnim.interpolator = ACCELERATE_INTERPOLATOR

        val alphaOutAnim = ObjectAnimator.ofFloat(vShutter, "alpha", 0.8f, 0f)
        alphaOutAnim.duration = 200
        alphaOutAnim.interpolator = DECELERATE_INTERPOLATOR

        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(alphaInAnim, alphaOutAnim)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                vShutter.visibility = View.GONE
            }
        })
        animatorSet.start()
    }

    private fun setupPhotoFilters() {
        val bitmap = BitmapFactory.decodeStream(resources.assets.open("texture/default_img.png"))
        val photoFiltersAdapter = PhotoFiltersAdapter(this, bitmap, hueItems) { item ->
            ivTakenPhoto.setFilter(ContrastColorFilter(this, item.filter))
            ivTakenPhoto.requestRender()
            if(currentBitmap != null) {
                mBackEnv = GLES20BackEnv(currentBitmap!!.width, currentBitmap!!.height)
                mBackEnv!!.setThreadOwner(mainLooper.thread.name)
                val contrastColorFilter = ContrastColorFilter(this, item.filter)
                contrastColorFilter.setBitmap(currentBitmap)
                mBackEnv!!.setFilter(contrastColorFilter)
                mBackEnv!!.setInput(currentBitmap)
            }
        }
        rvFilters.setHasFixedSize(true)
        rvFilters.adapter = photoFiltersAdapter
        rvFilters.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
        if(currentBitmap != null) {
            val photoFiltersAdapter = rvFilters.adapter as PhotoFiltersAdapter
            photoFiltersAdapter.setCurrentImg(currentBitmap!!)
            ivTakenPhoto.setImageBitmap(currentBitmap)
            ivTakenPhoto.setFilter(ContrastColorFilter(this, ColorFilter.Filter.NONE))
            ivTakenPhoto.requestRender()
        }
    }

    override fun onPause() {
        cameraView.stop()
        super.onPause()
    }

    override fun onDestroy() {
        cameraView.destroy()
        super.onDestroy()
    }

    override fun onHalf(view: View) {
        when (view.id) {
            R.id.btnShowGrid -> {
                mGridState++
                val currentGridState = mGridState % mGridDrawableRes.size
                Log.i("info", "currentGridState" + currentGridState)
                btnShowGrid.setImageResource(mGridDrawableRes[currentGridState])
                if (currentGridState == Constant.CAMERA_SHOW_GRID) {
                    cameraView.grid = Grid.DRAW_3X3
                } else {
                    cameraView.grid = Grid.OFF
                }
            }
            R.id.btnChangeCamera -> {
                mCameraIdState++
                btnChangeCamera.setImageResource(R.drawable.ic_camera_rear_white_24dp)
                cameraView.toggleFacing()
            }
        }
    }

    override fun onBackPressed() {
        if (currentState == STATE_SETUP_PHOTO) {
            btnTakePhoto.isEnabled = true
            vUpperPanel.showNext()
            vLowerPanel.showNext()
            updateState(STATE_TAKE_PHOTO)
        } else {
            super.onBackPressed()
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun updateStatusBarColor() {
        if (Utils.isAndroid5()) {
            window.statusBarColor = 0xfb8c00
        }
    }

    private fun setupRevealBackground(savedInstanceState: Bundle?) {
        vRevealBackground.setFillPaintColor(0xfffb8c00.toInt())
        vRevealBackground.setOnStateChangeListener(this)
        if (savedInstanceState == null) {
            val startingLocation = intent.getIntArrayExtra(ARG_REVEAL_START_LOCATION)
            vRevealBackground.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    vRevealBackground.viewTreeObserver.removeOnPreDrawListener(this)
                    vRevealBackground.startFromLocation(startingLocation)
                    return true
                }
            })
        } else {
            vRevealBackground.setToFinishedFrame()
        }
    }

    override fun onStateChange(state: Int) {
        if (RevealBackgroundView.STATE_FINISHED == state) {
            vPhotoRoot.visibility = View.VISIBLE
            cameraView.addCameraListener(object : CameraListener() {
                override fun onPictureTaken(picture: ByteArray?) {
                    CameraUtils.decodeBitmap(picture, 1000, 1000) { bitmap ->
                        currentBitmap = bitmap
                        showTakenPicture(bitmap)
                    }
                }
            })

            cameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM) // Pinch to zoom!
            cameraView.mapGesture(Gesture.TAP, GestureAction.FOCUS_WITH_MARKER) // Tap to focus!
            if (pendingIntro) {
                startIntroAnimation()
            }
        } else {
            vPhotoRoot.visibility = View.INVISIBLE
        }
    }

    private fun startIntroAnimation() {
        vUpperPanel.animate().translationY(0f).setDuration(400).interpolator = DECELERATE_INTERPOLATOR
        vLowerPanel.animate().translationY(0f).setDuration(400).setInterpolator(DECELERATE_INTERPOLATOR).start()
    }

    private fun updateState(state: Int) {
        currentState = state
        if (currentState == STATE_TAKE_PHOTO) {
            vUpperPanel.setInAnimation(this, R.anim.slide_in_from_right)
            vLowerPanel.setInAnimation(this, R.anim.slide_in_from_right)
            vUpperPanel.setOutAnimation(this, R.anim.slide_out_to_left)
            vLowerPanel.setOutAnimation(this, R.anim.slide_out_to_left)
            Handler().postDelayed({ ivTakenPhoto.visibility = View.GONE }, 400)
        } else if (currentState == STATE_SETUP_PHOTO) {
            vUpperPanel.setInAnimation(this, R.anim.slide_in_from_left)
            vLowerPanel.setInAnimation(this, R.anim.slide_in_from_left)
            vUpperPanel.setOutAnimation(this, R.anim.slide_out_to_right)
            vLowerPanel.setOutAnimation(this, R.anim.slide_out_to_right)
            ivTakenPhoto.visibility = View.VISIBLE
        }
    }


    private fun showTakenPicture(bitmap: Bitmap) {
        val photoFiltersAdapter = rvFilters.adapter as PhotoFiltersAdapter
        photoFiltersAdapter.setCurrentImg(bitmap)
        vUpperPanel.showNext()
        vLowerPanel.showNext()
        updateState(STATE_SETUP_PHOTO)
        ivTakenPhoto.setImageBitmap(bitmap)
        ivTakenPhoto.setFilter(ContrastColorFilter(this, ColorFilter.Filter.NONE))
        ivTakenPhoto.requestRender()
    }

    private fun saveBitmap(currentBitmap :Bitmap): File? {

        val fos: FileOutputStream
        try {
            // 判断手机设备是否有SD卡
            val isHasSDCard = Environment.getExternalStorageState() == android.os.Environment.MEDIA_MOUNTED
            val file: File
            if (isHasSDCard) {
                // SD卡根目录
                val sdRoot = Environment.getExternalStorageDirectory()
                Log.e("ssh", sdRoot.toString())
                file = File(sdRoot, "test.png")
                fos = FileOutputStream(file)
            } else
                throw Exception("创建文件失败!")
            //压缩图片 30 是压缩率，表示压缩70%; 如果不压缩是100，表示压缩率为0
            currentBitmap.compress(Bitmap.CompressFormat.PNG, 90, fos)

            fos.flush()
            fos.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }
}