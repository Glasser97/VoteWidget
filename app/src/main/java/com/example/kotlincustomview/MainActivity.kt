package com.example.kotlincustomview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.vote_widget_layout.*
import kotlinx.coroutines.*

class MainActivity : VoteButton.VoteClickListener, AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch{
            var displaySource = withContext(Dispatchers.IO){
                //delay(2000)
                VoteWidgetDisplaySource("看好","不看好",29,71,10000,false,false,false)
            }
            vote_widget.fill(displaySource)
        }

        vote_widget.setOnClickListener{
            coroutineScope.launch{
                var displaySource = withContext(Dispatchers.IO){
                    delay(1000)
                    VoteWidgetDisplaySource("看好","不看好",29,71,10000,false,false,false)
                }
                vote_widget.fill(displaySource)
            }
        }
        //var mDisplaySource = VoteWidgetDisplaySource("看好","不看好",2,2,10000,false,false,false)
        //vote_widget.fill(mDisplaySource)
        voteButton.voteClickListener = this
        doubleMoveBar.leftNo = 24
        doubleMoveBar.rightNo = 76

    }


    override fun onClickLeft() {
        var sb:StringBuilder = StringBuilder(textView2.text)
        var value = sb.toString().toInt()+1
        textView2.text = "$value"
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch{
            var displaySource = withContext(Dispatchers.IO){
                delay(1000)
                VoteWidgetDisplaySource("看好","不看好",2,5,10000,false,true,true)
            }
            vote_widget.fill(displaySource)
        }

    }

    override fun onClickRight() {
        var sb:StringBuilder = StringBuilder(textView.text)
        var value = sb.toString().toInt()+1
        textView.text = "$value"
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch{
            var displaySource = withContext(Dispatchers.IO){
                delay(1000)
                VoteWidgetDisplaySource("看好","不看好",2,5,10000,false,true,false)
            }
            vote_widget.fill(displaySource)
        }
    }
}
