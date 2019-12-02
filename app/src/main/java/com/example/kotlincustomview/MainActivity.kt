package com.example.kotlincustomview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var mDisplaySource = VoteWidgetDisplaySource("看好","不看好",73,27,1000000,false,true,false)
        vote_widget.fill(mDisplaySource)
        doubleMoveBar.leftNo = 24
        doubleMoveBar.rightNo = 76
    }
}
