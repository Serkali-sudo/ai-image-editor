package com.serhat.aieditor.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


import com.serhat.aieditor.GalleryActivity;
import com.serhat.aieditor.BeforeAfterSlider;
import com.serhat.aieditor.R;
import com.serhat.aieditor.Utils;
import com.serhat.aieditor.ZoomageView;
import com.serhat.aieditor.app.App;
import com.serhat.aieditor.app.ApplicationPath;
import com.serhat.aieditor.db.DatabaseHelper;
import com.serhat.aieditor.drawing.DrawView;
import com.serhat.aieditor.model.GalleryModel;
import com.serhat.aieditor.preference.Prefs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class DrawingActivity extends AppCompatActivity implements View.OnClickListener {

    private ZoomageView imageView;
    private TextInputEditText textInputEditText;

    private TextInputLayout textInputLayout;

    private MaterialCardView save_it_MC;

    private ImageButton run_IB;

    private LinearLayout big_pick_image_LL;

    private ProgressBar generating_PB;

    private LottieAnimationView scan_LT;

    private static final String enc = "6kNPSta21BCXUA95";
    private static final String vect = "1826C9GDB1912D23";

    private DrawView drawView;

    private static final String TAG = DrawingActivity.class.getSimpleName();

    private ImageView base_IV;

    private DatabaseHelper databaseHelper;

    private static final String AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";
    private static final String AES = "AES";


    private GalleryModel galleryModel;

    private boolean isGenerating = false;

    private Prefs prefs;
    private static final String FORMAT = "%02d:%02d:%02d";

    private Bitmap currOutBitmap;

    private String API_KEY = "";

    private float prevX, prevY;


//    private TextView nsfw_TV;

    private Bitmap currInputBitmap;

    private int imageWidth;
    private int imageHeight;

    private final Executor executor = Executors.newSingleThreadExecutor();


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.settings_IB) {
            openSettings();
        } else if (id == R.id.redo_IB) {
            drawView.redo();
        } else if (id == R.id.undo_IB) {
            drawView.undo();
        } else if (id == R.id.clear_IB) {
            MaterialAlertDialogBuilder newDialog = new MaterialAlertDialogBuilder(this);
            newDialog.setTitle("Clear the mask?");
            newDialog.setMessage("Clear your mask? You wont be able to bring it back.");
            newDialog.setPositiveButton("Yes", (dialog, which) -> {
                drawView.clearCanvas();
                Toast.makeText(this, "Cleared the mask!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
            newDialog.setNegativeButton("No", (dialog, which) -> dialog.cancel());
            newDialog.show();
        } else if (id == R.id.help_IB) {
            showTutorial();
        } else if (id == R.id.addImage_IB) {
            launchImagePicker();
        } else if (id == R.id.run_IB) {
//            imageView.setImageBitmap(drawView.save());
            generateImage(textInputEditText.getText().toString());
        } else if (id == R.id.big_add_image_IB) {
            launchImagePicker();
        }
//        else if (id == R.id.account_IB) {
//            createSignInIntent();
//        }
    }

    private void launchImagePicker() {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }


    private int adjustToDivisibleBy8(int dimension) {
        int roundedDimension = Math.round((float) dimension / 8) * 8;
        return Math.max(roundedDimension, 8);
    }


    ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                        if (inputStream != null) {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeStream(inputStream, null, options);
                            imageWidth = options.outWidth;
                            imageHeight = options.outHeight;
                            Utils.d(TAG, "Initial Dimensions: " + imageWidth + "x" + imageHeight);

                            if (imageWidth >= 1024 || imageHeight >= 1024) {
                                if (imageWidth >= 1664 || imageHeight >= 1664) {
                                    showCropDialog(uri, true);
                                } else {
                                    if (imageWidth % 32 == 0 && imageHeight % 32 == 0) {
                                        currInputBitmap = getImgFromUri(uri);
                                        if (currInputBitmap != null) {
                                            drawView.clearCanvas();
                                            big_pick_image_LL.setVisibility(View.GONE);
                                            base_IV.setImageBitmap(currInputBitmap);
                                            drawView.setUpCanvas(currInputBitmap.getWidth(), currInputBitmap.getHeight());
                                        } else {
                                            Toast.makeText(this, "Error: Image couldn't loaded", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        showCropDialog(uri, false);
                                    }
                                }
                            } else {
                                showCropDialog(uri, true);
                            }
                        }
                    } catch (IOException e) {
                        Utils.e(TAG, e.getMessage());
                    }


                }
            });

    private Bitmap getImgFromUri(Uri imageUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
            if (inputStream != null) {
                return BitmapFactory.decodeStream(inputStream);
            }
        } catch (IOException e) {
            Utils.e(TAG, e.getMessage());
        }
        return null;
    }

    public Bitmap scaleBitmap(Bitmap originalBitmap) {
        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();

        float aspectRatio = (float) originalWidth / originalHeight;

        int minDimension = 1024;

        int newWidth;
        int newHeight;

        if (aspectRatio > 1) {
            newWidth = minDimension;
            newHeight = (int) (minDimension / aspectRatio);
        } else {
            newHeight = minDimension;
            newWidth = (int) (minDimension * aspectRatio);
        }

        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
    }


    private void showCropDialog(Uri uri, boolean scale) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(DrawingActivity.this);
