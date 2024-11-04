package it.danieleverducci.lunatracker.entities

import org.json.JSONObject
import java.util.Date

class LunaEvent(
    val type: LunaEventType,
    val quantity: Int? = null,
){
    var time: Long  // In unix time (seconds since 1970)

    init {
        time = System.currentTimeMillis() / 1000
    }

    override fun toString(): String {
        return "${type.toString()} qty: $quantity time: ${Date(time * 1000)}"
    }

    fun toJson(): JSONObject {
        val jo = JSONObject()
        val type = when (type) {
            LunaEventType.BABY_BOTTLE -> "BABY_BOTTLE"
            LunaEventType.WEIGHT -> "SCALE"
            LunaEventType.BREASTFEEDING_LEFT_NIPPLE -> "BREASTFEEDING_LEFT_NIPPLE"
            LunaEventType.BREASTFEEDING_BOTH_NIPPLE -> "BREASTFEEDING_BOTH_NIPPLE"
            LunaEventType.BREASTFEEDING_RIGHT_NIPPLE -> "BREASTFEEDING_RIGHT_NIPPLE"
            LunaEventType.DIAPERCHANGE_POO -> "DIAPERCHANGE_POO"
            LunaEventType.DIAPERCHANGE_PEE -> "DIAPERCHANGE_PEE"
            else -> "UNKNOWN"
        }
        jo.put("type", type)
        jo.put("quantity", quantity)
        jo.put("time", time)
        return jo
    }

    companion object {
        fun fromJson(j: JSONObject): LunaEvent {
            val type = when (j.getString("type")) {
                "BABY_BOTTLE" -> LunaEventType.BABY_BOTTLE
                "SCALE" -> LunaEventType.WEIGHT
                "BREASTFEEDING_LEFT_NIPPLE" -> LunaEventType.BREASTFEEDING_LEFT_NIPPLE
                "BREASTFEEDING_BOTH_NIPPLE" -> LunaEventType.BREASTFEEDING_BOTH_NIPPLE
                "BREASTFEEDING_RIGHT_NIPPLE" -> LunaEventType.BREASTFEEDING_RIGHT_NIPPLE
                "DIAPERCHANGE_POO" -> LunaEventType.DIAPERCHANGE_POO
                "DIAPERCHANGE_PEE" -> LunaEventType.DIAPERCHANGE_PEE
                else -> LunaEventType.UNKNOWN
            }
            val quantity = j.optInt("quantity")
            val time = j.getLong("time")
            val evt = LunaEvent(type, quantity)
            evt.time = time
            return  evt
        }
    }
 }

enum class LunaEventType {
    BABY_BOTTLE,
    WEIGHT,
    BREASTFEEDING_LEFT_NIPPLE,
    BREASTFEEDING_BOTH_NIPPLE,
    BREASTFEEDING_RIGHT_NIPPLE,
    DIAPERCHANGE_POO,
    DIAPERCHANGE_PEE,
    UNKNOWN
}