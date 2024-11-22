package it.danieleverducci.lunatracker.entities

import org.json.JSONObject
import java.util.Date

/**
 * Represents a logged event.
 * It encloses, but doesn't parse entirely, a jsonObject. This was done to
 * allow expandability and backwards compatibility (if a field is added in a
 * release, it is simply ignored by previous ones).
 */
class LunaEvent {

    companion object {
        val TYPE_BABY_BOTTLE = "BABY_BOTTLE"
        val TYPE_WEIGHT = "WEIGHT"
        val TYPE_BREASTFEEDING_LEFT_NIPPLE = "BREASTFEEDING_LEFT_NIPPLE"
        val TYPE_BREASTFEEDING_BOTH_NIPPLE = "BREASTFEEDING_BOTH_NIPPLE"
        val TYPE_BREASTFEEDING_RIGHT_NIPPLE = "BREASTFEEDING_RIGHT_NIPPLE"
        val TYPE_DIAPERCHANGE_POO = "DIAPERCHANGE_POO"
        val TYPE_DIAPERCHANGE_PEE = "DIAPERCHANGE_PEE"
    }

    private val jo: JSONObject

    var time: Long  // In unix time (seconds since 1970)
        get() = jo.getLong("time")
        set(value) {
            jo.put("time", value)
        }
    var type: String
        get(): String = jo.getString("type")
        set(value) {
            jo.put("type", value)
        }
    var quantity: Int
        get() = jo.optInt("quantity")
        set(value) {
            if (value > 0)
                jo.put("quantity", value)
        }

    constructor(jo: JSONObject) {
        this.jo = jo
        // A minimal LunaEvent should have at least time and type
        if (!jo.has("time") || !jo.has("type"))
            throw IllegalArgumentException("JSONObject is not a LunaEvent")
    }

    constructor(type: String) {
        this.jo = JSONObject()
        this.time = System.currentTimeMillis() / 1000
        this.type = type
    }

    constructor(type: String, quantity: Int) {
        this.jo = JSONObject()
        this.time = System.currentTimeMillis() / 1000
        this.type = type
        this.quantity = quantity
    }

    fun toJson(): JSONObject {
        return jo
    }

    override fun toString(): String {
        return "${type} qty: $quantity time: ${Date(time * 1000)}"
    }
}