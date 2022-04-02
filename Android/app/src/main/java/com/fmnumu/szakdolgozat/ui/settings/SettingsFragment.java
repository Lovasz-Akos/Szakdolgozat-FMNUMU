package com.fmnumu.szakdolgozat.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fmnumu.szakdolgozat.MainActivity;
import com.fmnumu.szakdolgozat.R;
import com.fmnumu.szakdolgozat.databinding.FragmentSettingsBinding;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.List;

public class SettingsFragment extends Fragment {

    private SettingsViewModel settingsViewModel;
    private FragmentSettingsBinding binding;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ((MainActivity) getActivity()).getAllProfiles();

        Button deleteProfileButton = root.findViewById(R.id.profile_delete_button);
        deleteProfileButton.setOnClickListener(view -> {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
            dialogBuilder.setTitle("Pick a profile to delete");

            final Spinner typeSpinner = new Spinner(getContext());
            final String[] selectedProfile = {""};

            List<String> allProfiles = ((MainActivity)getActivity()).getAllProfiles(); //TODO: get all filenames

            typeSpinner.setAdapter(new ArrayAdapter<>(getContext(), R.layout.item_spinner, allProfiles));

            dialogBuilder.setView(typeSpinner);

            typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view1, int i, long l) {
                    //((MainActivity)getActivity()).deleteUserProfile(typeSpinner.getSelectedItem().toString());
                    selectedProfile[0] = typeSpinner.getSelectedItem().toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            dialogBuilder.setPositiveButton("Delete", (dialog1, which1) -> {
                ((MainActivity)getActivity()).deleteUserProfile(selectedProfile[0]);
            });
            dialogBuilder.setNegativeButton("Cancel", ((dialogInterface, i) -> {

            }));

            dialogBuilder.show();
        });

        TextInputLayout addressField = (TextInputLayout) root.findViewById(R.id.address_container);
        TextInputLayout usernameField = (TextInputLayout) root.findViewById(R.id.username_container);

        SharedPreferences addressPref = getContext().getSharedPreferences("address", Context.MODE_PRIVATE);
        SharedPreferences usernamePref = getContext().getSharedPreferences("username", Context.MODE_PRIVATE);

        addressField.getEditText().setText(addressPref.getString(getString(R.string.mqttAddressPersistent), null));
        usernameField.getEditText().setText(usernamePref.getString(getString(R.string.usernamePersistent), null));

        Button connect = root.findViewById(R.id.buttonMqttConnect);
        connect.setOnClickListener(view -> {
            String username = usernameField.getEditText().getText().toString();
            String mqttAddress = addressField.getEditText().getText().toString();

            if (!username.equals("")) {
                if (mqttAddress.equals("")) {
                    Toast toast = Toast.makeText(getContext(), "Address can't be empty", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    ((MainActivity) getActivity()).connectMQTT(mqttAddress);
                    try {
                        ((MainActivity) getActivity()).emptyCardMemory();
                        ((MainActivity) getActivity()).populatePersistentDataFields(username);
                        ((MainActivity) getActivity()).saveUserAndServer(username, mqttAddress);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                usernameField.getEditText().setError("Username is required");
            }
        });

        settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}