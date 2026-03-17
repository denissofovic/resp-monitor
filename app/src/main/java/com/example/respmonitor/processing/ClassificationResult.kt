package com.example.respmonitor.processing

data class ClassificationResult(
    val label: String,
    val confidence: Float,
    val validScore: Float,
    val invalidScore: Float
)