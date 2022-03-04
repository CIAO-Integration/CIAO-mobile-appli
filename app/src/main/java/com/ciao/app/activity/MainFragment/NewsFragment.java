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
 * News category Fragment
 */
public class NewsFragment extends Fragment {
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
        View root = binding.getRoot();
        Functions.initFragment((AppCompatActivity) getActivity(), binding, R.string.news);
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