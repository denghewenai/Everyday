package cn.gdut.android.everyday.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.gdut.android.everyday.R
import cn.gdut.android.everyday.models.FolderItem
import kotlinx.android.synthetic.main.folder_manage_item.view.*

/**
 * Created by Administrator on 2018/5/1 0001.
 */
class FolderManageAdapter(val context: Context,
                          private var folderList: MutableList<FolderItem>,
                          private val listener: (FolderItem) -> Unit) : RecyclerView.Adapter<FolderManageAdapter.ViewHolder>() {

    fun setFolderList(folderList: MutableList<FolderItem>){
        this.folderList = folderList
        val folderItem = FolderItem(null,null,R.drawable.ic_add_folder_image)
        this.folderList.add(0,folderItem)
        notifyDataSetChanged()
    }

    fun addFolderList(folderList: MutableList<FolderItem>){
        val startPosition = this.folderList.size
        this.folderList.addAll(folderList)
        val endPosition = this.folderList.size
        notifyItemRangeInserted(startPosition,endPosition)
    }

    override fun getItemCount(): Int = folderList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderManageAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.folder_manage_item, parent, false)
        return FolderManageAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderManageAdapter.ViewHolder, position: Int) =
            holder.bind(folderList[position],listener)


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(folderItem: FolderItem, listener: (FolderItem) -> Unit) = with(itemView) {
            cardFolderManage.setBackgroundResource(folderItem.folderBgId)
            tvFolderName.text = folderItem.fileInfo?.name
            if(folderItem.notesList != null) {
                tvCountNote.text = "${folderItem.notesList!!.size}张图"
            }

            setOnClickListener {
                listener(folderItem)
            }
        }
    }
}