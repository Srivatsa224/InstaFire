package com.srivatsa.instafire

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.srivatsa.instafire.models.Post
import com.srivatsa.instafire.models.User
import kotlinx.android.synthetic.main.activity_posts.*

private const val TAG="PostsActivity"
public const val EXTRA_USERNAME="EXTRA_USERNAME"

open class PostsActivity : AppCompatActivity() {

    //declaring inside onCreate, once we declare it here, it should not be null, so no ? at end of FirebaseFirestore

    //make query to Firestore to retrieve data
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var posts:MutableList<Post>
    private lateinit var adapter:PostsAdapter
    private var signedInUser: User?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)

        //create layout which refers one post - Done
        //create data source -
        posts= mutableListOf()
        //create adapter
        adapter= PostsAdapter(this, posts)
        //bind the adapter and layout manager to RecyclerView
        rvPosts.adapter=adapter
        rvPosts.layoutManager=LinearLayoutManager(this)
        firestoreDb= FirebaseFirestore.getInstance()

        firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser=userSnapshot.toObject(User::class.java)
                    Log.i(TAG,"signed in user: $signedInUser")
            }
            .addOnFailureListener{ exception ->
                Log.i(TAG,"Failed to fetch users", exception)
            }

        var postsReference=firestoreDb
            .collection("posts")
            .limit(20)
            .orderBy("creation_time_ms",Query.Direction.DESCENDING)

        val username= intent.getStringExtra(EXTRA_USERNAME)
        if(username!=null){
            supportActionBar?.title=username
            supportActionBar?.setIcon(R.drawable.instafire)
           postsReference= postsReference.whereEqualTo("user.username",username)

        }

        //whenever there is any change in the db this gives us data
        postsReference.addSnapshotListener { snapshot, exception ->
            if(exception!==null || snapshot==null){
                Log.e(TAG,"Exception when querying posts")
                return@addSnapshotListener
            }

           val postList=snapshot.toObjects(Post::class.java)
            posts.clear()
            posts.addAll(postList)
            adapter.notifyDataSetChanged()
            for (post in postList){
                Log.i(TAG,"Post ${post}")
            }
        }

        fabCreate.setOnClickListener {
            val intent=Intent(this,CreateActivity::class.java)
            startActivity(intent)
        }




    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
       menuInflater.inflate(R.menu.menu_posts,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.menu_profile){
            val intent=Intent(this,ProfileActivity::class.java)
            intent.putExtra(EXTRA_USERNAME,signedInUser?.username)
            startActivity(intent)

        }
        return super.onOptionsItemSelected(item)
    }
}
