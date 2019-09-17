package com.alamkanak.weekview.drawers

import android.graphics.Canvas
import com.alamkanak.weekview.DrawingContext

import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.config.WeekViewConfig
import com.alamkanak.weekview.config.WeekViewDrawConfig

class TimeColumnDrawer(private val config: WeekViewConfig) : BaseDrawer {
	private val drawConfig: WeekViewDrawConfig = config.drawConfig

	override fun draw(drawingContext: DrawingContext, canvas: Canvas) {
		var top = drawConfig.headerHeight
		val bottom = WeekView.viewHeight

		val bottomTimeOffset = 8f

		// Draw the background color for the header column.
		canvas.drawRect(0f, top, drawConfig.timeColumnWidth, bottom.toFloat(), drawConfig.timeColumnBackgroundPaint)

		canvas.restore()
		canvas.save()

		canvas.clipRect(0f, top, drawConfig.timeColumnWidth, bottom.toFloat())

		// The original header height
		val headerHeight = top

		for (i in config.hourLines.indices) {
			val headerBottomMargin = drawConfig.headerMarginBottom + config.headerRowBottomLineWidth
			val hourTop = config.hourHeight * (config.hourLines[i] - config.startTime) / 60.0f
			top = headerHeight + drawConfig.currentOrigin.y + hourTop + headerBottomMargin

			val lastHourTop = if (i > 0) config.hourHeight * (config.hourLines[i - 1] - config.startTime) / 60.0f else 0.0f

			// Draw the text if its y position is not outside of the visible area. The pivot point
			// of the text is the point at the bottom-right corner.
			val time = drawConfig.dateTimeInterpreter.interpretTime(config.hourLines[i])

			if (top - (hourTop - lastHourTop) < bottom) {
				if (i % 2 == 0)
					canvas.drawText(time, config.timeColumnPadding.toFloat(), top + drawConfig.timeTextHeight, drawConfig.timeTextTopPaint)
				else
					canvas.drawText(time, config.timeColumnPadding + drawConfig.timeTextWidth, top - bottomTimeOffset, drawConfig.timeTextBottomPaint)


				if (i % 2 == 1) {
					canvas.drawText((i / 2 + 1).toString(), config.timeColumnPadding + drawConfig.timeTextWidth / 2, top - (hourTop - lastHourTop) / 2 + bottomTimeOffset * 1.5f, drawConfig.timeCaptionPaint)
				}
			}
		}

		// Draw the vertical time column separator
		if (config.showTimeColumnSeparator) {
			val lineX = drawConfig.timeColumnWidth - config.timeColumnSeparatorStrokeWidth
			canvas.drawLine(lineX, drawConfig.headerHeight, lineX, bottom.toFloat(), drawConfig.timeColumnSeparatorPaint)
		}

		canvas.restore()
	}
}
