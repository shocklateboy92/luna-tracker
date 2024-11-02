package it.danieleverducci.lunatracker

import java.util.Date

class LunaEvent(
    val type: LunaEventType,
    val quantity: Int? = null
){
    val time: Long  // In unix time (seconds since 1970)

    init {
        time = System.currentTimeMillis() / 1000
    }
}

enum class LunaEventType {
    BABY_BOTTLE,
    BREASTFEEDING_LEFT_NIPPLE,
    BREASTFEEDING_BOTH_NIPPLE,
    BREASTFEEDING_RIGHT_NIPPLE,
    DIAPERCHANGE_POO,
    DIAPERCHANGE_PEE,
}