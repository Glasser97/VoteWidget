package com.example.kotlincustomview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.min


/**
 * TODO: document your custom view class.
 */
class VoteButton : View {


    /**
     * 点击态相关变量,按下的坐标
     */
    private var downX:Float = 0f
    private var downY:Float = 0f


    private var mHeight:Int = 0
    private var mWidth:Int = 0

    /**
     * 定义左右按钮文本
     */
    private var mLeftString:String = ""
    private var mRightString:String = ""


    /**
     * 点击事件接口
     */
    var voteClickListener:VoteClickListener? = null

    /**
     * 定义圆角
     */
    private lateinit var corner:CornerPathEffect

    private var mTextColor:Int = Color.WHITE
    private var mLeftColor:Int = Color.RED
    private var mRightColor:Int = Color.BLUE

    /**
     * 定义渐变色的shader
     */
    private var leftGradient: LinearGradient? =null
    private var rightGradient: LinearGradient? =null
    private var mTextSize:Float = 12F
    private var mLeftTextPaint:TextPaint = TextPaint()
    private var mRightTextPaint:TextPaint = TextPaint()
    private var leftColorPaint:Paint = Paint()
    private var rightColorPaint:Paint = Paint()

    /**
     * 左右文字的大小与居中需要唯一的距离
     */
    private var mLeftTextWidth:Float = 0F
    private var mLeftTextHeight:Float = 0F
    private var mRightTextWidth:Float = 0F
    private var mRightTextHeight:Float = 0F
    private var mLeftTextHorizonMovement:Float = 0F
    private var mLeftTextVerticalMovement:Float = 0F
    private var mRightTextHorizonMovement:Float = 0F
    private var mRightTextVerticalMovement:Float = 0F

    /**
     * 斜线的下端宽度,和按钮间缝隙宽度
     */
    private var mSlashUnderWidth:Float = 0F
    private var mSlashWidth:Float = 5F

    /**
     * 定义绘制的左右Path
     */
    private var leftButtonPath:Path = Path()
    private var rightButtonPath:Path = Path()

    /**
     *  定义颜色数组
     */
    private lateinit var leftColors:IntArray
    private lateinit var rightColors:IntArray
    private lateinit var colorPositions:FloatArray

    /**
     * 定义两个梯形的四个顶点边距
     */
    private var leftLeft:Float = 0F
    private var leftRight:Float = 0F
    private var leftTop:Float = 0F
    private var leftBottom:Float = 0F
    private var rightLeft:Float = 0F
    private var rightRight:Float = 0F
    private var rightTop:Float = 0F
    private var rightBottom:Float = 0F

    /**
     * 左右预览进度条的数据
     */
    private var preProcessBackColor:Int = 0x66D7DBDE
    private var preProcessColor:Int = 0xFFFFFF
    private var leftNumber:Int = 0
    private var rightNumber:Int = 0

    constructor(context:Context):super(context){
        init(null, 0)
    }

    constructor(context: Context,attrs: AttributeSet):super(context,attrs){
        init(attrs,0)
    }

    constructor(context: Context,attrs: AttributeSet,defStyle: Int):super(context,attrs,defStyle){
        init(attrs,defStyle)
    }

    /**
     * 填入初始化数据
     */
    fun fill(displaySource: VoteButtonDisplaySource){
        this.mTextColor = displaySource.mTextColor
        this.mLeftColor = displaySource.mLeftColor
        this.mRightColor = displaySource.mRightColor
        this.mSlashUnderWidth = displaySource.mSlashUnderWidth
        this.mSlashWidth = displaySource.mSlashWidth
        leftColorPaint.color = this.mLeftColor
        rightColorPaint.color = this.mRightColor
        setColors(this.mLeftColor,this.mRightColor)

        leftGradient = LinearGradient(leftLeft,leftBottom, leftRight, leftTop,
            leftColors, colorPositions,Shader.TileMode.CLAMP)
        rightGradient = LinearGradient(rightRight,rightTop, rightLeft,rightBottom,
            rightColors,colorPositions,Shader.TileMode.CLAMP)
        //在填入文字之前不重绘
    }

    /**
     * 填入左右选项文字数据
     */
    fun fill(mLeftString:String,mRightString:String,mLeftNo:Int,mRightNo:Int){
        this.mLeftString = mLeftString
        this.mRightString = mRightString
        //重测字体的大小,重绘按钮
        invalidateTextPaintAndMeasurements()
        invalidate()

    }

