package com.alamkanak.weekview.loaders

import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants.MILLIS_PER_WEEK
import java.util.*

/**
 * This class is responsible for loading [WeekViewEvent]s into [WeekView]. It can handle
 * both concrete [WeekViewEvent] objects and [WeekViewDisplayable] objects. The latter is
 * an interface that can be implemented in one's actual data class and handles the conversion to a
 * [WeekViewEvent].
 */
class WeekLoader<T> internal constructor(private var onWeekChangeListener: WeekViewLoader.PeriodChangeListener<T>) : WeekViewLoader<T> {
	override fun toWeekViewPeriodIndex(instance: DateTime) = instance.millis / MILLIS_PER_WEEK + 1

	override fun onLoad(periodIndex: Int): List<WeekViewEvent<T>> {
		val millis = periodIndex * MILLIS_PER_WEEK.toLong()

		val startDate = DateTime().withMillis(millis).dayOfWeek().withMinimumValue()
		val endDate = DateTime().withMillis(millis).dayOfWeek().withMaximumValue()

		val displayableItems = onWeekChangeListener.onPeriodChange(startDate, endDate)

		val events = ArrayList<WeekViewEvent<T>>()
		for (displayableItem in displayableItems) {
			events.add(displayableItem.toWeekViewEvent())
		}

		return events
	}
}
