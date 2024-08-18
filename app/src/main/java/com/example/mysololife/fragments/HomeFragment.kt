package com.example.mysololife.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mysololife.R
import com.example.mysololife.board.BoardModel
import com.example.mysololife.contentsList.BookmarkRVAdapter
import com.example.mysololife.contentsList.ContentModel
import com.example.mysololife.databinding.FragmentHomeBinding
import com.example.mysololife.utils.FBRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private lateinit var binding : FragmentHomeBinding

    val items = ArrayList<ContentModel>()
    val itemKeyList = ArrayList<String>()
    val bookmarkIdList = mutableListOf<String>()

    private val TAG = HomeFragment::class.java.simpleName

    lateinit var rvAdapter: BookmarkRVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d("HomeFragment", "onCreateView")

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_home, container, false )

        binding.homeTap.setOnClickListener {

        }
        binding.tipTap.setOnClickListener{

            Log.d("HomeFragment", "tipTap")
            it.findNavController().navigate(R.id.action_homeFragment_to_tipFragment)
        }

        binding.talkTap.setOnClickListener{
            it.findNavController().navigate(R.id.action_homeFragment_to_talkFragment)
        }

        binding.bookmarkTap.setOnClickListener {
            it.findNavController().navigate(R.id.action_homeFragment_to_bookmarkFragment)
        }

        binding.storeTap.setOnClickListener{
            it.findNavController().navigate(R.id.action_homeFragment_to_storeFragment)
        }

        rvAdapter = BookmarkRVAdapter(requireContext(), items, itemKeyList, bookmarkIdList)

        val rv : RecyclerView = binding.mainRV
        rv.layoutManager = GridLayoutManager(context,2)
        rv.adapter = rvAdapter

        getCategoryData()
        getRecentBoardData()
        return binding.root

    }

    private fun getRecentBoardData(){
        //최신 게시글 3개 가져오는 쿼리
        val recentPostsQuery = FBRef.boardRef.orderByKey().limitToLast(3)

        recentPostsQuery.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = snapshot.children.reversed()  //최신 게시글 순서로 정렬

                var index = 0
                for(postSnapshot in posts){
                    val post = postSnapshot.getValue(BoardModel::class.java)
                    val key = postSnapshot.key ?: continue

                    //해당하는 TextView에 텍스트 정렬
                    when (index) {
                        0 -> binding.board1.text = post?.title
                        1 -> binding.board2.text = post?.title
                        2 -> binding.board3.text = post?.title
                    }

                    index++//루프 내 index 증가
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database Error: ${error.message}")
            }

        })
    }

    private fun getCategoryData(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for(dataModel in dataSnapshot.children){

                    Log.d(TAG, dataModel.toString())
                    val item = dataModel.getValue(ContentModel::class.java)

                    items.add(item!!)
                    itemKeyList.add(dataModel.key.toString())

                }

                rvAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("ContentListActivity", "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.category1.addValueEventListener(postListener)
        FBRef.category2.addValueEventListener(postListener)
    }
}