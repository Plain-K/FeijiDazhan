package com.example.feijidazhan.model

/**
 * 战机基础属性数据类
 * 用于定义不同类型战机的基本属性
 */
data class AircraftStats(
    val name: String,
    val maxHealth: Int,
    val width: Int,
    val height: Int,
    val speed: Float,
    val armor: Int = 0  // 护甲值，减少受到的伤害
)

/**
 * 武器属性数据类
 */
data class WeaponStats(
    val name: String,
    val damage: Int,
    val fireRate: Long,  // 发射间隔（毫秒）
    val bulletSpeed: Int,
    val bulletWidth: Int = 10,
    val bulletHeight: Int = 10,
    val isPlayerWeapon: Boolean = true
)

/**
 * 装备属性数据类
 */
data class EquipmentStats(
    val name: String,
    val healthBonus: Int = 0,
    val armorBonus: Int = 0,
    val speedBonus: Float = 0f,
    val damageBonus: Int = 0,
    val fireRateBonus: Long = 0L
)

/**
 * 预定义的战机类型
 */
object AircraftTypes {
    // 基础战机
    val BASIC_FIGHTER = AircraftStats(
        name = "基础战机",
        maxHealth = 100,
        width = 80,
        height = 80,
        speed = 1f
    )

    // 重型战机 - 高血量低速度
    val HEAVY_FIGHTER = AircraftStats(
        name = "重型战机",
        maxHealth = 200,
        width = 200,  // 扩大2倍
        height = 200,  // 扩大2倍
        speed = 0.7f,
        armor = 20
    )

    // 轻型战机 - 低血量高速度
    val LIGHT_FIGHTER = AircraftStats(
        name = "轻型战机",
        maxHealth = 60,
        width = 48,  // 0.8倍
        height = 48,  // 0.8倍
        speed = 1.5f  // 提高到1.5倍
    )

    // 基础敌人
    val BASIC_ENEMY = AircraftStats(
        name = "基础敌人",
        maxHealth = 100,
        width = 60,
        height = 60,
        speed = 1f
    )

    // 精英敌人
    val ELITE_ENEMY = AircraftStats(
        name = "精英敌人",
        maxHealth = 300,
        width = 80,
        height = 80,
        speed = 0.8f,
        armor = 10
    )
}

/**
 * 预定义的武器类型
 */
object WeaponTypes {
    // 基础子弹
    val BASIC_BULLET = WeaponStats(
        name = "基础子弹",
        damage = 120,
        fireRate = 500L,
        bulletSpeed = 15
    )

    // 快速子弹
    val RAPID_BULLET = WeaponStats(
        name = "快速子弹",
        damage = 80,
        fireRate = 300L,
        bulletSpeed = 20
    )

    // 重型子弹
    val HEAVY_BULLET = WeaponStats(
        name = "重型子弹",
        damage = 200,
        fireRate = 800L,
        bulletSpeed = 12
    )

    // 激光炮 - 重型战机专用，每秒发射一次，直线穿透伤害
    val LASER_CANNON = WeaponStats(
        name = "激光炮",
        damage = 190,
        fireRate = 1000L,  // 每秒发射一次
        bulletSpeed = 0,   // 激光瞬间命中，不需要速度
        bulletWidth = 20,
        bulletHeight = 0   // 高度为0表示贯穿整个屏幕
    )

    // 敌人子弹
    val ENEMY_BULLET = WeaponStats(
        name = "敌人子弹",
        damage = 120,
        fireRate = 1000L,
        bulletSpeed = 10,
        isPlayerWeapon = false
    )
}
