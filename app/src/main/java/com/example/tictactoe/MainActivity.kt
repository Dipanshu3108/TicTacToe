package com.example.tictactoe

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private val board = Array(3) { DoubleArray(3) }
    private var activePlayer = 1.0
    private var gameActive = true
    private var easyMode = false
    private var mediumMode = false
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

//    private var playerTurn = true

    fun playerTap(view: View) {
        if (!gameActive) return

        val selectedBlock = view as ImageView
        val tag = selectedBlock.tag.toString().toInt()

        val row = tag / 3
        val col = tag % 3

        if (board[row][col] != 0.0) return

        board[row][col] = 1.0
        selectedBlock.setImageResource(R.drawable.x)
//        playerTurn = false
        checkWinner()

        if (gameActive) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(300) // Add a small delay for better UX
                when {
                    easyMode -> aiMove()
                    mediumMode -> aiMoveMedium()
                    hardMode -> aiMoveHard()
                }
//                if (hardMode) {
//                    aiMoveHard()
//                } else if (easyMode) {
//                    aiMove()
//                } else if (mediumMode){
//                    aiMoveMedium()
//                }
            }
        }
    }

    private fun aiMove() {
//        if (!easyMode || !mediumMode || !gameActive) return

        if (!gameActive) return
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == 0.0) emptyCells.add(Pair(i, j))
            }
        }

        if (emptyCells.isNotEmpty()) {
            val randomMove = emptyCells[Random.nextInt(emptyCells.size)]
            board[randomMove.first][randomMove.second] = 2.0
            val blockIndex = randomMove.first * 3 + randomMove.second
            blocks[blockIndex].setImageResource(R.drawable.o)
            activePlayer = 1.0
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

            if (a == b && b == c && a != 0.0) {
                gameActive = false
                if (a == 1.0) Toast.makeText(this, "Player 1 Wins!", Toast.LENGTH_SHORT).show()
                else Toast.makeText(this, "AI Wins!", Toast.LENGTH_SHORT).show()
                return
            }
        }

        if (isDraw()) {
            gameActive = false
            Toast.makeText(this, "It's a Draw!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isDraw(): Boolean {
        for (row in board) {
            for (cell in row) {
                if (cell == 0.0) return false
            }
        }
        return true
    }

    fun startEasyMode(view: View) {
        resetBoard()
        easyMode = true
        mediumMode = false
        hardMode = false
        Toast.makeText(this, "Easy Mode Started", Toast.LENGTH_SHORT).show()
    }

    fun startMediumMode(view: View){
        resetBoard()
        mediumMode = true
        easyMode = false
        hardMode = false
        Toast.makeText(this, "Medium Mode Started", Toast.LENGTH_SHORT).show()
    }

    fun startHardMode(view: View) {
        resetBoard()
        hardMode = true
        easyMode = false
        mediumMode = false
        Toast.makeText(this, "Hard Mode Started", Toast.LENGTH_SHORT).show()
    }

    private fun resetBoard() {
        for (i in 0..2) {
            for (j in 0..2) {
                board[i][j] = 0.0
            }
        }
        for (i in gameState.indices) {
            gameState[i] = 2.0  // Reset gameState along with the board
        }
        for (block in blocks) block.setImageDrawable(null)
        activePlayer = 1.0
        easyMode = false
        mediumMode = false
        hardMode = false
        gameActive = true
    }

    private var gameState = DoubleArray(9) { 2.0 }

    fun resetGame(view: View) {
        activePlayer = 0.0
        gameActive = true
        for (i in gameState.indices) gameState[i] = 2.0
        for (block in blocks) block.setImageResource(0)
        Toast.makeText(this, "Game has been reset!", Toast.LENGTH_SHORT).show()
    }

    private fun bestMove(): Pair<Int, Int> {
        var bestScore = Double.NEGATIVE_INFINITY
        var move = Pair(-1, -1)

        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == 0.0) {
                    board[i][j] = 2.0 // AI's move
                    val score = minimax(board, 0, false, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
                    board[i][j] = 0.0 // Undo the move
                    if (score > bestScore) {
                        bestScore = score
                        move = Pair(i, j)
                    }
                }
            }
        }
        return move
    }

    private fun minimax(board: Array<DoubleArray>, depth: Int, isMaximizing: Boolean, alpha: Double, beta: Double): Double {
        var mutableAlpha = alpha
        var mutableBeta = beta

        // Check if there's a winner or if the board is full
        val result = checkWinnerForMinimax(board)
        if (result != 0.0) return result // AI wins (2.0) or Player wins (1.0)
        if (isBoardFull(board)) return 0.0 // It's a draw

        // Maximizing player (AI's move)
        if (isMaximizing) {
            var bestScore = Double.NEGATIVE_INFINITY

            // Loop through all cells to find available moves
            for (i in 0..2) {
                for (j in 0..2) {
                    if (board[i][j] == 0.0) {
                        // Simulate AI move
                        board[i][j] = 2.0
                        // Recursively call minimax to evaluate the move
                        val score = minimax(board, depth + 1, false, mutableAlpha, mutableBeta)
                        // Undo the move after evaluation
                        board[i][j] = 0.0

                        // Update the best score for the maximizing player (AI)
                        bestScore = maxOf(score, bestScore)

                        // Alpha-beta pruning: update alpha and break if pruning condition is met
                        mutableAlpha = maxOf(mutableAlpha, bestScore)
                        if (mutableBeta <= mutableAlpha) {
                            return bestScore // Beta cut-off
                        }
                    }
                }
            }
            return bestScore
        } else {
            // Minimizing player (Human's move)
            var bestScore = Double.POSITIVE_INFINITY

            // Loop through all cells to find available moves
            for (i in 0..2) {
                for (j in 0..2) {
                    if (board[i][j] == 0.0) {
                        // Simulate player's move
                        board[i][j] = 1.0
                        // Recursively call minimax to evaluate the move
                        val score = minimax(board, depth + 1, true, mutableAlpha, mutableBeta)
                        // Undo the move after evaluation
                        board[i][j] = 0.0

                        // Update the best score for the minimizing player (Human)
                        bestScore = minOf(score, bestScore)

                        // Alpha-beta pruning: update beta and break if pruning condition is met
                        mutableBeta = minOf(mutableBeta, bestScore)
                        if (mutableBeta <= mutableAlpha) {
                            return bestScore // Alpha cut-off
                        }
                    }
                }
            }
            return bestScore
        }
    }

