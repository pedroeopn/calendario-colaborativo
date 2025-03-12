package com.pedroeopn.calendariocolaborativo;

import com.google.firebase.firestore.DocumentSnapshot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class Event {
    public static ArrayList<Event> eventsList = new ArrayList<>();

    public static ArrayList<Event> eventsForDate(LocalDate date) {
        ArrayList<Event> events = new ArrayList<>();

        for (Event event : eventsList) {
            if (event.getDate().equals(date))
                events.add(event);
        }

        return events;
    }

    public static ArrayList<Event> eventsForDateAndTime(LocalDate date, LocalTime time) {
        ArrayList<Event> events = new ArrayList<>();

        for (Event event : eventsList) {
            int eventHour = event.getTime().getHour();
            int cellHour = time.getHour();
            if (event.getDate().equals(date) && eventHour == cellHour)
                events.add(event);
        }

        return events;
    }

    // Novo campo para armazenar a ID do documento no Firestore.
    private String id;
    private String name;
    private LocalDate date;
    private LocalTime time;

    // Construtor sem id (útil ao criar novos eventos antes de persistir no Firestore)
    public Event(String name, LocalDate date, LocalTime time) {
        this.name = name;
        this.date = date;
        this.time = time;
    }

    // Construtor com id, para eventos que já foram persistidos e possui documento Firestore.
    public Event(String id, String name, LocalDate date, LocalTime time) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
    }

    // Método factory que cria um objeto Event a partir de um DocumentSnapshot.
    public static Event fromDocument(DocumentSnapshot documentSnapshot) {
        String id = documentSnapshot.getId();
        String name = documentSnapshot.getString("name");
        LocalDate date = LocalDate.parse(documentSnapshot.getString("date"));
        LocalTime time = LocalTime.parse(documentSnapshot.getString("time"));
        return new Event(id, name, date, time);
    }

    // Getter e setter para id.
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getters e setters existentes.
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }
}