package com.example.appboard

class Player(private val _char: Char) {
    var hasSwitch: Boolean = true
    var char : Char = _char
    var hasBomb = true
    var turnPlaying = false
    var hasBombPressed = false
    var hasSwitchPressed = false
}