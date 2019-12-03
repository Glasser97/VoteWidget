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
    var mLeftColor:Int = Color.RED
    var mRightColor:Int = Color.BLUE
//    private var mPreLeftColor:Int = mLeftColor
//    private var mPreRightColor:Int = mRightColor

    //定义渐变色的shader和颜色Paint
    private lateinit var leftGradient: LinearGradient
    private lateinit var rightGradient: LinearGradient
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

    //定义两个梯形的四个顶点边距
    private var leftLeft:Float = 0F
    private var leftRight:Float = 0F
    private var leftTop:Float = 0F
    private var leftBottom:Float = 0F
    private var rightLeft:Float = 0F
    private var rightRight:Float = 0F
    private var rightTop:Float = 0F
    private var rightBottom:Float = 0F
    private var leftGradientRight:Float = 0F
    private var rightGradientLeft:Float = 0F




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
     * 填入初始数据
     */
    fun fill(displaySource: DoubleMoveBarDisplaySource){
        this.mLeftColor = displaySource.mLeftColor
        this.mRightColor = displaySource.mRightColor
        this.mSlashUnderWidth = displaySource.mSlashUnderWidth
        this.mSlashWidth = displaySource.mSlashWidth
        leftColorPaint.color = mLeftColor
        rightColorPaint.color = mRightColor
        setColors(this.mLeftColor,this.mRightColor)
        //设置颜色后更新新的Gradient
        leftGradient = LinearGradient(leftLeft, leftBottom, leftGradientRight, leftTop,
            leftColors,colorPositions,Shader.TileMode.CLAMP)
        rightGradient = LinearGradient(rightRight,rightTop,rightGradientLeft,rightBottom,
            rightColors,colorPositions,Shader.TileMode.CLAMP)
    }

    /**
     * 填入数字数据
     */
    fun fill(mLeftNo:Int,mRightNo:Int){
        if(leftNo != mLeftNo || rightNo != mRightNo){
            this.leftNo = mLeftNo
            this.rightNo = mRightNo
            //progress = 0.01F
            //记得重置path,避免绘制更新前的脏画面
            resetDrawPaths()
            startMoveAnimation()
        }
    }


    private fun init(attrs:AttributeSet?,defStyle:Int){
        val a: TypedArray = context.obtainStyledAttributes(attrs,R.styleable.DoubleMoveBar,defStyle,0)

        mLeftColor = a.getColor(R.styleable.DoubleMoveBar_leftColor,mLeftColor)
        mRightColor = a.getColor(R.styleable.DoubleMoveBar_rightColor,mRightColor)
        mSlashUnderWidth = a.getDimension(R.styleable.DoubleMoveBar_slashUnderWidth,mSlashUnderWidth)
        mSlashWidth = a.getDimension(R.styleable.DoubleMoveBar_slashWidth,mSlashWidth)
        a.recycle()

        //初始化圆角
        corner = CornerPathEffect(10F)

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
        leftColorPaint.style = Paint.Style.FILL
        leftColorPaint.pathEffect = corner
        leftColorPaint.color = mLeftColor

        rightColorPaint.flags = Paint.ANTI_ALIAS_FLAG
        rightColorPaint.style = Paint.Style.FILL
        rightColorPaint.pathEffect = corner
        rightColorPaint.color = mRightColor
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        //设置渐变的Gradient
        val contentHeight = height-paddingTop-paddingBottom
        val contentWidth = width-paddingLeft-paddingRight
        val leftRectWidth:Int = (computePercent(leftNo,rightNo) * (contentWidth-mSlashUnderWidth-mSlashWidth)).toInt()
        leftTop = paddingTop.toFloat()
        leftLeft = paddingLeft.toFloat()
        leftBottom = paddingTop+contentHeight.toFloat()
        leftGradientRight = paddingLeft+leftRectWidth+mSlashUnderWidth
        rightTop = paddingTop.toFloat()
        rightBottom = paddingTop+contentHeight.toFloat()
        rightRight = width-paddingRight.toFloat()
        rightGradientLeft = paddingLeft+leftRectWidth+mSlashWidth
        leftGradient = LinearGradient(leftLeft, leftBottom, leftGradientRight, leftTop,
            leftColors,colorPositions,Shader.TileMode.CLAMP)
        rightGradient = LinearGradient(rightRight,rightTop,rightGradientLeft,rightBottom,
            rightColors,colorPositions,Shader.TileMode.CLAMP)

    }

    override fun onDraw(canvas: Canvas?){
        super.onDraw(canvas)
        setBackgroundColor(Color.TRANSPARENT)

        val contentWidth = width-paddingLeft-paddingRight
        val contentHeight = height-paddingTop-paddingBottom

        //计算左边矩形部分占用的长度
        val leftRectWidth:Int = (computePercent(leftNo,rightNo) * (contentWidth-mSlashUnderWidth-mSlashWidth)).toInt()

        //计算出左边梯形的四个顶点
        leftTop = paddingTop.toFloat()
        leftLeft = paddingLeft.toFloat()
        leftBottom = paddingTop+contentHeight.toFloat()
        leftRight = paddingLeft+leftRectWidth*progress+mSlashUnderWidth
        //这是左边梯形渐变色的顶点,不会改变
        leftGradientRight = paddingLeft+leftRectWidth+mSlashUnderWidth
        //这是右边梯形的四个顶点
        rightTop = paddingTop.toFloat()
        rightBottom = paddingTop+contentHeight.toFloat()
        rightRight = width-paddingRight.toFloat()
        val k:Float = leftRectWidth+mSlashUnderWidth+mSlashWidth-contentWidth
        val b:Float = paddingLeft+contentWidth-mSlashUnderWidth
        rightLeft = k*progress+b
        //这是右边梯形渐变色的顶点
        rightGradientLeft = paddingLeft+leftRectWidth+mSlashWidth


        //设置左右渐变色Shader
//        if(leftGradient == null || mPreLeftColor != mLeftColor){
//            mPreLeftColor = mLeftColor
//            leftGradient = LinearGradient(leftLeft, leftBottom, leftGradientRight, leftTop,
//                leftColors,colorPositions,Shader.TileMode.CLAMP)
//        }
//        if(rightGradient == null || mPreRightColor != mRightColor){
//            mPreRightColor = mRightColor
//            rightGradient = LinearGradient(rightRight,rightTop,rightGradientLeft,rightBottom,
//                rightColors,colorPositions,Shader.TileMode.CLAMP)
//        }

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


    /**
     * 开始动画
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator.start()
    }

    /**
     * 开始动画
     */
    fun startMoveAnimation(){
        animator.start()
    }

    /**
     * 重置path实例,避免绘制刷新画面前的脏画面
     */
    private fun resetDrawPaths(){
        leftButtonPath.reset()
        rightButtonPath.reset()
    }

    /**
     * 计算左右进度条分别占用的比例
     * @param leftNo 左边的投票数
     * @param rightNo 右边的投票数
     */
    private fun computePercent(leftNo:Int, rightNo:Int):Float{
        val half:Float = 1F/2F
        return if((leftNo == 0) && (rightNo == 0)){
            half
        }else if(leftNo == 0 && rightNo != 0){
            0.02F
        }else if(rightNo == 0 && leftNo != 0){
            0.98F
        }else{
            leftNo.toFloat()/(leftNo+rightNo).toFloat()
        }
    }

    /**
     * 设置获取较亮的渐变色
     * @param color 用户设置的颜色
     * @param brighterK 三个颜色通道变亮的比例
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

    /**
     * 设置左右梯形的颜色数组
     * @param leftColor 左边梯形的颜色
     * @param rightColor 右边梯形的颜色
     */

    private fun setColors(leftColor:Int, rightColor:Int){
        leftColors = intArrayOf(leftColor,brighterColor(leftColor,1.2F))
        rightColors = intArrayOf(rightColor,brighterColor(rightColor,1.2F))
    }

}

data class DoubleMoveBarDisplaySource(var mLeftColor:Int,var mRightColor:Int,var mSlashUnderWidth:Float,var mSlashWidth:Float)