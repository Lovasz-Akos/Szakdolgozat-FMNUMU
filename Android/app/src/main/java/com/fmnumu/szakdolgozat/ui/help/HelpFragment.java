package com.fmnumu.szakdolgozat.ui.help;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.fmnumu.szakdolgozat.databinding.FragmentHelpBinding;

public class HelpFragment extends Fragment {

    private HelpViewModel helpViewModel;
    private FragmentHelpBinding binding;

    public static HelpFragment newInstance() {
        return new HelpFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        helpViewModel =
                new ViewModelProvider(this).get(HelpViewModel.class);
        binding = FragmentHelpBinding.inflate(inflater, container, false);
        View root = binding.getRoot();



        return root;
    }

}