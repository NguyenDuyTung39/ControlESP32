package com.example.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference databaseRef;
    private TextView tvTemperature, tvHumidity;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ UI
        tvTemperature = findViewById(R.id.tvTemperature);
        tvHumidity = findViewById(R.id.tvHumidity);

        // Kết nối Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference("data");

        // Bắt đầu lắng nghe dữ liệu mỗi 1 giây
        startFetchingData();
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
}
