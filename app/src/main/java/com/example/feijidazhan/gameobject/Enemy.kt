package com.example.feijidazhan.gameobject

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.example.feijidazhan.interfaces.AircraftInterface
import com.example.feijidazhan.model.AircraftStats
import com.example.feijidazhan.model.EquipmentStats
import com.example.feijidazhan.model.WeaponStats
import java.util.*

/**
 * 敌人类
 * 支持多种敌人类型
 */
class Enemy(
    val stats: AircraftStats,
    screenWidth: Int,
    private val movePattern: MovePattern = MovePattern.STRAIGHT
) : AircraftInterface {
    
    override val type: String = stats.name
    
    // 位置属性
    override var x: Int = (Math.random() * (screenWidth - stats.width)).toInt()
    override var y: Int = -stats.height
    override var speed: Float = stats.speed
    
    // 尺寸属性
    override var width: Int = stats.width
    override var height: Int = stats.height
    
    // 生命属性
    override var health: Int = stats.maxHealth
    override var maxHealth: Int = stats.maxHealth
    override var armor: Int = stats.armor
    override var isAlive: Boolean = true
    
    // 攻击属性
    override var damage: Int = 0
    override var fireRate: Long = 1000L
    override var lastAttackTime: Long = System.currentTimeMillis()
    
    // 状态属性
    override var bitmap: Bitmap? = null
    
    // 当前武器
    private var currentWeapon: WeaponStats? = null
    
    // 移动模式相关
    private var moveTime: Long = System.currentTimeMillis()
    private var moveDirection: Int = 1 // 1 为右，-1 为左
    
    init {
        // 默认装备敌人武器
        equipWeapon(com.example.feijidazhan.model.WeaponTypes.ENEMY_BULLET)
    }
    
    override fun move(dx: Int, dy: Int) {
        x += (dx * speed).toInt()
        y += (dy * speed).toInt()
    }
    
    override fun updatePosition() {
        when (movePattern) {
            MovePattern.STRAIGHT -> {
                // 直线向下移动
                y += (5 * speed).toInt()
            }
            MovePattern.ZIGZAG -> {
                // 锯齿形移动
                y += (3 * speed).toInt()
                val elapsed = System.currentTimeMillis() - moveTime
                if (elapsed > 1000) {
                    moveDirection *= -1
                    moveTime = System.currentTimeMillis()
                }
                x += (3 * speed * moveDirection).toInt()
            }
            MovePattern.CIRCLE -> {
                // 圆形移动（简化版）
                y += (2 * speed).toInt()
                val elapsed = System.currentTimeMillis() - moveTime
                val angle = (elapsed / 1000.0) * Math.PI * 2
                x += (Math.cos(angle) * 2 * speed).toInt()
            }
        }
    }
    
    override fun canAttack(): Boolean {
        val currentTime = System.currentTimeMillis()
        return currentTime - lastAttackTime >= fireRate && Math.random() < 0.02
    }
    
    override fun attack(): Bullet? {
        if (!canAttack() || !isAlive) return null
        
        lastAttackTime = System.currentTimeMillis()
        
        return Bullet(
            x = x + width / 2,
            y = y + height,
            dx = 0,
            dy = currentWeapon!!.bulletSpeed,
            damage = damage,
            isPlayerBullet = false,
            width = currentWeapon!!.bulletWidth,
            height = currentWeapon!!.bulletHeight,
            speed = 1f,
            bitmap = null
        )
    }
    
    override fun takeDamage(damage: Int): Int {
        if (!isAlive) return 0
        
        // 计算护甲减免
        val actualDamage = (damage - armor).coerceAtLeast(0)
        health -= actualDamage
        
        if (health <= 0) {
            health = 0
            isAlive = false
            onDeath()
        }
        
        return actualDamage
    }
    
    override fun heal(amount: Int) {
        if (!isAlive) return
        health = (health + amount).coerceAtMost(maxHealth)
    }
    
    override fun onDeath() {
        // 可以在这里添加死亡特效、掉落道具等
    }
    
    override fun equipWeapon(weapon: WeaponStats) {
        currentWeapon = weapon
        damage = weapon.damage
        fireRate = weapon.fireRate
    }
    
    override fun equipEquipment(equipment: EquipmentStats) {
        // 应用装备加成
        maxHealth += equipment.healthBonus
        health += equipment.healthBonus
        armor += equipment.armorBonus
        speed += equipment.speedBonus
        damage += equipment.damageBonus
        fireRate -= equipment.fireRateBonus
    }
    
    override fun draw(canvas: Canvas, paint: Paint) {
        bitmap?.let {
            canvas.drawBitmap(it, x.toFloat(), y.toFloat(), paint)
        } ?: run {
            // 如果没有图片，绘制默认矩形
            paint.color = when (type) {
                "精英敌人" -> android.graphics.Color.MAGENTA
                else -> android.graphics.Color.RED
            }
            canvas.drawRect(
                x.toFloat(),
                y.toFloat(),
                (x + width).toFloat(),
                (y + height).toFloat(),
                paint
            )
        }
    }
    
    override fun getBounds(): Rect {
        return Rect(x, y, x + width, y + height)
    }
    
    /**
     * 检查是否超出屏幕
     */
    fun isOutOfScreen(screenHeight: Int): Boolean {
        return y > screenHeight
    }
    
    /**
     * 敌人移动模式
     */
    enum class MovePattern {
        STRAIGHT,   // 直线
        ZIGZAG,     // 锯齿形
        CIRCLE      // 圆形
    }
    
    companion object {
        /**
         * 创建基础敌人
         */
        fun createBasicEnemy(screenWidth: Int): Enemy {
            return Enemy(
                stats = com.example.feijidazhan.model.AircraftTypes.BASIC_ENEMY,
                screenWidth = screenWidth,
                movePattern = MovePattern.STRAIGHT
            )
        }
        
        /**
         * 创建精英敌人
         */
        fun createEliteEnemy(screenWidth: Int): Enemy {
            return Enemy(
                stats = com.example.feijidazhan.model.AircraftTypes.ELITE_ENEMY,
                screenWidth = screenWidth,
                movePattern = MovePattern.ZIGZAG
            )
        }
    }
}
