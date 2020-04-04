package com.srivatsa.instafire

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG="PostsActivity"
class PostsActivity : AppCompatActivity() {

    //declaring inside onCreate, once we declare it here, it should not be null, so no ? at end of FirebaseFirestore

    //make query to Firestore to retrieve data
    private lateinit var firestoreDb: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)

        firestoreDb= FirebaseFirestore.getInstance()
        val postsReference=firestoreDb.collection("posts")
        //whenever there is any change in the db this gives us data
        postsReference.addSnapshotListener { snapshot, exception ->
            if(exception!==null || snapshot==null){
                Log.e(TAG,"Exception when querying posts")
                return@addSnapshotListener
            }
            for (document in snapshot.documents){
                Log.i(TAG,"Document ${document.id}:${document.data}")
            }
        }








    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
       menuInflater.inflate(R.menu.menu_posts,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.menu_profile){
            val intent=Intent(this,ProfileActivity::class.java)
            startActivity(intent)

        }
        return super.onOptionsItemSelected(item)
    }
}
