package com.pedroeopn.calendariocolaborativo;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirestoreRepository {
    private final FirebaseFirestore db;
    private final CollectionReference eventsCollection;

    public FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
        // Ativa a persistência offline (normalmente já está ativada nos dispositivos)
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        eventsCollection = db.collection("events");
    }

    // Inscreve-se para atualizações em tempo real dos eventos com a data especificada.
    public ListenerRegistration subscribeToEventsForDate(String date, EventListener<QuerySnapshot> listener) {
        return eventsCollection.whereEqualTo("date", date)
                .addSnapshotListener(listener);
    }

    // Adiciona um novo evento ao Firestore.
    public void addEvent(Event event, OnOperationCompleteListener listener) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", event.getName());
        data.put("date", event.getDate().toString()); // Formato ISO-8601, por exemplo
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

    // Atualiza um evento existente. (É necessário rastrear o ID do documento do evento)
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

    // Deleta um evento a partir do ID do documento.
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