    private fun init(attrs:AttributeSet?,defStyle:Int){
        val a:TypedArray = context.obtainStyledAttributes(attrs,R.styleable.VoteButton,defStyle,0)

        //mLeftString = a.getString(R.styleable.VoteButton_leftString)
        //mRightString = a.getString(R.styleable.VoteButton_rightString)
        mTextColor = a.getColor(R.styleable.VoteButton_textColor,mTextColor)
        mLeftColor = a.getColor(R.styleable.VoteButton_leftColor,mLeftColor)
        mRightColor = a.getColor(R.styleable.VoteButton_rightColor,mRightColor)
        mSlashUnderWidth = a.getDimension(R.styleable.VoteButton_slashUnderWidth,mSlashUnderWidth)
        mSlashWidth = a.getDimension(R.styleable.VoteButton_slashWidth,mSlashWidth)
        mTextSize = a.getDimension(R.styleable.VoteButton_textSize,mTextSize)

        a.recycle()

        //初始化圆角
        corner = CornerPathEffect(15F)

        //初始化渐变颜色数组和渐变位置数组
        setColors(mLeftColor, mRightColor)

        //初始化画笔
        leftColorPaint.flags = Paint.ANTI_ALIAS_FLAG
        leftColorPaint.color = mLeftColor
        leftColorPaint.style = Paint.Style.FILL
        leftColorPaint.pathEffect = corner
        rightColorPaint.flags = Paint.ANTI_ALIAS_FLAG
        rightColorPaint.color = mRightColor
        rightColorPaint.style = Paint.Style.FILL
        rightColorPaint.pathEffect = corner

        invalidateTextPaintAndMeasurements()

    }

    private fun invalidateTextPaintAndMeasurements(){
        mLeftTextPaint.flags = Paint.ANTI_ALIAS_FLAG
        mLeftTextPaint.textAlign = Paint.Align.LEFT
        mRightTextPaint.flags = Paint.ANTI_ALIAS_FLAG
        mRightTextPaint.textAlign = Paint.Align.RIGHT
        mLeftTextPaint.textSize = mTextSize
        mLeftTextPaint.color = mTextColor
        mRightTextPaint.textSize = mTextSize
        mRightTextPaint.color = mTextColor
        mLeftTextWidth = mLeftTextPaint.measureText(mLeftString)
        mRightTextWidth = mRightTextPaint.measureText(mRightString)
        val leftFontMetrics:Paint.FontMetrics = mLeftTextPaint.fontMetrics
        mLeftTextHeight = leftFontMetrics.bottom
        val rightFontMetrics:Paint.FontMetrics = mRightTextPaint.fontMetrics
        mRightTextHeight = rightFontMetrics.bottom
        //计算文字居中需要位移的距离
        mLeftTextHorizonMovement = -mLeftTextWidth/2
        mLeftTextVerticalMovement = -(leftFontMetrics.ascent+leftFontMetrics.descent)/2
        mRightTextHorizonMovement = mRightTextWidth/2
        mRightTextVerticalMovement = -(rightFontMetrics.ascent+rightFontMetrics.descent)/2


    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mHeight = measuredHeight(heightMeasureSpec)
        mWidth = measuredWidth(widthMeasureSpec)
        setMeasuredDimension(mWidth,mHeight)
    }

    private fun measuredHeight(heightMeasureSpec: Int):Int{
        var result:Int
        val specMode:Int = MeasureSpec.getMode(heightMeasureSpec)
        val specSize:Int = MeasureSpec.getSize(heightMeasureSpec)
        if(specMode == MeasureSpec.EXACTLY){
            result = specSize
        }else{
            result = (mTextSize+context.resources.getDimension(R.dimen.dp_20)).toInt()
            if(specMode == MeasureSpec.AT_MOST){
                result = min(result,specSize)
            }
        }
        return result
    }

    private fun measuredWidth(widthMeasureSpec: Int):Int{
        return MeasureSpec.getSize(widthMeasureSpec)
    }

