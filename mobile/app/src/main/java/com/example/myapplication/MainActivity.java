package com.example.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference databaseRef, dataBTN;
    private TextView tvTemperature, tvHumidity;
    private Button btnToggleLed;
    private Handler handler = new Handler();
    private boolean isLedOn = false; // Trạng thái LED

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ UI
        tvTemperature = findViewById(R.id.tvTemperature);
        tvHumidity = findViewById(R.id.tvHumidity);
        btnToggleLed = findViewById(R.id.btnToggleLed);

        // Kết nối Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference("data");
        dataBTN = FirebaseDatabase.getInstance().getReference("led_status");
        // Bắt đầu lắng nghe dữ liệu mỗi 1 giây
        startFetchingData();

        // Lắng nghe trạng thái LED từ Firebase
        fetchLedStatus();

        // Sự kiện bấm nút
        btnToggleLed.setOnClickListener(v -> toggleLed());
    }

    private void startFetchingData() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchLatestData();
                handler.postDelayed(this, 1000);  // Lặp lại sau 1 giây
            }
        }, 1000);
    }

    private void fetchLatestData() {
        databaseRef.orderByKey().limitToLast(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot childSnapshot : task.getResult().getChildren()) {
                    Double temperature = childSnapshot.child("temperature").getValue(Double.class);
                    Double humidity = childSnapshot.child("humidity").getValue(Double.class);

                    Log.d("Firebase", "Temp: " + temperature + "°C, Humidity: " + humidity + "%");

                    // Cập nhật giao diện
                    updateUI(temperature, humidity);
                }
            } else {
                Log.e("Firebase", "Lỗi khi lấy dữ liệu", task.getException());
            }
        });
    }

    private void updateUI(Double temperature, Double humidity) {
        runOnUiThread(() -> {
            tvTemperature.setText("Temperature: " + temperature + " °C");
            tvHumidity.setText("Humidity: " + humidity + " %");
        });
    }

    private void toggleLed() {
        isLedOn = !isLedOn; // Đảo trạng thái LED
        dataBTN.child("led_status").setValue(isLedOn ? "ON" : "OFF")
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "LED Status Updated: " + (isLedOn ? "ON" : "OFF")))
                .addOnFailureListener(e -> Log.e("Firebase", "Lỗi khi cập nhật LED", e));
    }

    private void fetchLedStatus() {
        dataBTN.child("led_status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.getValue(String.class);
                    isLedOn = "ON".equals(status);
                    updateLedButtonText();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi lấy trạng thái LED", error.toException());
            }
        });
    }

    private void updateLedButtonText() {
        runOnUiThread(() -> btnToggleLed.setText(isLedOn ? "Turn OFF LED" : "Turn ON LED"));
    }
}
