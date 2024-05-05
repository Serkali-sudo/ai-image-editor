package com.serhat.aieditor;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationBarView;
import com.serhat.aieditor.app.ApplicationPath;
import com.serhat.aieditor.db.DatabaseHelper;
import com.serhat.aieditor.fragment.HistoryFragment;
import com.serhat.aieditor.fragment.SavedFragment;
import com.serhat.aieditor.fragment.UpscaledFragment;
import com.serhat.aieditor.model.GalleryModel;
import com.serhat.aieditor.viewpager.FragmentPagerItems;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class GalleryActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private ViewPager2 viewPager2;

    private BottomNavigationView nav_view;

    public DatabaseHelper databaseHelper;

    private View contentView;
    private int systemVisibility;

    private final String TAG = "GalleryActivity";

    private GalleryModel currGalleryModel = null;
    public WindowInsetsControllerCompat windowInsetsController;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery);
        windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        viewPager2 = findViewById(R.id.viewPager);
        nav_view = findViewById(R.id.nav_view);
        databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
        initFragments();
        if (getIntent() != null) {
            viewPager2.setCurrentItem(getIntent().getIntExtra("isBookmark", 0));
        }
    }

    public void openImageFullscreen(GalleryModel galleryModel) {
        currGalleryModel = galleryModel;
        contentView = View.inflate(GalleryActivity.this, R.layout.fullscreen_image, null);
        ZoomageView bigImageView = contentView.findViewById(R.id.bigImageView);
        ImageButton go_back_IB = contentView.findViewById(R.id.go_back_IB);
        ImageButton image_info_IB = contentView.findViewById(R.id.image_info_IB);
        ImageButton download_IB = contentView.findViewById(R.id.download_IB);
        MaterialCardView download_MC = contentView.findViewById(R.id.download_MC);
        ImageButton share_IB = contentView.findViewById(R.id.share_IB);

        if (galleryModel != null) {
            bigImageView.setImageURI(Uri.parse(galleryModel.path));
        }
        go_back_IB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeImage();
            }
        });
        image_info_IB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (galleryModel != null) {
                    new MaterialAlertDialogBuilder(GalleryActivity.this)
                            .setTitle("Image Details")
                            .setMessage(Html.fromHtml("<b>Resolution:</b> " + galleryModel.width + "x" + galleryModel.height
                                    + "<br> <b>Prompt:</b> " + galleryModel.prompt
                                    + "<br> <b>Negative Prompt:</b> " + galleryModel.negative_prompt
                                    + "<br> <b>Date:</b> " + galleryModel.addedDate
                                    + "<br> <b>Seed:</b> " + galleryModel.seed
                                    + "<br> <b>Image Path:</b> " + galleryModel.path))
                            .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }
                            ).setNeutralButton("Copy seed", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (!TextUtils.isEmpty(galleryModel.seed)) {
                                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clipData = ClipData.newPlainText("seed", galleryModel.seed);
                                        clipboard.setPrimaryClip(clipData);
                                        Toast.makeText(GalleryActivity.this, "Copied seed.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(GalleryActivity.this, "Error: Seed is not available", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            })


                            .show();
                } else {
                    Toast.makeText(GalleryActivity.this, "Couldn't get details", Toast.LENGTH_SHORT).show();
                }
            }
        });
        download_IB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkSaveCurrImg(galleryModel);
            }
        });
        share_IB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (galleryModel != null) {
                    try {
                        File file = new File(galleryModel.path);
                        Uri fileUri = FileProvider.getUriForFile(GalleryActivity.this, getPackageName() + ".provider", file);
                        String mimeType = getMimeType(fileUri.toString());
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setDataAndType(fileUri, mimeType);
                        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(GalleryActivity.this, "Cant share", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        download_MC.setVisibility(viewPager2.getCurrentItem() == 0 ? View.VISIBLE : View.GONE);
        systemVisibility = getWindow().getDecorView().getSystemUiVisibility();
        ((FrameLayout) getWindow().getDecorView()).addView(contentView,
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        getOnBackPressedDispatcher().addCallback(onBackPressedCallback);
    }

    private void closeImage() {
        if (contentView != null) {
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars());
            ((FrameLayout) getWindow().getDecorView()).removeView(contentView);
            getWindow().getDecorView().setSystemUiVisibility(systemVisibility);
            contentView = null;
        }
        onBackPressedCallback.remove();
    }



    private void initFragments() {
        viewPager2.setAdapter(new MainViewPager2Adapter(this, FragmentPagerItems.with(this)
                .add("History", HistoryFragment.class)
                .add("Saved", SavedFragment.class)
                .add("Upscaled", UpscaledFragment.class)
                .create()));
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    nav_view.setSelectedItemId(R.id.navigation_history);
                    toolbar.setTitle("Generation History");
                } else if (position == 1) {
                    nav_view.setSelectedItemId(R.id.navigation_saved);
                    toolbar.setTitle("Saved Images");
                } else if (position == 2) {
                    nav_view.setSelectedItemId(R.id.navigation_upscaled);
                    toolbar.setTitle("Upscaled Images");
                }
            }
        });

        nav_view.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_history) {
                    viewPager2.setCurrentItem(0);
                    return true;
                } else if (item.getItemId() == R.id.navigation_saved) {
                    viewPager2.setCurrentItem(1);
                    return true;
                } else if (item.getItemId() == R.id.navigation_upscaled) {
                    viewPager2.setCurrentItem(2);
                    return true;
                }
                return false;
            }
        });
    }


    private void checkSaveCurrImg(GalleryModel galleryModel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            saveCurrImg(galleryModel);
            return;
        }
        if ((ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        ) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            saveCurrImg(galleryModel);
        }

    }


    private void saveCurrImg(GalleryModel galleryModel) {
        if (galleryModel != null && !TextUtils.isEmpty(galleryModel.path)) {
            GalleryModel gModel = new GalleryModel();
            gModel.prompt = galleryModel.prompt;
            gModel.negative_prompt = "";
            gModel.width = galleryModel.width;
            gModel.height = galleryModel.height;

            File file = new File(galleryModel.path);
            File destinationFile = new File(ApplicationPath.savePath(), System.currentTimeMillis() + ".jpg");

            if (file.exists()) {
                try {
                    copy(file, destinationFile);
                    gModel.path = destinationFile.getAbsolutePath();
                    if (databaseHelper.getSaved(gModel.path) == null) {
                        databaseHelper.addSaved(gModel);
                    }
                    Toast.makeText(this, "Image copied to permanent storage", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Utils.e(TAG, e.getMessage());
                    Toast.makeText(this, "Error: Couldn't save the image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (!result) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(GalleryActivity.this,
                                "android.permission.WRITE_EXTERNAL_STORAGE")) {
                            new MaterialAlertDialogBuilder(GalleryActivity.this)
                                    .setTitle(getString(R.string.app_name) + " needs permission")
                                    .setMessage("This app requires WRITE_EXTERNAL_STORAGE permission to save the image to permanent storage")
                                    .setPositiveButton("Give Permission", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            checkSaveCurrImg(currGalleryModel);
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();

                        }
                    } else {
                        saveCurrImg(currGalleryModel);
                    }
                }
            }
    );

    public static String getMimeType(String url) {
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (fileExtension != null) {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
        }
        return null;
    }

    OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            closeImage();
        }
    };

    private void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }



}
