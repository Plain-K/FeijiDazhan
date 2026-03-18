package com.example.feijidazhan.gameobject

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

/**
 * 爆炸特效类
 */
class Explosion(
    private var x: Int,
    private var y: Int,
    private val width: Int,
    private val height: Int,
    private val duration: Long = 500 // 爆炸持续时间（毫秒）
) {
    
    private val startTime = System.currentTimeMillis()
    private var currentFrame = 0
    private val maxFrames = 8 // 爆炸动画帧数
    
    // 爆炸状态
    var isActive: Boolean = true
        private set
    
    /**
     * 更新爆炸动画
     */
    fun update() {
        val elapsedTime = System.currentTimeMillis() - startTime
        if (elapsedTime >= duration) {
            isActive = false
        }
        
        // 计算当前帧
        val progress = elapsedTime.toFloat() / duration
        currentFrame = (progress * maxFrames).toInt().coerceAtMost(maxFrames - 1)
    }
    
    /**
     * 绘制爆炸特效
     */
    fun draw(canvas: Canvas, paint: Paint) {
        if (!isActive) return
        
        // 绘制爆炸动画
        val alpha = (255 * (1 - (System.currentTimeMillis() - startTime).toFloat() / duration)).toInt()
        paint.alpha = alpha
        
        // 绘制爆炸圆圈
        val radius = width / 2f * (currentFrame + 1) / maxFrames
        canvas.drawCircle(
            (x + width / 2).toFloat(),
            (y + height / 2).toFloat(),
            radius,
            paint
        )
        
        // 重置透明度
        paint.alpha = 255
    }
    
    /**
     * 获取爆炸边界
     */
    fun getBounds(): Rect {
        return Rect(x, y, x + width, y + height)
    }
}
