package com.fmnumu.szakdolgozat.ui.connection;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fmnumu.szakdolgozat.MainActivity;
import com.fmnumu.szakdolgozat.R;
import com.fmnumu.szakdolgozat.databinding.FragmentConnectionBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;

public class ConnectionFragment extends Fragment{

    private ConnectionViewModel connectionViewModel;
    private FragmentConnectionBinding binding;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentConnectionBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ((MainActivity)getActivity()).listAllFiles();

        Button deleteAllFiles = root.findViewById(R.id.buttonDELETE);
        deleteAllFiles.setOnClickListener(view -> {
            ((MainActivity)getActivity()).deleteAllFiles();
        });

        TextInputLayout addressField = (TextInputLayout) root.findViewById(R.id.address_container);
        TextInputLayout usernameField = (TextInputLayout) root.findViewById(R.id.username_container);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String defaultValue = getResources().getString(R.string._192_168_0_200);
        addressField.getEditText().setText(sharedPref.getString(getString(R.string.mqttAdrr), defaultValue));

        Button connect = root.findViewById(R.id.buttonMqttConnect);
        connect.setOnClickListener(view -> {
            String username = String.valueOf(usernameField.getEditText().getText());
            String mqttAddress = String.valueOf(addressField.getEditText().getText());
            if (!username.equals("")){
                if (mqttAddress.equals("")){
                    Toast toast = Toast.makeText(getContext(), "Address can't be empty", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else{
                    ((MainActivity)getActivity()).connectMQTT(mqttAddress);
                    try {
                        ((MainActivity)getActivity()).emptyCardMemory();
                        ((MainActivity)getActivity()).populatePersistentDataFields(username);
                        ((MainActivity)getActivity()).saveMqttAddress();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else{
                usernameField.getEditText().setError("Username is required");
            }
        });

        connectionViewModel =
                new ViewModelProvider(this).get(ConnectionViewModel.class);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}