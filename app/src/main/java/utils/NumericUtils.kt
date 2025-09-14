package utils

import android.content.Context
import android.icu.util.LocaleData
import android.icu.util.ULocale
import it.danieleverducci.lunatracker.R
import it.danieleverducci.lunatracker.entities.LunaEvent
import java.text.NumberFormat

class NumericUtils (val context: Context) {
    val numberFormat: NumberFormat
    val measurement_unit_liquid_base: String
    val measurement_unit_weight_base: String
    val measurement_unit_weight_tiny: String
    val measurement_unit_temperature_base: String

    init {
        this.numberFormat = NumberFormat.getInstance()
        val measurementSystem = LocaleData.getMeasurementSystem(ULocale.getDefault())
        this.measurement_unit_liquid_base = context.getString(
            if (measurementSystem == LocaleData. MeasurementSystem.SI)
                R.string.measurement_unit_liquid_base_metric
            else
                R.string.measurement_unit_liquid_base_imperial
        )
        this.measurement_unit_weight_base = context.getString(
            if (measurementSystem == LocaleData. MeasurementSystem.SI)
                R.string.measurement_unit_weight_base_metric
            else
                R.string.measurement_unit_weight_base_imperial
        )
        this.measurement_unit_weight_tiny = context.getString(
            if (measurementSystem == LocaleData. MeasurementSystem.SI)
                R.string.measurement_unit_weight_tiny_metric
            else
                R.string.measurement_unit_weight_tiny_imperial
        )
        this.measurement_unit_temperature_base = context.getString(
            if (measurementSystem == LocaleData. MeasurementSystem.SI)
                R.string.measurement_unit_temperature_base_metric
            else
                R.string.measurement_unit_temperature_base_imperial
        )
    }

    fun formatEventQuantity(item: LunaEvent): String {
        val formatted = StringBuilder()
        if ((item.quantity ?: 0) > 0) {
            when (item.type) {
                LunaEvent.TYPE_TEMPERATURE -> {
                    formatted.append((item.quantity / 10.0f).toString())
                    formatted.append(" ")
                    formatted.append(measurement_unit_temperature_base)
                }
                LunaEvent.TYPE_BREASTFEEDING_LEFT_NIPPLE,
                LunaEvent.TYPE_BREASTFEEDING_RIGHT_NIPPLE,
                LunaEvent.TYPE_BREASTFEEDING_BOTH_NIPPLE -> {
                    // Format duration for breastfeeding events (quantity is in seconds)
                    val durationSeconds = item.quantity
                    val minutes = durationSeconds / 60
                    val seconds = durationSeconds % 60

                    if (minutes > 0) {
                        formatted.append("$minutes min")
                        if (seconds > 0) {
                            formatted.append(" $seconds sec")
                        }
                    } else {
                        formatted.append("$seconds sec")
                    }
                }
                else -> {
                    formatted.append(item.quantity)
                    formatted.append(" ")
                    formatted.append(
                        when (item.type) {
                            LunaEvent.TYPE_BABY_BOTTLE -> measurement_unit_liquid_base
                            LunaEvent.TYPE_WEIGHT -> measurement_unit_weight_base
                            LunaEvent.TYPE_MEDICINE -> measurement_unit_weight_tiny
                            else -> ""
                        }
                    )
                }
            }
        }
        return formatted.toString()
    }

    /**
     * Returns a valid quantity range for the event type.
     * @return min, max, normal
     */
    fun getValidEventQuantityRange(lunaEventType: String): Triple<Int, Int, Int>? {
        val measurementSystem = LocaleData.getMeasurementSystem(ULocale.getDefault())
        return when (lunaEventType) {
            LunaEvent.TYPE_TEMPERATURE -> {
                if (measurementSystem == LocaleData. MeasurementSystem.SI)
                    Triple(
                        context.resources.getInteger(R.integer.human_body_temp_min_metric),
                        context.resources.getInteger(R.integer.human_body_temp_max_metric),
                        context.resources.getInteger(R.integer.human_body_temp_normal_metric)
                    )
                else
                    Triple(
                        context.resources.getInteger(R.integer.human_body_temp_min_imperial),
                        context.resources.getInteger(R.integer.human_body_temp_max_imperial),
                        context.resources.getInteger(R.integer.human_body_temp_normal_imperial)
                    )
            }
            else -> null
        }
    }
}