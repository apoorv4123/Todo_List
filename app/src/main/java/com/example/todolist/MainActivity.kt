package com.example.todolist

import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Icon
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R.menu.main_menu
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

//colon used to extend
//AppCompatActivity is a Base class for activities that wish to use some newer platform feartures on older devices. 
class MainActivity : AppCompatActivity() {
    val list = arrayListOf<TodoModel>()
    //TodoAdapter is used for recycler view.
     var adapter = TodoAdapter(list)

    val db by lazy {
        //AppDatabase extends RoomDatabase class which ensures the fluent access of database 
        AppDatabase.getDatabase(this)
    }

    //onCreate function starts the activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        //todoRv is the id of Recycler View.
        todoRv.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
        //initSwipe function performs swiping action, if we swipe left it performs delete and if swiped right then marked as done. 
        initSwipe()
        //this first clear the previous one data and then add all the data to the list
        db.todoDao().getTask().observe(this, Observer {
            if (!it.isNullOrEmpty()) {
                list.clear()
                list.addAll(it)
                //it will notify the adapter to update data
                adapter.notifyDataSetChanged()
            } else {
                list.clear()
                adapter.notifyDataSetChanged()
            }
        })

        //Same as onClickListener when a item is clicked it gets the id and match the id and perform the respective function
        //Three dots(
        // .
        // .
        // . ) it shows history in this
    }

   // override fun onCreateOptionsMenu(menu: Menu?): Boolean {
     //   menuInflater.inflate(R.menu.main_menu,menu)
       // return super.onCreateOptionsMenu(menu)
    //}
    //override fun onOptionsItemSelected(item: MenuItem): Boolean {
      //  when (item.itemId) {
        //    R.id.history -> {
          //      startActivity(Intent(this, HistoryActivity::class.java))
            //}
        //}
        //return super.onOptionsItemSelected(item)
    //}
    
    //when + action button is clicked then TaskActivity comes into play.
    fun openNewTask(view: View) {
        //Intent is used to invoke a activity.
        startActivity(Intent(this, TaskActivity::class.java))
    }

    //function initSwipe
    private fun initSwipe() {
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(
            //If we don't want to add up and down action action then set it's value to 0.
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                //GlobalScope is used to perform work in background.
                if (direction == ItemTouchHelper.LEFT) {
                    GlobalScope.launch(Dispatchers.IO){
                        //Swiped left perform delete task action.
                        db.todoDao().deleteTask(adapter.getItemId(position))
                    }
                } else if (direction == ItemTouchHelper.RIGHT) {
                    GlobalScope.launch(Dispatchers.IO){
                        //Swiped right mark as done
                        db.todoDao().finishTask(adapter.getItemId(position))
                    }
                }
            }
\
            //fun to place icon
            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView

                    val paint = Paint()
                    val icon: Bitmap

                    if (dX > 0) {

                        //check_black is icon 
                        icon = BitmapFactory.decodeResource(resources, R.mipmap.ic_check_black)
                        
                        //Putting background color as green when swiped right
                        paint.color = Color.parseColor("#388E3C")

                        //Drwaing Rectangle
                        canvas.drawRect(
                            itemView.left.toFloat(), itemView.top.toFloat(),
                            itemView.left.toFloat() + dX, itemView.bottom.toFloat(), paint
                        )

                        canvas.drawBitmap(
                            icon,
                            //Position of icon
                            itemView.left.toFloat(),
                            itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height.toFloat()) / 2,
                            paint
                        )
                    }
                    else if(dX<0)
                    {
                        icon=BitmapFactory.decodeResource(resources,R.mipmap.ic_cancel_black)
                        //Red color
                        paint.color=Color.parseColor("#D32F2F")

                        //Drawing rectangle
                        canvas.drawRect(itemView.right.toFloat()+dX,itemView.top.toFloat(),
                            itemView.right.toFloat(),itemView.bottom.toFloat(),paint)

                        canvas.drawBitmap(
                            icon,
                            //Delete icon position
                            itemView.right.toFloat()-icon.width,
                            itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height.toFloat()) / 2,
                            paint
                        )
                    }
                    //perform translation.
                   viewHolder.itemView.translationX=dX

                } else {
                    super.onChildDraw(
                        canvas,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(todoRv)

    }

}



