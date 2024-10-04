package com.example.tictactoe

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val board = Array(3) { IntArray(3) }
    private var activePlayer = 1
    private var gameActive = true
    private var easyMode = false
    private var hardMode = false

    private lateinit var blocks: List<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        blocks = listOf(
            findViewById(R.id.block1),
            findViewById(R.id.block2),
            findViewById(R.id.block3),
            findViewById(R.id.block4),
            findViewById(R.id.block5),
            findViewById(R.id.block6),
            findViewById(R.id.block7),
            findViewById(R.id.block8),
            findViewById(R.id.block9)
        )
    }

    fun playerTap(view: View) {
        if (!gameActive) return

        val selectedBlock = view as ImageView
        val tag = selectedBlock.tag.toString().toInt()

        val row = tag / 3
        val col = tag % 3

        if (board[row][col] != 0) return

        if (activePlayer == 1) {
            board[row][col] = 1
            gameState[tag] = 1  // Update the gameState as well
            selectedBlock.setImageResource(R.drawable.x)
            activePlayer = 2
            checkWinner()

            if (gameActive && hardMode) {
                aiMoveHard()
            } else if (gameActive) {
                aiMove()
            }
        }
    }

    private fun aiMove() {
        if (!easyMode || !gameActive) return

        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == 0) emptyCells.add(Pair(i, j))
            }
        }

        if (emptyCells.isNotEmpty()) {
            val randomMove = emptyCells[Random.nextInt(emptyCells.size)]
            board[randomMove.first][randomMove.second] = 2
            val blockIndex = randomMove.first * 3 + randomMove.second
            blocks[blockIndex].setImageResource(R.drawable.o)
            activePlayer = 1
            checkWinner()
        }
    }

    private fun checkWinner() {
        val winPositions = arrayOf(
            arrayOf(0, 1, 2), arrayOf(3, 4, 5), arrayOf(6, 7, 8),
            arrayOf(0, 3, 6), arrayOf(1, 4, 7), arrayOf(2, 5, 8),
            arrayOf(0, 4, 8), arrayOf(2, 4, 6)
        )

        for (position in winPositions) {
            val a = board[position[0] / 3][position[0] % 3]
            val b = board[position[1] / 3][position[1] % 3]
            val c = board[position[2] / 3][position[2] % 3]

            if (a == b && b == c && a != 0) {
                gameActive = false
                if (a == 1) Toast.makeText(this, "Player 1 Wins!", Toast.LENGTH_SHORT).show()
                else Toast.makeText(this, "AI Wins!", Toast.LENGTH_SHORT).show()
                return
            }
        }

        var draw = true
        for (row in board) {
            if (row.contains(0)) {
                draw = false
                break
            }
        }
        if (draw) {
            gameActive = false
            Toast.makeText(this, "It's a Draw!", Toast.LENGTH_SHORT).show()
        }
    }

    fun startEasyMode(view: View) {
        resetBoard()
        easyMode = true
        Toast.makeText(this, "Easy Mode Started", Toast.LENGTH_SHORT).show()
    }

    private fun resetBoard() {
        for (i in 0..2) {
            for (j in 0..2) {
                board[i][j] = 0
            }
        }
        for (i in gameState.indices) {
            gameState[i] = 2  // Reset gameState along with the board
        }
        for (block in blocks) block.setImageDrawable(null)
        activePlayer = 1
        easyMode = false
        hardMode = false
        gameActive = true
    }

    var gameState = IntArray(9) { 2 }

    fun resetGame(view: View) {
        activePlayer = 0
        gameActive = true
        for (i in gameState.indices) gameState[i] = 2
        for (block in blocks) block.setImageResource(0)
        Toast.makeText(this, "Game has been reset!", Toast.LENGTH_SHORT).show()
    }

    fun minimax(board: IntArray, depth: Int, isMaximizing: Boolean, alpha: Int, beta: Int): Int {
        val score = checkWinnerForMinimax(board)

        if (score == 10) return score - depth
        if (score == -10) return score + depth
        if (isBoardFull()) return 0

        var newAlpha = alpha
        var newBeta = beta

        if (isMaximizing) {
            var best = Int.MIN_VALUE

            for (i in board.indices) {
                if (board[i] == 2) {
                    board[i] = 1
                    val value = minimax(board, depth + 1, false, newAlpha, newBeta)
                    best = maxOf(best, value)
                    board[i] = 2
                    newAlpha = maxOf(newAlpha, best)
                    if (newBeta <= newAlpha) break
                }
            }
            return best
        } else {
            var best = Int.MAX_VALUE

            for (i in board.indices) {
                if (board[i] == 2) {
                    board[i] = 0
                    val value = minimax(board, depth + 1, true, newAlpha, newBeta)
                    best = minOf(best, value)
                    board[i] = 2
                    newBeta = minOf(newBeta, best)
                    if (newBeta <= newAlpha) break
                }
            }
            return best
        }
    }

    fun aiMoveHard() {
        var bestMove = -1
        var bestValue = Int.MIN_VALUE

        for (i in gameState.indices) {
            if (gameState[i] == 2) {
                gameState[i] = 1
                board[i / 3][i % 3] = 1  // Sync the board with the AI move
                val moveValue = minimax(gameState, 0, false, Int.MIN_VALUE, Int.MAX_VALUE)
                gameState[i] = 2
                board[i / 3][i % 3] = 0  // Revert the board change after evaluation

                if (moveValue > bestValue) {
                    bestMove = i
                    bestValue = moveValue
                }
            }
        }

        if (bestMove != -1) {
            gameState[bestMove] = 1
            board[bestMove / 3][bestMove % 3] = 1  // Update the board with the best move
            updateGameUI(bestMove, R.drawable.o)
        }
    }

    fun isBoardFull(): Boolean {
        for (i in gameState) {
            if (i == 2) return false
        }
        return true
    }

    fun checkWinnerForMinimax(board: IntArray): Int {
        val winningPositions = arrayOf(
            intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)
        )
        for (position in winningPositions) {
            if (board[position[0]] == board[position[1]] &&
                board[position[1]] == board[position[2]] &&
                board[position[0]] != 2
            ) {
                return if (board[position[0]] == 1) 10 else -10
            }
        }
        return 0
    }

    private fun updateGameUI(index: Int, drawable: Int) {
        blocks[index].setImageResource(drawable)
        activePlayer = 1
        checkWinner()
    }

    fun startHardGame(view: View) {
//        resetGame(view)
        resetBoard()
        hardMode = true
        Toast.makeText(this, "Hard mode activated! You start.", Toast.LENGTH_SHORT).show()
    }
}
