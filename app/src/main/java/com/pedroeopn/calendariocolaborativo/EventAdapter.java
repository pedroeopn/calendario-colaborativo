package com.pedroeopn.calendariocolaborativo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class EventAdapter extends ArrayAdapter<Event> {
    public EventAdapter(@NonNull Context context, List<Event> events) {
        super(context, 0, events);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Event event = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_cell, parent, false);
        }

        TextView eventCellTV = convertView.findViewById(R.id.eventCellTV);
        ImageButton deleteButton = convertView.findViewById(R.id.deleteButton);

        // Define o título do evento concatenando o nome e o horário formatado.
        String eventTitle = event.getName() + " " + CalendarUtils.formattedTime(event.getTime());
        eventCellTV.setText(eventTitle);

        // Configura o listener do botão para remover o evento tanto localmente quanto no Firestore.
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (event.getId() == null || event.getId().isEmpty()) {
                    Toast.makeText(getContext(), "ID do evento não definido.", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirestoreRepository repository = new FirestoreRepository();
                repository.deleteEvent(event.getId(), new FirestoreRepository.OnOperationCompleteListener() {
                    @Override
                    public void onSuccess() {
                        // Remove o evento do adapter e atualiza a ListView
                        remove(event);
                        notifyDataSetChanged();
                        Toast.makeText(getContext(), "Evento removido com sucesso.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Erro ao remover evento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return convertView;
    }
}