    /**
     * 在这个回调中创建ondraw 中需要的Shader对象
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        //重新创建新的Gradient
        val contentWidth = width-paddingLeft-paddingRight
        val contentHeight = height-paddingTop-paddingBottom
        val leftRectWidth = (contentWidth-mSlashUnderWidth-mSlashWidth)/2
        val rightRectWidth = (contentWidth-mSlashUnderWidth-mSlashWidth)/2
        leftTop = paddingTop.toFloat()
        leftLeft = paddingLeft.toFloat()
        leftBottom = paddingTop+contentHeight.toFloat()
        leftRight = paddingLeft+leftRectWidth+mSlashUnderWidth

        rightTop = paddingTop.toFloat()
        rightRight = width-paddingRight.toFloat()
        rightLeft = width-paddingRight-rightRectWidth-mSlashUnderWidth
        rightBottom = paddingTop+contentHeight.toFloat()

        leftGradient = LinearGradient(leftLeft,leftBottom,
            leftRight, leftTop,
            leftColors, colorPositions,Shader.TileMode.CLAMP)
        rightGradient = LinearGradient(rightRight,rightTop,
            rightLeft,rightBottom,
            rightColors,colorPositions,Shader.TileMode.CLAMP)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        setBackgroundColor(Color.TRANSPARENT)

        val contentWidth = width-paddingLeft-paddingRight
        val contentHeight = height-paddingTop-paddingBottom

        val halfH = contentHeight/2
        val leftRectWidth = (contentWidth-mSlashUnderWidth-mSlashWidth)/2
        val rightRectWidth = (contentWidth-mSlashUnderWidth-mSlashWidth)/2

        //计算两边梯形的四个顶点
        leftTop = paddingTop.toFloat()
        leftLeft = paddingLeft.toFloat()
        leftBottom = paddingTop+contentHeight.toFloat()
        leftRight = paddingLeft+leftRectWidth+mSlashUnderWidth

        rightTop = paddingTop.toFloat()
        rightRight = width-paddingRight.toFloat()
        rightLeft = width-paddingRight-rightRectWidth-mSlashUnderWidth
        rightBottom = paddingTop+contentHeight.toFloat()

        //使用Path绘制出左边的按钮,没有从顶点开始是因为圆角可能会不能闭合,逆时针旋转绘制
        leftButtonPath.moveTo(leftRight-mSlashUnderWidth, leftTop)
        leftButtonPath.lineTo(leftLeft,leftTop)
        leftButtonPath.lineTo(leftLeft,leftBottom)
        leftButtonPath.lineTo(leftRight-mSlashUnderWidth,leftBottom)
        leftButtonPath.lineTo(leftRight,leftTop)
        leftButtonPath.close()
        leftColorPaint.shader = leftGradient
        canvas?.drawPath(leftButtonPath,leftColorPaint)

        //同样的使用Path绘制出右边的按钮,逆时针旋转绘制
        rightButtonPath.moveTo(rightLeft+mSlashUnderWidth,rightBottom)
        rightButtonPath.lineTo(rightRight,rightBottom)
        rightButtonPath.lineTo(rightRight,rightTop)
        rightButtonPath.lineTo(rightLeft+mSlashUnderWidth,rightTop)
        rightButtonPath.lineTo(rightLeft,rightBottom)
        rightButtonPath.close()
        rightColorPaint.shader = rightGradient
        canvas?.drawPath(rightButtonPath,rightColorPaint)

        //计算文字的中点,绘制文字
        val leftHorizonMidpoint:Float = paddingLeft+leftRectWidth/2
        val leftVerticalMidpoint:Float = paddingTop+halfH.toFloat()
        val rightHorizonMidpoint:Float = width-paddingRight-rightRectWidth/2
        val rightVerticalMidpoint:Float = paddingTop+halfH.toFloat()
        //绘制文字
        canvas?.drawText(mLeftString, leftHorizonMidpoint+mLeftTextHorizonMovement,
            leftVerticalMidpoint+mLeftTextVerticalMovement,mLeftTextPaint)
        canvas?.drawText(mRightString, rightHorizonMidpoint+mRightTextHorizonMovement,
            rightVerticalMidpoint+mRightTextVerticalMovement,mRightTextPaint)
    }

    /**
     * 设置左边按钮点击下来
     */
    private fun setLeftPressed(){
        leftColors = intArrayOf(brighterColor(mLeftColor,0.7F),brighterColor(mLeftColor, 0.7F))
        leftGradient = LinearGradient(leftLeft,leftBottom, leftRight, leftTop,
            leftColors, colorPositions,Shader.TileMode.CLAMP)
        this.invalidate()
    }
    /**
     * 设置右边按钮点击下来
     */
    private fun setRightPressed(){
        rightColors = intArrayOf(brighterColor(mRightColor,0.7F),brighterColor(mRightColor,0.7F))
        rightGradient = LinearGradient(rightRight,rightTop, rightLeft,rightBottom,
            rightColors,colorPositions,Shader.TileMode.CLAMP)
        this.invalidate()
    }

