package com.pedroeopn.calendariocolaborativo;

import static com.pedroeopn.calendariocolaborativo.CalendarUtils.selectedDate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
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

    // Add an instance variable to hold the listener registration
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
        String dayOfWeek = selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("pt", "br"));
        dayOfWeekTV.setText(dayOfWeek);
        setHourAdapter();
    }

    private void setHourAdapter() {
        HourAdapter hourAdapter = new HourAdapter(getApplicationContext(), hourEventList());
        hourListView.setAdapter(hourAdapter);
    }

    private ArrayList<HourEvent> hourEventList() {
        ArrayList<HourEvent> list = new ArrayList<>();

        for (int hour = 0; hour < 24; hour++) {
            LocalTime time = LocalTime.of(hour, 0);
            ArrayList<Event> events = Event.eventsForDateAndTime(selectedDate, time);
            HourEvent hourEvent = new HourEvent(time, events);
            list.add(hourEvent);
        }

        return list;
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
        // Subscribe to events for the selected date using ISO-8601 date string
        dailyEventListener = repository.getEventsForDate(selectedDate.toString(), new FirestoreRepository.EventListener() {
            @Override
            public void onEventLoaded(List<Event> events) {
                // Handle loaded events
            }

            @Override
            public void onEventLoadError(Exception e) {
                Log.e("DailyCalendarActivity", "Error loading events.", e);
            }
        });
    }
}
