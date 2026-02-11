package com.example.amit_mitzmacher_tal_mitzmacher.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.amit_mitzmacher_tal_mitzmacher.MainActivity;
import com.example.amit_mitzmacher_tal_mitzmacher.R;
import com.example.amit_mitzmacher_tal_mitzmacher.databinding.FragmentRegisterBinding;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnRegister.setOnClickListener(v -> {
            String email = binding.EmailReg.getText().toString().trim();
            String password = binding.PasswordReg.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) {
                    activity.register(email, password, () -> {
                        Navigation.findNavController(view).navigateUp();
                    });
                }
            } else {
                Toast.makeText(getContext(), getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnBackToLogin.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_registerFragment_to_logInFragment)
        );


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}