package com.streamhash.streamview.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;

import com.streamhash.streamview.R;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.ui.fragment.VideoContentFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoContentActivity extends BaseActivity {

    APIInterface apiInterface;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    String pageType;
    int categoryId, subCategoryId, genreId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_content);
        ButterKnife.bind(this);
        apiInterface = APIClient.getClient().create(APIInterface.class);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        Intent caller = getIntent();
        if (caller != null) {
            pageType = caller.getStringExtra(VideoContentFragment.PAGE_TYPE);
            //NO Break, because genre will surely
            switch (pageType) {
                case VideoContentFragment.TYPE_GENRE:
                    genreId = caller.getIntExtra(VideoContentFragment.GENRE_ID, -1);
                case VideoContentFragment.TYPE_SUB_CATEGORY:
                    subCategoryId = caller.getIntExtra(VideoContentFragment.SUB_CATEGORY_ID, -1);
                case VideoContentFragment.TYPE_CATEGORY:
                    categoryId = caller.getIntExtra(VideoContentFragment.CATEGORY_ID, -1);
            }
            String toolbarTitle = caller.getStringExtra(VideoContentFragment.TITLE);
            toolbar.setTitle(toolbarTitle);
            setUpFragment();
        }
    }

    private void setUpFragment() {
        VideoContentFragment videoContentFragment = VideoContentFragment
                .getInstance(pageType, categoryId, subCategoryId, genreId);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, videoContentFragment);
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
