package com.hinalin.mousho.data.model

import java.time.LocalDateTime

data class OverheatEvent(
    val timestamp: LocalDateTime,
    val temperature: Float,
)
