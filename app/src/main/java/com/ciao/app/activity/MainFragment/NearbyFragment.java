package com.ciao.app.activity.MainFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.ciao.app.Functions;
import com.ciao.app.R;
import com.ciao.app.databinding.FragmentMainBinding;

/**
 * Around category Fragment
 */
public class NearbyFragment extends Fragment {
    /**
     * Binding to get Views
     */
    private FragmentMainBinding binding;

    /**
     * Create Fragment
     *
     * @param inflater           Inflater
     * @param container          Container
     * @param savedInstanceState SavedInstanceState
     * @return View
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * On resume, update nearby production list
     */
    @Override
    public void onResume() {
        super.onResume();
        Functions.initFragment((AppCompatActivity) getActivity(), binding, R.string.nearby);
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