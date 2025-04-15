package com.pedroeopn.calendariocolaborativo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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

    private void addNotification(Context context) {
        // Certifica-se de que o canal de notificação foi criado
        createNotificationChannel(context);

        // Cria o builder e informa o ID do canal ("Add") como segundo parâmetro.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Add")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Calendário Colaborativo")
                .setContentText("Uma atividade foi criada!")
                // Define a prioridade para dispositivos abaixo de API 26
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Cria uma intent que abre DailyCalendarActivity ao clicar na notificação.
        Intent notificationIntent = new Intent(context, DailyCalendarActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);

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
            NotificationChannel channel = new NotificationChannel("Add", name, importance);
            channel.setDescription(description);
            // Registra o canal com o sistema para que as notificações possam ser exibidas.
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void saveEventAction(View view) {
        String eventName = eventNameET.getText().toString().trim();
        if (eventName.isEmpty()) {
            Toast.makeText(this, "O nome do evento não pode ser vazio", Toast.LENGTH_SHORT).show();
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
                runOnUiThread(() -> {
                    // Dispara a notificação utilizando o contexto da aplicação
                    addNotification(getApplicationContext());
                    // Fecha a Activity após enviar a notificação
                    finish();
                });
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