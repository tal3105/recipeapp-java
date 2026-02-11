package com.example.amit_mitzmacher_tal_mitzmacher.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.amit_mitzmacher_tal_mitzmacher.MainActivity;
import com.example.amit_mitzmacher_tal_mitzmacher.R;

public class LogInFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_log_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPassword = view.findViewById(R.id.etPassword);
        Button buttonLogin = view.findViewById(R.id.btnLogin);
        Button buttonGoToRegister = view.findViewById(R.id.btnGoToRegister);

        buttonLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) {
                    activity.login(email, password, () -> {
                        Navigation.findNavController(view).navigate(R.id.action_logInFragment_to_homeFragment);
                    });
                }
            } else {
                Toast.makeText(getContext(), getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show();
            }
        });

        buttonGoToRegister.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_logInFragment_to_registerFragment)
        );
    }
}