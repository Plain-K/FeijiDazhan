package com.example.feijidazhan.gameobject

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.example.feijidazhan.interfaces.Damageable

/**
 * 激光类
 * 激光炮发射的激光，对直线上所有敌人造成伤害
 */
class Laser(
    val startX: Int,
    val startY: Int,
    val endY: Int,  // 激光终点Y坐标（向上发射，所以是0或屏幕顶部）
    var damage: Int,
    var width: Int = 20,
    val duration: Long = 200L  // 激光显示持续时间（毫秒）
) {
    var isActive: Boolean = true
    val isPlayerBullet: Boolean = true
    val createTime: Long = System.currentTimeMillis()

    // 激光覆盖的矩形区域（用于碰撞检测）
    val bounds: Rect
        get() = Rect(startX - width / 2, endY, startX + width / 2, startY)

    /**
     * 检查激光是否还在显示时间内
     */
    fun isStillActive(): Boolean {
        return System.currentTimeMillis() - createTime < duration && isActive
    }

    /**
     * 绘制激光
     */
    fun draw(canvas: Canvas, paint: Paint) {
        // 计算透明度（随时间淡出）
        val elapsed = System.currentTimeMillis() - createTime
        val alpha = (255 * (1 - elapsed.toFloat() / duration)).toInt().coerceIn(0, 255)

        // 绘制激光主体（粗线条）
        paint.color = Color.argb(alpha, 0, 255, 255)  // 青色激光
        paint.strokeWidth = width.toFloat()
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawLine(
            startX.toFloat(),
            startY.toFloat(),
            startX.toFloat(),
            endY.toFloat(),
            paint
        )

        // 绘制激光核心（更亮的白色）
        paint.color = Color.argb(alpha, 255, 255, 255)
        paint.strokeWidth = (width / 3).toFloat()
        canvas.drawLine(
            startX.toFloat(),
            startY.toFloat(),
            startX.toFloat(),
            endY.toFloat(),
            paint
        )
    }

    /**
     * 检查目标是否在激光路径上
     */
    fun checkCollision(target: Damageable): Boolean {
        if (!target.isAlive || !isActive) return false

        val targetBounds = Rect(
            target.x,
            target.y,
            target.x + target.width,
            target.y + target.height
        )
        return Rect.intersects(bounds, targetBounds)
    }

    /**
     * 获取已经受到伤害的目标ID集合，避免同一激光对同一目标造成多次伤害
     */
    private val damagedTargets = mutableSetOf<String>()

    /**
     * 对目标造成伤害（每个目标只造成一次伤害）
     */
    fun dealDamage(target: Damageable): Boolean {
        val targetId = "${target.x}_${target.y}_${target.hashCode()}"
        if (targetId in damagedTargets) return false

        if (checkCollision(target)) {
            damagedTargets.add(targetId)
            target.takeDamage(damage)
            return true
        }
        return false
    }
}
