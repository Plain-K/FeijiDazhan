package com.example.feijidazhan

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.feijidazhan.model.AircraftStats
import com.example.feijidazhan.model.AircraftTypes

class MainActivity : AppCompatActivity() {

    private var currentGameView: GameView? = null
    
    // 当前选中的战机类型，默认为基础战机
    private var selectedAircraft: AircraftStats = AircraftTypes.BASIC_FIGHTER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 隐藏标题栏和状态栏
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        // 显示主菜单
        showMainMenu()
    }

    private fun showMainMenu() {
        currentGameView?.stopGame()
        currentGameView = null
        val mainMenuView = MainMenuView(
            context = this,
            onStartGame = {
                // 点击开始游戏按钮后切换到游戏界面
                startGame()
            },
            onOpenHangar = {
                // 点击机库按钮后切换到机库界面
                showHangar()
            }
        )
        setContentView(mainMenuView)
    }

    private fun showHangar() {
        val hangarView = HangarView(
            context = this,
            currentAircraft = selectedAircraft,
            onSelectAircraft = { aircraft ->
                // 选择战机后返回主菜单
                selectedAircraft = aircraft
                showMainMenu()
            },
            onBackToMenu = {
                // 返回主菜单
                showMainMenu()
            }
        )
        setContentView(hangarView)
    }

    private fun startGame() {
        val gameView = GameView(
            context = this,
            aircraftStats = selectedAircraft,
            onGameOver = { score ->
                // 游戏结束后显示游戏结束界面
                showGameOver(score)
            }
        )
        currentGameView = gameView
        setContentView(gameView)
    }

    private fun showGameOver(score: Int) {
        currentGameView = null
        val gameOverView = GameOverView(
            context = this,
            score = score,
            onContinue = {
                // 继续游戏 - 重新开始
                startGame()
            },
            onReturnToMain = {
                // 返回主界面
                showMainMenu()
            }
        )
        setContentView(gameOverView)
    }
}
