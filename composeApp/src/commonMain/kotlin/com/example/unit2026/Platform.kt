package com.example.unit2026

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform