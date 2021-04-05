package com.github.su23.consul.app.leader.elector.core

enum class ElectionState {
    ElectedFirstTime,
    Elected,
    Relegation,
    NotElected,
    Error
}