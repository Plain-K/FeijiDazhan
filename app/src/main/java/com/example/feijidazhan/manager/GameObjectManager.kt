package com.example.feijidazhan.manager

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.media.MediaPlayer
import com.example.feijidazhan.gameobject.Bullet
import com.example.feijidazhan.gameobject.Enemy
import com.example.feijidazhan.gameobject.Explosion
import com.example.feijidazhan.gameobject.Laser
import com.example.feijidazhan.gameobject.Player
import com.example.feijidazhan.interfaces.Damageable

/**
 * 游戏对象管理器
 * 统一管理所有游戏对象的更新、绘制和碰撞检测
 */
class GameObjectManager(
    private val context: Context,
    private val screenWidth: Int,
    private val screenHeight: Int
) {
    // 游戏对象集合
    val player: Player? get() = _player
    private var _player: Player? = null
    
    private val enemies = mutableListOf<Enemy>()
    private val playerBullets = mutableListOf<Bullet>()
    private val enemyBullets = mutableListOf<Bullet>()
    private val playerLasers = mutableListOf<Laser>()  // 玩家激光
    private val explosions = mutableListOf<Explosion>()  // 爆炸特效
    
    // 待添加的对象（避免在遍历时修改集合）
    private val enemiesToAdd = mutableListOf<Enemy>()
    private val bulletsToAdd = mutableListOf<Bullet>()
    private val lasersToAdd = mutableListOf<Laser>()
    private val explosionsToAdd = mutableListOf<Explosion>()
    
    // 游戏状态
    var score: Int = 0
        private set
    var isGameOver: Boolean = false
        private set
    
    // 敌人生成相关
    private var lastEnemySpawnTime: Long = System.currentTimeMillis()
    private val enemySpawnInterval: Long = 2000L // 2秒生成一个敌人
    
    /**
     * 初始化玩家
     */
    fun initPlayer(player: Player) {
        _player = player
    }
    
    // 位图资源
    var enemyBitmap: android.graphics.Bitmap? = null
    var eliteEnemyBitmap: android.graphics.Bitmap? = null
    var playerBulletBitmap: android.graphics.Bitmap? = null
    var enemyBulletBitmap: android.graphics.Bitmap? = null
    
    /**
     * 更新所有游戏对象
     */
    fun update() {
        if (isGameOver) return
        
        // 生成敌人
        spawnEnemies()
        
        // 更新敌人
        enemies.forEach { enemy ->
            enemy.updatePosition()
            // 敌人自动攻击
            enemy.attack()?.let { bullet ->
                addEnemyBullet(bullet)
            }
        }
        
        // 更新子弹
        playerBullets.forEach { it.updatePosition() }
        enemyBullets.forEach { it.updatePosition() }
        
        // 更新爆炸特效
        explosions.forEach { it.update() }
        
        // 更新玩家位置
        player?.updatePosition()
        
        // 激光自动攻击（每秒一次）
        player?.let { p ->
            p.createLaser()?.let { laser ->
                addPlayerLaser(laser)
            }
        }
        
        // 碰撞检测
        checkCollisions()
        
        // 清理无效对象
        cleanup()
        
        // 添加待添加的对象
        flushPendingObjects()
        
        // 检查游戏结束
        checkGameOver()
    }
    
    /**
     * 绘制所有游戏对象
     */
    fun draw(canvas: Canvas, paint: Paint) {
        // 绘制玩家
        player?.draw(canvas, paint)
        
        // 绘制敌人
        enemies.forEach { it.draw(canvas, paint) }
        
        // 绘制子弹
        playerBullets.forEach { it.draw(canvas, paint) }
        enemyBullets.forEach { it.draw(canvas, paint) }
        
        // 绘制激光
        playerLasers.forEach { it.draw(canvas, paint) }
        
        // 绘制爆炸特效
        explosions.forEach { it.draw(canvas, paint) }
    }
    
    /**
     * 玩家发射子弹
     */
    fun playerShoot(): Boolean {
        val player = this.player ?: return false
        val bullet = player.attack() ?: return false
        addPlayerBullet(bullet)
        return true
    }
    
    /**
     * 添加玩家子弹
     */
    fun addPlayerBullet(bullet: Bullet) {
        bullet.bitmap = playerBulletBitmap
        bulletsToAdd.add(bullet)
    }
    
    /**
     * 添加玩家激光
     */
    fun addPlayerLaser(laser: Laser) {
        lasersToAdd.add(laser)
    }
    
    /**
     * 添加敌人子弹
     */
    fun addEnemyBullet(bullet: Bullet) {
        bullet.bitmap = enemyBulletBitmap
        bulletsToAdd.add(bullet)
    }
    
    /**
     * 添加爆炸特效
     */
    fun addExplosion(explosion: Explosion) {
        explosionsToAdd.add(explosion)
    }
    
    /**
     * 添加敌人
     */
    fun addEnemy(enemy: Enemy) {
        enemiesToAdd.add(enemy)
    }
    
    /**
     * 生成敌人
     */
    private fun spawnEnemies() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastEnemySpawnTime >= enemySpawnInterval) {
            // 随机生成基础敌人或精英敌人
            val enemy = if (Math.random() < 0.8) {
                Enemy.createBasicEnemy(screenWidth)
            } else {
                Enemy.createEliteEnemy(screenWidth)
            }
            // 设置敌人位图
            when (enemy.type) {
                "精英敌人" -> enemy.bitmap = eliteEnemyBitmap
                else -> enemy.bitmap = enemyBitmap
            }
            addEnemy(enemy)
            lastEnemySpawnTime = currentTime
        }
    }
    
    /**
     * 碰撞检测
     */
    private fun checkCollisions() {
        val player = this.player ?: return
        
        // 玩家子弹与敌人碰撞
        playerBullets.forEach { bullet ->
            if (!bullet.isActive) return@forEach
            enemies.forEach { enemy ->
                if (enemy.isAlive && bullet.checkCollision(enemy)) {
                    val wasAlive = enemy.isAlive
                    val damage = enemy.takeDamage(bullet.damage)
                    bullet.isActive = false
                    if (wasAlive && !enemy.isAlive) {
                        score += 10
                        // 创建爆炸特效
                        val explosion = Explosion(enemy.x, enemy.y, enemy.width, enemy.height)
                        addExplosion(explosion)
                        // 播放爆炸声音
                        playExplosionSound()
                    }
                }
            }
        }
        
        // 玩家激光与敌人碰撞（激光对路径上所有敌人造成伤害）
        playerLasers.forEach { laser ->
            if (!laser.isActive) return@forEach
            enemies.forEach { enemy ->
                if (enemy.isAlive) {
                    val wasAlive = enemy.isAlive
                    val hit = laser.dealDamage(enemy)
                    if (hit && wasAlive && !enemy.isAlive) {
                        score += 10
                        // 创建爆炸特效
                        val explosion = Explosion(enemy.x, enemy.y, enemy.width, enemy.height)
                        addExplosion(explosion)
                        // 播放爆炸声音
                        playExplosionSound()
                    }
                }
            }
        }
        
        // 敌人子弹与玩家碰撞
        enemyBullets.forEach { bullet ->
            if (player.isAlive && bullet.checkCollision(player)) {
                player.takeDamage(bullet.damage)
                bullet.isActive = false
            }
        }
        
        // 敌人与玩家碰撞
        enemies.forEach { enemy ->
            if (player.isAlive && enemy.isAlive) {
                val playerBounds = player.getBounds()
                val enemyBounds = enemy.getBounds()
                if (android.graphics.Rect.intersects(playerBounds, enemyBounds)) {
                    // 碰撞伤害
                    player.takeDamage(enemy.damage)
                    enemy.takeDamage(player.damage)
                }
            }
        }
    }
    
    /**
     * 清理无效对象
     */
    private fun cleanup() {
        // 移除超出屏幕或已销毁的敌人
        enemies.removeAll { 
            it.isOutOfScreen(screenHeight) || !it.isAlive 
        }

        // 移除无效的子弹
        playerBullets.removeAll { !it.isActive }
        enemyBullets.removeAll { !it.isActive }

        // 移除过期的激光
        playerLasers.removeAll { !it.isStillActive() }
        
        // 移除过期的爆炸特效
        explosions.removeAll { !it.isActive }
    }

    /**
     * 刷新待添加的对象
     */
    private fun flushPendingObjects() {
        // 分类添加子弹
        bulletsToAdd.forEach { bullet ->
            if (bullet.isPlayerBullet) {
                playerBullets.add(bullet)
            } else {
                enemyBullets.add(bullet)
            }
        }
        bulletsToAdd.clear()

        // 添加激光
        playerLasers.addAll(lasersToAdd)
        lasersToAdd.clear()

        // 添加敌人
        enemies.addAll(enemiesToAdd)
        enemiesToAdd.clear()
        
        // 添加爆炸特效
        explosions.addAll(explosionsToAdd)
        explosionsToAdd.clear()
    }
    
    /**
     * 检查游戏结束
     */
    private fun checkGameOver() {
        if (player?.isAlive == false) {
            isGameOver = true
        }
    }
    
    /**
     * 重置游戏
     */
    fun reset() {
        enemies.clear()
        playerBullets.clear()
        enemyBullets.clear()
        playerLasers.clear()
        explosions.clear()
        enemiesToAdd.clear()
        bulletsToAdd.clear()
        lasersToAdd.clear()
        explosionsToAdd.clear()
        score = 0
        isGameOver = false
        lastEnemySpawnTime = System.currentTimeMillis()
        player?.reset()
    }
    
    /**
     * 清理所有资源
     */
    fun clear() {
        enemies.clear()
        playerBullets.clear()
        enemyBullets.clear()
        playerLasers.clear()
        explosions.clear()
        enemiesToAdd.clear()
        bulletsToAdd.clear()
        lasersToAdd.clear()
        explosionsToAdd.clear()
        _player = null
    }
    
    /**
     * 播放爆炸声音
     */
    private fun playExplosionSound() {
        try {
            // 使用自定义的爆炸音效
            val mediaPlayer = MediaPlayer.create(context, com.example.feijidazhan.R.raw.shot)
            mediaPlayer.start()
            // 播放完成后释放资源
            mediaPlayer.setOnCompletionListener {
                it.release()
            }
        } catch (e: Exception) {
            // 忽略音效播放错误
        }
    }
    
    /**
     * 获取当前游戏状态信息
     */
    fun getGameState(): GameState {
        return GameState(
            score = score,
            playerHealth = player?.health ?: 0,
            playerMaxHealth = player?.maxHealth ?: 0,
            enemyCount = enemies.size,
            isGameOver = isGameOver
        )
    }
    
    /**
     * 游戏状态数据类
     */
    data class GameState(
        val score: Int,
        val playerHealth: Int,
        val playerMaxHealth: Int,
        val enemyCount: Int,
        val isGameOver: Boolean
    )
}
