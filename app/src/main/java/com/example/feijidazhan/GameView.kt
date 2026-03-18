package com.example.feijidazhan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.feijidazhan.gameobject.Player
import com.example.feijidazhan.manager.GameObjectManager
import com.example.feijidazhan.model.AircraftStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Random

/**
 * 游戏主视图
 * 使用新的规范化架构
 */
class GameView(
    context: Context,
    private val aircraftStats: AircraftStats,
    private val onGameOver: (Int) -> Unit
) : SurfaceView(context), SurfaceHolder.Callback {
    
    /**
     * 云层类
     */
    private data class Cloud(
        var x: Float,
        var y: Float,
        val alpha: Int
    )

    private val paint = Paint()
    private var gameLoopJob: Job? = null
    private var isRunning = false

    // 游戏对象管理器
    private lateinit var gameManager: GameObjectManager

    // 位图资源
    private lateinit var playerBitmap: Bitmap
    private lateinit var enemyBitmap: Bitmap
    private lateinit var eliteEnemyBitmap: Bitmap
    private lateinit var playerBulletBitmap: Bitmap
    private lateinit var enemyBulletBitmap: Bitmap
    private lateinit var backgroundBitmap: Bitmap
    private lateinit var cloudBitmap: Bitmap
    
    // 背景滚动相关
    private var backgroundY1 = 0
    private var backgroundY2 = 0
    private val backgroundSpeed = 3
    
    // 云层相关
    private val clouds = mutableListOf<Cloud>()
    private val cloudSpeed = 1.5f
    
    // 倒计时相关
    private var countdownTime = 3
    private var isCountingDown = true
    private var lastCountdownUpdate = 0L

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        val width = width
        val height = height

        // 初始化游戏对象管理器
        gameManager = GameObjectManager(context, width, height)

        // 加载位图资源
        loadBitmaps()

        // 初始化背景滚动位置
        backgroundY1 = 0
        backgroundY2 = -backgroundBitmap.height

        // 初始化云层
        initClouds(width, height)

        // 设置敌人和子弹位图
        gameManager.enemyBitmap = enemyBitmap
        gameManager.eliteEnemyBitmap = eliteEnemyBitmap
        gameManager.playerBulletBitmap = playerBulletBitmap
        gameManager.enemyBulletBitmap = enemyBulletBitmap

        // 创建玩家战机（使用选中的战机类型）
        val player = Player(aircraftStats, width, height)
        player.bitmap = playerBitmap
        gameManager.initPlayer(player)

        // 初始化倒计时
        countdownTime = 3
        isCountingDown = true
        lastCountdownUpdate = System.currentTimeMillis()

        // 开始游戏循环
        startGame()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopGame()
    }

    /**
     * 加载位图资源
     */
    private fun loadBitmaps() {
        val originalPlayerBitmap = BitmapFactory.decodeResource(resources, R.drawable.player)
        val originalEnemyBitmap = BitmapFactory.decodeResource(resources, R.drawable.enemy)
        val originalBulletBitmap = BitmapFactory.decodeResource(resources, R.drawable.bullet)
        val originalBackgroundBitmap = BitmapFactory.decodeResource(resources, R.drawable.bg_valley)
        val originalCloudBitmap = BitmapFactory.decodeResource(resources, R.drawable.cloud)

        // 根据战机类型缩放玩家图片
        playerBitmap = when (aircraftStats.name) {
            "重型战机" -> Bitmap.createScaledBitmap(originalPlayerBitmap, 250, 250, true)
            "轻型战机" -> Bitmap.createScaledBitmap(originalPlayerBitmap, 90, 90, true)
            else -> Bitmap.createScaledBitmap(originalPlayerBitmap, 160, 160, true)
        }
        enemyBitmap = Bitmap.createScaledBitmap(originalEnemyBitmap, 60, 60, true)
        eliteEnemyBitmap = Bitmap.createScaledBitmap(originalEnemyBitmap, 80, 80, true)
        // 子弹大小调整：
        // 玩家子弹和普通敌人一样大 (60x60)
        playerBulletBitmap = Bitmap.createScaledBitmap(originalBulletBitmap, 60, 60, true)
        // 敌人子弹为玩家的1/4 (40x40)
        enemyBulletBitmap = Bitmap.createScaledBitmap(originalBulletBitmap, 40, 40, true)
        backgroundBitmap = Bitmap.createScaledBitmap(originalBackgroundBitmap, width, height, true)
        // 加载并缩放云朵图片到屏幕宽度的1/3，保持宽高比例
        val originalWidth = originalCloudBitmap.width
        val originalHeight = originalCloudBitmap.height
        val cloudWidth = width / 3
        val cloudHeight = (cloudWidth * originalHeight.toFloat() / originalWidth.toFloat()).toInt()
        cloudBitmap = Bitmap.createScaledBitmap(originalCloudBitmap, cloudWidth, cloudHeight, true)
    }

    /**
     * 开始游戏
     */
    private fun startGame() {
        isRunning = true
        gameLoopJob = CoroutineScope(Dispatchers.Default).launch {
            while (isRunning && !gameManager.isGameOver) {
                update()
                draw()
                delay(16) // 约60fps
            }

            // 游戏结束
            if (gameManager.isGameOver) {
                withContext(Dispatchers.Main) {
                    onGameOver(gameManager.score)
                }
            }
        }
    }

    /**
     * 停止游戏
     */
    fun stopGame() {
        isRunning = false
        gameLoopJob?.cancel()
        gameManager.clear()
    }

    /**
     * 重新开始游戏
     */
    fun restartGame() {
        stopGame()
        gameManager.reset()
        startGame()
    }

    /**
     * 更新游戏逻辑
     */
    private fun update() {
        if (isCountingDown) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastCountdownUpdate >= 1000) {
                countdownTime--
                lastCountdownUpdate = currentTime
                if (countdownTime <= 0) {
                    isCountingDown = false
                }
            }
            return
        }
        
        // 更新游戏对象
        gameManager.update()

        // 玩家自动射击
        gameManager.playerShoot()
    }

    /**
     * 初始化云层
     */
    private fun initClouds(screenWidth: Int, screenHeight: Int) {
        clouds.clear()
        val random = Random()
        
        // 创建4-5朵云
        val cloudCount = 4 + random.nextInt(2) // 4-5朵
        repeat(cloudCount) {
            val x = random.nextFloat() * screenWidth
            val y = random.nextFloat() * screenHeight
            val alpha = 100 + random.nextInt(100) // 100-200
            
            clouds.add(Cloud(x, y, alpha))
        }
    }
    
    /**
     * 更新云层位置
     */
    private fun updateClouds(screenWidth: Int) {
        val random = Random()
        val cloudWidth = cloudBitmap.width
        val cloudHeight = cloudBitmap.height
        
        clouds.forEach { cloud ->
            cloud.y += cloudSpeed
            
            // 当云移出屏幕时，重新生成
            if (cloud.y > height + cloudHeight) {
                cloud.y = -cloudHeight.toFloat()
                cloud.x = random.nextFloat() * width
            }
        }
    }
    
    /**
     * 绘制云层
     */
    private fun drawClouds(canvas: Canvas) {
        clouds.forEach { cloud ->
            paint.alpha = cloud.alpha
            
            // 绘制云朵图片
            canvas.drawBitmap(cloudBitmap, cloud.x, cloud.y, paint)
        }
        
        // 重置透明度
        paint.alpha = 255
    }
    
    /**
     * 绘制游戏画面
     */
    private fun draw() {
        val canvas = holder.lockCanvas()
        if (canvas != null) {
            try {
                // 更新背景位置
                backgroundY1 += backgroundSpeed
                backgroundY2 += backgroundSpeed
                
                // 重置背景位置
                if (backgroundY1 >= backgroundBitmap.height) {
                    backgroundY1 = backgroundY2 - backgroundBitmap.height
                }
                if (backgroundY2 >= backgroundBitmap.height) {
                    backgroundY2 = backgroundY1 - backgroundBitmap.height
                }
                
                // 更新云层位置
                updateClouds(width)
                
                // 绘制背景
                canvas.drawBitmap(backgroundBitmap, 0f, backgroundY1.toFloat(), paint)
                canvas.drawBitmap(backgroundBitmap, 0f, backgroundY2.toFloat(), paint)
                
                // 绘制云层
                drawClouds(canvas)
                
                // 绘制所有游戏对象
                gameManager.draw(canvas, paint)
                
                // 绘制倒计时
                if (isCountingDown) {
                    paint.color = Color.WHITE
                    paint.textSize = 100f
                    paint.textAlign = Paint.Align.CENTER
                    val text = countdownTime.toString()
                    val x = width / 2f
                    val y = height / 2f + 40
                    canvas.drawText(text, x, y, paint)
                }
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    /**
     * 绘制UI界面
     */
    private fun drawUI(canvas: Canvas) {
        val gameState = gameManager.getGameState()

        // 绘制分数
        paint.color = Color.WHITE
        paint.textSize = 40f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Score: ${gameState.score}", 30f, 60f, paint)

        // 绘制生命值
        paint.color = Color.GREEN
        canvas.drawText(
            "HP: ${gameState.playerHealth}/${gameState.playerMaxHealth}",
            30f,
            110f,
            paint
        )

        // 绘制敌人数量
        paint.color = Color.YELLOW
        canvas.drawText("Enemies: ${gameState.enemyCount}", 30f, 160f, paint)
    }

    /**
     * 处理触摸事件
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_DOWN -> {
                gameManager.player?.let { player ->
                    player.moveTo(event.x.toInt(), event.y.toInt())
                }
            }
        }
        return true
    }
}
