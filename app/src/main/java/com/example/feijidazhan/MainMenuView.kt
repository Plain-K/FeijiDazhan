package com.example.feijidazhan

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class MainMenuView(
    context: Context,
    private val onStartGame: () -> Unit,
    private val onOpenHangar: () -> Unit
) : SurfaceView(context), SurfaceHolder.Callback {
    private val paint = Paint()
    private val buttonPaint = Paint()
    private val buttonTextPaint = Paint()
    private val titlePaint = Paint()
    private var screenWidth = 0
    private var screenHeight = 0

    // 按钮区域
    private val startButtonRect = RectF()
    private val hangarButtonRect = RectF()

    init {
        holder.addCallback(this)
        isFocusable = true

        // 初始化画笔
        buttonPaint.color = Color.parseColor("#4CAF50")
        buttonPaint.isAntiAlias = true

        buttonTextPaint.color = Color.WHITE
        buttonTextPaint.textSize = 50f
        buttonTextPaint.isAntiAlias = true
        buttonTextPaint.textAlign = Paint.Align.CENTER

        titlePaint.color = Color.YELLOW
        titlePaint.textSize = 80f
        titlePaint.isAntiAlias = true
        titlePaint.textAlign = Paint.Align.CENTER
        titlePaint.isFakeBoldText = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        screenWidth = width
        screenHeight = height
        draw()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
        draw()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    private fun draw() {
        val canvas = holder.lockCanvas()
        if (canvas != null) {
            // 绘制背景
            canvas.drawColor(Color.BLACK)

            // 绘制标题
            val title = "飞机大战"
            canvas.drawText(title, screenWidth / 2f, screenHeight / 4f, titlePaint)

            // 计算按钮位置和大小
            val buttonWidth = 300f
            val buttonHeight = 100f
            val buttonSpacing = 30f
            val buttonLeft = (screenWidth - buttonWidth) / 2f

            // 开始游戏按钮
            val startButtonTop = screenHeight / 2f
            startButtonRect.set(buttonLeft, startButtonTop, buttonLeft + buttonWidth, startButtonTop + buttonHeight)

            // 机库按钮
            val hangarButtonTop = startButtonTop + buttonHeight + buttonSpacing
            hangarButtonRect.set(buttonLeft, hangarButtonTop, buttonLeft + buttonWidth, hangarButtonTop + buttonHeight)

            // 绘制开始游戏按钮
            buttonPaint.color = Color.parseColor("#4CAF50")
            canvas.drawRoundRect(startButtonRect, 20f, 20f, buttonPaint)
            canvas.drawText("开始游戏", screenWidth / 2f, startButtonTop + buttonHeight / 2f + 15f, buttonTextPaint)

            // 绘制机库按钮
            buttonPaint.color = Color.parseColor("#2196F3")
            canvas.drawRoundRect(hangarButtonRect, 20f, 20f, buttonPaint)
            canvas.drawText("机库", screenWidth / 2f, hangarButtonTop + buttonHeight / 2f + 15f, buttonTextPaint)

            // 绘制提示文字
            paint.color = Color.GRAY
            paint.textSize = 30f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("选择战机或开始游戏", screenWidth / 2f, screenHeight * 0.75f, paint)

            holder.unlockCanvasAndPost(canvas)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y

                // 检查是否点击了开始游戏按钮
                if (startButtonRect.contains(x, y)) {
                    onStartGame()
                    return true
                }

                // 检查是否点击了机库按钮
                if (hangarButtonRect.contains(x, y)) {
                    onOpenHangar()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
}
