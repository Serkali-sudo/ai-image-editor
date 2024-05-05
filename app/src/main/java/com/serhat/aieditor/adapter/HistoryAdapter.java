package com.serhat.aieditor.adapter;

import android.content.DialogInterface;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.serhat.aieditor.GalleryActivity;
import com.serhat.aieditor.R;
import com.serhat.aieditor.Utils;
import com.serhat.aieditor.model.GalleryModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryAdapterViewHolder> {

    private ActionMode mActionMode;

    private List<GalleryModel> listData = new ArrayList<>();

    private List<GalleryModel> tempListData = new ArrayList<>();

    private GalleryActivity activity;

    private CurrTab currTab = CurrTab.HISTORY;

    public enum CurrTab {
        HISTORY, SAVED, UPSCALED
    }


    private boolean isMultiSelectMode = false;


    public HistoryAdapter(GalleryActivity activity, List<GalleryModel> listData, CurrTab currTab) {
        this.activity = activity;
        this.listData = listData;
        this.currTab = currTab;
    }


    @NonNull
    @Override
    public HistoryAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HistoryAdapterViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gallery_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapterViewHolder holder, int position) {
        holder.onBind(listData.get(position));
    }

    public boolean isListEmpty() {
        return listData == null || listData.size() == 0;
    }

    public void deleteItem(int pos) {
        this.listData.remove(pos);
        notifyItemRemoved(pos);
    }


    public void clearList() {
        this.listData.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }


    public class HistoryAdapterViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView, selected_IV;
        private View view;


        public HistoryAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.galleryImage);
            selected_IV = itemView.findViewById(R.id.selected_IV);
            view = itemView;
        }


        public void onBind(GalleryModel galleryModel) {
//            imageView.setImageURI(Uri.parse(galleryModel.path));


            if (galleryModel.path != null) {
                File file = new File(galleryModel.path);
                if (file.exists()) {
//                    BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inJustDecodeBounds = true;
//                    BitmapFactory.decodeFile(galleryModel.path, options);
//                    int imageWidth = options.outWidth;
//                    int imageHeight = options.outHeight;
                    view.getLayoutParams().width = Utils.pxToDp(activity, galleryModel.width);
                    view.getLayoutParams().height = Utils.pxToDp(activity, galleryModel.height);
                    Glide.with(itemView.getContext()).load(file)
                            .into(imageView);
//                    imageView.setImageURI(Uri.parse(galleryModel.path));
                }
            }
            if (galleryModel.isSelected) {
                selected_IV.setVisibility(View.VISIBLE);
            } else {
                selected_IV.setVisibility(View.GONE);
            }


            ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (lp instanceof FlexboxLayoutManager.LayoutParams) {
                ((FlexboxLayoutManager.LayoutParams) lp).setFlexGrow(1f);
            }


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isMultiSelectMode) {
                        if (galleryModel.isSelected) {
                            galleryModel.isSelected = false;
                            selected_IV.setVisibility(View.GONE);
                            tempListData.remove(galleryModel);
                            if (tempListData.size() == 0) {
                                isMultiSelectMode = false;
                                if (mActionMode != null) {
                                    mActionMode.finish();
                                }
                            } else {
                                if (mActionMode != null) {
                                    mActionMode.setTitle("Selected: " + tempListData.size() + "/" + getItemCount());
                                }
                            }
                        } else {
                            galleryModel.isSelected = true;
                            selected_IV.setVisibility(View.VISIBLE);
                            tempListData.add(galleryModel);
                            if (mActionMode != null) {
                                mActionMode.setTitle("Selected: " + tempListData.size() + "/" + getItemCount());
                            }
                        }
                    } else {
                        activity.openImageFullscreen(galleryModel);
                    }
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (galleryModel.isSelected) {
                        galleryModel.isSelected = false;
                        selected_IV.setVisibility(View.GONE);
                        tempListData.remove(galleryModel);
                        if (tempListData.size() == 0) {
                            isMultiSelectMode = false;
                            if (mActionMode != null) {
                                mActionMode.finish();
                            }
                        } else {
                            if (mActionMode != null) {
                                mActionMode.setTitle("Selected: " + tempListData.size() + "/" + getItemCount());
                            }
                        }

                    } else {
                        galleryModel.isSelected = true;
                        selected_IV.setVisibility(View.VISIBLE);
                        tempListData.add(galleryModel);
                        isMultiSelectMode = true;
                        if (mActionMode == null) {
                            mActionMode = activity.startSupportActionMode(mActionModeCallback);
                            mActionMode.setTitle("Selected: " + tempListData.size() + "/" + getItemCount());
                        }

                    }


//                    historyClick.onHistoryLongClick(galleryModel, getAdapterPosition());
                    return true;
                }
            });

        }
    }

    public void enableActionMode() {
        if (mActionMode == null) {
            mActionMode = activity.startSupportActionMode(mActionModeCallback);
            mActionMode.setTitle("Selected: " + tempListData.size() + "/" + getItemCount());
            isMultiSelectMode = true;
        }
    }

    private void showSelectedRemoveDialog() {
        new MaterialAlertDialogBuilder(activity)
                .setTitle("Delete Selected Images?")
                .setMessage("Are you sure you want yo delete all of the selected images?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeItems();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    public ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.multi_select, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.delete_multi) {
                showSelectedRemoveDialog();
                return true;
            }
            return false;
        }


        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            isMultiSelectMode = false;
            removeTicks();
        }
    };


    private void removeItems() {
        if (tempListData.size() > 0) {
            for (GalleryModel item : tempListData) {
                int index = listData.indexOf(item);
                if (index != -1) {
                    item.deleteFromDisk();
                    if (currTab == CurrTab.HISTORY) {
                        activity.databaseHelper.deleteHistoryByID(item.id);
                    } else if (currTab == CurrTab.SAVED) {
                        activity.databaseHelper.deleteSavedByID(item.id);
                    } else if (currTab == CurrTab.UPSCALED) {
                        activity.databaseHelper.deleteUpscaledByID(item.id);
                    }
                    listData.remove(item);
                    notifyItemRemoved(index);
                }
            }
            tempListData.clear();
            if (mActionMode != null) {
                mActionMode.finish();
            }
        }
    }

    private void removeTicks() {
        if (tempListData.size() > 0) {
            for (GalleryModel item : tempListData) {
                int index = listData.indexOf(item);
                if (index != -1) {
                    listData.get(index).isSelected = false;
                    notifyItemChanged(index);
                }
            }
            tempListData.clear();
        }
    }


}