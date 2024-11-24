package it.danieleverducci.lunatracker.adapters

import android.content.Context
import android.icu.util.LocaleData
import android.icu.util.ULocale
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.danieleverducci.lunatracker.entities.LunaEvent
import it.danieleverducci.lunatracker.R
import utils.DateUtils

class LunaEventRecyclerAdapter: RecyclerView.Adapter<LunaEventRecyclerAdapter.LunaEventVH> {
    private val context: Context
    val items = ArrayList<LunaEvent>()
    val measurement_unit_liquid_base: String
    val measurement_unit_weight_base: String
    val measurement_unit_weight_tiny: String
    var onItemClickListener: OnItemClickListener? = null

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
        // Colors
        holder.root.setBackgroundResource(
            if (position % 2 == 0) R.color.list_background_even else R.color.list_background_odd
        )
        // Contents
        holder.type.text = item.getTypeEmoji(context)
        holder.description.text = when(item.type) {
            LunaEvent.TYPE_MEDICINE -> item.notes
            LunaEvent.TYPE_NOTE -> item.notes
            LunaEvent.TYPE_CUSTOM -> item.notes
            else -> item.getTypeDescription(context)
        }
        holder.time.text = DateUtils.formatTimeAgo(context, item.time)
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
        // Listeners
        if (onItemClickListener != null) {
            holder.root.setOnClickListener({
                onItemClickListener?.onItemClick(item)
            })
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class LunaEventVH: RecyclerView.ViewHolder {
        val root: View
        val type: TextView
        val description: TextView
        val quantity: TextView
        val time: TextView

        constructor(v: View) : super(v) {
            root = v
            type = v.findViewById<TextView>(R.id.type)
            description = v.findViewById<TextView>(R.id.description)
            quantity = v.findViewById<TextView>(R.id.quantity)
            time = v.findViewById<TextView>(R.id.time)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(event: LunaEvent)
    }
}