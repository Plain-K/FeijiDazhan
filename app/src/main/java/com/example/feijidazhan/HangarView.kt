package com.example.feijidazhan

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.feijidazhan.model.AircraftStats
import com.example.feijidazhan.model.AircraftTypes

/**
 * 机库界面
 * 用于选择和预览不同的战机
 */
class HangarView(
    context: Context,
    private val currentAircraft: AircraftStats,
    private val onSelectAircraft: (AircraftStats) -> Unit,
    private val onBackToMenu: () -> Unit
) : SurfaceView(context), SurfaceHolder.Callback {

    private val paint = Paint()
    private val titlePaint = Paint()
    private val aircraftNamePaint = Paint()
    private val statsPaint = Paint()
    private val buttonPaint = Paint()
    private val buttonTextPaint = Paint()
    private val selectedPaint = Paint()
    private var screenWidth = 0
    private var screenHeight = 0

    // 可选战机列表
    private val aircraftList = listOf(
        AircraftTypes.BASIC_FIGHTER,
        AircraftTypes.HEAVY_FIGHTER,
        AircraftTypes.LIGHT_FIGHTER
    )

    // 当前选中的战机索引（根据传入的当前战机初始化）
    private var selectedIndex = aircraftList.indexOfFirst { it.name == currentAircraft.name }.coerceAtLeast(0)

    // 按钮区域
    private val selectButtonRect = RectF()
    private val backButtonRect = RectF()
    private val leftArrowRect = RectF()
    private val rightArrowRect = RectF()

    init {
        holder.addCallback(this)
        isFocusable = true

        // 初始化画笔
        titlePaint.color = Color.YELLOW
        titlePaint.textSize = 70f
        titlePaint.isAntiAlias = true
        titlePaint.textAlign = Paint.Align.CENTER
        titlePaint.isFakeBoldText = true

        aircraftNamePaint.color = Color.WHITE
        aircraftNamePaint.textSize = 50f
        aircraftNamePaint.isAntiAlias = true
        aircraftNamePaint.textAlign = Paint.Align.CENTER

        statsPaint.color = Color.CYAN
        statsPaint.textSize = 35f
        statsPaint.isAntiAlias = true
        statsPaint.textAlign = Paint.Align.CENTER

        buttonPaint.color = Color.parseColor("#4CAF50")
        buttonPaint.isAntiAlias = true

        buttonTextPaint.color = Color.WHITE
        buttonTextPaint.textSize = 40f
        buttonTextPaint.isAntiAlias = true
        buttonTextPaint.textAlign = Paint.Align.CENTER

        selectedPaint.color = Color.parseColor("#FFD700")
        selectedPaint.style = Paint.Style.STROKE
        selectedPaint.strokeWidth = 5f
        selectedPaint.isAntiAlias = true
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
            canvas.drawText("机库", screenWidth / 2f, screenHeight / 8f, titlePaint)

            // 计算布局参数
            val previewSize = 200f
            val previewX = (screenWidth - previewSize) / 2f
            val previewY = screenHeight / 4f

            // 绘制战机预览框
            val previewRect = RectF(previewX, previewY, previewX + previewSize, previewY + previewSize)
            paint.color = Color.DKGRAY
            canvas.drawRect(previewRect, paint)
            selectedPaint.color = Color.YELLOW
            canvas.drawRect(previewRect, selectedPaint)

            // 绘制战机（用矩形代替，实际可以用图片）
            val aircraft = aircraftList[selectedIndex]
            paint.color = when (selectedIndex) {
                0 -> Color.GREEN      // 基础战机
                1 -> Color.BLUE       // 重型战机
                2 -> Color.RED        // 轻型战机
                else -> Color.GREEN
            }
            val aircraftRect = RectF(
                previewX + 50f,
                previewY + 50f,
                previewX + previewSize - 50f,
                previewY + previewSize - 50f
            )
            canvas.drawRect(aircraftRect, paint)

            // 绘制战机名称
            canvas.drawText(aircraft.name, screenWidth / 2f, previewY + previewSize + 60f, aircraftNamePaint)

            // 绘制战机属性
            val statsY = previewY + previewSize + 120f
            canvas.drawText("生命值: ${aircraft.maxHealth}", screenWidth / 2f, statsY, statsPaint)
            canvas.drawText("速度: ${aircraft.speed}x", screenWidth / 2f, statsY + 45f, statsPaint)
            canvas.drawText("护甲: ${aircraft.armor}", screenWidth / 2f, statsY + 90f, statsPaint)

            // 绘制左右切换箭头
            val arrowSize = 80f
            val arrowY = previewY + previewSize / 2f - arrowSize / 2f

            // 左箭头
            leftArrowRect.set(50f, arrowY, 50f + arrowSize, arrowY + arrowSize)
            buttonPaint.color = Color.parseColor("#2196F3")
            canvas.drawRoundRect(leftArrowRect, 10f, 10f, buttonPaint)
            buttonTextPaint.textSize = 50f
            canvas.drawText("<", 50f + arrowSize / 2f, arrowY + arrowSize / 2f + 20f, buttonTextPaint)

            // 右箭头
            rightArrowRect.set(screenWidth - 50f - arrowSize, arrowY, screenWidth - 50f, arrowY + arrowSize)
            canvas.drawRoundRect(rightArrowRect, 10f, 10f, buttonPaint)
            canvas.drawText(">", screenWidth - 50f - arrowSize / 2f, arrowY + arrowSize / 2f + 20f, buttonTextPaint)

            // 绘制选择按钮
            val buttonWidth = 280f
            val buttonHeight = 80f
            val buttonY = screenHeight * 0.75f

            selectButtonRect.set(
                (screenWidth - buttonWidth) / 2f,
                buttonY,
                (screenWidth + buttonWidth) / 2f,
                buttonY + buttonHeight
            )
            buttonPaint.color = Color.parseColor("#4CAF50")
            canvas.drawRoundRect(selectButtonRect, 20f, 20f, buttonPaint)
            buttonTextPaint.textSize = 40f
            canvas.drawText("选择此战机", screenWidth / 2f, buttonY + buttonHeight / 2f + 15f, buttonTextPaint)

            // 绘制返回按钮
            backButtonRect.set(
                (screenWidth - buttonWidth) / 2f,
                buttonY + buttonHeight + 30f,
                (screenWidth + buttonWidth) / 2f,
                buttonY + buttonHeight * 2 + 30f
            )
            buttonPaint.color = Color.parseColor("#757575")
            canvas.drawRoundRect(backButtonRect, 20f, 20f, buttonPaint)
            canvas.drawText("返回主菜单", screenWidth / 2f, buttonY + buttonHeight * 1.5f + 45f, buttonTextPaint)

            holder.unlockCanvasAndPost(canvas)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y

                // 左箭头 - 上一个战机
                if (leftArrowRect.contains(x, y)) {
                    selectedIndex = if (selectedIndex > 0) selectedIndex - 1 else aircraftList.size - 1
                    draw()
                    return true
                }

                // 右箭头 - 下一个战机
                if (rightArrowRect.contains(x, y)) {
                    selectedIndex = (selectedIndex + 1) % aircraftList.size
                    draw()
                    return true
                }

                // 选择按钮
                if (selectButtonRect.contains(x, y)) {
                    onSelectAircraft(aircraftList[selectedIndex])
                    return true
                }

                // 返回按钮
                if (backButtonRect.contains(x, y)) {
                    onBackToMenu()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
}
