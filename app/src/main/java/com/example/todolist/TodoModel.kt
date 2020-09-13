package com.example.todolist

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class TodoModel(
    var tittle:String,
    var discription:String,
    var category: String,
    var date: Long,
    var time:Long,
    var isFinished:Int=-1 ,
    @PrimaryKey(autoGenerate = true)
    var id: Long=0
)