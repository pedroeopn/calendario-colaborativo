package com.pedroeopn.calendariocolaborativo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.util.ArrayList;

import static com.pedroeopn.calendariocolaborativo.CalendarUtils.daysInWeekArray;
import static com.pedroeopn.calendariocolaborativo.CalendarUtils.monthYearFromDate;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

public class WeekViewActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener {

    private AdView mAdView;
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private ListView eventListView;
    private ArrayList<Event> eventList = new ArrayList<>();
    private EventAdapter adapter;
    private ListenerRegistration eventListenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }

        setContentView(R.layout.activity_week_view);
        initWidgets();
        adapter = new EventAdapter(this, eventList);
        eventListView.setAdapter(adapter);

        new Thread(() -> {
            MobileAds.initialize(this, initializationStatus -> loadAd());
        }).start();

        setWeekView();
    }

    private void initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);
        eventListView = findViewById(R.id.eventListView);
    }

    private void setWeekView() {
        monthYearText.setText(monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<LocalDate> days = daysInWeekArray(CalendarUtils.selectedDate);
        CalendarAdapter calendarAdapter = new CalendarAdapter(days, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
        setEventAdpater();
    }

    public void previousWeekAction(View view) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusWeeks(1);
        setWeekView();
    }

    public void nextWeekAction(View view) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusWeeks(1);
        setWeekView();
    }

    @Override
    public void onItemClick(int position, LocalDate date) {
        CalendarUtils.selectedDate = date;
        setWeekView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setEventAdpater();
    }

    private void setEventAdpater() {
        if (CalendarUtils.selectedDate == null) {
            Log.e("WeekViewActivity", "CalendarUtils.selectedDate is null.");
            return;
        }
        FirestoreRepository repository = new FirestoreRepository();
        // Remove o listener anterior para evitar atualizações duplicadas.
        if (eventListenerRegistration != null) {
            eventListenerRegistration.remove();
        }
        // Inscreve-se para os eventos da data selecionada.
        eventListenerRegistration = repository.subscribeToEventsForDate(CalendarUtils.selectedDate.toString(), (querySnapshot, error) -> {
            if (error != null) {
                Toast.makeText(getApplicationContext(),
                        "Error loading events: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e("WeekViewActivity", "Error loading events", error);
                return;
            }
            ArrayList<Event> dailyEvents = new ArrayList<>();
            if (querySnapshot != null) {
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    // Agora usamos o método factory que já extrai o id do documento.
                    try {
                        dailyEvents.add(Event.fromDocument(document));
                    } catch (Exception e) {
                        Log.e("WeekViewActivity", "Error parsing event from document " + document.getId(), e);
                    }
                }
            }
            runOnUiThread(() -> {
                if (eventListView != null) {
                    EventAdapter eventAdapter = new EventAdapter(getApplicationContext(), dailyEvents);
                    eventListView.setAdapter(eventAdapter);
                } else {
                    Log.e("WeekViewActivity", "eventListView is null. Check widget initialization.");
                }
            });
        });
    }

    public void newEventAction(View view) {
        startActivity(new Intent(this, EventEditActivity.class));
    }

    public void dailyAction(View view) {
        startActivity(new Intent(this, DailyCalendarActivity.class));
    }

    public void updateEventList(LocalDate selectedDate) {
        ArrayList<Event> eventsForSelectedDate = Event.eventsForDate(selectedDate);
        EventAdapter eventAdapter = new EventAdapter(this, eventsForSelectedDate);
        eventListView.setAdapter(eventAdapter);
    }

    public void backToMonthView(View view) {
        Intent intent = new Intent(WeekViewActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadAd() {
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }
}