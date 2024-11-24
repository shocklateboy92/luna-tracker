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
    }

    fun formatEventQuantity(item: LunaEvent): String {
        return if ((item.quantity ?: 0) > 0) {
            numberFormat.format(item.quantity) + " " + when (item.type) {
                LunaEvent.TYPE_BABY_BOTTLE -> measurement_unit_liquid_base
                LunaEvent.TYPE_WEIGHT -> measurement_unit_weight_base
                LunaEvent.TYPE_MEDICINE -> measurement_unit_weight_tiny
                else -> ""
            }
        } else {
            ""
        }
    }
}