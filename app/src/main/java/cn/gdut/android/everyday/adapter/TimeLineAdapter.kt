package cn.gdut.android.everyday.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import cn.gdut.android.everyday.R
import cn.gdut.android.everyday.models.Note
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.time_line_item.view.*

/**
 * Created by Administrator on 2018/5/1 0001.
 */
class TimeLineAdapter(val context:Context,
                      private var noteList: MutableList<Note>,
                      private val listener: (Note,ImageView) -> Unit) : RecyclerView.Adapter<TimeLineAdapter.ViewHolder>() {


    fun setNoteList(noteList:MutableList<Note>){
        this.noteList = noteList
        notifyDataSetChanged()
    }

    fun addNoteList(noteList: MutableList<Note>){
        val startPosition = this.noteList.size
        this.noteList.addAll(noteList)
        val endPosition = this.noteList.size
        notifyItemRangeInserted(startPosition,endPosition)
    }

    override fun getItemCount(): Int = noteList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.time_line_item, parent, false)
        return TimeLineAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeLineAdapter.ViewHolder, position: Int) =
            holder.bind(noteList[position],listener)


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(note: Note,listener: (Note,ImageView) -> Unit) = with(itemView) {
            tvDescription.text = note.describe
            Picasso.Builder(context)
                    .build()
                    .load(note.url)
                    .into(ivPhotoNote, object : Callback {
                        override fun onSuccess() {
                            ivPhotoNote.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setInterpolator(OvershootInterpolator())
                                    .setDuration(400)
                                    .setStartDelay(200)
                                    .start()
                        }

                        override fun onError() {
                            ivPhotoNote.setImageResource(R.mipmap.ic_launcher)
                        }
                    })

            setOnClickListener {
                listener(note,ivPhotoNote)
            }
        }
    }
}