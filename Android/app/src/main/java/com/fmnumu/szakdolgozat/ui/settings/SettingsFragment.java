package com.fmnumu.szakdolgozat.ui.settings;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.fmnumu.szakdolgozat.MainActivity;
import com.fmnumu.szakdolgozat.R;
import com.fmnumu.szakdolgozat.databinding.FragmentSettingsBinding;

import java.util.List;

public class SettingsFragment extends Fragment {

    private SettingsViewModel settingsViewModel;
    private FragmentSettingsBinding binding;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

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

            dialogBuilder.show();
        });

        return root;
    }

}