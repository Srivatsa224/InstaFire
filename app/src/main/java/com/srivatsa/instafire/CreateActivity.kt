package com.srivatsa.instafire

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.srivatsa.instafire.models.Post
import com.srivatsa.instafire.models.User
import kotlinx.android.synthetic.main.activity_create.*
private const val TAG="CreateActivity"
private const val PICK_PHOTO_CODE =123

class CreateActivity : AppCompatActivity() {
    private var photoUri: Uri?=null
    private var signedInUser: User?=null
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

            //initiating inside on create method
        storageReference= FirebaseStorage.getInstance().reference
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

        btnPickImage.setOnClickListener {
            Log.i(TAG,"Open image picker on phone")
            //implicit intent
            val imagePickerIntent=Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type="image/*"
            if(imagePickerIntent.resolveActivity(packageManager)!=null){
                startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE)
            }
        }
        btnSubmit.setOnClickListener {
            hadlesubmitbutton()
        }


    }

    private fun hadlesubmitbutton() {
        if(photoUri==null){
            Toast.makeText(this,"Please select an image", Toast.LENGTH_SHORT).show()
            return
        }
        if(etDescription.text.isBlank()){
            Toast.makeText(this,"Please enter the description",Toast.LENGTH_SHORT).show()
            return
        }
        if(signedInUser==null){
            Toast.makeText(this,"User not signed in, please wait", Toast.LENGTH_LONG).show()
            return
        }


       btnSubmit.isEnabled=false
        val photoUploadUri=photoUri as Uri
        val photoReference=storageReference.child("/images/${System.currentTimeMillis()}-photo.jpg")


        photoReference.putFile(photoUploadUri)
            .continueWithTask {photoUploadTask->
                Log.i(TAG, "photo upload url {photoUploadTask.result?.bytesTransferred}")
                photoReference.downloadUrl

            }.continueWithTask{downloadUrlTask->
                val post=Post(
                    etDescription.text.toString(),
                    downloadUrlTask.result.toString(),
                    System.currentTimeMillis(),
                    signedInUser)

                firestoreDb.collection("posts").add(post)
            }.addOnCompleteListener { postCreationTask->
                btnSubmit.isEnabled=true
                if(!postCreationTask.isSuccessful){
                    Log.e(TAG,"Exception during uploading",postCreationTask.exception)
                    Toast.makeText(this,"Post upload failed!",Toast.LENGTH_SHORT).show()
                }

                etDescription.text.clear()
                imageView.setImageResource(0)
                Toast.makeText(this,"Congrats, post uploaded successfully!",Toast.LENGTH_SHORT).show()
                val profileIntent=Intent(this,ProfileActivity::class.java)
                profileIntent.putExtra(EXTRA_USERNAME,signedInUser?.username)
                startActivity(profileIntent)
                finish()
            }
        //retrieve image url of uploaded image
        //create a post object with url and add to posts collection











    }

    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== PICK_PHOTO_CODE){
            if(resultCode==Activity.RESULT_OK){
                 photoUri=data?.data
                Log.i(TAG,"photoUri $photoUri")
                imageView.setImageURI(photoUri)


            }
            else{
                Toast.makeText(this,"Image picker action cancelled..",Toast.LENGTH_SHORT).show()
            }
        }
    }
}
