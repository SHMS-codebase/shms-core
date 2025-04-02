package com.healthcaremngnt.util;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

import com.healthcaremngnt.constants.MessageConstants;

public class DateFormatter {

	public static String formatWithOrdinalSuffix(LocalDateTime date) {
		if (date == null) {
			throw new IllegalArgumentException(MessageConstants.DATE_NULL);
		}

		int day = date.getDayOfMonth();
		String daySuffix = determineDaySuffix(day);

		return String.format("%s %d%s, %d", date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH), day,
				daySuffix, date.getYear());
	}

	private static String determineDaySuffix(int day) {
		if (day >= 11 && day <= 13) {
			return "th";
		}
		return switch (day % 10) {
		case 1 -> "st";
		case 2 -> "nd";
		case 3 -> "rd";
		default -> "th";
		};
	}

}