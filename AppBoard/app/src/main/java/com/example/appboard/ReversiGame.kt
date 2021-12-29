package com.example.appboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView

class ReversiGame : Activity() {
    private var board = arrayOf(CharArray(8), CharArray(8), CharArray(8), CharArray(8), CharArray(8), CharArray(8), CharArray(8),CharArray(8))
    private var possiblePlays = ArrayList<PossiblePlay>()

    var player1 = Player('B')
    var player2 = Player('W')

    val switchPositions = ArrayList<Pair<Int, Int>>()

    //region Ons
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.board)

        setUI()
    }

    fun onPlay(v: View) {
        val positions = v.resources.getResourceName(v.id).replace("com.example.appboard:id/Pos", "")
        val positionX = positions.take(1).toInt()
        val positionY = positions.takeLast(1).toInt()

        var player = getCurrentPlayer()

        if(player.hasSwitchPressed)
        {
            setSwitchMove(v, positionX, positionY)

            if(switchPositions.size == 3){
                makeSwitchMove(v)
            }else{
                return
            }
        } else {
            if(!validMove(positionX, positionY)){
                return
            }

            if(player.hasBombPressed){
                makeBombMove(v, positionX, positionY)
            }else{
                makeMove(v, positionX, positionY)
            }
        }

        setPlayerTurnPlaying()
        cleanValidPlays(v)
        calculateValidPlays(v)
        updateScore(v)

        if(checkIfGameEnded()){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        setButtonsForNextRound(v)
    }

    private fun makeSwitchMove(v: View) {
        var player = getCurrentPlayer()
        var playerNP = getNotCurrentPlayer()

        for(switchPosition in switchPositions){
            var peace: ImageButton = findViewById(getIdByPositions(v, switchPosition.first, switchPosition.second))

            if(board[switchPosition.first][switchPosition.second] == player.char)
            {
                board[switchPosition.first][switchPosition.second] = playerNP.char
                peace.setImageResource(getNormalCell(playerNP.char))
            }else
            {
                board[switchPosition.first][switchPosition.second] = player.char
                peace.setImageResource(getNormalCell(player.char))
            }
        }

        player.hasSwitch = false
        player.hasSwitchPressed = false
        setButtonBackgroundResourceById(v, "switchButton", R.drawable.normal_button)
    }

    fun onBomb(v: View) {
        var player = getCurrentPlayer()

        if(player.hasBomb){
            if(player.hasBombPressed){
                setButtonBackgroundResourceById(v, "bombButton", R.drawable.normal_button)
                player.hasBombPressed = false
            }else{
                setButtonBackgroundResourceById(v, "bombButton", R.drawable.pressed_button)
                player.hasBombPressed = true
                player.hasSwitchPressed = false
            }
        }

        setButtonBackgroundResourceById(v, "switchButton", R.drawable.normal_button)
        cleanSwitchPositions(v)
    }

    fun onSwitch(v: View) {
        var player = getCurrentPlayer()

        if(player.hasSwitch){
            if(player.hasSwitchPressed){
                setButtonBackgroundResourceById(v, "switchButton", R.drawable.normal_button)
                player.hasSwitchPressed = false

                cleanSwitchPositions(v)
            }else{
                setButtonBackgroundResourceById(v, "switchButton", R.drawable.pressed_button)
                player.hasSwitchPressed = true
            }
        }

        setButtonBackgroundResourceById(v, "bombButton", R.drawable.normal_button)
    }

    private fun cleanSwitchPositions(v: View) {
        var player = getCurrentPlayer()
        var playerNP = getNotCurrentPlayer()

        for(switchPosition in switchPositions){
            var peace: ImageButton = findViewById(getIdByPositions(v, switchPosition.first, switchPosition.second))

            if(board[switchPosition.first][switchPosition.second] == player.char)
            {
                peace.setImageResource(getNormalCell(player.char))
            }else
            {
                peace.setImageResource(getNormalCell(playerNP.char))
            }
        }

        switchPositions.clear()
    }
    //endregion

    //region Gets
    private fun getIdByPositions(v: View, positionX: Int, positionY: Int): Int {
        return v.resources.getIdentifier("Pos$positionX$positionY", "id", packageName)
    }

    private fun getCurrentPlayer(): Player {
        if(player1.turnPlaying) return player1

        return player2
    }

    private fun getNotCurrentPlayer(): Player {
        if(player1.turnPlaying){
            return player2
        }

        return player1
    }

    private fun getButtonById(v: View, buttonId: String): Button? {
        var buttonId = v.resources.getIdentifier(buttonId, "id", packageName)

        return findViewById(buttonId)
    }

    private fun getNumberOfPeaces(char: Char): Int {
        var numberOfPeaces = 0

        for(x in 0..7)
        {
            for(y in 0..7)
            {
                if(board[x][y] == char)
                {
                    numberOfPeaces++
                }
            }
        }

        return numberOfPeaces
    }

    private fun getPossiblePlay(positionX: Int, positionY: Int): ArrayList<String> {
        var peacesToFlip = ArrayList<String>()

        for(possiblePlay in possiblePlays) {
            if (possiblePlay.Pos == (positionX.toString() + positionY.toString())) {
                peacesToFlip.addAll(possiblePlay.arrayToFlip)
            }
        }

        return peacesToFlip
    }
    
    private fun getPressedCell(playerChar: Char): Int {
        if(playerChar == 'W'){
            return R.drawable.white_pressed_peace
        }

        return R.drawable.black_pressed_peace
    }

    private fun getNormalCell(playerChar: Char): Int {
        if(playerChar == 'W'){
            return R.drawable.white_peace
        }

        return R.drawable.black_peace
    }
    //endregion

    //region Sets
    private fun setUI() {
        player1.turnPlaying = true

        for(x in 0..7)
        {
            for(y in 0..7)
            {
                board[x][y] = ' '
            }
        }

        setPeace(3, 4, R.id.Pos34, R.drawable.black_peace, 'B')
        setPeace(4, 3, R.id.Pos43, R.drawable.black_peace, 'B')
        setPeace(4, 4, R.id.Pos33, R.drawable.white_peace, 'W')
        setPeace(3, 3, R.id.Pos44, R.drawable.white_peace, 'W')

        var arrayOfPeacesToFlip23 = ArrayList<String>()
        arrayOfPeacesToFlip23.add("33")
        setPossiblePlay(2, 3, R.id.Pos23, arrayOfPeacesToFlip23)

        var arrayOfPeacesToFlip32 = ArrayList<String>()
        arrayOfPeacesToFlip32.add("33")
        setPossiblePlay(3, 2, R.id.Pos32, arrayOfPeacesToFlip32)

        var arrayOfPeacesToFlip45 = ArrayList<String>()
        arrayOfPeacesToFlip45.add("44")
        setPossiblePlay(4, 5, R.id.Pos45, arrayOfPeacesToFlip45)

        var arrayOfPeacesToFlip54 = ArrayList<String>()
        arrayOfPeacesToFlip54.add("44")
        setPossiblePlay(5, 4, R.id.Pos54,arrayOfPeacesToFlip54)
    }

    private fun setPeaceByView(v: View, positionX: Int, positionY: Int, peaceImageId: Int, peaceChar: Char){
        var id = getIdByPositions(v, positionX, positionY)
        setPeace(positionX, positionY, id, peaceImageId, peaceChar)
    }

    private fun setPeace(positionX: Int, positionY: Int, id: Int, peaceImageId: Int, peaceChar: Char){
        var peace: ImageButton = findViewById(id)
        peace.setImageResource(peaceImageId)
        board[positionX][positionY] = peaceChar
    }

    private fun setPossiblePlay(positionX: Int, positionY: Int, peaceImageId: Int, arrayOfPeacesToFlip: ArrayList<String>){
        setPeace(positionX, positionY, peaceImageId, R.drawable.possible_play, '*')

        var possiblePlay = PossiblePlay()
        possiblePlay.Pos = positionX.toString() + positionY.toString()
        possiblePlay.arrayToFlip = arrayOfPeacesToFlip
        possiblePlays.add(possiblePlay)
    }

    private fun setButtonsForNextRound(v: View) {
        var bombButton = getButtonById(v, "bombButton")
        var switchButton = getButtonById(v, "switchButton")

        if((player1.turnPlaying && !player1.hasBomb) || (player2.turnPlaying && !player2.hasBomb))
        {
            bombButton!!.isEnabled = false
        }

        if((player1.turnPlaying && player1.hasBomb) || (player2.turnPlaying && player2.hasBomb))
        {
            bombButton!!.isEnabled = true
        }

        if((player1.turnPlaying && !player1.hasSwitch) || (player2.turnPlaying && !player2.hasSwitch) )
        {
            switchButton!!.isEnabled = false
        }

        if((player1.turnPlaying && player1.hasSwitch) || (player2.turnPlaying && player2.hasSwitch))
        {
            switchButton!!.isEnabled = true
        }
    }

    private fun setButtonBackgroundResourceById(v: View, buttonId: String, drawableId: Int) {
        var buttonId = v.resources.getIdentifier(buttonId, "id", packageName)
        var button = findViewById<Button>(buttonId)
        button.setBackgroundResource(drawableId)
    }

    private fun setPlayerTurnPlaying() {
        if(player1.turnPlaying) {
            player1.turnPlaying = false
            player2.turnPlaying = true
        } else {
            player1.turnPlaying = true
            player2.turnPlaying = false
        }
    }

    private fun setSwitchMove(v: View, x: Int, y: Int) {
        if(board[x][y] == ' ' || board[x][y] == '*'){
            return
        }

        setPeaceForSwitch(v, x, y)
    }

    private fun setPeaceForSwitch(v: View, x: Int, y: Int) {
        var player = getCurrentPlayer()
        var playerNP = getNotCurrentPlayer()

        var isPositionDuplicate = checkIfPositionIsDuplicate(x, y)

        if(isPositionDuplicate){
            switchPositions.remove(Pair(x, y))
            var peace: ImageButton = findViewById(getIdByPositions(v, x, y))

            if((board[x][y] == player.char)){
                peace.setImageResource(getNormalCell(player.char))
            }

            if(board[x][y] == playerNP.char){
                peace.setImageResource(getNormalCell(playerNP.char))
            }

            return
        }

        var myPeacesCounter = 0
        var opponentPeacesCounter = 0

        for(switchPosition in switchPositions){
            if(board[switchPosition.first][switchPosition.second] == player.char){
                myPeacesCounter++
            }else{
                opponentPeacesCounter++
            }
        }

        if((board[x][y] == player.char) && myPeacesCounter < 1){
            switchPositions.add(Pair(x, y))
            var peace: ImageButton = findViewById(getIdByPositions(v, x, y))
            peace.setImageResource(getPressedCell(player.char))
        }

        if(board[x][y] == playerNP.char && opponentPeacesCounter < 2){
            switchPositions.add(Pair(x, y))
            var peace: ImageButton = findViewById(getIdByPositions(v, x, y))
            peace.setImageResource(getPressedCell(playerNP.char))
        }
    }
    //endregion

    //region Moves
    private fun makeMove(v: View, positionX: Int, positionY: Int){
        var peaceImageId = if(player1.turnPlaying) R.drawable.black_peace else R.drawable.white_peace
        var playerChar = if(player1.turnPlaying) player1.char else player2.char

        move(v, positionX, positionY, peaceImageId)
        setPeaceByView(v, positionX, positionY, peaceImageId, playerChar)
    }

    private fun move(v: View, positionX: Int, positionY: Int, peaceImageId: Int){
        var peacesToFlip = getPossiblePlay(positionX, positionY)
        var playerChar = getCurrentPlayer().char

        for(peaceToFlip in peacesToFlip){
            val x = peaceToFlip.take(1).toInt()
            val y = peaceToFlip.takeLast(1).toInt()

            setPeaceByView(v, x, y, peaceImageId, playerChar)
        }
    }

    private fun makeBombMove(v: View, x: Int, y: Int) {
        var player = getCurrentPlayer()

        if(!player.hasBombPressed){
            return
        }

        val bombPositions = ArrayList<Pair<Int, Int>>()
        bombPositions.add(Pair(x, y))
        bombPositions.add(Pair(x - 1, y))
        bombPositions.add(Pair(x, y + 1))
        bombPositions.add(Pair(x + 1, y))
        bombPositions.add(Pair(x, y - 1))
        bombPositions.add(Pair(x - 1, y - 1))
        bombPositions.add(Pair(x - 1, y + 1))
        bombPositions.add(Pair(x + 1, y - 1))
        bombPositions.add(Pair(x + 1, y + 1))

        for(bombPosition in bombPositions){
            if(!checkBoundries(bombPosition.first, bombPosition.second)){
                setPeaceByView(v, bombPosition.first, bombPosition.second, R.drawable.cell, ' ')
            }
        }

        player.hasBomb = false
        player.hasBombPressed = false
        setButtonBackgroundResourceById(v, "bombButton", R.drawable.normal_button)
    }

    private fun validMove(positionX: Int, positionY: Int): Boolean {
        if(board[positionX][positionY] != '*'){
            return false
        }

        return true
    }
    //endregion

    private fun checkIfPositionIsDuplicate(x: Int, y: Int): Boolean {
        for(switchPosition in switchPositions)
        {
            if(x == switchPosition.first && y == switchPosition.second)
            {
                return true
            }
        }

        return false
    }

    private fun checkIfGameEnded(): Boolean {
        for(x in 0..7)
        {
            for(y in 0..7)
            {
                if(board[x][y] == ' ')
                {
                    return false
                }

                if(board[x][y] == '*')
                {
                    return false
                }
            }
        }

        return true
    }

    private fun updateScore(v: View) {
        updateScoreByPlayer(v, "scorePlayer1", 'B')
        updateScoreByPlayer(v, "scorePlayer2", 'W')
    }

    private fun updateScoreByPlayer(v: View, textViewId: String, playerChar: Char) {
        var id = v.resources.getIdentifier(textViewId, "id", packageName)
        var score :TextView = findViewById(id)

        score.text = getNumberOfPeaces(playerChar).toString()
    }

    private fun cleanValidPlays(v: View){
        for(x in 0..7)
        {
            for(y in 0..7)
            {
                if(board[x][y] == '*')
                {
                    setPeaceByView(v, x, y, R.drawable.cell, ' ')
                }
            }
        }

        possiblePlays.clear()
    }

    private fun calculateValidPlays(v: View){
        for(x in 0..7)
        {
            for(y in 0..7)
            {
                if(board[x][y] == ' ')
                {
                    var possiblePlay = PossiblePlay()
                    possiblePlay.Pos = (x.toString() + y.toString())

                    var validTop = calculateTopValidPlay(x, y, possiblePlay)
                    var validRight = calculateRightValidPlay(x, y, possiblePlay)
                    var validBottom = calculateBottomValidPlay(x, y, possiblePlay)
                    var validLeft = calculateLeftValidPlay(x, y, possiblePlay)

                    var validTopRight = calculateTopRightValidPlay(x, y, possiblePlay)
                    var validBottomRight = calculateBottomRightValidPlay(x, y, possiblePlay)
                    var validBottomLeft = calculateBottomLeftValidPlay(x, y, possiblePlay)
                    var validTopLeft = calculateTopLeftValidPlay(x, y, possiblePlay)

                    if(validTop || validRight || validBottom || validLeft || validTopRight || validBottomRight || validBottomLeft || validTopLeft)
                    {
                        setPeace(x, y, getIdByPositions(v, x, y), R.drawable.possible_play,'*')
                    }
                }
            }
        }
    }

    private fun calculateTopValidPlay(x: Int, y: Int, possiblePlay: PossiblePlay): Boolean {
        if(checkBoundries(x-1, y) || checkBoundries(x-2, y)){
            return false
        }

        if(board[x-1][y] != getNotCurrentPlayer().char){
            return false
        }

        var auxArrayToFlip = ArrayList<String>()
        auxArrayToFlip.add((x-1).toString() + (y).toString())

        for(i in x-2 downTo 0)
        {
            if(checkBoundries(i, y) || board[i][y] == ' ' || board[i][y] == '*') {
                return false
            }

            if(board[i][y] == getCurrentPlayer().char){
                possiblePlay.arrayToFlip.addAll(auxArrayToFlip)
                possiblePlays.add(possiblePlay)

                return true
            }

            auxArrayToFlip.add((i).toString() + (y).toString())
        }

        return false
    }

    private fun calculateRightValidPlay(x: Int, y: Int, possiblePlay: PossiblePlay): Boolean {
        if(checkBoundries(x, y + 1) || checkBoundries(x, y + 2)){
            return false
        }

        if(board[x][y+1] != getNotCurrentPlayer().char){
            return false
        }

        var auxArrayToFlip = ArrayList<String>()
        auxArrayToFlip.add((x).toString() + (y + 1).toString())

        for(i in y+2 until 7)
        {
            if(checkBoundries(x, i) || board[x][i] == ' '|| board[x][i] == '*') {
                return false
            }

            if(board[x][i] == getCurrentPlayer().char){
                possiblePlay.arrayToFlip.addAll(auxArrayToFlip)
                possiblePlays.add(possiblePlay)

                return true
            }

            auxArrayToFlip.add((x).toString() + (i).toString())
        }

        return false
    }

    private fun calculateBottomValidPlay(x: Int, y: Int, possiblePlay: PossiblePlay): Boolean {
        if(checkBoundries(x + 1, y) || checkBoundries(x + 2, y)){
            return false
        }

        if(board[x+1][y] != getNotCurrentPlayer().char){
            return false
        }

        var auxArrayToFlip = ArrayList<String>()
        auxArrayToFlip.add((x+1).toString() + (y).toString())

        for(i in x+2 until 7)
        {
            if(checkBoundries(i, y) || board[i][y] == ' ' || board[i][y] == '*') {
                return false
            }

            if(board[i][y] == getCurrentPlayer().char){
                possiblePlay.arrayToFlip.addAll(auxArrayToFlip)
                possiblePlays.add(possiblePlay)

                return true
            }

            possiblePlay.arrayToFlip.add((i).toString() + (y).toString())
        }

        return false
    }

    private fun calculateLeftValidPlay(x: Int, y: Int, possiblePlay: PossiblePlay): Boolean {
        if(checkBoundries(x, y-1) || checkBoundries(x, y-2)){
            return false
        }

        if(board[x][y-1] != getNotCurrentPlayer().char){
            return false
        }

        var auxArrayToFlip = ArrayList<String>()
        auxArrayToFlip.add((x).toString() + (y - 1).toString())

        for(i in y-2 downTo 0)
        {
            if(checkBoundries(x, i) || board[x][i] == ' ' || board[x][i] == '*') {
                return false
            }

            if(board[x][i] == getCurrentPlayer().char){
                possiblePlay.arrayToFlip.addAll(auxArrayToFlip)
                possiblePlays.add(possiblePlay)

                return true
            }

            auxArrayToFlip.add((x).toString() + (i).toString())
        }

        return false
    }

    private fun calculateBottomRightValidPlay(x: Int, y: Int, possiblePlay: PossiblePlay): Boolean {
        if(checkBoundries(x-1, y-1) || checkBoundries(x-2, y-2)){
            return false
        }

        if(board[x-1][y-1] != getNotCurrentPlayer().char){
            return false
        }

        var auxArrayToFlip = ArrayList<String>()
        auxArrayToFlip.add((x-1).toString() + (y - 1).toString())

        var auxY = y - 2

        for(i in x-2 downTo 0)
        {
            if(checkBoundries(i, auxY) || board[i][auxY] == ' ' || board[i][auxY] == '*') {
                return false
            }

            if(board[i][auxY] == getCurrentPlayer().char){
                possiblePlay.arrayToFlip.addAll(auxArrayToFlip)
                possiblePlays.add(possiblePlay)

                return true
            }

            possiblePlay.arrayToFlip.add((i).toString() + (auxY).toString())
            auxY--
        }

        return false
    }

    private fun calculateBottomLeftValidPlay(x: Int, y: Int, possiblePlay: PossiblePlay): Boolean {
        if(checkBoundries(x-1, y+1) || checkBoundries(x-2, y+2)){
            return false
        }

        if(board[x-1][y+1] != getNotCurrentPlayer().char){
            return false
        }

        var auxArrayToFlip = ArrayList<String>()
        auxArrayToFlip.add((x - 1).toString() + (y + 1).toString())
        var auxY = y + 2

        for(i in x-2 downTo 0)
        {
            if(checkBoundries(i, auxY) || board[i][auxY] == ' ' || board[i][auxY] == '*') {
                return false
            }

            if(board[i][auxY] == getCurrentPlayer().char){
                possiblePlay.arrayToFlip.addAll(auxArrayToFlip)
                possiblePlays.add(possiblePlay)

                return true
            }

            possiblePlay.arrayToFlip.add((i).toString() + (auxY).toString())
            auxY++
        }

        return false
    }

    private fun calculateTopRightValidPlay(x: Int, y: Int, possiblePlay: PossiblePlay): Boolean {
        if(checkBoundries(x+1, y-1) || checkBoundries(x+2, y-2)){
            return false
        }

        if(board[x+1][y-1] != getNotCurrentPlayer().char){
            return false
        }

        var auxArrayToFlip = ArrayList<String>()
        auxArrayToFlip.add((x + 1).toString() + (y - 1).toString())
        var auxY = y -2

        for(i in x+2 until 7)
        {
            if(checkBoundries(i, auxY) || board[i][auxY] == ' ' || board[i][auxY] == '*') {
                return false
            }

            if(board[i][auxY] == getCurrentPlayer().char){
                possiblePlay.arrayToFlip.addAll(auxArrayToFlip)
                possiblePlays.add(possiblePlay)

                return true
            }

            possiblePlay.arrayToFlip.add((i).toString() + (auxY).toString())
            auxY--
        }

        return false
    }

    private fun calculateTopLeftValidPlay(x: Int, y: Int, possiblePlay: PossiblePlay): Boolean {
        if(checkBoundries(x+1, y+1) || checkBoundries(x+2, y+2)){
            return false
        }

        if(board[x+1][y+1] != getNotCurrentPlayer().char){
            return false
        }

        var auxArrayToFlip = ArrayList<String>()
        auxArrayToFlip.add((x + 1).toString() + (y + 1).toString())

        var auxY = y +2

        for(i in x+2 until 7)
        {
            if(checkBoundries(i, auxY) || board[i][auxY] == ' ' || board[i][auxY] == '*') {
                return false
            }

            if(board[i][auxY] == getCurrentPlayer().char){
                possiblePlay.arrayToFlip.addAll(auxArrayToFlip)
                possiblePlays.add(possiblePlay)

                return true
            }

            possiblePlay.arrayToFlip.add((i).toString() + (auxY).toString())
            auxY++
        }

        return false
    }

    private fun checkBoundries(x: Int, y: Int): Boolean {
        if((x < 0) || (y < 0) || (x > 7) || (y > 7)){
            return true
        }

        return false
    }
}