//        bottomSheetDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                BottomSheetDialog b = (BottomSheetDialog) dialog;
//                FrameLayout f = (FrameLayout) b.findViewById(com.google.android.material.R.id.design_bottom_sheet);
//                if (f != null) {
//                    BottomSheetBehavior.from(f).setState(BottomSheetBehavior.STATE_EXPANDED);
//                }
//            }
//        });
        View contentView = View.inflate(DrawingActivity.this, R.layout.crop_image, null);
        bottomSheetDialog.setContentView(contentView);
        CropImageView cropImageView = contentView.findViewById(R.id.cropImageView);
        Button cropButton = contentView.findViewById(R.id.cropBtn);
        ImageButton crop_info_IB = contentView.findViewById(R.id.crop_info_IB);
        LinearLayout loading_LL = contentView.findViewById(R.id.loading_LL);
        TextView title_TV = contentView.findViewById(R.id.title_TV);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = getImgFromUri(uri);
                if (bitmap != null) {
                    Bitmap scaleBitmap = scale ? scaleBitmap(bitmap) : bitmap;
                    Utils.d(TAG, "Scaled To:" + scaleBitmap.getWidth() + "x" + scaleBitmap.getHeight());

                    int imgWidth = scaleBitmap.getWidth();
                    int imgHeight = scaleBitmap.getHeight();

                    int maxCropWidth = imgWidth - (imgWidth % 32);
                    int maxCropHeight = imgHeight - (imgHeight % 32);
                    Utils.d(TAG, "Max Crop:" + maxCropWidth + "x" + maxCropHeight);
                    imageWidth = maxCropWidth;
                    imageHeight = maxCropHeight;
                    CropImageOptions cropOptions = new CropImageOptions();
                    cropOptions.guidelines = CropImageView.Guidelines.ON;
                    cropOptions.aspectRatioX = maxCropWidth;
                    cropOptions.aspectRatioY = maxCropHeight;
                    cropOptions.fixAspectRatio = true;
                    cropOptions.maxCropResultWidth = maxCropWidth;
                    cropOptions.minCropResultWidth = maxCropWidth;
                    cropOptions.maxCropResultHeight = maxCropHeight;
                    cropOptions.minCropResultHeight = maxCropHeight;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cropImageView.setImageCropOptions(cropOptions);
                            cropImageView.setImageBitmap(scaleBitmap);
                            loading_LL.setVisibility(View.GONE);
                            title_TV.setText("Auto Cropped");

                            FrameLayout f = (FrameLayout) bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                            if (f != null) {
                                BottomSheetBehavior.from(f).setState(BottomSheetBehavior.STATE_EXPANDED);
                            }
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loading_LL.setVisibility(View.GONE);
                            Toast.makeText(DrawingActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });


        crop_info_IB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createInfoWindow(v, "This image resolution is not right for" +
                        " the ai model so the app will auto crop it to right size." +
                        "\nAnd if one of the image dimensions is bigger than 1644px " +
                        "then the app will downscale the image back to 1024px." +
                        "\nAlso if both of the image dimensions is smaller than 1024px then the app will scale it" +
                        " to 1024 without disrupting aspect ratio.");
            }
        });

        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap croppedBitmap = cropImageView.getCroppedImage();
                if (croppedBitmap != null) {
                    currInputBitmap = croppedBitmap;
                    drawView.clearCanvas();
                    big_pick_image_LL.setVisibility(View.GONE);
                    base_IV.setImageBitmap(currInputBitmap);
                    drawView.setUpCanvas(currInputBitmap.getWidth(), currInputBitmap.getHeight());
                    Utils.d(TAG, "Cropped To:" + croppedBitmap.getWidth() + "x" + croppedBitmap.getHeight());
                    bottomSheetDialog.dismiss();
                } else {
                    Toast.makeText(DrawingActivity.this, "Error: Image couldn't cropped.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bottomSheetDialog.show();

    }


    private void saveImage() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View contentView = View.inflate(this, R.layout.save_image, null);
        bottomSheetDialog.setContentView(contentView);
        MaterialCardView down_orig_MC = contentView.findViewById(R.id.down_orig_MC);
        MaterialCardView upscale_MC = contentView.findViewById(R.id.upscale_MC);

        down_orig_MC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                saveCurrImg();
            }
        });
        upscale_MC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUpscaleSettings();
                bottomSheetDialog.dismiss();

            }
        });


        bottomSheetDialog.show();

    }

    private void checkAndSave() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            saveImage();
            return;
        }
        if ((ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        ) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            saveImage();
        }

    }


    private void saveCurrImg() {
        String filename = System.currentTimeMillis() + ".jpg";
        File imageFile = new File(ApplicationPath.savePath(), filename);
        try {
            FileOutputStream outStream = new FileOutputStream(imageFile);
            currOutBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
            GalleryModel gModel = new GalleryModel();
            gModel.prompt = galleryModel.prompt;
            gModel.negative_prompt = "";
            gModel.width = galleryModel.width;
            gModel.height = galleryModel.height;
            gModel.path = imageFile.getAbsolutePath();
            Utils.i(TAG, "DB Image Path:" + gModel.path);
            if (databaseHelper.getSaved(gModel.path) == null) {
                databaseHelper.addSaved(gModel);
                Toast.makeText(DrawingActivity.this, "Image Saved", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Utils.i(TAG, e.getMessage());
        }
    }

    private void openUpscaleSettings() {
        if (TextUtils.isEmpty(API_KEY)) {
            Toast.makeText(this, "No api key found", Toast.LENGTH_LONG).show();
            showAPIkeyDialog();
            return;
        }

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog b = (BottomSheetDialog) dialog;
                FrameLayout f = (FrameLayout) b.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (f != null) {
                    BottomSheetBehavior.from(f).setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });
        View contentView = View.inflate(this, R.layout.upscale, null);
        bottomSheetDialog.setContentView(contentView);
        final ImageView[] prew_IV = {contentView.findViewById(R.id.prew_IV)};
        Spinner model_spinner = contentView.findViewById(R.id.model_spinner);
        Spinner scale_spinner = contentView.findViewById(R.id.scale_spinner);
        SwitchMaterial face_upscale_SV = contentView.findViewById(R.id.face_upscale_SV);
        MaterialCardView upscale_image_MC = contentView.findViewById(R.id.upscale_image_MC);
        ImageButton upscale_model_info_IB = contentView.findViewById(R.id.upscale_model_info_IB);
        ImageButton rescale_info_IB = contentView.findViewById(R.id.rescale_info_IB);
        ImageButton face_info_IB = contentView.findViewById(R.id.face_info_IB);

        upscale_model_info_IB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createInfoWindow(v, "Select a model that fits your style and scale needs.");
            }
        });
        rescale_info_IB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createInfoWindow(v, "This settings determines image resolution." +
                        "\nFor example lets say you have 512x512 image and you selected rescale factor of 2" +
                        " Your upscaled image's resolution will be 1024x1024.\nIf you pick 1 then your resolution will stay same.");
            }
        });

        face_info_IB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createInfoWindow(v, "If your image contains a blurry face enable this setting.");
            }
        });


        prew_IV[0].setImageBitmap(currOutBitmap);

        final String[] modelName = {"RealESRGAN_x4plus"};
        final int[] rescale_factor = {2};
        final boolean[] face = {false};

        List<String> model_arr = Arrays.asList(getResources().getStringArray(R.array.upscaling_model_array));
        ArrayAdapter<String> models = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, model_arr);
        model_spinner.setAdapter(models);
        model_spinner.setSelection(0);
        model_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                modelName[0] = models.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        List<String> upscale_arr = Arrays.asList(getResources().getStringArray(R.array.rescale_array));
        ArrayAdapter<String> scale = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, upscale_arr);
        scale_spinner.setAdapter(scale);
        scale_spinner.setSelection(1);
        scale_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                rescale_factor[0] = Integer.parseInt(scale.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        face_upscale_SV.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                face[0] = isChecked;
            }
        });

        upscale_image_MC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upscaleWithAPI(modelName[0], rescale_factor[0], face[0]);
                bottomSheetDialog.dismiss();
            }
        });


        bottomSheetDialog.show();


    }


    private void upscaleWithAPI(String model, int rescale_factor, boolean face) {
        AlertDialog progressDialog = new MaterialAlertDialogBuilder(this).create();
        View contentView = View.inflate(this, R.layout.progress_dialog, null);
        TextView loading_TV = contentView.findViewById(R.id.loadingTV);
        loading_TV.setText("Upscaling the image");
        progressDialog.setView(contentView);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        currOutBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String base64Image = "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);


        JSONObject postData = new JSONObject();
        try {
            postData.put("image_url", base64Image);
            postData.put("model", model);
            postData.put("scale", rescale_factor);
            postData.put("face", face);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                    "https://fal.run/fal-ai/esrgan", postData,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Utils.i(TAG, response.toString());

                                JSONObject imageObject = response.getJSONObject("image");

                                String image_url = imageObject.getString("url");

                                int imageWidth = imageObject.getInt("width");
                                int imageHeight = imageObject.getInt("height");


                                prefs.setInt("up_pp", (prefs.getInt("up_pp", 0) - 1));

                                long currentExpire = prefs.getLong("up_expire", 0);
                                long milli = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24);
                                if (currentExpire == 0) {
                                    prefs.setLong("up_expire", milli);
                                }


                                Glide.with(DrawingActivity.this).asBitmap().load(image_url)
                                        .into(new CustomTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                String filename = "Upscaled_" + System.currentTimeMillis() + ".png";
                                                File imageFile = new File(ApplicationPath.upscalePath(), filename);
                                                try {
                                                    FileOutputStream outStream = new FileOutputStream(imageFile);
                                                    resource.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                                                    outStream.flush();
                                                    outStream.close();
                                                    GalleryModel gModel = new GalleryModel();
                                                    gModel.prompt = "";
                                                    gModel.negative_prompt = "";
                                                    gModel.width = imageWidth;
                                                    gModel.height = imageHeight;
                                                    gModel.path = imageFile.getAbsolutePath();
                                                    Utils.i(TAG, "DB Image Path:" + gModel.path);
                                                    if (databaseHelper.getUpscaled(gModel.path) == null) {
                                                        databaseHelper.addUpscaled(gModel);
                                                        Toast.makeText(DrawingActivity.this, "Upscaled Image Saved", Toast.LENGTH_SHORT).show();
                                                        if (progressDialog.isShowing()) {
                                                            progressDialog.dismiss();
                                                        }
                                                        showBeforeAfterDialog(currOutBitmap, resource);
                                                    }
                                                } catch (IOException e) {
                                                    Utils.i(TAG, e.getMessage());
                                                }
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {

                                            }
                                        });


                            } catch (JSONException e) {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                Toast.makeText(DrawingActivity.this, "Error (JSONException): " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

//                        System.out.println("Response: " + response.toString());
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Utils.i(TAG, "Volley Error:" + error.getMessage());

                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", API_KEY);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    40000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                Volley.newRequestQueue(DrawingActivity.this).add(jsonObjectRequest);
            } else {
                newRequestNoSSL().add(jsonObjectRequest);
            }


        } catch (Exception e) {
            Utils.e(TAG, e.getMessage());
        }


    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (!result) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(DrawingActivity.this,
                                "android.permission.WRITE_EXTERNAL_STORAGE")) {
                            new MaterialAlertDialogBuilder(DrawingActivity.this)
                                    .setTitle(getString(R.string.app_name) + " needs permission")
                                    .setMessage("This app requires WRITE_EXTERNAL_STORAGE permission to save the image to permanent storage")
                                    .setPositiveButton("Give Permission", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            checkAndSave();
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
                        saveImage();
                    }
                }
            }
    );


    @SuppressLint("ClickableViewAccessibility")
    private void setUpFloatingSave() {
        save_it_MC = findViewById(R.id.save_it_MC);
        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                if (currOutBitmap != null) {
                    checkAndSave();
                } else {
                    Toast.makeText(DrawingActivity.this, "No Image Found!", Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });
        save_it_MC.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                gestureDetector.onTouchEvent(event);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        prevX = event.getRawX();
                        prevY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float moveX = event.getRawX() - prevX;
                        save_it_MC.setX(save_it_MC.getX() + moveX);
                        prevX = event.getRawX();
                        float moveY = event.getRawY() - prevY;
                        save_it_MC.setY(save_it_MC.getY() + moveY);
                        prevY = event.getRawY();
                        float width = imageView.getMeasuredWidth();
                        float height = imageView.getMeasuredHeight();
                        if ((save_it_MC.getX() + save_it_MC.getWidth()) >= width
                                || save_it_MC.getX() <= 0) {
                            save_it_MC.setX(save_it_MC.getX() - moveX);
                        }
                        if ((save_it_MC.getY() + save_it_MC.getHeight()) >= height
                                || save_it_MC.getY() <= 0) {
                            save_it_MC.setY(save_it_MC.getY() - moveY);
                        }
                        break;
                }

                return true;
            }
        });
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawing_activity);


        prefs = Prefs.getInstance(getApplicationContext());

        databaseHelper = DatabaseHelper.getInstance(getApplicationContext());

        String key = prefs.getString("api_key", "");
        if (!TextUtils.isEmpty(key)) {
            API_KEY = decrypt(key);
        } else {
            showAPIkeyDialog();
        }

        if (!prefs.getBoolean("is_tutorial_showed", false)) {
            showTutorial();
            prefs.setBoolean("is_tutorial_showed", true);
        }


        MaterialToolbar materialToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(materialToolbar);
