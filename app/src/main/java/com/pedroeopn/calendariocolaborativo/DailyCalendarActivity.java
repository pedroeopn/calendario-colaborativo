package com.pedroeopn.calendariocolaborativo;

import static com.pedroeopn.calendariocolaborativo.CalendarUtils.selectedDate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DailyCalendarActivity extends AppCompatActivity {

    private TextView monthDayText;
    private TextView dayOfWeekTV;
    private ListView hourListView;

    // Instance variable to hold the listener registration
    private ListenerRegistration dailyEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_calendar);
        initWidgets();
    }

    private void initWidgets() {
        monthDayText = findViewById(R.id.monthDayText);
        dayOfWeekTV = findViewById(R.id.dayOfWeekTV);
        hourListView = findViewById(R.id.hourListView);
    }

    private void setDayView() {
        monthDayText.setText(CalendarUtils.monthDayFromDate(selectedDate));
        String dayOfWeek = selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        dayOfWeekTV.setText(dayOfWeek);
        // Initialize the adapter with an empty list until Firestore loads events.
        setHourAdapter(new ArrayList<>());
    }

    // Builds the HourAdapter based on a list of events for the day.
    private void setHourAdapter(ArrayList<Event> dailyEvents) {
        ArrayList<HourEvent> hourEvents = new ArrayList<>();
        // For each hour of the day, collect events that match that hour.
        for (int hour = 0; hour < 24; hour++) {
            LocalTime hourSlot = LocalTime.of(hour, 0);
            ArrayList<Event> eventsForHour = new ArrayList<>();
            for (Event event : dailyEvents) {
                // Assuming event.getTime() returns a LocalTime.
                if (event.getTime().getHour() == hourSlot.getHour()) {
                    eventsForHour.add(event);
                }
            }
            hourEvents.add(new HourEvent(hourSlot, eventsForHour));
        }
        HourAdapter hourAdapter = new HourAdapter(getApplicationContext(), hourEvents);
        hourListView.setAdapter(hourAdapter);
    }

    public void previousDayAction(View view) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusDays(1);
        setDayView();
    }

    public void nextDayAction(View view) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusDays(1);
        setDayView();
    }

    public void newEventAction(View view) {
        startActivity(new Intent(this, EventEditActivity.class));
    }

    public void backToWeekView(View view) {
        Intent intent = new Intent(DailyCalendarActivity.this, WeekViewActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setDayView();
        loadDailyEvents();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dailyEventListener != null) {
            dailyEventListener.remove();
        }
    }

    private void loadDailyEvents() {
        if (CalendarUtils.selectedDate == null) {
            Log.e("DailyCalendarActivity", "Selected date is null.");
            return;
        }
        FirestoreRepository repository = new FirestoreRepository();
        // Subscribe to events for the selected date using ISO-8601 date string.
        dailyEventListener = repository.subscribeToEventsForDate(selectedDate.toString(),
                new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot querySnapshot, FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("DailyCalendarActivity", "Error loading events.", error);
                            return;
                        }
                        final ArrayList<Event> dailyEvents = new ArrayList<>();
                        if (querySnapshot != null) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                String name = document.getString("name");
                                String dateString = document.getString("date");
                                String timeString = document.getString("time");
                                if (name == null || dateString == null || timeString == null) {
                                    continue;
                                }
                                try {
                                    LocalDate eventDate = LocalDate.parse(dateString);
                                    LocalTime eventTime = LocalTime.parse(timeString);
                                    dailyEvents.add(new Event(name, eventDate, eventTime));
                                } catch (Exception e) {
                                    Log.e("DailyCalendarActivity", "Error parsing event from document " + document.getId(), e);
                                }
                            }
                        }
                        // Update the adapter on the UI thread with the freshly loaded events.
                        runOnUiThread(() -> setHourAdapter(dailyEvents));
                    }
                }
        );
    }
}