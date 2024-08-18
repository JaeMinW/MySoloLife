package com.example.mysololife.board

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.mysololife.R
import com.example.mysololife.comment.CommentLVAdapter
import com.example.mysololife.comment.CommentModel
import com.example.mysololife.databinding.ActivityBoardInsideBinding
import com.example.mysololife.utils.FBAuth
import com.example.mysololife.utils.FBRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.storage

class BoardInsideActivity : AppCompatActivity() {

    private val TAG = BoardInsideActivity::class.java.simpleName

    private lateinit var binding : ActivityBoardInsideBinding

    private lateinit var key:String

    private val commentDataList = mutableListOf<CommentModel>()

    private lateinit var commentAdapter : CommentLVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_board_inside)

        binding.boardSettingIcon.setOnClickListener {
            showDialog()
        }
//        1번방법
//        val title = intent.getStringExtra("title").toString()
//        val content = intent.getStringExtra("content").toString()
//        val time = intent.getStringExtra("time").toString()
//
//        binding.titleArea.text =title
//        binding.timeArea.text = time
//        binding.contentArea.text = content


//        2번 방법
        key = intent.getStringExtra("key").toString()

        getBoardData(key)
        getImageData(key)

        binding.commentBtn.setOnClickListener {
            insertComment(key)
        }

        commentAdapter = CommentLVAdapter(commentDataList)
        binding.commentLV.adapter = commentAdapter

        getCommentData(key)
    }

    fun getCommentData(key : String){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                commentDataList.clear()

                for(dataModel in dataSnapshot.children){
                    val item = dataModel.getValue(CommentModel::class.java)
                    commentDataList.add(item!!)
                }
                //데이터를 받아오고 동기화
                //동기화 안하면 ListView에 아무것도 나오지 않음
                commentAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.commentRef.child(key).addValueEventListener(postListener)
    }

    private fun insertComment(key: String) {
        //데이터 저장 방식
        //comment
        //  - BoardKey
        //      - CommentKey
        //          - CommentData
        //          - CommentData
        //          - CommentData

        val commentText = binding.commentArea.text.toString()

        if(commentText.isEmpty()){
            Toast.makeText(this,"댓글을 입력하세요", Toast.LENGTH_LONG).show()
            return
        }

        FBRef.commentRef
            .child(key)
            .push()
            .setValue(
                CommentModel(
                    commentText,
                FBAuth.getTime())
            )
        Toast.makeText(this, "댓글 입력 완료", Toast.LENGTH_LONG).show()
        binding.commentArea.setText("")
    }

    private fun showDialog(){
        if(!isFinishing && !isDestroyed) {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("게시글 수정/삭제")

            val alertDialog = mBuilder.show()
            alertDialog.findViewById<Button>(R.id.editBtn)?.setOnClickListener {
                val intent = Intent(this, BoardEditActivity::class.java)
                intent.putExtra("key", key)
                startActivity(intent)
            }
            alertDialog.findViewById<Button>(R.id.removeBtn)?.setOnClickListener {
                FBRef.boardRef.child(key).removeValue().addOnCompleteListener {
                    if (it.isSuccessful) {
                        deleteImageData(key)
                        Toast.makeText(this, "삭제완료", Toast.LENGTH_LONG).show()
                        alertDialog.dismiss()
                        finish()
                    }else{
                        Toast.makeText(this, "삭제실패", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }else{
            Log.w(TAG, "Activity is finishing or destroyed, dialog not shown")
        }
    }

    private fun deleteImageData(key: String) {
        val storageReference = Firebase.storage.reference.child(key + ".png")
        storageReference.delete().addOnCompleteListener{task ->
            if(task.isSuccessful){
                Log.d(TAG, "Image deleted successfully")
            }else{
                Log.w(TAG, "Failed to delete image")
            }
        }
    }
    private fun getImageData(key: String) {
        // Reference to an image file in Cloud Storage
        val storageReference = Firebase.storage.reference.child(key + ".png")

        // ImageView in your Activity
        val imageViewFromFB = binding.getImageArea

        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if(task.isSuccessful){

                Glide.with(this)
                    .load(task.result)
                    .into(imageViewFromFB)

            }else{
                binding.getImageArea.isVisible = false
            }
        })
    }

    private fun getBoardData(key : String){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                try {
                    val dataModel = dataSnapshot.getValue(BoardModel::class.java)
                    Log.d(TAG, dataModel!!.title!!)

                    binding.titleArea.text = dataModel!!.title
                    binding.timeArea.text = dataModel!!.time
                    binding.contentArea.text = dataModel!!.content

                    val myUid = FBAuth.getUid()
                    val writerUid = dataModel.uid

                    if(myUid.equals(writerUid)){
                        Log.d(TAG,"내가 쓴 글")
                        binding.boardSettingIcon.isVisible = true
                    }else{
                        Log.d(TAG,"내가 쓴 글 아님")
                    }

                } catch (e: Exception) {
                    Log.d(TAG, "삭제완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.boardRef.child(key).addValueEventListener(postListener)
    }
}