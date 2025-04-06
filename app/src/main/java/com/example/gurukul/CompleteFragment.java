package com.example.gurukul;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CompleteFragment extends Fragment {

    private static final String TAG = "CompleteFragment";

    public CompleteFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view;
        try {
            view = inflater.inflate(R.layout.fragment_complete, container, false);

            // Initialize your views here safely
            // Example:
            // RecyclerView recyclerView = view.findViewById(R.id.recyclerViewCompleted);
            // Setup your adapter and data fetch here

        } catch (Exception e) {
            Log.e(TAG, "Error inflating fragment view", e);
            Toast.makeText(getContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
            // Return a fallback view to avoid crash
            return new View(requireContext());
        }

        return view;
    }
}
