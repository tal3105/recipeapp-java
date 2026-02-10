package com.example.amit_mitzmacher_tal_mitzmacher;

import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
    }

    // פונקציית התחברות שמקבלת Runnable לביצוע ניווט בהצלחה
    public void login(String email, String password, Runnable onSuccess) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                        if (onSuccess != null) onSuccess.run();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : getString(R.string.error_unknown);
                        Toast.makeText(this, getString(R.string.error_prefix) + " " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // פונקציית הרשמה שמקבלת Runnable לביצוע ניווט בהצלחה
    public void register(String email, String password, Runnable onSuccess) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_SHORT).show();
                        if (onSuccess != null) onSuccess.run();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : getString(R.string.error_unknown);
                        Toast.makeText(this, getString(R.string.error_prefix) + " " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }
}