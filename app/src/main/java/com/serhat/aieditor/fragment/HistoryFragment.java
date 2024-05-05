package com.serhat.aieditor.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.serhat.aieditor.GalleryActivity;
import com.serhat.aieditor.R;
import com.serhat.aieditor.adapter.HistoryAdapter;
import com.serhat.aieditor.model.GalleryModel;


import java.io.File;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    public GalleryActivity activity;
    private HistoryAdapter historyAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (GalleryActivity) getActivity();
        setHasOptionsMenu(true);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.gallery_fragment, container, false);
        recyclerView = layout.findViewById(R.id.bookmark_RV);
        FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(activity);
        flexboxLayoutManager.setFlexWrap(FlexWrap.WRAP);
        flexboxLayoutManager.setFlexDirection(FlexDirection.ROW);
        flexboxLayoutManager.setAlignItems(AlignItems.STRETCH);
        recyclerView.setLayoutManager(flexboxLayoutManager);


        return layout;
    }


    public void enableMultiMode() {
        if (historyAdapter != null) {
            historyAdapter.enableActionMode();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        List<GalleryModel> historyList = activity.databaseHelper.getAllHistory();
        historyAdapter = new HistoryAdapter(activity, historyList, HistoryAdapter.CurrTab.HISTORY);
        if (historyList != null) {
            recyclerView.setAdapter(historyAdapter);
        }


    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.bhmenu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            activity.finish();
            activity.setResult(RESULT_OK);
            return true;
        } else if (id == R.id.remove_all_bh) {
            if (activity.databaseHelper != null) {
                new MaterialAlertDialogBuilder(activity)
                        .setTitle("Delete All History?")
                        .setMessage("Are you sure that you want to delete all history?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        File result = new File(activity.getExternalFilesDir("history"), "generation_history");
                                        if (result.exists()) {
                                            result.delete();
                                        }
                                    }
                                }).start();

                                activity.databaseHelper.deleteAllHistory();
                                historyAdapter.clearList();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();


            }
        } else if (id == R.id.multi_select_menu) {
            enableMultiMode();
        }
        return super.onOptionsItemSelected(item);
    }
}
