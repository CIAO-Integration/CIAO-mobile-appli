package com.ciao.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ciao.app.Functions;
import com.ciao.app.R;
import com.ciao.app.databinding.FragmentMainBinding;

/**
 * Sport category Fragment
 */
public class SportFragment extends Fragment {
    /**
     * Binding to get Views
     */
    private FragmentMainBinding binding;

    /**
     * Create Fragment
     * @param inflater Inflater
     * @param container Container
     * @param savedInstanceState SavedInstanceState
     * @return View
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Functions.initFragment(getContext(), binding, R.string.sport);
        return root;
    }

    /**
     * Action on destroy
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}