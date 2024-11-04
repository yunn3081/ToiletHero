package com.example.myapplication.toilethero.topToilet

import Toilet
import ToiletAdapter
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TopClassToiletFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var toiletAdapter: ToiletAdapter
    private val toiletsList = mutableListOf<Toilet>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_top_class_toilet, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewTopToilets)
        recyclerView.layoutManager = LinearLayoutManager(context)
        toiletAdapter = ToiletAdapter(toiletsList)
        recyclerView.adapter = toiletAdapter

        // 添加 ItemDecoration 设置项之间的间距
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.bottom = 16 // 设置底部间距为 16dp
            }
        })

        // 初始化 Firebase 数据库引用
        database = FirebaseDatabase.getInstance().getReference("restrooms")

        fetchTopTenToilets()

        return view
    }

    private fun fetchTopTenToilets() {
        database.orderByChild("averageOverallScore").limitToLast(10)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    toiletsList.clear()
                    for (toiletSnapshot in snapshot.children) {
                        val roomNumber = toiletSnapshot.child("roomNumber").getValue(String::class.java) ?: ""
                        val buildingName = toiletSnapshot.child("buildingName").getValue(String::class.java) ?: ""
                        val averageOverallScore = toiletSnapshot.child("averageOverallScore").getValue(Float::class.java) ?: 0f
                        val toilet = Toilet(roomNumber = roomNumber, buildingName = buildingName, averageOverallScore = averageOverallScore)

                        toiletsList.add(toilet)
                    }
                    // 按评分降序排序
                    toiletsList.sortByDescending { it.averageOverallScore }
                    toiletAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // 处理数据库错误
                }
            })
    }
}