package com.example.feijidazhan

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameOverView(
    context: Context,
    private val score: Int,
    private val onContinue: () -> Unit,
    private val onReturnToMain: () -> Unit
) : SurfaceView(context), SurfaceHolder.Callback {
    private val paint = Paint()
    private val titlePaint = Paint()
    private val scorePaint = Paint()
    private val buttonPaint = Paint()
    private val buttonTextPaint = Paint()
    private var screenWidth = 0
    private var screenHeight = 0

    // 按钮区域
    private val continueButtonRect = RectF()
    private val returnButtonRect = RectF()

    init {
        holder.addCallback(this)
        isFocusable = true

        // 初始化画笔
        titlePaint.color = Color.RED
        titlePaint.textSize = 80f
        titlePaint.isAntiAlias = true
        titlePaint.textAlign = Paint.Align.CENTER
        titlePaint.isFakeBoldText = true

        scorePaint.color = Color.WHITE
        scorePaint.textSize = 50f
        scorePaint.isAntiAlias = true
        scorePaint.textAlign = Paint.Align.CENTER

        buttonPaint.color = Color.parseColor("#2196F3")
        buttonPaint.isAntiAlias = true

        buttonTextPaint.color = Color.WHITE
        buttonTextPaint.textSize = 40f
        buttonTextPaint.isAntiAlias = true
        buttonTextPaint.textAlign = Paint.Align.CENTER
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
            val title = "Game Over"
            canvas.drawText(title, screenWidth / 2f, screenHeight / 4f, titlePaint)

            // 绘制分数
            canvas.drawText("最终得分: $score", screenWidth / 2f, screenHeight / 4f + 100f, scorePaint)

            // 计算按钮位置和大小
            val buttonWidth = 300f
            val buttonHeight = 80f
            val buttonSpacing = 40f

            // 继续按钮
            val continueButtonLeft = (screenWidth - buttonWidth) / 2f
            val continueButtonTop = screenHeight / 2f
            val continueButtonRight = continueButtonLeft + buttonWidth
            val continueButtonBottom = continueButtonTop + buttonHeight

            continueButtonRect.set(continueButtonLeft, continueButtonTop, continueButtonRight, continueButtonBottom)

            // 返回主界面按钮
            val returnButtonLeft = continueButtonLeft
            val returnButtonTop = continueButtonBottom + buttonSpacing
            val returnButtonRight = continueButtonRight
            val returnButtonBottom = returnButtonTop + buttonHeight

            returnButtonRect.set(returnButtonLeft, returnButtonTop, returnButtonRight, returnButtonBottom)

            // 绘制继续按钮
            buttonPaint.color = Color.parseColor("#4CAF50") // 绿色
            canvas.drawRoundRect(continueButtonRect, 20f, 20f, buttonPaint)
            canvas.drawText("继续游戏", screenWidth / 2f, continueButtonTop + buttonHeight / 2f + 15f, buttonTextPaint)

            // 绘制返回主界面按钮
            buttonPaint.color = Color.parseColor("#2196F3") // 蓝色
            canvas.drawRoundRect(returnButtonRect, 20f, 20f, buttonPaint)
            canvas.drawText("返回主界面", screenWidth / 2f, returnButtonTop + buttonHeight / 2f + 15f, buttonTextPaint)

            holder.unlockCanvasAndPost(canvas)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y

                // 检查是否点击了继续按钮
                if (continueButtonRect.contains(x, y)) {
                    onContinue()
                    return true
                }

                // 检查是否点击了返回主界面按钮
                if (returnButtonRect.contains(x, y)) {
                    onReturnToMain()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
}
