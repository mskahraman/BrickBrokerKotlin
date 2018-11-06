package com.example.mehmetsabir.brickbroker

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.*
import java.util.*

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

        init {
            ourHolder = holder
            paint = Paint()
            val display: Display = windowManager.defaultDisplay
            val size = Point()

            display.getSize(size)
            screenX = size.x
            screenY = size.y

            paddle = Paddle(screenX, screenY)
            ball = Ball()

            createBlocksAndRestart()

        }

        private fun createBlocksAndRestart() {

            var b: Int
            var a: Int

            ball.reset(screenX, screenY - 150)

            for (row in 0..2) {
                val r = Random()
                a = r.nextInt(screenX - 300)
                b = r.nextInt(screenY / 4)
                if (row == 1) {
                    b = r.nextInt(screenY / 4) + screenY / 4
                } else if (row == 2) {
                    b = r.nextInt(screenY / 4) + screenY / 2
                }

                blocks[numBlocks] = Blocks(applicationContext, a, b, 250 + a, 40 + b)
                numBlocks++
            }
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
                ball.reset(screenX, screenY - 150)
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
                canvas?.drawColor(Color.argb(255, 26, 128, 182))

                paint.color = Color.argb(255, 255, 255, 255)
                canvas?.drawRect(paddle.getRect(), paint)
                canvas?.drawRect(ball.getRect(), paint)
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
                    ball.reset(screenX, screenY - 150)
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
        length = 300
        height = 30
        xSize = screenX

        x = screenX / 2 - 150
        y = screenY - 150

        rect = Rect(x, y, x + length, y + height)
        paddleSpeed = 850f
    }

    internal fun getRect(): Rect {
        return rect
    }


    internal fun setMovementState(state: Int) {
        paddleMoving = state
    }

    fun update(fps: Long) {
        if (paddleMoving == leftOfScreen && x > 0) {
            x = Math.round(x - paddleSpeed / fps)
        }

        if (paddleMoving == rightOfScreen && x < xSize - 300) {
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

private class Ball {

    private var rect: Rect
    private var xVelocity: Float = 0.toFloat()
    private var yVelocity: Float = 0.toFloat()
    private var ballWidth = 15f
    private var ballHeight = 15f

    init {

        xVelocity = 400f
        yVelocity = -800f
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

