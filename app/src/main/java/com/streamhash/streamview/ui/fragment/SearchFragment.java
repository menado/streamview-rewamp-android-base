package com.streamhash.streamview.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.streamhash.streamview.R;
import com.streamhash.streamview.model.Video;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIConstants;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.util.KeyboardUtils;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.ui.activity.MainActivity;
import com.streamhash.streamview.ui.adapter.VideoTileAdapter;
import com.streamhash.streamview.util.ParserUtils;
import com.streamhash.streamview.util.UiUtils;
import com.streamhash.streamview.util.sharedpref.PrefKeys;
import com.streamhash.streamview.util.sharedpref.PrefUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    MainActivity activity;
    Unbinder unbinder;

    VideoTileAdapter searchAdapter;
    ArrayList<Video> searchResults = new ArrayList<>();
    @BindView(R.id.searchView)
    EditText searchView;

    APIInterface apiInterface;
    @BindView(R.id.hintLayout)
    LinearLayout hintLayout;
    @BindView(R.id.searching)
    ProgressBar searching;
    @BindView(R.id.no_results)
    TextView noResults;
    @BindView(R.id.searchRecycler)
    RecyclerView searchRecycler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        unbinder = ButterKnife.bind(this, view);
        apiInterface = APIClient.getClient().create(APIInterface.class);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpSearchView();
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String key = s.toString();
                if (key.length() == 0) {
                    hintLayout.setVisibility(View.VISIBLE);
                    searchRecycler.setVisibility(View.GONE);
                    noResults.setVisibility(View.GONE);
                } else {
                    hintLayout.setVisibility(View.GONE);
                    searchAndUpdateViews(key);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        KeyboardUtils.showSoftInput(searchView, activity);
    }

    @OnClick(R.id.clearSearchText)
    protected void clearSearchText() {
        searchView.setText(null);
    }

    private void setUpSearchView() {
        noResults.setVisibility(View.GONE);
        searchAdapter = new VideoTileAdapter(activity, searchResults, VideoTileAdapter.VIDEO_SECTION_TYPE_NORMAL, true);
        searchRecycler.setHasFixedSize(true);
        searchRecycler.setLayoutManager(new GridLayoutManager(activity, 3));
        searchRecycler.setAdapter(searchAdapter);
    }

    private void searchAndUpdateViews(String key) {
        searching.setVisibility(View.VISIBLE);
        PrefUtils prefUtils = PrefUtils.getInstance(getActivity());
        Call<String> call = apiInterface.searchVideos(
                prefUtils.getIntValue(PrefKeys.USER_ID, -1)
                , prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "")
                , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, 0)
                , key);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (isAdded()) {
                    searching.setVisibility(View.GONE);
                    JSONObject searchResponse = null;
                    try {
                        searchResponse = new JSONObject(response.body());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (searchResponse != null) {
                        searchResults.clear();
                        if (searchResponse.optString(APIConstants.Params.SUCCESS).equals(APIConstants.Constants.TRUE)) {
                            JSONArray data = searchResponse.optJSONArray(APIConstants.Params.DATA);
                            for (int i = 0; i < data.length(); i++) {
                                try {
                                    JSONObject user = data.getJSONObject(i);
                                    Video video = ParserUtils.parseVideoData(user);
                                    searchResults.add(video);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            onDataChange();
                        } else {
                            UiUtils.showShortToast(activity, searchResponse.optString(APIConstants.Params.ERROR_MESSAGE));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                if (isAdded()) {
                    searching.setVisibility(View.GONE);
                    NetworkUtils.onApiError(activity);
                }
            }
        });
    }

    private void onDataChange() {
        if (isAdded()) {
            boolean isEmptyData = searchResults.isEmpty();
            searchRecycler.setVisibility(isEmptyData ? View.GONE : View.VISIBLE);
            noResults.setVisibility(isEmptyData ? View.VISIBLE : View.GONE);
            searchAdapter.notifyDataSetChanged();
            hintLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
