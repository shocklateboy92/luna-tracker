package utils

import android.content.Context
import android.text.format.DateFormat
import it.danieleverducci.lunatracker.R
import java.util.Date

class DateUtils {
    companion object {

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
    }
}