package com.pedroeopn.calendariocolaborativo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalTime;

public class EventEditActivity extends AppCompatActivity
{
    private EditText eventNameET;
    private TextView eventDateTV, eventTimeTV;

    private LocalTime time;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);
        initWidgets();
        time = LocalTime.now();
        eventDateTV.setText("Date: " + CalendarUtils.formattedDate(CalendarUtils.selectedDate));
        eventTimeTV.setText("Time: " + CalendarUtils.formattedTime(time));
    }

    private void initWidgets()
    {
        eventNameET = findViewById(R.id.eventNameET);
        eventDateTV = findViewById(R.id.eventDateTV);
        eventTimeTV = findViewById(R.id.eventTimeTV);
    }

    public void saveEventAction(View view) {
        String eventName = eventNameET.getText().toString().trim();
        if (eventName.isEmpty()) {
            Toast.makeText(this, "Event name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        // Create the event object
        Event newEvent = new Event(eventName, CalendarUtils.selectedDate, time);

        // Use the Firestore repository to add the event.
        FirestoreRepository repository = new FirestoreRepository();
        repository.addEvent(newEvent, new FirestoreRepository.OnOperationCompleteListener() {
            @Override
            public void onSuccess() {
                // You may want to perform additional actions (for example, update local caches)
                runOnUiThread(() -> finish());
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(EventEditActivity.this,
                            "Failed to save event: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}