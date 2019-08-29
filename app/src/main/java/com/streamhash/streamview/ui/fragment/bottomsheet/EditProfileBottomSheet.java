package com.streamhash.streamview.ui.fragment.bottomsheet;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.streamhash.streamview.R;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.util.GlideApp;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.util.AppUtils;
import com.streamhash.streamview.util.UiUtils;
import com.streamhash.streamview.util.sharedpref.PrefKeys;
import com.streamhash.streamview.util.sharedpref.PrefUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;
import static com.streamhash.streamview.network.APIConstants.Constants;
import static com.streamhash.streamview.network.APIConstants.Params;

public class EditProfileBottomSheet extends BottomSheetDialogFragment {

    private static final int PICK_IMAGE = 100;

    Unbinder unbinder;
    @BindView(R.id.userPicture)
    CircularImageView userPicture;
    @BindView(R.id.ed_name)
    EditText edName;
    @BindView(R.id.layout_name)
    TextInputLayout layoutName;
    @BindView(R.id.ed_email)
    EditText edEmail;
    @BindView(R.id.layout_email)
    TextInputLayout layoutEmail;
    @BindView(R.id.ed_phone)
    EditText edPhone;
    @BindView(R.id.layout_phone)
    TextInputLayout layoutPhone;


    APIInterface apiInterface;
    PrefUtils prefUtils;
    private Uri fileToUpload = null;


    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.layout_edit_profile, null);
        unbinder = ButterKnife.bind(this, contentView);
        dialog.setContentView(contentView);
        apiInterface = APIClient.getClient().create(APIInterface.class);
        prefUtils = PrefUtils.getInstance(getActivity());


        PrefUtils prefUtils = PrefUtils.getInstance(getActivity());
        String name = prefUtils.getStringValue(PrefKeys.USER_NAME, "");
        String phone = prefUtils.getStringValue(PrefKeys.USER_MOBILE, "");
        String email = prefUtils.getStringValue(PrefKeys.USER_EMAIL, "");
        String image = prefUtils.getStringValue(PrefKeys.USER_PICTURE, "");

        try {
            edName.setText(name);
            edName.setSelection(name.length());
            edPhone.setText(phone);
            edPhone.setSelection(phone.length());
            edEmail.setText(email);
            edEmail.setSelection(email.length());

            GlideApp.with(this)
                    .load(image)
                    .into(userPicture);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Permission check
        AppUtils.permissionCheck(getActivity());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.userPicture, R.id.save_profile})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.userPicture:
                callImagePicker();
                break;
            case R.id.save_profile:
                if (validateFields()) {
                    saveProfileInBackend(fileToUpload);
                }
                break;
        }
    }

    private void callImagePicker() {
        try {
            Intent openGalleryIntent = new Intent(Intent.ACTION_PICK);
            openGalleryIntent.setType("image/*");
            startActivityForResult(openGalleryIntent, PICK_IMAGE);
        } catch (Exception e) {
            e.printStackTrace();
            UiUtils.showShortToast(getActivity(), "Sorry..No apps to perform Image picking.");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            fileToUpload = data.getData();
            GlideApp.with(this)
                    .load(fileToUpload)
                    .into(userPicture);
        }

    }

    private boolean validateFields() {
        if (edName.getText().toString().trim().length() == 0) {
            UiUtils.showShortToast(getActivity(), getString(R.string.names_cant_be_empty));
            return false;
        }
        //Phone validation
        String phone = edPhone.getText().toString().trim();
        if (phone.length() != 0 && (phone.length() < 4 || phone.length() > 16)) {
            UiUtils.showShortToast(getActivity(), getString(R.string.phone_cant_be_stuff));
            return false;
        }
        if (edEmail.getText().toString().trim().length() == 0) {
            UiUtils.showShortToast(getActivity(), getString(R.string.email_cant_be_empty));
            return false;
        }
        if (!AppUtils.isValidEmail(edEmail.getText().toString())) {
            UiUtils.showShortToast(getActivity(), getString(R.string.enter_valid_email));
            return false;
        }
        return true;
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

    private void saveProfileInBackend(Uri profileImageUri) {
        UiUtils.showLoadingDialog(getActivity());
        PrefUtils preferences = PrefUtils.getInstance(getActivity());

        MultipartBody.Part multipartBody = null;

        if (profileImageUri != null) {
            String filePath = getRealPathFromURIPath(profileImageUri, getActivity());
            File file = new File(filePath);

            // create RequestBody instance tempFrom file
            RequestBody requestFile =
                    RequestBody.create(MediaType.parse("image/*"), file);

            // MultipartBody.Part is used to send also the actual file name
            multipartBody =
                    MultipartBody.Part.createFormData(Params.PICTURE, file.getAbsolutePath(), requestFile);
        }

        Call<String> call = apiInterface.updateUserProfile(getPartFor(String.valueOf(preferences.getIntValue(PrefKeys.USER_ID, -1)))
                , getPartFor(preferences.getStringValue(PrefKeys.SESSION_TOKEN, ""))
                , getPartFor(preferences.getStringValue(PrefKeys.USER_EMAIL, ""))
                , getPartFor(edName.getText().toString())
                , getPartFor(edEmail.getText().toString())
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
                        //update in sharedpref too
                        UiUtils.showShortToast(getActivity(), updateProfileResponse.optString(Params.MESSAGE));
                        prefUtils.setValue(PrefKeys.USER_NAME, edName.getText().toString());
                        prefUtils.setValue(PrefKeys.USER_EMAIL, edEmail.getText().toString());
                        prefUtils.setValue(PrefKeys.USER_PICTURE, updateProfileResponse.optString(Params.PICTURE));
                        prefUtils.setValue(PrefKeys.USER_MOBILE, edPhone.getText().toString());
                        dismiss();
                    } else {
                        UiUtils.showShortToast(getActivity(), updateProfileResponse.optString(Params.ERROR_MESSAGE));
                    }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(getActivity());
            }
        });
    }

    private RequestBody getPartFor(String stuff) {
        return RequestBody.create(MediaType.parse("text/plain"), stuff);
    }
}
