package com.example.kotlincustomview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.vote_widget_layout.view.*

/**
 *
 * created by graysonzeng on 2019/11/28.
 * email: graysonzeng@futunn.com
 **/
class VoteWidget:RelativeLayout {
    companion object{
        const val TAG:String = "VoteWidget"
    }

    /**
     * 根布局
     */
    private var root:View? = null

    /**
     * 未完成投票的视图层
     */
    //左右按钮的点击控件
    private var mVoteButton:VoteButton? = null

    //剩余时间文案控件
    private var mRemainTimeTextView:TextView? = null

    /**
     * 已完成投票的视图层
     */
    //投票完成的动态进度条DoubleMoveBar
    private var mDoubleMoveBar:DoubleMoveBar? = null

    //投票完成后的左边标题选项
    private var mLeftTitleTextView:TextView? =null

    //投票完成后的右边标题选项
    private var mRightTitleTextView:TextView? = null

    //投票完成后的左边的百分比
    private var mLeftPercentTextView:TextView? = null

    //投票完成后右边的百分比
    private var mRightPercentTextView:TextView? = null

    //投票完成后的左边图像控件
    private var mLeftVoteResultIcon:View? = null

    //投票完成后的右边图像控件
    private var mRightVoteResultIcon:View? =null


    /**
     * 填入的视图数据
     */
    private var mVoteWidgetDisplaySource:VoteWidgetDisplaySource? =null
    private var mLeftTitle:String = "不看好"
    private var mRightTitle:String = "看好"
    private var mLeftNumber:Int = 0
    private var mRightNumber:Int = 0
    private var mDeadline:Long = 0
    private var isPermanent:Boolean = true
    private var isVoted:Boolean = true



    fun fill(displaySource:VoteWidgetDisplaySource){
        mVoteWidgetDisplaySource = displaySource
        mLeftTitle = displaySource.mLeftTitle
        mRightTitle = displaySource.mRightTitle
        mLeftNumber = displaySource.mLeftNumber
        mRightNumber = displaySource.mRightNumber
        mDeadline = displaySource.mDeadline
        isPermanent = displaySource.isPermanent
        isVoted = displaySource.isVoted

        if(isVoted){

        }else{

        }
    }

    /**
     * 构造器
     */
    constructor(context:Context):super(context){
        init(null, 0)
    }

    constructor(context: Context,attrs: AttributeSet):super(context,attrs){
        init(attrs,0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle:Int):super(context,attrs,defStyle){
        init(attrs,defStyle)
    }

    private fun init(attrs:AttributeSet?,defStyle:Int){
        root = LayoutInflater.from(context).inflate(R.layout.vote_widget_layout,this,true)

    }




    /**
     * 牛牛投票计算剩余时间
     */
    fun formatCountDownForSnsVote(countDownTime: Long): String? { // 倒计时时间比现在晚，则该阶段的倒计时已结束
        if (countDownTime <= 0) {
            return "投票已结束"
        }
        val ss = 1
        val mi = ss * 60
        val hh = mi * 60
        val dd = hh * 24
        val day = countDownTime / dd
        val hour = (countDownTime - day * dd) / hh
        val minute = (countDownTime - day * dd - hour * hh) / mi
        val second = (countDownTime - day * dd - hour * hh - minute * mi) / ss
        return if (day >= 90) {
            "剩余时间大于90天"
        } else if (day >= 1) {
            "剩余${day}天"
        } else if (hour >= 1) {
            "剩余${hour}小时"
        } else {
            if (minute < 1 && second != 0L) {
                "剩余1分钟"
            } else {
                "剩余${minute}分钟"
            }
        }
    }

}

data class VoteWidgetDisplaySource(var mLeftTitle:String, var mRightTitle:String, var mLeftNumber:Int, var mRightNumber:Int, var mDeadline:Long, var isPermanent:Boolean, var isVoted:Boolean)