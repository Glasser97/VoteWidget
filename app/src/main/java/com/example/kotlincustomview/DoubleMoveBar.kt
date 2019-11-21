package com.example.kotlincustomview

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import kotlin.math.min
import android.graphics.Shader
import android.graphics.LinearGradient
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator


/**
 * TODO: document your custom view class.
 */
class DoubleMoveBar : View {
    private var mHeight:Int = 0
    private var mWidth:Int = 0

    //定义圆角
    private lateinit var corner: CornerPathEffect
    private var mLeftColor:Int = Color.RED
    private var mRightColor:Int = Color.BLUE

    //定义渐变色的shader和颜色Paint
    private var leftGradient: LinearGradient? =null
    private var rightGradient: LinearGradient? =null
    private var leftColorPaint:Paint = Paint()
    private var rightColorPaint:Paint = Paint()

    //定义斜边的斜度和宽度
    private var mSlashWidth:Float = 5F
    private var mSlashUnderWidth:Float = 0F

    //默认的左右投票数量
    var leftNo:Int = 0
    var rightNo:Int = 0

    //定义绘制的左右Path
    private var leftButtonPath:Path = Path()
    private var rightButtonPath:Path = Path()

    //定义动画的Animator和插值器
    private var animator = ObjectAnimator()
    private var interpolator = AccelerateDecelerateInterpolator()

    //定义颜色数组
    private lateinit var leftColors:IntArray
    private lateinit var rightColors:IntArray
    private lateinit var colorPositions:FloatArray

    //定义动画的progress
    private var progress:Float = 1F
    set(value) {
        field = value
        invalidate()
    }

    //定义两个梯形的各四个边距
    var leftLeft:Float = 0F
    var leftRight:Float = 0F
    var leftTop:Float = 0F
    var leftBottom:Float = 0F
    var rightLeft:Float = 0F
    var rightRight:Float = 0F
    var rightTop:Float = 0F
    var rightBottom:Float = 0F
    var leftGradientRight:Float = 0F
    var rightGradientLeft:Float = 0F




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
        val a: TypedArray = context.obtainStyledAttributes(attrs,R.styleable.DoubleMoveBar,defStyle,0)

        mLeftColor = a.getColor(R.styleable.DoubleMoveBar_leftColor,mLeftColor)
        mRightColor = a.getColor(R.styleable.DoubleMoveBar_rightColor,mRightColor)
        mSlashUnderWidth = a.getDimension(R.styleable.DoubleMoveBar_slashUnderWidth,mSlashUnderWidth)
        mSlashWidth = a.getDimension(R.styleable.DoubleMoveBar_slashWidth,mSlashWidth)
        a.recycle()

        //初始化圆角
        corner = CornerPathEffect(1F)

        //初始化渐变颜色数组和渐变位置数组
        setColors(mLeftColor, mRightColor)
        colorPositions = floatArrayOf(0.2F,0.8F)

        //初始化animator
        animator.setPropertyName("progress")
        animator.setFloatValues(0.01F,1F)
        animator.duration = 1000
        animator.target = this
        animator.interpolator = interpolator

        //初始化画笔
        leftColorPaint.flags = Paint.ANTI_ALIAS_FLAG
        leftColorPaint.color = mLeftColor
        leftColorPaint.style = Paint.Style.FILL
        leftColorPaint.pathEffect = corner
        rightColorPaint.flags = Paint.ANTI_ALIAS_FLAG
        rightColorPaint.color = mRightColor
        rightColorPaint.style = Paint.Style.FILL
        rightColorPaint.pathEffect = corner
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
            result = (context.resources.getDimension(R.dimen.ft_value_1080p_54px)).toInt()
            if(specMode == MeasureSpec.AT_MOST){
                result = min(result,specSize)
            }
        }
        return result
    }

    private fun measuredWidth(widthMeasureSpec: Int):Int{
        return MeasureSpec.getSize(widthMeasureSpec)
    }

    override fun onDraw(canvas: Canvas?){
        super.onDraw(canvas)
        setBackgroundColor(Color.TRANSPARENT)

        val contentWidth = width-paddingLeft-paddingRight
        val contentHeight = height-paddingTop-paddingBottom

        //计算左边矩形部分占用的长度
        val leftRectWidth:Int = (computePercent(leftNo,rightNo) * (contentWidth-mSlashUnderWidth-mSlashWidth)).toInt()

        //计算出左右梯形的四个顶点
        leftTop = paddingTop.toFloat()
        leftLeft = paddingLeft.toFloat()
        leftBottom = paddingTop+contentHeight.toFloat()
        leftRight = paddingLeft+leftRectWidth*progress+mSlashUnderWidth
        leftGradientRight = paddingLeft+leftRectWidth+mSlashUnderWidth
        rightTop = paddingTop.toFloat()
        rightBottom = paddingTop+contentHeight.toFloat()
        rightRight = width-paddingRight.toFloat()
        val k:Float = leftRectWidth+mSlashUnderWidth+mSlashWidth-contentWidth
        val b:Float = paddingLeft+contentWidth-mSlashUnderWidth
        rightLeft = k*progress+b
        rightGradientLeft = paddingLeft+leftRectWidth+mSlashWidth


        //设置左右渐变色Shader
        if(leftGradient == null){
            leftGradient = LinearGradient(leftLeft, leftBottom, leftGradientRight, leftTop,
                leftColors,colorPositions,Shader.TileMode.CLAMP)
        }
        if(rightGradient == null){
            rightGradient = LinearGradient(rightRight,rightTop,rightGradientLeft,rightBottom,
                rightColors,colorPositions,Shader.TileMode.CLAMP)
        }

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
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator.start()
    }

    private fun computePercent(leftNo:Int, rightNo:Int):Float{
        val half:Float = 1F/2F
        return if((leftNo == 0) || (rightNo == 0)){
            half
        }else{
            leftNo.toFloat()/(leftNo+rightNo).toFloat()
        }
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
        leftColors = intArrayOf(leftColor,brighterColor(leftColor,2F))
        rightColors = intArrayOf(rightColor,brighterColor(rightColor,2F))
    }

}



