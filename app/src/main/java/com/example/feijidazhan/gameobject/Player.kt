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
 * 玩家战机类
 * 支持多种战机类型和装备
 */
class Player(
    val stats: AircraftStats,
    screenWidth: Int,
    screenHeight: Int
) : AircraftInterface {
    
    override val type: String = stats.name
    
    // 位置属性
    override var x: Int = screenWidth / 2 - stats.width / 2
    override var y: Int = screenHeight - stats.height - 50
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
    override var fireRate: Long = 500L
    override var lastAttackTime: Long = 0L
    
    // 状态属性
    override var bitmap: Bitmap? = null
    
    // 当前武器
    private var currentWeapon: WeaponStats? = null
    
    // 屏幕边界
    private val screenWidth: Int = screenWidth
    private val screenHeight: Int = screenHeight
    
    // 平滑移动相关
    private var targetX: Int = x
    private var targetY: Int = y
    private val moveSpeed = 0.1f  // 平滑移动速度系数
    
    init {
        // 根据战机类型装备不同武器
        val weaponType = when (stats.name) {
            "重型战机" -> com.example.feijidazhan.model.WeaponTypes.LASER_CANNON
            else -> com.example.feijidazhan.model.WeaponTypes.BASIC_BULLET
        }
        equipWeapon(weaponType)
    }
    
    override fun move(dx: Int, dy: Int) {
        x += (dx * speed).toInt()
        y += (dy * speed).toInt()
        
        // 限制在屏幕范围内
        constrainToScreen()
    }
    
    override fun updatePosition() {
        // 平滑移动到目标位置
        x += ((targetX - x) * moveSpeed).toInt()
        y += ((targetY - y) * moveSpeed).toInt()
        
        // 限制在屏幕范围内
        constrainToScreen()
    }
    
    /**
     * 移动到指定位置（用于触摸控制）
     */
    fun moveTo(targetX: Int, targetY: Int) {
        this.targetX = targetX - width / 2
        this.targetY = targetY - height / 2
    }
    
    /**
     * 限制在屏幕范围内
     */
    private fun constrainToScreen() {
        if (x < 0) x = 0
        if (x > screenWidth - width) x = screenWidth - width
        if (y < 0) y = 0
        if (y > screenHeight - height) y = screenHeight - height
    }
    
    override fun canAttack(): Boolean {
        val currentTime = System.currentTimeMillis()
        return currentTime - lastAttackTime >= fireRate
    }
    
    override fun attack(): Bullet? {
        if (!canAttack() || !isAlive) return null
        
        lastAttackTime = System.currentTimeMillis()
        
        // 激光炮不在这里创建，由专门的 createLaser 方法处理
        if (currentWeapon?.name == "激光炮") {
            return null
        }
        
        return Bullet(
            x = x + width / 2,
            y = y,
            dx = 0,
            dy = -currentWeapon!!.bulletSpeed,
            damage = damage,
            isPlayerBullet = true,
            width = currentWeapon!!.bulletWidth,
            height = currentWeapon!!.bulletHeight,
            speed = 1f,
            bitmap = null
        )
    }
    
    /**
     * 创建激光（用于激光炮）
     */
    fun createLaser(): Laser? {
        if (!canAttack() || !isAlive) return null
        if (currentWeapon?.name != "激光炮") return null
        
        lastAttackTime = System.currentTimeMillis()
        
        return Laser(
            startX = x + width / 2,
            startY = y,
            endY = 0,  // 激光贯穿到屏幕顶部
            damage = damage,
            width = currentWeapon!!.bulletWidth
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
        // 可以在这里添加死亡特效、音效等
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
            paint.color = android.graphics.Color.GREEN
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
     * 重置玩家状态（用于重新开始游戏）
     */
    fun reset() {
        health = maxHealth
        isAlive = true
        x = screenWidth / 2 - width / 2
        y = screenHeight - height - 50
        lastAttackTime = 0L
    }
}
