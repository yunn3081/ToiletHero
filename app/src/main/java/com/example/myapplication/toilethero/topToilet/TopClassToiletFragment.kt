package com.example.myapplication.toilethero.topToilet

import Toilet
import ToiletAdapter
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
                        val roomID = toiletSnapshot.key ?: ""
                        val roomNumber = toiletSnapshot.child("roomNumber").getValue(String::class.java) ?: ""
                        val buildingName = toiletSnapshot.child("buildingName").getValue(String::class.java) ?: ""
                        val averageOverallScore = toiletSnapshot.child("averageOverallScore").getValue(Float::class.java) ?: 0f
                        val toilet = Toilet(roomID = roomID, roomNumber = roomNumber, buildingName = buildingName, averageOverallScore = averageOverallScore)

                        toiletsList.add(toilet)
                    }
                    // 按评分降序排序
                    toiletsList.sortByDescending { it.averageOverallScore }

                    // 初始化 Adapter 並設置到 RecyclerView
                    toiletAdapter = ToiletAdapter(toiletsList) { roomID ->
                        navigateToToiletProfile(roomID)
                    }
                    recyclerView.adapter = toiletAdapter

                    // 添加 ItemDecoration 設置項之間的間距
                    recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
                        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                            outRect.bottom = 16 // 設置底部間距為 16dp
                        }
                    })

                    // 通知 Adapter 更新資料
                    toiletAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("TopClassToiletFragment", "Error fetching data: ${error.message}")
                }
            })
    }


    private fun navigateToToiletProfile(roomID: String) {
        Log.d("TopClassToiletFragment", "Navigating to ToiletProfileFragment with roomID: $roomID")
        val navController = findNavController() // 使用 NavController
        val bundle = Bundle().apply {
            putString("roomID", roomID)
        }
        navController.navigate(R.id.toiletProfileFragment, bundle)
    }

}