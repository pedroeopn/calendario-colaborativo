package com.pedroeopn.calendariocolaborativo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

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
                        removeNotification(getContext().getApplicationContext());
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

    private void removeNotification(Context context) {
        // Certifica-se de que o canal de notificação foi criado
        createNotificationChannel(context);

        // Cria o builder e informa o ID do canal ("Add") como segundo parâmetro.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Delete")
                .setSmallIcon(R.drawable.baseline_delete_red)
                .setContentTitle("Calendário Colaborativo")
                .setContentText("Uma atividade foi removida!")
                // Define a prioridade para dispositivos abaixo de API 26
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Cria uma intent que abre DailyCalendarActivity ao clicar na notificação.
        Intent notificationIntent = new Intent(context, DailyCalendarActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);

        // Obtém o NotificationManager e dispara a notificação usando um ID único.

        // Obtém o NotificationManager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Gera um id único para cada notificação, por exemplo, baseado no timestamp atual
        int notificationId = (int) System.currentTimeMillis();

        // Cria e dispara a notificação com o id exclusivo
        notificationManager.notify(notificationId, builder.build());
    }

    private void createNotificationChannel(Context context) {
        // Esse canal é necessário apenas em dispositivos com API 26 ou superior.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Obtém o nome e a descrição do canal a partir dos recursos.
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            // Cria o canal com o ID "Add". Esse mesmo ID deve ser utilizado no builder.
            NotificationChannel channel = new NotificationChannel("Delete", name, importance);
            channel.setDescription(description);
            // Registra o canal com o sistema para que as notificações possam ser exibidas.
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}