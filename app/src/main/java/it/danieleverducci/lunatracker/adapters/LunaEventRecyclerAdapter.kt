package it.danieleverducci.lunatracker.adapters

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.danieleverducci.lunatracker.entities.LunaEvent
import it.danieleverducci.lunatracker.entities.LunaEventType
import it.danieleverducci.lunatracker.R
import java.text.DateFormat
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
                LunaEventType.BREASTFEEDING_LEFT_NIPPLE -> R.string.event_breastfeeding_left_desc
                LunaEventType.BREASTFEEDING_BOTH_NIPPLE -> R.string.event_breastfeeding_both_desc
                LunaEventType.BREASTFEEDING_RIGHT_NIPPLE -> R.string.event_breastfeeding_right_desc
                LunaEventType.DIAPERCHANGE_POO -> R.string.event_diaperchange_poo_desc
                LunaEventType.DIAPERCHANGE_PEE -> R.string.event_diaperchange_pee_desc
                else -> R.string.event_unknown_desc
            }
        )
        holder.quantity.text = if ((item.quantity ?: 0) > 0) item.quantity.toString() else ""
        holder.time.text = DateUtils.getRelativeTimeSpanString(item.time * 1000)
    }

    override fun getItemCount(): Int {
        return items.size
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