//        account_IB = findViewById(R.id.account_IB);
        imageView = findViewById(R.id.imageView);
        textInputLayout = findViewById(R.id.textInputLayout);
        textInputEditText = findViewById(R.id.textInputEditText);
        ImageButton settings_IB = findViewById(R.id.settings_IB);
        ImageButton help_IB = findViewById(R.id.help_IB);
//        nsfw_TV = findViewById(R.id.nsfw_TV);

        settings_IB.setOnClickListener(this);
        help_IB.setOnClickListener(this);

        setUpFloatingSave();

//        account_IB.setOnClickListener(this);

        setUpPaintViews();


//        queryUserByUUID(fAuth.getCurrentUser().getUid());


        textInputEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (TextUtils.isEmpty(textInputEditText.getText().toString())) {
                    textInputLayout.setError("Prompt is empty. Please write a prompt");
                } else {
                    textInputLayout.setErrorEnabled(false);
                    Utils.hideKeyboard(DrawingActivity.this);
                    textInputEditText.clearFocus();
                }
                return false;
            }
        });


        ViewTreeObserver vto = drawView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                drawView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                drawView.setUpCanvas(512, 512);
//                setCanvasRatio(prefs.getInt("ratio_value", 0));
            }
        });


    }


    private void setUpPaintViews() {
        drawView = findViewById(R.id.draw_view);
        ImageButton undo_IB = findViewById(R.id.undo_IB);
        ImageButton redo_IB = findViewById(R.id.redo_IB);
        ImageButton clear_IB = findViewById(R.id.clear_IB);
        base_IV = findViewById(R.id.base_IV);
        ImageButton add_image_IB = findViewById(R.id.addImage_IB);
        run_IB = findViewById(R.id.run_IB);
        ImageButton big_add_image_IB = findViewById(R.id.big_add_image_IB);
        big_pick_image_LL = findViewById(R.id.big_pick_image_LL);
        scan_LT = findViewById(R.id.scan_LT);
        generating_PB = findViewById(R.id.generating_PB);
        undo_IB.setOnClickListener(this);
        redo_IB.setOnClickListener(this);
        clear_IB.setOnClickListener(this);
        add_image_IB.setOnClickListener(this);
        run_IB.setOnClickListener(this);
        big_add_image_IB.setOnClickListener(this);
    }


    private void openSettings() {
        BottomSheetDialog bottomSheetDialog =
                new BottomSheetDialog(DrawingActivity.this);
        bottomSheetDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog b = (BottomSheetDialog) dialog;
                FrameLayout f = (FrameLayout)
                        b.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (f != null) {
                    BottomSheetBehavior.from(f).setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });

        View contentView = View.inflate(DrawingActivity.this,
                R.layout.settings, null);


        TextInputLayout neg_prompt_IL = contentView.findViewById(R.id.neg_prompt_IL);
        TextInputEditText neg_prompt_ET = contentView.findViewById(R.id.neg_prompt_ET);
        MaterialSwitch neg_prompt_SV = contentView.findViewById(R.id.neg_prompt_SV);
        MaterialSwitch save_history_SM = contentView.findViewById(R.id.save_history_SM);

        TextInputLayout custom_seed_IL = contentView.findViewById(R.id.custom_seed_IL);
        TextInputEditText custom_seed_ET = contentView.findViewById(R.id.custom_seed_ET);
        MaterialSwitch custom_seed_SV = contentView.findViewById(R.id.custom_seed_SV);

        TextInputLayout strength_IL = contentView.findViewById(R.id.strength_IL);
        TextInputEditText strength_ET = contentView.findViewById(R.id.strength_ET);
        RangeSlider strength_RS = contentView.findViewById(R.id.strength_RS);

        TextInputLayout api_key_IL = contentView.findViewById(R.id.api_key_IL);
        TextInputEditText api_key_ET = contentView.findViewById(R.id.api_key_ET);
        ImageButton api_secret_IB = contentView.findViewById(R.id.api_secret_IB);


        ImageButton negative_prompt_info_IB = contentView.findViewById(R.id.negative_prompt_info_IB);
        ImageButton custom_seed_info_IB = contentView.findViewById(R.id.custom_seed_info_IB);
        ImageButton save_history_info_IB = contentView.findViewById(R.id.save_history_info_IB);
        ImageButton strength_info_IB = contentView.findViewById(R.id.strength_info_IB);
        ImageButton api_key_info_IB = contentView.findViewById(R.id.api_key_info_IB);

        String neg_prompt = prefs.getString("neg_prompt", "");
        if (!TextUtils.isEmpty(neg_prompt)) {
            neg_prompt_ET.setText(neg_prompt);
        }

        if (prefs.getBoolean("is_negative", false)) {
            neg_prompt_SV.setChecked(true);
            neg_prompt_IL.setEnabled(true);
        }


        long seed = prefs.getLong("custom_seed", 42);

        custom_seed_ET.setText(String.valueOf(seed));

        if (!TextUtils.isEmpty(API_KEY)) {
            api_key_ET.setText(API_KEY);
        }


        float strength = prefs.getFloat("strength", 0.95f);
        strength_ET.setText(String.valueOf(strength));

        if (!prefs.getBoolean("is_custom_seed", false)) {
            custom_seed_SV.setChecked(false);
            custom_seed_IL.setEnabled(false);
        }


        negative_prompt_info_IB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createInfoWindow(v, "Negative prompt is a prompt for what you don't want to see in the result." +
                        "\n For example: 'blurry, ugly ...' .");
            }
        });

        custom_seed_info_IB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createInfoWindow(v, "Seeds are numerical values used to determine the starting point of image generation.\n" +
                        "Controlling the seed helps you generate reproducible images.\n" +
                        "If disabled every image will be generated with random seed.");
            }
        });
        strength_info_IB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createInfoWindow(v, "Determines how much the generated image resembles the initial image.\nDefault value: 0.95");
            }
        });


        save_history_info_IB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createInfoWindow(v, "Disable this if you don't want to save every image generated to your device.");
            }
        });

        api_key_info_IB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createInfoWindow(v, "This app requires an API key from fal.ai to function.\nAfter all this is just a client app.");
            }
        });


        neg_prompt_ET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                prefs.setString("neg_prompt", v.getText().toString());
                Utils.hideKeyboard(DrawingActivity.this);
                neg_prompt_ET.clearFocus();
                return false;
            }
        });


        neg_prompt_SV.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                neg_prompt_IL.setEnabled(isChecked);
                prefs.setBoolean("is_negative", isChecked);
            }
        });


        api_key_ET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String key = v.getText().toString();
                if (!TextUtils.isEmpty(key)) {
                    API_KEY = key;
                    prefs.setString("api_key", encrypt(key));
                    Utils.hideKeyboard(DrawingActivity.this);
                    api_key_ET.clearFocus();
                    api_key_IL.setErrorEnabled(false);
                } else {
                    api_key_IL.setError("API key cannot be empty");
                }
                return false;
            }
        });

        api_secret_IB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!v.isSelected()) {
                    api_key_ET.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    api_secret_IB.setImageResource(R.drawable.eye_line);
                } else {
                    api_key_ET.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    api_secret_IB.setImageResource(R.drawable.eye_close_line);
                }
                v.setSelected(!v.isSelected());

            }
        });


        custom_seed_ET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (!TextUtils.isEmpty(Objects.requireNonNull(v.getText()).toString())) {
                    if (Long.parseLong(v.getText().toString()) > 0) {
                        prefs.setLong("custom_seed", Long.parseLong(v.getText().toString()));
                        Utils.hideKeyboard(DrawingActivity.this);
                        custom_seed_ET.clearFocus();
                        custom_seed_IL.setErrorEnabled(false);
                    } else {
                        custom_seed_IL.setError("Seed must be bigger than 0");
                    }
                } else {
                    custom_seed_IL.setError("Seed cannot be empty");
                }

                return false;
            }
        });

        custom_seed_SV.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                custom_seed_IL.setEnabled(isChecked);
                prefs.setBoolean("is_custom_seed", isChecked);
            }
        });


        strength_RS.setValues(strength);
        strength_RS.setValueFrom(0.1f);
        strength_RS.setValueTo(1f);
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        strength_RS.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull RangeSlider rangeSlider, float v, boolean b) {
                try {
                    float value = Float.parseFloat(decimalFormat.format(v));
                    prefs.setFloat("strength", value);
                    strength_ET.setText(String.valueOf(value));
                } catch (NumberFormatException e) {
                    Utils.e(TAG, e.getMessage());
                }
            }
        });

