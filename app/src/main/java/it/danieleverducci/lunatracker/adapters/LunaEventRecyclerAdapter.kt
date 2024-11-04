package it.danieleverducci.lunatracker.adapters

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.danieleverducci.lunatracker.entities.LunaEvent
import it.danieleverducci.lunatracker.entities.LunaEventType
import it.danieleverducci.lunatracker.R
import java.util.Date

class LunaEventRecyclerAdapter: RecyclerView.Adapter<LunaEventRecyclerAdapter.LunaEventVH> {
    private val context: Context
    val items = ArrayList<LunaEvent>()

    constructor(context: Context) {
        this.context = context
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
        holder.type.text = context.getString(
            when (item.type) {
                LunaEventType.BABY_BOTTLE -> R.string.event_bottle_type
                LunaEventType.WEIGHT -> R.string.event_scale_type
                LunaEventType.BREASTFEEDING_LEFT_NIPPLE -> R.string.event_breastfeeding_left_type
                LunaEventType.BREASTFEEDING_BOTH_NIPPLE -> R.string.event_breastfeeding_both_type
                LunaEventType.BREASTFEEDING_RIGHT_NIPPLE -> R.string.event_breastfeeding_right_type
                LunaEventType.DIAPERCHANGE_POO -> R.string.event_diaperchange_poo_type
                LunaEventType.DIAPERCHANGE_PEE -> R.string.event_diaperchange_pee_type
                else -> R.string.event_unknown_type
            }
        )
        holder.description.text = context.getString(
            when (item.type) {
                LunaEventType.BABY_BOTTLE -> R.string.event_bottle_desc
                LunaEventType.WEIGHT -> R.string.event_scale_desc
                LunaEventType.BREASTFEEDING_LEFT_NIPPLE -> R.string.event_breastfeeding_left_desc
                LunaEventType.BREASTFEEDING_BOTH_NIPPLE -> R.string.event_breastfeeding_both_desc
                LunaEventType.BREASTFEEDING_RIGHT_NIPPLE -> R.string.event_breastfeeding_right_desc
                LunaEventType.DIAPERCHANGE_POO -> R.string.event_diaperchange_poo_desc
                LunaEventType.DIAPERCHANGE_PEE -> R.string.event_diaperchange_pee_desc
                else -> R.string.event_unknown_desc
            }
        )
        holder.quantity.text = if ((item.quantity ?: 0) > 0) item.quantity.toString() else ""
        holder.time.text = formatTimeAgo(context, item.time)
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
        if (formattedTime.isNotEmpty())
            formattedTime.append(", ")
        if (minutesAgo > 0) {
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