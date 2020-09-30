package com.example.todolist

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.NotificationCompat
import androidx.core.view.get
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : AppCompatActivity() {
    val list = arrayListOf<TodoModel>()
    var adapter = TodoAdapter(list)

    //it provides access to database .
    val db by lazy {
        AppDatabase.getDatabase(this)
    }

    //function to start activity.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)



        //todoRv is id of Recycler View.
        todoRv.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter

        }
        initSwipe()
        db.todoDao().getTask().observe(this, Observer {
            if (!it.isNullOrEmpty()) {
                list.clear()
                list.addAll(it)
                adapter.notifyDataSetChanged()
                val a=it.get(0).tittle.toString()
                Log.d("123",a)
            } else {
                list.clear()
                adapter.notifyDataSetChanged()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        val item = menu?.findItem(R.id.search)
        val searchView = item?.actionView as SearchView
        item.setOnActionExpandListener(object :MenuItem.OnActionExpandListener{
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                displayTodo()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                displayTodo()
                return true
            }

        })
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(!newText.isNullOrEmpty()){
                    displayTodo(newText)
                }
                return true
            }

        })

        return super.onCreateOptionsMenu(menu)
    }

    fun displayTodo(newText : String=" ")
    {
        db.todoDao().getTask().observe(this, Observer {
            if(it.isNotEmpty())
            {
                list.clear()
                list.addAll(
                    it.filter { todo->
                        todo.tittle.contains(newText,true)
                    }
                )
                adapter.notifyDataSetChanged()
            }
        })

    }

    fun openNewTask(view: View) {
        startActivity(Intent(this, TaskActivity::class.java))
    }

    private fun initSwipe() {
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(
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

                if (direction == ItemTouchHelper.LEFT) {
                    GlobalScope.launch(Dispatchers.IO){
                        db.todoDao().deleteTask(adapter.getItemId(position))
                    }
                } else if (direction == ItemTouchHelper.RIGHT) {
                    GlobalScope.launch(Dispatchers.IO){
                        db.todoDao().finishTask(adapter.getItemId(position))
                    }
                }
            }

            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) = if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                val itemView = viewHolder.itemView

                val paint = Paint()
                val icon: Bitmap?

                //if it is moved right then marked it done.
                if (dX > 0) {
                    icon=getBitmap(this@MainActivity,R.mipmap.ic_check_black)
                    paint.color = Color.parseColor("#388E3C")
                    //Drawing rectangle
                    canvas.drawRect(
                        itemView.left.toFloat(), itemView.top.toFloat(),
                        itemView.left.toFloat() + dX, itemView.bottom.toFloat(), paint
                    )
                    if (icon != null) {
                        canvas.drawBitmap(
                            icon,
                            itemView.left.toFloat(),
                            //position of the check black.
                            itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height.toFloat()) / 2,
                            paint
                        )
                    }
                }
                //if it is moved left
                else if(dX<0)
                {
                    icon=getBitmap(this@MainActivity,R.mipmap.ic_cancel_black)
                    paint.color=Color.parseColor("#D32F2F")

                    //drawing rectangle
                    canvas.drawRect(itemView.right.toFloat()+dX,itemView.top.toFloat(),
                        itemView.right.toFloat(),itemView.bottom.toFloat(),paint)

                    if (icon != null) {
                        canvas.drawBitmap(
                            icon,
                            //position of delete icon.
                            itemView.right.toFloat()-icon.width,
                            itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height.toFloat()) / 2,
                            paint
                        )
                    }
                }
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
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(todoRv)
    }

    //converting vector image to bitmap.
    private fun getBitmap(context: Context, vectorDrawableId: Int): Bitmap? {
        var bitmap: Bitmap? = null
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            val vectorDrawable: Drawable? = context.getDrawable(vectorDrawableId)
            if (vectorDrawable != null) {
                bitmap = Bitmap.createBitmap(
                    vectorDrawable.intrinsicWidth,
                    vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
                )
            }
            val canvas = bitmap?.let { Canvas(it) }
            if (vectorDrawable != null) {
                if (canvas != null) {
                    vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
                }
            }
            if (canvas != null) {
                if (vectorDrawable != null) {
                    vectorDrawable.draw(canvas)
                }
            }
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), vectorDrawableId)
        }
        return bitmap
    }

}