//    medium mode:
    private fun aiMoveMedium(){
        if (!gameActive) return

        if (Random.nextBoolean()) {
            aiMove()  // Perform a random move
            Log.d("TicTacToe", "AI made a random move.")
        } else {
            aiMoveHard()  // Perform a minimax move (hard mode logic)
            Log.d("TicTacToe", "AI made a optimal move.")
        }
    }

    private fun aiMoveHard() {
//        if (!hardMode || !gameActive) return

        if (!gameActive)  return
        val (row, col) = bestMove()
        board[row][col] = 2.0
        val blockIndex = row * 3 + col
        blocks[blockIndex].setImageResource(R.drawable.o)
        activePlayer = 1.0
//        playerTurn = true
        checkWinner()
    }

    private fun isBoardFull(board: Array<DoubleArray>): Boolean {
        for (row in board) {
            for (cell in row) {
                if (cell == 0.0) return false
            }
        }
        return true
    }

    private fun checkWinnerForMinimax(board: Array<DoubleArray>): Double {
        // Check rows, columns, and diagonals
        for (i in 0..2) {
            if (board[i][0] == board[i][1] && board[i][1] == board[i][2] && board[i][0] != 0.0) {
                return if (board[i][0] == 2.0) 10.0 else -10.0
            }
            if (board[0][i] == board[1][i] && board[1][i] == board[2][i] && board[0][i] != 0.0) {
                return if (board[0][i] == 2.0) 10.0 else -10.0
            }
        }
        if (board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[0][0] != 0.0) {
            return if (board[0][0] == 2.0) 10.0 else -10.0
        }
        if (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[0][2] != 0.0) {
            return if (board[0][2] == 2.0) 10.0 else -10.0
        }
        return 0.0
    }

    private fun updateGameUI(moveIndex: Int, resId: Int) {
        blocks[moveIndex].setImageResource(resId)
        activePlayer = 1.0
        checkWinner()
    }
}