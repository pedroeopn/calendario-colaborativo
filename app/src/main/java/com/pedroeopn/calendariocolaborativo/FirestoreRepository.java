package com.pedroeopn.calendariocolaborativo;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.HashMap;
import java.util.Map;

public class FirestoreRepository {
    private final FirebaseFirestore db;
    private final CollectionReference eventsCollection;

    public FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
        // Explicitly enable offline persistence (note: offline persistence is enabled by default on mobile)
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        eventsCollection = db.collection("events");
    }

    // Subscribes to realtime updates for events with the specified date.
    public ListenerRegistration subscribeToEventsForDate(String date, EventListener<QuerySnapshot> listener) {
        return eventsCollection.whereEqualTo("date", date)
                .addSnapshotListener(listener);
    }

    // Adds a new event to Firestore.
    public void addEvent(Event event, OnOperationCompleteListener listener) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", event.getName());
        data.put("date", event.getDate().toString()); // ISO-8601 formatted, for example
        data.put("time", event.getTime().toString());

        eventsCollection.add(data)
                .addOnSuccessListener(documentReference -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    // Updates an existing event. (Requires you to track the event’s document ID.)
    public void updateEvent(String eventId, Event event, OnOperationCompleteListener listener) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", event.getName());
        data.put("date", event.getDate().toString());
        data.put("time", event.getTime().toString());

        eventsCollection.document(eventId).update(data)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    // Deletes an event by its document ID.
    public void deleteEvent(String eventId, OnOperationCompleteListener listener) {
        eventsCollection.document(eventId).delete()
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    public interface OnOperationCompleteListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}