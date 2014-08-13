package com.globant.labs.swipper.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;

public class CalendarUtil {

	public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(
			"dd-MMM-yyyy 'at' HH:mm", Locale.ENGLISH);

	public static void deleteEventWithID(Context curActivity, Long id) {

		ContentResolver cr = curActivity.getContentResolver();
		Uri EVENTS_URI = Uri.parse("content://com.android.calendar/events");
		Cursor cursor = cr.query(EVENTS_URI, new String[] { "calendar_id",
				"title", "description", "dtstart", "dtend", "eventLocation",
				"deleted", "_id" }, null, null, null);
		cursor.moveToFirst();
		// fetching calendars name
		String CNames[] = new String[cursor.getCount()];

		// fetching calendars id
		int cont = 0;

		for (int i = 0; i < CNames.length; i++) {
			String titleL = cursor.getString(1);
			long eventId = cursor.getLong(cursor.getColumnIndex("_id"));
			if (id == (eventId)) {
				cr.delete(ContentUris.withAppendedId(EVENTS_URI, eventId),
						null, null);
				break;
			}

			CNames[i] = cursor.getString(1);
			cursor.moveToNext();
		}
	}

	public static long pushAppointmentsToCalender(Activity curActivity,
			String title, String comment, Long eventIDFM, long startDate,
			long endDate, boolean needReminder, boolean needMailService,
			Boolean modify) {

		/***************** Event: note(without alert) *******************/

		String eventUriString = "content://com.android.calendar/events";
		ContentValues eventValues = new ContentValues();

		eventValues.put("calendar_id", 1); // id, We need to choose from
											// our mobile for primary
											// its 1
		eventValues.put("title", title);

		if (modify && eventIDFM != -1) {
			deleteEventWithID(curActivity, eventIDFM);
		}
		eventValues.put("description", comment+" #ResistenciarteAPP #CodigoCiudadano");
		eventValues.put("eventLocation", "Resistencia");
		eventValues.put("dtstart", startDate);
		eventValues.put("dtend", endDate);

		// values.put("allDay", 1); //If it is bithday alarm or such
		// kind (which should remind me for whole day) 0 for false, 1
		// for true
		eventValues.put("eventStatus", 1); // This information is
		// sufficient for most
		// entries tentative (0),
		// confirmed (1) or canceled
		// (2):
		eventValues.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

		eventValues.put("hasAlarm", 1); // 0 for false, 1 for true

		Uri eventUri = curActivity.getApplicationContext().getContentResolver()
				.insert(Uri.parse(eventUriString), eventValues);

		long eventID = Long.parseLong(eventUri.getLastPathSegment());

		if (needReminder) {
			/***************** Event: Reminder(with alert) Adding reminder to event *******************/

			String reminderUriString = "content://com.android.calendar/reminders";

			ContentValues reminderValues = new ContentValues();

			reminderValues.put("event_id", eventID);
			reminderValues.put("minutes", 45); // Default value of the
												// system. Minutes is a
												// integer
			reminderValues.put("method", 1); // Alert Methods: Default(0),
												// Alert(1), Email(2),
												// SMS(3)

			Uri reminderUri = curActivity.getApplicationContext()
					.getContentResolver()
					.insert(Uri.parse(reminderUriString), reminderValues);
			
		}

		/***************** Event: Meeting(without alert) Adding Attendies to the meeting *******************/

		

		return eventID;
	}
	
	public static long isAlreadyAtCalendar(Context context,
			long stTime, long enTime, String tittleP) {
		Cursor cursor = context.getContentResolver()
				.query(Uri.parse("content://com.android.calendar/events"),
						new String[] { "_id", "title", "description",
								"dtstart", "dtend", "eventLocation", "deleted",  }, null,
						null, null);
		cursor.moveToFirst();
		// fetching calendars name
		String CNames[] = new String[cursor.getCount()];

		// fetching calendars id
		int cont = 0;

		for (int i = 0; i < CNames.length; i++) {

			String eid = cursor.getString(0);

			String desc = cursor.getString(2);
			String title = cursor.getString(1);

			Date mDate = new Date(cursor.getLong(3));
			Date nDate = new Date(cursor.getLong(4));

			long mTime = mDate.getTime();
			long lTime = nDate.getTime();
			if (stTime <= mTime && enTime >= lTime
					&& tittleP.contains(title.trim())) {
				cursor.close();
				return Long.valueOf(eid);
			}

			CNames[i] = cursor.getString(1);
			cursor.moveToNext();
		}
		cursor.close();
		return -1;
	}

}
