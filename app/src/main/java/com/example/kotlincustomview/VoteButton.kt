package com.example.kotlincustomview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.LinearGradient
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import kotlin.math.min
import android.graphics.LinearGradient as LinearGradient1

/**
 * TODO: document your custom view class.
 */
class VoteButton : View {

    private var mHeight:Int = 0
    private var mWidth:Int = 0
    //定义左右按钮文本
    private var mLeftString:String? = null
    private var mRightString:String? = null
    //点击事件接口
    var voteClickListener:VoteClickListener? = null
    //定义圆角
    private lateinit var corner:CornerPathEffect

    private var mTextColor:Int = Color.WHITE
    private var mLeftColor:Int = Color.RED
    private var mRightColor:Int = Color.BLUE

    //定义渐变色的shader
    private var leftGradient: LinearGradient? =null
    private var rightGradient: LinearGradient? =null

    private var mLeftNo:Int = 0
    private var mRightNo:Int = 0
    private var mTextSize:Float = 12F
    private var mSlashWidth:Float = 5F

    private var mLeftTextPaint:TextPaint = TextPaint()
    private var mRightTextPaint:TextPaint = TextPaint()
    private var leftColorPaint:Paint = Paint()
    private var rightColorPaint:Paint = Paint()
    private var mLeftTextWidth:Float = 0F
    private var mLeftTextHeight:Float = 0F
    private var mRightTextWidth:Float = 0F
    private var mRightTextHeight:Float = 0F

    private var mSlashUnderWidth:Float = 0F

    //定义绘制的左右Path
    private var leftButtonPath:Path = Path()
    private var rightButtonPath:Path = Path()

    //定义颜色数组
    private lateinit var leftColors:IntArray
    private lateinit var rightColors:IntArray
    private lateinit var colorPositions:FloatArray

    constructor(context:Context):super(context){
        init(null, 0)
    }

    constructor(context: Context,attrs: AttributeSet):super(context,attrs){
        init(attrs,0)
    }

    constructor(context: Context,attrs: AttributeSet,defStyle: Int):super(context,attrs,defStyle){
        init(attrs,defStyle)
    }

    private fun init(attrs:AttributeSet?,defStyle:Int){
        val a:TypedArray = context.obtainStyledAttributes(attrs,R.styleable.VoteButton,defStyle,0)

        mLeftString = a.getString(R.styleable.VoteButton_leftString)
        mRightString = a.getString(R.styleable.VoteButton_rightString)
        mTextColor = a.getColor(R.styleable.VoteButton_textColor,mTextColor)
        mLeftColor = a.getColor(R.styleable.VoteButton_leftColor,mLeftColor)
        mRightColor = a.getColor(R.styleable.VoteButton_rightColor,mRightColor)
        mSlashUnderWidth = a.getDimension(R.styleable.VoteButton_slashUnderWidth,mSlashUnderWidth)
        mSlashWidth = a.getDimension(R.styleable.VoteButton_slashWidth,mSlashWidth)
        mTextSize = a.getDimension(R.styleable.VoteButton_textSize,mTextSize)

        a.recycle()

        //初始化圆角
        corner = CornerPathEffect(10F)

        //初始化渐变颜色数组和渐变位置数组
        setColors(mLeftColor, mRightColor)
        colorPositions = floatArrayOf(0.3F,0.8F)

        //初始化画笔
        mLeftTextPaint.flags = Paint.ANTI_ALIAS_FLAG
        mLeftTextPaint.textAlign = Paint.Align.LEFT
        mRightTextPaint.flags = Paint.ANTI_ALIAS_FLAG
        mRightTextPaint.textAlign = Paint.Align.RIGHT
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
        mLeftTextPaint.textSize = mTextSize
        mLeftTextPaint.color = mTextColor
        mRightTextPaint.textSize = mTextSize
        mLeftTextPaint.color = mTextColor
        mLeftTextWidth = mLeftTextPaint.measureText(mLeftString)
        mRightTextWidth = mRightTextPaint.measureText(mRightString)
        val leftFontMetrics:Paint.FontMetrics = mLeftTextPaint.fontMetrics
        mLeftTextHeight = leftFontMetrics.bottom
        val rightFontMetrics:Paint.FontMetrics = mRightTextPaint.fontMetrics
        mRightTextHeight = rightFontMetrics.bottom

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

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        setBackgroundColor(Color.TRANSPARENT)

        val contentWidth = width-paddingLeft-paddingRight
        val contentHeight = height-paddingTop-paddingBottom

        val halfH = contentHeight/2
        val leftRectWidth = contentWidth/2
        val halfSlash:Int = (mSlashUnderWidth/2+mSlashWidth/2).toInt()

        //判断是否已经有渐变色shader,没有的话创建
        if(leftGradient == null){
            leftGradient = LinearGradient(paddingLeft.toFloat(),paddingTop+halfH.toFloat(),
                paddingLeft+leftRectWidth-halfSlash+mSlashUnderWidth, paddingTop.toFloat(),
                leftColors, colorPositions,Shader.TileMode.CLAMP)
        }
        if(rightGradient == null){
            rightGradient = LinearGradient(width-paddingRight.toFloat(),paddingTop+halfH.toFloat(),
                paddingLeft+leftRectWidth+halfSlash-mSlashUnderWidth,paddingTop+contentHeight.toFloat(),
                rightColors,colorPositions,Shader.TileMode.CLAMP)
        }

        //使用Path绘制出左边的按钮,没有从顶点开始是因为圆角可能会不能闭合,逆时针旋转绘制
        leftButtonPath.moveTo(paddingLeft+leftRectWidth-halfSlash.toFloat(), paddingTop.toFloat())
        leftButtonPath.lineTo(paddingLeft.toFloat(),paddingTop.toFloat())
        leftButtonPath.lineTo(paddingLeft.toFloat(),paddingTop+contentHeight.toFloat())
        leftButtonPath.lineTo(paddingLeft+leftRectWidth-halfSlash.toFloat(),paddingTop+contentHeight.toFloat())
        leftButtonPath.lineTo(paddingLeft+leftRectWidth-halfSlash+mSlashUnderWidth,paddingTop.toFloat())
        leftButtonPath.close()
        leftColorPaint.shader = leftGradient
        canvas?.drawPath(leftButtonPath,leftColorPaint)

        //同样的使用Path绘制出右边的按钮,逆时针旋转绘制
        rightButtonPath.moveTo(paddingLeft+leftRectWidth+halfSlash.toFloat(),paddingTop+contentHeight.toFloat())
        rightButtonPath.lineTo(width-paddingRight.toFloat(),paddingTop+contentHeight.toFloat())
        rightButtonPath.lineTo(width-paddingRight.toFloat(),paddingTop.toFloat())
        rightButtonPath.lineTo(paddingLeft+leftRectWidth+halfSlash.toFloat(),paddingTop.toFloat())
        rightButtonPath.lineTo(paddingLeft+leftRectWidth+halfSlash-mSlashUnderWidth,paddingTop+contentHeight.toFloat())
        rightButtonPath.close()
        rightColorPaint.shader = rightGradient
        canvas?.drawPath(rightButtonPath,rightColorPaint)

    }


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
        leftColors = intArrayOf(leftColor,brighterColor(leftColor,1.5F))
        rightColors = intArrayOf(rightColor,brighterColor(rightColor,1.5F))
    }

    //点击事件接口
    interface VoteClickListener{
        fun onClickLeft()
        fun onClickRight()
    }


}
