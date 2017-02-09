package jp.moremal.moremall.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jp.moremal.moremall.R;
import jp.moremal.moremall.adapter.LoadAllImageAdapter;
import jp.moremal.moremall.custom.MarginDecorationImage;
import jp.moremal.moremall.model.BaseItem;
import jp.moremal.moremall.utils.Constants;
import jp.moremal.moremall.utils.SharePreferencesUtils;

/**
 * Created by cuonghc on 12/30/15.
 */
public class LoadAllImageFragment extends BaseFragment implements View.OnClickListener{

    public String TAG =  LoadAllImageFragment.class.getName();
    //---------------------------------------------------------------------------------------------------
    private LoadAllImageAdapter mLoadAllImageAdapter;
    private GridLayoutManager mGridLayoutManager;
    protected RecyclerView mRecyclerView;
    private final static String LIMIT = "LIMIT";
    private int mLimit;
    private View mViewProgressBar;
    List<BaseItem> mBaseItemList = new ArrayList<>();
    private Button mBtnCancel, mBtnSave;
    private ImageView mBtnCamera;
    private ArrayList<String> mListUri = new ArrayList<>();

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String PREFERENCE_IMAGE_NAME = "PREFERENCE_IMAGE_NAME";
    public static String PREFERENCE_LIST_URI = "PREFERENCE_LIST_URI";
    public static String INTENT_KEY_LIST_URI = "INTENT_KEY_LIST_URI";
    private String mUrlImageCamera;
    //---------------------------------------------------------------------------------------------------
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_load_all_image, null);
    }

    public static LoadAllImageFragment newInstance(int limit) {
        LoadAllImageFragment itemFragment = new LoadAllImageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(LIMIT, limit);
        itemFragment.setArguments(bundle);
        return itemFragment;
    }

    /**
     * Init base default when first run
     */
    protected void initBase(View view) {
        super.initBase(view);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mGridLayoutManager = new GridLayoutManager(mMainActivity,3);
        mLimit = getArguments().getInt(LIMIT);
        mBtnCancel = (Button) view.findViewById(R.id.btn_myshop_image_selection_cancel);
        mBtnSave = (Button) view.findViewById(R.id.btn_myshop_image_selection_save);
        mBtnCamera = (ImageView) view.findViewById(R.id.iv_myshop_camera);
    }

    /**
     * Execute base default when first run
     */
    protected void executeBase() {
        super.executeBase();
        // setup GridLayout Manager
        mGridLayoutManager.setSmoothScrollbarEnabled(true);

        // setup RecycleView
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new MarginDecorationImage(mMainActivity, R.dimen.view_item_image_item_space_item));
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        // Create product adapter
        mLoadAllImageAdapter = new LoadAllImageAdapter(mMainActivity, mLimit);
        mRecyclerView.setAdapter(mLoadAllImageAdapter);
        mLoadAllImageAdapter.notifyDataSetChanged();

        //build bottom button
        mBtnCancel.setOnClickListener(this);
        mBtnSave.setOnClickListener(this);
        mBtnCamera.setOnClickListener(this);
    }
    //---------------------------------------------------------------------------------------------------
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_myshop_image_selection_cancel:
                mMainActivity.onBackPressed();
                break;
            case R.id.iv_myshop_camera:
                dispatchTakePictureIntent();
                break;
            case R.id.btn_myshop_image_selection_save:
                mListUri = SharePreferencesUtils.getStringArrayListFromPreference(mMainActivity, PREFERENCE_LIST_URI, new ArrayList<String>());
                mMainActivity.onBackPressed();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
//                Uri photoUri = null;
//                if (data == null) {
//                    photoUri = Uri.parse(SharePreferencesUtils.getStringFromPreference(mMainActivity, PREFERENCE_IMAGE_NAME, ""));
//                } else {
//                    photoUri = data.getData();
//                }
//                photoUri = Uri.parse(SharePreferencesUtils.getStringFromPreference(mMainActivity, PREFERENCE_IMAGE_NAME, ""));
//                String link = SharePreferencesUtils.getStringFromPreference(mMainActivity, PREFERENCE_IMAGE_NAME, "");
//                mListUri = SharePreferencesUtils.getStringArrayListFromPreference(mMainActivity, PREFERENCE_LIST_URI, new ArrayList<String>());
                mLoadAllImageAdapter.getImageObjectSelectedList().add(mUrlImageCamera);
//                mListUri.add(link);
                SharePreferencesUtils.putStringArrayListToPreference(mMainActivity, PREFERENCE_LIST_URI, mListUri);
                mMainActivity.onBackPressed();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(TAG, "Camera Cancelled");
            } else {
                Log.d(TAG, "Callout for image capture failed!");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getTargetFragment() != null) {
            Intent intent = new Intent();
            intent.putStringArrayListExtra(INTENT_KEY_LIST_URI, new ArrayList<>(mLoadAllImageAdapter.getImageObjectSelectedList()));
            getTargetFragment().onActivityResult(Constants.REFRESH_FRAGMENT, Activity.RESULT_OK, intent);
        }
    }

    //---------------------------------------------------------------------------------------------------
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            mUrlImageCamera = getOutputPhotoFile().getAbsolutePath();
            Uri fileUri = Uri.fromFile(getOutputPhotoFile());
//            SharePreferencesUtils.putStringToPreference(mMainActivity, PREFERENCE_IMAGE_NAME, fileUri.toString());
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    //---------------------------------------------------------------------------------------------------
    private File getOutputPhotoFile() {
        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), mMainActivity.getPackageName());
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Log.e(TAG, "Failed to create storage directory.");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyMMdd_HHmmss", Locale.UK).format(new Date());
        return new File(directory.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
    }
    //---------------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------------
}
