package it.danieleverducci.lunatracker.adapters

import android.content.Context
import android.icu.util.LocaleData
import android.icu.util.ULocale
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.danieleverducci.lunatracker.entities.LunaEvent
import it.danieleverducci.lunatracker.R
import java.util.Date
import java.util.Locale

class LunaEventRecyclerAdapter: RecyclerView.Adapter<LunaEventRecyclerAdapter.LunaEventVH> {
    private val context: Context
    val items = ArrayList<LunaEvent>()
    val measurement_unit_liquid_base: String
    val measurement_unit_weight_base: String
    val measurement_unit_weight_tiny: String

    constructor(context: Context) {
        this.context = context
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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LunaEventVH {

        val view = LayoutInflater.from(context).inflate(R.layout.row_luna_event, parent, false)
        return LunaEventVH(view)
    }

    override fun onBindViewHolder(
        holder: LunaEventVH,
        position: Int
    ) {
        val item = items.get(position)
        holder.type.text = item.getTypeEmoji(context)
        holder.description.text = item.getTypeDescription(context)
        holder.time.text = formatTimeAgo(context, item.time)
        val qtyText = if ((item.quantity ?: 0) > 0) {
            item.quantity.toString() + " " + when (item.type) {
                LunaEvent.TYPE_BABY_BOTTLE -> measurement_unit_liquid_base
                LunaEvent.TYPE_WEIGHT -> measurement_unit_weight_base
                LunaEvent.TYPE_MEDICINE -> measurement_unit_weight_tiny
                else -> ""
            }
        } else {
            ""
        }
        holder.quantity.text = qtyText
    }

    override fun getItemCount(): Int {
        return items.size
    }

    /**
     * Formats the provided unix timestamp in a string like "3 hours, 26 minutes ago)
     */
    fun formatTimeAgo(context: Context, unixTime: Long): String {
        val secondsDiff = (System.currentTimeMillis() / 1000) - unixTime
        val minutesDiff = secondsDiff / 60

        if (minutesDiff < 1)
            return context.getString(R.string.now)

        val hoursAgo = (secondsDiff / (60 * 60)).toInt()
        val minutesAgo = (minutesDiff % 60).toInt()

        if (hoursAgo > 24)
            return DateFormat.getDateFormat(context).format(Date(unixTime*1000)) + "\n" +
                DateFormat.getTimeFormat(context).format(Date(unixTime*1000))

        var formattedTime = StringBuilder()
        if (hoursAgo > 0) {
            formattedTime.append(hoursAgo).append(" ")
            if (hoursAgo.toInt() == 1)
                formattedTime.append(context.getString(R.string.hour_ago))
            else
                formattedTime.append(context.getString(R.string.hours_ago))
        }
        if (minutesAgo > 0) {
            if (formattedTime.isNotEmpty())
                formattedTime.append(", ")
            formattedTime.append(minutesAgo).append(" ")
            if (minutesAgo.toInt() == 1)
                formattedTime.append(context.getString(R.string.minute_ago))
            else
                formattedTime.append(context.getString(R.string.minutes_ago))
        }
        return formattedTime.toString()
    }

    class LunaEventVH: RecyclerView.ViewHolder {
        val type: TextView
        val description: TextView
        val quantity: TextView
        val time: TextView

        constructor(v: View) : super(v) {
            type = v.findViewById<TextView>(R.id.type)
            description = v.findViewById<TextView>(R.id.description)
            quantity = v.findViewById<TextView>(R.id.quantity)
            time = v.findViewById<TextView>(R.id.time)
        }
    }
}