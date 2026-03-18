package com.example.feijidazhan.gameobject

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.example.feijidazhan.interfaces.BulletInterface
import com.example.feijidazhan.interfaces.Damageable

/**
 * 子弹类
 * 支持多种子弹类型
 */
class Bullet(
    override var x: Int,
    override var y: Int,
    private val dx: Int,
    private val dy: Int,
    override var damage: Int,
    override var isPlayerBullet: Boolean,
    override var width: Int = 10,
    override var height: Int = 10,
    override var speed: Float = 1f,
    override var bitmap: Bitmap? = null
) : BulletInterface {
    
    override var isActive: Boolean = true
    
    override fun move(dx: Int, dy: Int) {
        x += dx
        y += dy
    }
    
    override fun updatePosition() {
        x += (dx * speed).toInt()
        y += (dy * speed).toInt()
    }
    
    override fun draw(canvas: Canvas, paint: Paint) {
        bitmap?.let {
            canvas.drawBitmap(it, x.toFloat(), y.toFloat(), paint)
        } ?: run {
            // 如果没有图片，绘制默认圆形子弹
            paint.color = if (isPlayerBullet) android.graphics.Color.YELLOW else android.graphics.Color.RED
            canvas.drawCircle(
                x + width / 2f,
                y + height / 2f,
                width / 2f,
                paint
            )
        }
    }
    
    override fun getBounds(): Rect {
        return Rect(x, y, x + width, y + height)
    }
    
    override fun checkCollision(target: Damageable): Boolean {
        if (!target.isAlive || !isActive) return false
        
        val targetBounds = Rect(
            target.x,
            target.y,
            target.x + target.width,
            target.y + target.height
        )
        return Rect.intersects(getBounds(), targetBounds)
    }
}
