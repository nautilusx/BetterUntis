package com.alamkanak.weekview.loaders

import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import org.joda.time.DateTime

interface WeekViewLoader<T> {

	/**
	 * Convert a date into a double that will be used to reference when you're loading data.
	 *
	 * All periods that have the same integer part, define one period. Dates that are later in time
	 * should have a greater return value.
	 *
	 * @param instance the date
	 * @return The period index in which the date falls (floating point number).
	 */
	fun toWeekViewPeriodIndex(instance: DateTime): Long

	/**
	 * Load the events within the period
	 * @param periodIndex the period to load
	 * @return A list with the events of this period
	 */
	fun onLoad(periodIndex: Int): List<WeekViewEvent<T>>

	interface PeriodChangeListener<T> {

		/**
		 * Called when the period displayed in the [WeekView] changes.
		 * @param startDate A [DateTime] representing the start date of the period
		 * @param endDate A [DateTime] representing the end date of the period
		 * @return The list of [WeekViewDisplayable] of the provided period
		 */
		fun onPeriodChange(startDate: DateTime, endDate: DateTime): List<WeekViewDisplayable<T>>
	}
}
