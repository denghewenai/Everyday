package cn.gdut.android.everyday.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.MenuItem
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.BmobUser
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.SaveListener
import cn.gdut.android.everyday.R
import cn.gdut.android.everyday.adapter.FolderManageAdapter
import cn.gdut.android.everyday.models.FileInfo
import cn.gdut.android.everyday.models.FolderItem
import cn.gdut.android.everyday.models.Note
import cn.gdut.android.everyday.ui.dialog.ConfirmCreateFolderDialog
import kotlinx.android.synthetic.main.activity_manage_folder.*
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.startActivity
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


/**
 * Created by denghewen on 2018/5/4 0004.
 */
class FolderManageActivity : AppCompatActivity() {
    private lateinit var mFolderManageAdapter: FolderManageAdapter
    private val folderBgIds:IntArray = intArrayOf(R.drawable.ic_folder_style_red
            ,R.drawable.ic_folder_style_yellow,R.drawable.ic_folder_style_blue
            ,R.drawable.ic_folder_style_orange,R.drawable.ic_folder_style_purple
            ,R.drawable.ic_folder_style_green)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_folder)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.folder_title)
        recyclerViewFolder.layoutManager = GridLayoutManager(this, 2)
        initViews()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        //When home is clicked
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initViews() {
        val dataList = ArrayList<FolderItem>()
//        val folderItem = FolderItem(null,null,R.drawable.ic_add_folder_image)
//        dataList.add(folderItem)
        mFolderManageAdapter = FolderManageAdapter(this, dataList) { folderItem ->
            if(folderItem.fileInfo == null){
                val dialog = ConfirmCreateFolderDialog(this)
                dialog.setOnConfirmListener(object :ConfirmCreateFolderDialog.OnConfirmListener {
                    override fun onCancel() {
                        dialog.dismiss()
                    }

                    override fun onOk(inputMessage: String) {
                        dialog.dismiss()
                        addFolder(inputMessage)
                    }
                }).show()
            } else {
                startActivity<TimeLineActivity>(TimeLineActivity.FOLDER_ID to folderItem.fileInfo!!.objectId)
            }
        }
        recyclerViewFolder.adapter = mFolderManageAdapter
        refreshFolderList()
    }

    private fun addFolder(folderName :String) {
        val bmobFolder = FileInfo(folderName)
        bmobFolder.save(object :SaveListener<String>(){
            override fun done(objectId: String?, e: BmobException?) {
                refreshFolderList()
            }
        })
    }

    private fun refreshFolderList() {
        val query = BmobQuery<FileInfo>()
        query.addWhereEqualTo("userId", BmobUser.getCurrentUser().objectId)
        query.order("createdAt")
        query.setLimit(TimeLineActivity.ONE_REQUEST_LIMIT)
        var currentFile :FileInfo? = null
        query.findObjectsObservable(FileInfo::class.java)
                .flatMap { fileList -> Observable.from(fileList) }
                .concatMap {file ->
                    currentFile = file
                    Log.i("info", "currentFile1" + currentFile)
                    queryNotes(file)
                }
                .map { notes ->
                    Log.i("info", "currentFileNotes2" + currentFile)
                    val folderBgId: Int = generateFolderBgId(currentFile!!.name)
                    val folderItem = FolderItem(currentFile, notes, folderBgId)
                    folderItem
                }
                .collect({ ArrayList<FolderItem>() }
                        , { folders, folderItem -> folders.add(folderItem) })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { folders ->
                    //                        mFolderManageAdapter.addFolderList(folders)
                    mFolderManageAdapter.setFolderList(folders)
                    Log.i("info", "size" + folders.size)
                }
    }

    private fun queryNotesNumber(folder: FileInfo): Observable<Int> {
        val query = BmobQuery<Note>()
        query.addWhereEqualTo("userId", BmobUser.getCurrentUser().objectId)
        query.addWhereEqualTo("folderId", folder.objectId)
        query.order("-createdAt")
        return query.countObservable(Note::class.java)
    }

    private fun queryNotes(folder: FileInfo): Observable<MutableList<Note>> {
        val query = BmobQuery<Note>()
        query.addWhereEqualTo("userId", BmobUser.getCurrentUser().objectId)
        query.addWhereEqualTo("folderId", folder.objectId)
        query.order("-createdAt")
        return query.findObjectsObservable(Note::class.java)
    }

    private fun generateFolderBgId(folderName: String): Int {
        return folderBgIds[folderName[0].toInt() % folderBgIds.size]
    }

}