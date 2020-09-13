package com.example.todolist

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_todo.view.*
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random


class TodoAdapter (val list: List<TodoModel>): RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoAdapter.TodoViewHolder {
       return TodoViewHolder(
           LayoutInflater.from(parent.context).inflate(R.layout.item_todo,parent,false)
       )
    }


    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: TodoAdapter.TodoViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemId(position: Int): Long {
        return list[position].id
    }


    class TodoViewHolder (itemView: View):RecyclerView.ViewHolder(itemView){
        fun bind(todoModel: TodoModel) {
            with(itemView)
            {
                val colors=resources.getIntArray(R.array.random_color)
                viewColorTag.setBackgroundColor(Color.GREEN)
                txtShowTitle.text=todoModel.tittle
                txtShowCategory.text=todoModel.category
                txtShowTask.text=todoModel.discription
                updateTime(todoModel.time)
                updateDate(todoModel.date)
            }
        }

        private fun updateTime(time:Long)
        {
            val myformat="h mm a"
            val sdf=SimpleDateFormat(myformat)
            itemView.txtShowTime.text=sdf.format(Date(time))
        }

        private fun updateDate(date: Long)
        {
            val myformat="EEE, d mmm yyyy"
            val sdf=SimpleDateFormat(myformat)
            itemView.txtShowDate.text=sdf.format(Date(date))
        }

    }
}