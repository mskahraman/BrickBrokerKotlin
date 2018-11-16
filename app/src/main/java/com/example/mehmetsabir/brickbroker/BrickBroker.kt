package com.example.mehmetsabir.brickbroker

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.RadioGroup
import kotlinx.android.synthetic.main.alertlayout.*
import java.util.*
import kotlin.math.roundToInt

class BrickBroker : Activity() {

    private var blockBreakerView: BlockBreakerView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        blockBreakerView = BlockBreakerView(this@BrickBroker)
        setContentView(blockBreakerView)
    }
    inner class BlockBreakerView(context: Context?) : SurfaceView(context), Runnable {
        private var gameThread: Thread? = null
        private var ourHolder: SurfaceHolder? = null
        private var playing: Boolean = false
        private var paused = true
        private var canvas: Canvas? = null
        private var paint: Paint
        private var fps: Long = 0
        private var timeThisFrame: Long = 0
        private var screenX: Int = 0
        private var screenY: Int = 0
        private var paddle: Paddle
        private var ball: Ball
        private var blocks = arrayOfNulls<Blocks>(200)
        private var numBlocks = 0
        private var score = 0
        private var bundle : Bundle?=null
        private var levels : Int = 0
        init {
            ourHolder = holder
            paint = Paint()
            val display: Display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            screenX = size.x
            screenY = size.y
            paddle = Paddle(screenX, screenY)
            ball = Ball(screenX)
            bundle = intent.extras
            levels = bundle!!.getInt("level")
            createBlocksAndRestart()
        }
        private fun createBlocksAndRestart() {
            var b: Int=0
            var a: Int
            ball.reset(screenX, (screenY / 1.084).roundToInt())
            for (row in 0..levels) {
                val r = Random()
                a = r.nextInt(screenX - (screenX / 3.6).roundToInt())
                if(levels==2){
                    if(row == 0){
                        b = r.nextInt(screenY / 4)
                    }else if (row == 1) {
                        b = r.nextInt(screenY / 4) + screenY / 4
                    } else if (row == 2) {
                        b = r.nextInt(screenY / 4) + screenY / 2
                    }
                }else if(levels==4){
                    if(row == 0){
                        b = r.nextInt((screenY / 7.27).roundToInt())
                    } else if (row == 1) {
                        b = r.nextInt((screenY /  7.27).roundToInt()) + (screenY /  7.27).roundToInt()
                    } else if (row == 2) {
                        b = r.nextInt((screenY /  7.27).roundToInt()) + (screenY / 3.63).roundToInt()
                    } else if (row == 3) {
                        b = r.nextInt((screenY /  7.27).roundToInt()) + (screenY / 2.42).roundToInt()
                    } else if (row == 4) {
                        b = r.nextInt((screenY /  7.27).roundToInt()) + (screenY / 1.81).roundToInt()
                    }
                }else if(levels==6){
                    if(row == 0){
                        b = r.nextInt((screenY / 9.41).roundToInt())
                    } else if (row == 1) {
                        b = r.nextInt((screenY /  9.41).roundToInt()) + (screenY /  9.41).roundToInt()
                    } else if (row == 2) {
                        b = r.nextInt((screenY /  9.41).roundToInt()) + (screenY / 4.7).roundToInt()
                    } else if (row == 3) {
                        b = r.nextInt((screenY /  9.41).roundToInt()) + (screenY / 3.13).roundToInt()
                    } else if (row == 4) {
                        b = r.nextInt((screenY /  9.41).roundToInt()) + (screenY / 2.35).roundToInt()
                    }else if (row == 5) {
                        b = r.nextInt((screenY /  9.41).roundToInt()) + (screenY / 1.88).roundToInt()
                    }else if (row == 6) {
                        b = r.nextInt((screenY /  9.41).roundToInt()) + (screenY / 1.56).roundToInt()
                    }
                }
                blocks[numBlocks] = Blocks(applicationContext, a, b, ((screenY/7.84).roundToInt()) + a, (screenX /27 )+ b)
                numBlocks++
            }
        }
        private fun drawBlocksDifferentPoint(){
        }
        override fun run() {
            while (playing) {
                val startFrameTime = System.currentTimeMillis()
                if (!paused) {
                    update()
                }
                draw()
                timeThisFrame = System.currentTimeMillis() - startFrameTime
                if (timeThisFrame >= 1) {
                    fps = 1000 / timeThisFrame
                }
            }
        }
        private fun update() {
            paddle.update(fps)
            ball.update(fps)
            for (i in 0 until numBlocks) {
                if (blocks[i]!!.getVisibilityOfBlocks()) {
                    if (Rect.intersects(blocks[i]?.getRect(), ball.getRect())) {
                        blocks[i]?.setInvisible()
                        ball.reverseYVelocity()
                        score += 10
                    }
                }
            }
            if (Rect.intersects(paddle.getRect(), ball.getRect())) {
                ball.setRandomXVelocity()
                ball.reverseYVelocity()
                ball.clearObstacleY((paddle.getRect().top - 2).toFloat())
            }
            if (ball.getRect().bottom > screenY) {
                ball.reverseYVelocity()
                ball.clearObstacleY((screenY - 2).toFloat())
                ball.reset(screenX, (screenY / 1.084).roundToInt())
            }
            if (ball.getRect().top < 0) {
                ball.reverseYVelocity()
                ball.clearObstacleY(12f)
            }
            if (ball.getRect().left < 0) {
                ball.reverseXVelocity()
                ball.clearObstacleX(2f)
            }
            if (ball.getRect().right > screenX - 10) {
                ball.reverseXVelocity()
                ball.clearObstacleX((screenX - 22).toFloat())
            }
            if (score == numBlocks * 10) {
                paused = true
                // createBlocksAndRestart()
            }
        }
        fun draw() {
            if (ourHolder?.surface?.isValid!!) {
                canvas = ourHolder?.lockCanvas()
                canvas?.drawColor(Color.argb(255, 21, 19, 133))
                paint.color = Color.argb(255, 255, 255, 255)
                canvas?.drawRect(paddle.getRect(), paint)
                canvas?.drawRect(ball.getRect(), paint)
//                paint.style = Paint.Style.FILL
//                paint.textSize = screenX / 12f
//                canvas?.drawText("Brick Broken Game", (screenX/5f) , (screenY/11f), paint)
                paint.color = Color.argb(255, 249, 129, 0)
                for (i in 0 until numBlocks) {
                    if (blocks[i]?.getVisibilityOfBlocks()!!) {
                        canvas?.drawRect(blocks[i]!!.getRect(), paint)
                    }
                }
                paint.color = Color.argb(255, 255, 255, 255)
                paint.textSize = 40f
                if (score == numBlocks * 10) {
                    paint.textSize = 90f
                    canvas?.drawText("Bitti", (screenX / 2).toFloat() - 10f, (screenY / 2).toFloat(), paint)
                    ball.reset(screenX, (screenY / 1.084).roundToInt())
                }
                ourHolder?.unlockCanvasAndPost(canvas)
            }
        }
        fun pause() {
            playing = false
            try {
                gameThread?.join()
            } catch (e: InterruptedException) {
                Log.e("Error:", "joining thread")
            }
        }
        fun resume() {
            playing = true
            gameThread = Thread(this)
            gameThread?.start()
        }
        override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
            when (motionEvent.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    paused = false
                    if (motionEvent.x > screenX / 2) {
                        paddle.setMovementState(paddle.rightOfScreen)
                    } else {
                        paddle.setMovementState(paddle.leftOfScreen)
                    }
                }
                MotionEvent.ACTION_UP ->
                    paddle.setMovementState(paddle.stopped)
            }
            return true
        }
    }
    override fun onResume() {
        super.onResume()
        blockBreakerView?.resume()
    }
    override fun onPause() {
        super.onPause()
        blockBreakerView?.pause()
    }
}
private class Paddle(screenX: Int, screenY: Int) {
    private var rect: Rect
    private var length: Int = 0
    private var height: Int = 0
    private var x: Int = 0
    private var y: Int = 0
    private var paddleSpeed: Float = 0f
    internal val stopped = 0
    internal val leftOfScreen = 1
    internal val rightOfScreen = 2
    var xSize: Int = 0
    private var paddleMoving = stopped
    init {
        length = (screenX / 3.6).roundToInt()
        height = screenY / 64
        xSize = screenX
        x = screenX / 2 - (length/2)
        y = (screenY / 1.084).roundToInt()
        rect = Rect(x, y, x + length, y + height)
        paddleSpeed = screenX / 1.2f
    }
    internal fun getRect(): Rect {
        return rect
    }
    internal fun setMovementState(state: Int) {
        paddleMoving = state
    }
    internal fun update(fps: Long) {
        if (paddleMoving == leftOfScreen && x > 0) {
            x = Math.round(x - paddleSpeed / fps)
        }
        if (paddleMoving == rightOfScreen && x < xSize - ((xSize / 3.6).roundToInt())) {
            x = Math.round(x + paddleSpeed / fps)
        }
        rect.left = x
        rect.right = x + length
    }
}
private class Blocks(context: Context?, left: Int, top: Int, width: Int, height: Int) : View(context) {
    private val rect: Rect
    private var isVisible: Boolean = false
    init {
        isVisible = true
        rect = Rect(left, top, width, height)
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val drawable = resources.getDrawable(R.drawable.brick)
        drawable.bounds = rect
        drawable.draw(canvas)
    }
    fun getRect(): Rect {
        return this.rect
    }
    internal fun setInvisible() {
        isVisible = false
    }
    internal fun getVisibilityOfBlocks(): Boolean {
        return isVisible
    }
}
private class Ball(screenX: Int) {
    private var rect: Rect
    private var xVelocity: Float = 0.toFloat()
    private var yVelocity: Float = 0.toFloat()
    private var ballWidth =  screenX / 72f
    private var ballHeight = screenX / 72f
    init {
        xVelocity = screenX / 2.7f
        yVelocity = -screenX / 1.35f
        rect = Rect()
    }
    internal fun getRect(): Rect {
        return rect
    }
    internal fun update(fps: Long) {
        rect.left = Math.round(rect.left + xVelocity / fps)
        rect.top = Math.round(rect.top + yVelocity / fps)
        rect.right = Math.round(rect.left + ballWidth)
        rect.bottom = Math.round(rect.top - ballHeight)
    }
    internal fun reverseYVelocity() {
        yVelocity = -yVelocity
    }
    internal fun reverseXVelocity() {
        xVelocity = -xVelocity
    }
    internal fun setRandomXVelocity() {
        val generator = Random()
        val answer = generator.nextInt(2)
        if (answer == 0) {
            reverseXVelocity()
        }
    }
    internal fun clearObstacleY(y: Float) {
        rect.bottom = Math.round(y)
        rect.top = Math.round(y - ballHeight)
    }
    internal fun clearObstacleX(x: Float) {
        rect.left = Math.round(x)
        rect.right = Math.round(x + ballWidth)
    }
    internal fun reset(x: Int, y: Int) {
        rect.left = x / 2
        rect.top = y - 20
        rect.right = Math.round(x / 2 + ballWidth)
        rect.bottom = Math.round(y.toFloat() - 20f - ballHeight)
    }
}
