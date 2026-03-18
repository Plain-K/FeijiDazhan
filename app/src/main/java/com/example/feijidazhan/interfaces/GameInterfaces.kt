package com.example.feijidazhan.interfaces

import android.graphics.Canvas
import android.graphics.Bitmap

/**
 * 可移动接口
 * 所有可以移动的游戏对象都应实现此接口
 */
interface Movable {
    var x: Int
    var y: Int
    var speed: Float
    
    fun move(dx: Int, dy: Int)
    fun updatePosition()
}

/**
 * 可攻击接口
 * 所有可以攻击的游戏对象都应实现此接口
 */
interface Attackable {
    var damage: Int
    var fireRate: Long
    var lastAttackTime: Long
    
    fun canAttack(): Boolean
    fun attack(): BulletInterface?
}

/**
 * 可受伤接口
 * 所有可以受到伤害的游戏对象都应实现此接口
 */
interface Damageable {
    var health: Int
    var maxHealth: Int
    var armor: Int
    var isAlive: Boolean
    var x: Int
    var y: Int
    var width: Int
    var height: Int
    
    fun takeDamage(damage: Int): Int  // 返回实际受到的伤害
    fun heal(amount: Int)
    fun onDeath()
}

/**
 * 可绘制接口
 * 所有需要绘制的游戏对象都应实现此接口
 */
interface Drawable {
    var width: Int
    var height: Int
    
    fun draw(canvas: Canvas, paint: android.graphics.Paint)
    fun getBounds(): android.graphics.Rect
}

/**
 * 子弹接口
 */
interface BulletInterface : Movable, Drawable {
    var isActive: Boolean
    var damage: Int
    var isPlayerBullet: Boolean
    var bitmap: Bitmap?
    
    fun checkCollision(target: Damageable): Boolean
}

/**
 * 战机接口
 */
interface AircraftInterface : Movable, Attackable, Damageable, Drawable {
    val type: String
    var bitmap: Bitmap?
    
    fun equipWeapon(weapon: com.example.feijidazhan.model.WeaponStats)
    fun equipEquipment(equipment: com.example.feijidazhan.model.EquipmentStats)
}

/**
 * 游戏对象接口
 * 所有游戏对象的基接口
 */
interface GameObject : Drawable {
    val id: String
    var isActive: Boolean
    
    fun update()
    fun destroy()
}