    /**
     * 取消按下态的Shadow
     */
    private fun cancelPressShadow(){
        setColors(mLeftColor,mRightColor)
        leftGradient = LinearGradient(leftLeft,leftBottom,leftRight ,leftTop ,
            leftColors, colorPositions,Shader.TileMode.CLAMP)
        rightGradient = LinearGradient(rightRight,rightTop, rightLeft,rightBottom,
            rightColors, colorPositions,Shader.TileMode.CLAMP)
        this.invalidate()
    }

    /**
     * 点击事件的处理
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action){
            MotionEvent.ACTION_DOWN ->{
                downX = event.x
                downY = event.y
                //发送延时runnable,进行着色
                if(isLeft(downX, downY)){
                    setLeftPressed()
                }else if(isRight(downX, downY)){
                    setRightPressed()
                }
            }
            MotionEvent.ACTION_MOVE ->{
                //判断移动的时候是否还在对应按钮内,不在则取消点击态
                if(isLeft(downX,downY) && !isLeft(event.x,event.y)){
                    cancelPressShadow()
                }else if(isRight(downX,downY) && !isRight(event.x,event.y)){
                    cancelPressShadow()
                }
            }
            MotionEvent.ACTION_UP -> {
                if(isLeft(downX,downY) && isLeft(event.x,event.y)){
                    voteClickListener?.onClickLeft()
                }else if(isRight(downX,downY) && isRight(event.x,event.y)){
                    voteClickListener?.onClickRight()
                }
                cancelPressShadow()
            }
//            MotionEvent.ACTION_CANCEL ->{
//                cancelPressShadow()
//            }
        }
        return true
    }


    /**
     * 判断点击的事件是不是是落在左边的梯形按钮上
     * @x 这是以按钮的左下角为原点的横坐标
     * @ya y是获取的点击纵坐标
     * y 是以按钮的左下角为原点的点击纵坐标
     */

    private fun isLeft(x:Float,ya:Float):Boolean{
        val y = height- ya
        val resLeft = height*x - mSlashUnderWidth*y + mSlashUnderWidth*height-height*leftRight
        return resLeft <= 0 && x > paddingLeft && y < height-paddingTop && y > paddingBottom
    }

    /**
     * 判断点击的事件是不是是落在左边的梯形按钮上
     * @x 这是以按钮的左下角为原点的横坐标
     * @ya y是获取的点击纵坐标
     * y 是以按钮的左下角为原点的点击纵坐标
     */

    private fun isRight(x:Float,ya:Float):Boolean{
        val y = height - ya
        val resRight = height*x - mSlashUnderWidth*y - height*rightLeft
        return resRight > 0 && x < width-paddingRight && y < height-paddingTop && y > paddingBottom
    }


    /**
     * 计算获取更高的亮度的颜色
     */
    private fun brighterColor(color:Int, brighterK:Float):Int{
        val alpha:Int = color and 0xff000000.toInt()
        var red:Int = (((color and 0x00ff0000) shr 16) * brighterK).toInt()
        var green:Int = (((color and 0x0000ff00) shr 8) * brighterK).toInt()
        var blue:Int = ((color and 0x0000ff) * brighterK).toInt()
        red = if(red > 255) 255 else red
        green = if(red > 255) 255 else green
        blue = if(blue > 255) 255 else blue
        return alpha + (red shl 16) + (green shl 8) + blue
    }


    private fun setColors(leftColor:Int, rightColor:Int){
        leftColors = intArrayOf(leftColor,brighterColor(leftColor,1.2F))
        rightColors = intArrayOf(rightColor,brighterColor(rightColor,1.2F))
        colorPositions = floatArrayOf(0.3F,0.8F)
    }

    //点击事件接口
    interface VoteClickListener{
        fun onClickLeft()
        fun onClickRight()
    }
}

data class VoteButtonDisplaySource(var mLeftColor: Int, var mRightColor: Int,var mTextColor:Int,var mSlashUnderWidth:Float,var mSlashWidth: Float)
