package com.streamhash.streamview.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.streamhash.streamview.R;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIConstants;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.util.AppUtils;
import com.streamhash.streamview.util.GlideApp;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.util.UiUtils;
import com.streamhash.streamview.util.sharedpref.PrefKeys;
import com.streamhash.streamview.util.sharedpref.PrefUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.streamhash.streamview.network.APIConstants.*;

public class SubProfileEditActivity extends BaseActivity {


    public static final String ID = "subProfileIdUnderChange";
    public static final String NAME = "userName";
    public static final String PICTURE = "picture";
    public static final String COUNT = "count";
    public static final String IS_EDITING = "isEditing";
    private static final int PICK_IMAGE = 100;
    String name = "";
    String picture = "";
    int subProfileIdUnderChange;

    @BindView(R.id.subProfileName)
    EditText subProfileName;
    @BindView(R.id.deleteSubProfile)
    View deleteSubProfile;
    @BindView(R.id.subProfilePicture)
    ImageView subProfilePicture;


    APIInterface apiInterface;
    PrefUtils prefUtils;
    private Uri fileToUpload = null;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_sub_profile);
        ButterKnife.bind(this);
        apiInterface = APIClient.getClient().create(APIInterface.class);
        prefUtils = PrefUtils.getInstance(this);
        //Permission check
        AppUtils.permissionCheck(this);

        Intent intent = getIntent();
        if (intent != null) {
            isEditMode = intent.getBooleanExtra(IS_EDITING, false);
            subProfileIdUnderChange = intent.getIntExtra(ID, 0);
            deleteSubProfile.setVisibility(isEditMode && subProfileIdUnderChange != 0 ? View.VISIBLE : View.GONE);
            name = intent.getStringExtra(NAME);
            picture = intent.getStringExtra(PICTURE);
            setUpDataOnToViews();
        } else {
            UiUtils.showShortToast(this, getString(R.string.something_went_wrong));
            finish();
        }
    }

    private void setUpDataOnToViews() {
        try {
            subProfileName.setText(subProfileIdUnderChange == 0 ? null : name);
            subProfileName.setHint(subProfileIdUnderChange == 0 ? getString(R.string.enter_a_name) : null);
            if (subProfileIdUnderChange != 0)
                subProfileName.setSelection(name.length());
            GlideApp.with(this)
                    .load(picture)
                    .into(subProfilePicture);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private RequestBody getPartFor(String stuff) {
        return RequestBody.create(okhttp3.MultipartBody.FORM, stuff);
    }

    @OnClick({R.id.cancelEdit, R.id.saveEdit, R.id.editPicture, R.id.deleteSubProfile})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cancelEdit:
                finish();
                break;
            case R.id.saveEdit:
                if (validateFields()) {
                    if (!isEditMode || subProfileIdUnderChange == 0) {
                        addSubProfile();
                    } else {
                        editSubProfile();
                    }
                }
                break;
            case R.id.editPicture:
                callImagePicker();
                break;
            case R.id.deleteSubProfile:
                deleteSubProfileConfirm();
                break;
        }
    }

    private void deleteSubProfileConfirm() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirmation)
                .setMessage("Are you sure to delete your Sub profile?")
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    UiUtils.showLoadingDialog(this);
                    Call<String> call = apiInterface.deleteSubProfile(id, token, subProfileId, subProfileIdUnderChange);
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            UiUtils.hideLoadingDialog();
                            JSONObject subProfileDeleteResponse = null;
                            try {
                                subProfileDeleteResponse = new JSONObject(response.body());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (subProfileDeleteResponse != null) {
                                if (subProfileDeleteResponse.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                                    PrefUtils.getInstance(SubProfileEditActivity.this).removeKey(PrefKeys.ACTIVE_SUB_PROFILE);
                                    UiUtils.showShortToast(SubProfileEditActivity.this, subProfileDeleteResponse.optString(Params.MESSAGE) + ". Restarting the app..");
                                    restartApp();
                                } else {
                                    UiUtils.showShortToast(SubProfileEditActivity.this, subProfileDeleteResponse.optString(Params.ERROR_MESSAGE));
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            UiUtils.hideLoadingDialog();
                            NetworkUtils.onApiError(SubProfileEditActivity.this);
                        }
                    });
                })
                .setNegativeButton(getString(R.string.no), null)
                .setIcon(R.mipmap.ic_launcher)
                .create().show();
    }

    private boolean validateFields() {
        if (subProfileName.getText().toString().length() == 0) {
            UiUtils.showShortToast(this, getString(R.string.names_cant_be_empty));
            return false;
        }
        return true;
    }

    private void editSubProfile() {
        UiUtils.showLoadingDialog(this);

        MultipartBody.Part multipartBody = null;

        if (fileToUpload != null) {
            String filePath = getRealPathFromURIPath(fileToUpload, this);
            File file = new File(filePath);
            Timber.d("Filename %s", file.getName());

            // create RequestBody instance tempFrom file
            RequestBody requestFile =
                    RequestBody.create(MediaType.parse("image/*"), file);

            // MultipartBody.Part is used to send also the actual file name
            multipartBody =
                    MultipartBody.Part.createFormData(Params.PICTURE, file.getAbsolutePath(), requestFile);
        }

        Call<String> call = apiInterface.editSubProfile(getPartFor(String.valueOf(id))
                , getPartFor(token)
                , getPartFor(String.valueOf(subProfileIdUnderChange))
                , getPartFor(subProfileName.getText().toString())
                , multipartBody);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject updateProfileResponse = null;
                try {
                    updateProfileResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (updateProfileResponse != null)
                    if (updateProfileResponse.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                        UiUtils.showShortToast(SubProfileEditActivity.this, updateProfileResponse.optString(Params.MESSAGE));
                        finish();
                    } else {
                        UiUtils.showShortToast(SubProfileEditActivity.this, updateProfileResponse.optString(Params.ERROR_MESSAGE));
                    }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(SubProfileEditActivity.this);
            }
        });
    }

    private void addSubProfile() {
        UiUtils.showLoadingDialog(this);

        MultipartBody.Part multipartBody = null;

        if (fileToUpload != null) {
            String filePath = getRealPathFromURIPath(fileToUpload, this);
            File file = new File(filePath);

            // create RequestBody instance tempFrom file
            RequestBody requestFile =
                    RequestBody.create(MediaType.parse("image/*"), file);

            // MultipartBody.Part is used to send also the actual file name
            multipartBody =
                    MultipartBody.Part.createFormData(Params.PICTURE, file.getAbsolutePath(), requestFile);

        }

        Call<String> call = apiInterface.addSubProfile(getPartFor(String.valueOf(id))
                , getPartFor(token)
                , getPartFor(subProfileName.getText().toString())
                , multipartBody);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject addSubProfileResponse = null;
                try {
                    addSubProfileResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (addSubProfileResponse != null)
                    if (addSubProfileResponse.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                        UiUtils.showShortToast(SubProfileEditActivity.this, addSubProfileResponse.optString(Params.MESSAGE) + ". Restarting app..");
                        PrefUtils.getInstance(SubProfileEditActivity.this)
                                .removeKey(PrefKeys.ACTIVE_SUB_PROFILE);
                        restartApp();
                    } else {
                        UiUtils.showShortToast(SubProfileEditActivity.this, addSubProfileResponse.optString(Params.ERROR_MESSAGE));
                    }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(SubProfileEditActivity.this);
            }
        });
    }

    private void restartApp() {
        Intent restart = new Intent(SubProfileEditActivity.this, SplashActivity.class);
        restart.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(restart);
        SubProfileEditActivity.this.finish();
    }

    private void callImagePicker() {
        try {
            Intent openGalleryIntent = new Intent(Intent.ACTION_PICK);
            openGalleryIntent.setType("image/*");
            startActivityForResult(openGalleryIntent, PICK_IMAGE);
        } catch (Exception e) {
            e.printStackTrace();
            UiUtils.showShortToast(this, "Sorry..No apps to perform Image picking.");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            fileToUpload = data.getData();
            GlideApp.with(this)
                    .load(fileToUpload)
                    .into(subProfilePicture);
        }

    }

    private String getRealPathFromURIPath(Uri contentURI, Activity activity) {
        Cursor cursor = activity.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }
}