//        strength_ET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (!TextUtils.isEmpty(v.getText().toString())) {
//                    float strength = Float.parseFloat(v.getText().toString());
//                    if (strength > 0f) {
//                        prefs.setFloat("strength", strength);
//                        strength_RS.setValues(strength);
//                        Utils.hideKeyboard(DrawingActivity.this);
//                        strength_ET.clearFocus();
//                        strength_IL.setErrorEnabled(false);
//                    } else {
//                        strength_IL.setError("Strength must be bigger than 0");
//                    }
//                } else {
//                    strength_IL.setError("Please choose a strength between 0-1");
//                }
//
//                return false;
//            }
//        });


        save_history_SM.setChecked(prefs.getBoolean("save_history", true));

        save_history_SM.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.setBoolean("save_history", isChecked);
            }
        });


        bottomSheetDialog.setContentView(contentView);
        bottomSheetDialog.show();
    }


    private void showAPIkeyDialog() {
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(this).create();
        alertDialog.setTitle("Enter API Key");
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        View contentView = View.inflate(this, R.layout.input_dialog, null);
        TextInputLayout api_key_IL = contentView.findViewById(R.id.api_key_IL);
        TextInputEditText api_key_ET = contentView.findViewById(R.id.api_key_ET);


        alertDialog.setView(contentView, 20, 20, 20, 20);

        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Get api key", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://fal.ai/"));
                startActivity(intent);
            }
        });

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Set it", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String key = api_key_ET.getText().toString();
                if (!TextUtils.isEmpty(key)) {
                    api_key_IL.setErrorEnabled(false);
                    API_KEY = key;
                    prefs.setString("api_key", encrypt(key));
                    Toast.makeText(DrawingActivity.this, "API key saved.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(DrawingActivity.this, "No api key set", Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialog.show();

    }


    private void createInfoWindow(View view, String info) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.info_layout, null);
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setContentView(popupView);
        TextView info_TV = popupView.findViewById(R.id.info_TV);
        info_TV.setText(info);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            popupView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                }
            });
        }


        popupWindow.showAsDropDown(view);
    }

    public String getBase64FromBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return "data:image/png;base64," + Base64.encodeToString(byteArray,
                    Base64.DEFAULT);
        }
        return null;
    }

    private void updateLoadingAnimations() {
        if (isGenerating) {
            run_IB.setVisibility(View.GONE);
            generating_PB.setVisibility(View.VISIBLE);
            scan_LT.setVisibility(View.VISIBLE);
            scan_LT.playAnimation();
        } else {
            generating_PB.setVisibility(View.GONE);
            run_IB.setVisibility(View.VISIBLE);
            scan_LT.setVisibility(View.GONE);
            scan_LT.cancelAnimation();
        }
    }

    private void generateImage(String prompt) {
        if (!isGenerating) {

            if (TextUtils.isEmpty(API_KEY)) {
                Toast.makeText(this, "No api key found", Toast.LENGTH_LONG).show();
                showAPIkeyDialog();
                return;
            }

            if (currInputBitmap == null) {
                Toast.makeText(this, "You have to add an image first", Toast.LENGTH_SHORT).show();
                return;
            }
            if (drawView.isCanvasClear()) {
                Toast.makeText(this, "Please paint the part you want to edit in the image", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(prompt)) {
                textInputLayout.setError("Prompt is empty. Please write a prompt");
                return;
            }

            isGenerating = true;
            updateLoadingAnimations();


            executor.execute(new Runnable() {
                @Override
                public void run() {
                    JSONObject postData = new JSONObject();
                    try {
                        postData.put("enable_safety_checks", false);
                        postData.put("prompt", prompt);
                        if (prefs.getBoolean("is_negative", false)) {
                            String neg_prompt = prefs.getString("neg_prompt", "");
                            if (!TextUtils.isEmpty(neg_prompt)) {
                                postData.put("negative_prompt", neg_prompt);
                            }
                        }

                        JSONObject imageSize = new JSONObject();
                        imageSize.put("width", imageWidth);
                        imageSize.put("height", imageHeight);
                        postData.put("image_size", imageSize);
                        Utils.d(TAG, "Used Dimensions: " + imageWidth + "x" + imageHeight);

                        postData.put("image_url", getBase64FromBitmap(currInputBitmap));
                        postData.put("mask_url", drawView.captureCanvasAsBase64());
                        postData.put("sync_mode", true);
//                        postData.put("num_inference_steps", 8);
                        postData.put("num_images", 1);

                        if (prefs.getBoolean("is_custom_seed", false)) {
                            long seed = prefs.getLong("custom_seed", 42);
                            postData.put("seed", seed);
                        }
                        postData.put("strength", prefs.getFloat("strength", 0.95f));

                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                                "https://fal.run/fal-ai/fast-lightning-sdxl/inpainting", postData,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            Utils.d(TAG, "Full Result:" + response.toString());
                                            if (response.has("images")) {
                                                JSONArray imagesArray = response.getJSONArray("images");

                                                JSONObject imageObject = imagesArray.getJSONObject(0);
                                                String imageUrl = imageObject.getString("url");
                                                int imageWidth = imageObject.getInt("width");
                                                int imageHeight = imageObject.getInt("height");

                                                String seed = response.getString("seed");

                                                executor.execute(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        final String pureBase64Encoded = imageUrl.substring(imageUrl.indexOf(",") + 1);
                                                        byte[] imageByteArray = Base64.decode(pureBase64Encoded, Base64.DEFAULT);

                                                        currOutBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);

                                                        galleryModel = new GalleryModel();
                                                        galleryModel.prompt = prompt;
                                                        galleryModel.negative_prompt = "";

                                                        if (prefs.getBoolean("is_negative", false)) {
                                                            String neg_prompt = prefs.getString("neg_prompt", "");
                                                            if (!TextUtils.isEmpty(neg_prompt)) {
                                                                galleryModel.negative_prompt = neg_prompt;
                                                            }
                                                        }
                                                        galleryModel.width = imageWidth;
                                                        galleryModel.height = imageHeight;
                                                        galleryModel.seed = seed;


                                                        if (prefs.getBoolean("save_history", true)) {
                                                            try {
                                                                File cacheDir = getExternalFilesDir("history");
                                                                if (cacheDir != null) {
                                                                    File result = new File(cacheDir, "generation_history");
                                                                    if (result.isDirectory() || result.mkdirs()) {
                                                                        String filename = System.currentTimeMillis() + ".jpg";
                                                                        File imageFile = new File(result, filename);
                                                                        try {
                                                                            FileOutputStream outStream = new FileOutputStream(imageFile);
                                                                            currOutBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                                                                            outStream.flush();
                                                                            outStream.close();
                                                                            galleryModel.path = imageFile.getAbsolutePath();
                                                                            Utils.i(TAG, "DB Image Path:" + galleryModel.path);
                                                                            if (databaseHelper.getHistory(galleryModel.path) == null) {
                                                                                databaseHelper.addHistory(galleryModel);
                                                                            }
                                                                        } catch (
                                                                                IOException e) {
                                                                            Utils.i(TAG, e.getMessage());
                                                                        }

                                                                    }

                                                                }
                                                            } catch (Exception e) {
                                                                Utils.e(TAG, e.getMessage());
                                                            }
                                                        }
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                imageView.setImageBitmap(currOutBitmap);
                                                                isGenerating = false;
                                                                updateLoadingAnimations();
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        } catch (JSONException e) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(DrawingActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        if(error.getMessage() != null){
                                            Utils.e(TAG, error.getMessage());
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                isGenerating = false;
                                                updateLoadingAnimations();
                                                Toast.makeText(DrawingActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }
                                }) {
                            @Override
                            public Map<String, String> getHeaders() {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Authorization", "Key " + API_KEY);
                                headers.put("Content-Type", "application/json");
                                return headers;
                            }
                        };
                        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                                20000,
                                0,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        // Below android 7.1 regular volley request fails due to ssl issues with the api
                        // So if device api is below android 7.1 it accepts everything
                        // Not very secure for devices below 7.1
                        // i should probably try implement it with okhttp or something maybe it wont have this problem.
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                            Volley.newRequestQueue(DrawingActivity.this).add(jsonObjectRequest);
                        } else {
                            newRequestNoSSL().add(jsonObjectRequest);
                        }
                    } catch (Exception e) {
                        Utils.e(TAG, e.getMessage());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                isGenerating = false;
                                updateLoadingAnimations();
                            }
                        });

                    }
                }
            });
        } else {
            Toast.makeText(this, "Already generating image.", Toast.LENGTH_SHORT).show();
        }


    }


    private RequestQueue newRequestNoSSL() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, null);

            HurlStack hurlStack = new HurlStack(null, sslContext.getSocketFactory());

            return Volley.newRequestQueue(DrawingActivity.this, hurlStack);

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            Utils.e(TAG, e.getMessage());
            return Volley.newRequestQueue(DrawingActivity.this);
        }
    }

    public static String encrypt(String input) {
        try {
            Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
            SecretKeySpec keySpec = new SecretKeySpec(enc.getBytes(StandardCharsets.UTF_8), AES);
            IvParameterSpec ivSpec = new IvParameterSpec(vect.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encryptedBytes = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
        } catch (Exception e) {
            Utils.e(TAG, e.getMessage());
            return null;
        }
    }

    public static String decrypt(String encryptedInput) {
        try {
            Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
            SecretKeySpec keySpec = new SecretKeySpec(enc.getBytes(StandardCharsets.UTF_8), AES);
            IvParameterSpec ivSpec = new IvParameterSpec(vect.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] encryptedBytes = Base64.decode(encryptedInput, Base64.DEFAULT);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Utils.e(TAG, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.history) {
            Intent hgm = new Intent(this, GalleryActivity.class);
            hgm.putExtra("isBookmark", 0);
            mainActivityResultLauncher.launch(hgm);
        } else if (id == R.id.saved) {
            Intent hgm = new Intent(this, GalleryActivity.class);
            hgm.putExtra("isBookmark", 1);
            mainActivityResultLauncher.launch(hgm);
        } else if (id == R.id.upscale) {
            Intent hgm = new Intent(this, GalleryActivity.class);
            hgm.putExtra("isBookmark", 2);
            mainActivityResultLauncher.launch(hgm);
        } else if (id == R.id.settings_menu) {
            startActivity(new Intent(this, PrefsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }


    public ActivityResultLauncher<Intent> mainActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                }
            });


    @Override
    protected void onDestroy() {
        if (currInputBitmap != null) {
            currInputBitmap.recycle();
        }
        if (currOutBitmap != null) {
            currOutBitmap.recycle();
        }
        super.onDestroy();
    }


    private void showBeforeAfterDialog(Bitmap foregroundBitmap, Bitmap backgroundBitmap) {
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(this).create();

        View contentView = View.inflate(this, R.layout.upscale_compare_dialog, null);
        BeforeAfterSlider beforeAfterSlider = contentView.findViewById(R.id.before_after_slider);
        beforeAfterSlider.setBack(backgroundBitmap);
        beforeAfterSlider.setFore(foregroundBitmap);
//        beforeAfterSlider.setTestImages();

        alertDialog.setView(contentView);
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }


    private void showTutorial() {
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(this).create();
        alertDialog.setTitle("Tutorial");
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        View contentView = View.inflate(this, R.layout.layout_image, null);
        alertDialog.setView(contentView);
        ImageView gif_IV = contentView.findViewById(R.id.gif_IV);
        Glide.with(this).asGif().load(R.raw.tut2).into(gif_IV);
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();

    }


}

