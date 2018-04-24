package cn.gdut.android.everyday.adapter

import android.content.Context
import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.gdut.android.everyday.R
import cn.gdut.android.everyday.image.filter.ColorFilter
import cn.gdut.android.everyday.models.FilterItem
import kotlinx.android.synthetic.main.item_photo_filter.view.*

/**
 * Created by denghewen on 2018/4/8 0008.
 */
class PhotoFiltersAdapter(val context: Context, private var currentImg: Bitmap
                          , private var filterList: List<FilterItem>
                          , private val listener: (FilterItem) -> Unit)
    : RecyclerView.Adapter<PhotoFiltersAdapter.PhotoFilterViewHolder>() {

    fun setCurrentImg(bitmap: Bitmap) {
        currentImg = bitmap
        notifyDataSetChanged()
    }

    fun setFilterList(list: List<FilterItem>){
        filterList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoFilterViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_photo_filter, parent, false)
        return PhotoFilterViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoFilterViewHolder, position: Int)
            = holder.bind(filterList[position], currentImg, listener)

    override fun getItemCount(): Int = filterList.size

    class PhotoFilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: FilterItem, bitmap: Bitmap, listener: (FilterItem) -> Unit) = with(itemView) {
            tvFilterName.text = item.filterName
            sglFilterThumb.setImageBitmap(bitmap)
            sglFilterThumb.setFilter(ColorFilter(context, item.filter))
            setOnClickListener {
                listener(item)
            }
        }
    }
}
