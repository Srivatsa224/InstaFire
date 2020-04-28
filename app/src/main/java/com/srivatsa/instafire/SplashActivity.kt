package com.srivatsa.instafire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView

class SplashActivity : AppCompatActivity() {

    private lateinit var image:ImageView
    lateinit var handler:Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        image=findViewById(R.id.insatfire)

        handler=Handler()
        handler.postDelayed({
            val i=Intent(this, PostsActivity::class.java)
            startActivity(i)
            finish()
        },3000)//3 sec delay
    }





}
