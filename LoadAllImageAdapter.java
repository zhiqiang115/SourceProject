package jp.moremal.moremall.adapter;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.ImageSizeUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import jp.moremal.moremall.BaseActivity;
import jp.moremal.moremall.R;
import jp.moremal.moremall.event.RecyclerViewOnListener;
import jp.moremal.moremall.fragment.LoadAllImageFragment;
import jp.moremal.moremall.utils.SharePreferencesUtils;

/**
 * Created by cuonghc on 10/12/15.
 */
public class LoadAllImageAdapter extends RecyclerView.Adapter<LoadAllImageAdapter.LoadAllImageHolder> {

    private List<String> mImageObjectList = new ArrayList<>();
    private List<String> mImageObjectSelectedList = new ArrayList<>();
    private int mLimit;
    private boolean mIsDisableAll = false;
    private BaseActivity mBaseActivity;
    private List<Bitmap> mImageObjectBitmapList = new ArrayList<>();
    private ImageSize mImageSize;

    public LoadAllImageAdapter(BaseActivity baseActivity, int limit) {
        mBaseActivity = baseActivity;
        mImageObjectList = getAllShownImagesPath(baseActivity);
        mLimit = limit;
        int size = mBaseActivity.getResources().getDimensionPixelOffset(R.dimen.view_item_image_size);
        mImageSize = new ImageSize(size, size);
    }


    public ArrayList<String> getAllShownImagesPath(Activity activity) {
        ArrayList<String> listOfAllImages = new ArrayList<>();
        Uri uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.MediaColumns.DATA, MediaStore.MediaColumns._ID };
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        int column_id = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
        while (cursor.moveToNext()) {
            String absolutePathOfImage = cursor.getString(column_index);
            listOfAllImages.add(absolutePathOfImage);
//            Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(mBaseActivity.getContentResolver(), cursor.getInt(column_id), MediaStore.Images.Thumbnails.MINI_KIND, null);
//            mImageObjectBitmapList.add(bitmap);
        }
        return listOfAllImages;
    }

    @Override
    public LoadAllImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_load_all_image, parent, false);
        return new LoadAllImageHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(final LoadAllImageHolder holder, int position) {
        final String integer = mImageObjectList.get(position);
        holder.mImageView.setTag(position);
        ImageLoader.getInstance().displayImage("file://" + integer, holder.mImageView);

        if (!mImageObjectSelectedList.contains(integer)) {
            unChecked(holder);
        } else {
            checked(holder, integer);
        }

        if (mIsDisableAll) {
            if (!mImageObjectSelectedList.contains(integer)) {
                holder.mViewDisable.setVisibility(View.VISIBLE);
            } else {
                holder.mViewDisable.setVisibility(View.GONE);
            }
        } else {
            if (holder.mViewDisable.getVisibility() == View.VISIBLE) {
                holder.mViewDisable.setVisibility(View.GONE);
            }
        }
    }

    private void checked(LoadAllImageHolder holder, Object object) {
        holder.mTextViewCount.setText((mImageObjectSelectedList.indexOf(object) + 1) + "");
        holder.mImageView.setBackgroundResource(R.drawable.border_around_blue_2);
        holder.mTextViewCount.setBackgroundResource(R.drawable.around_blue);
    }

    private void unChecked(LoadAllImageHolder holder) {
        holder.mTextViewCount.setText("");
        holder.mImageView.setBackground(null);
        holder.mImageView.setPadding(0, 0, 0, 0);
        holder.mTextViewCount.setBackgroundResource(R.drawable.border_around_white_transparent);
    }

    public List<String> getImageObjectSelectedList() {
        return mImageObjectSelectedList;
    }

    @Override
    public int getItemCount() {
        return mImageObjectList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    private void saveImageToSharePreferences(String image_path){
        ArrayList<String> listImage = SharePreferencesUtils.getStringArrayListFromPreference(mBaseActivity, LoadAllImageFragment.PREFERENCE_LIST_URI, new ArrayList<String>());
        listImage.add(image_path);
        SharePreferencesUtils.putStringArrayListToPreference(mBaseActivity, LoadAllImageFragment.PREFERENCE_LIST_URI, listImage);
    }

    private void removeImageToSharePreferences(String image_path){
        ArrayList<String> listImage = SharePreferencesUtils.getStringArrayListFromPreference(mBaseActivity, LoadAllImageFragment.PREFERENCE_LIST_URI, new ArrayList<String>());
        if (listImage.contains(image_path)){
            listImage.remove(image_path);
        }
        SharePreferencesUtils.putStringArrayListToPreference(mBaseActivity, LoadAllImageFragment.PREFERENCE_LIST_URI, listImage);
    }

    //----------------------View Holder-------------------------------------------------------------------

    class LoadAllImageHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTextViewCount = null;
        private ImageView mImageView = null;
        private View mItemView;
        private View mViewDisable;

        /**
         * Detect View Id for itemView
         *
         * @param itemView view that you want to find the ID
         */
        public LoadAllImageHolder(View itemView, int itemType) {
            super(itemView);
            mItemView = itemView;
            mTextViewCount = (TextView) itemView.findViewById(R.id.txt_count);
            mImageView = (ImageView) itemView.findViewById(R.id.img_image);
            mViewDisable = itemView.findViewById(R.id.view_disable);
            mItemView.setOnClickListener(this);
            mItemView.setTag(this);
        }

        // set onclick recycle view for item
        @Override
        public void onClick(View v) {
            LoadAllImageHolder loadAllImageHolder = (LoadAllImageHolder) v.getTag();
            String integer = mImageObjectList.get(Integer.parseInt(loadAllImageHolder.mImageView.getTag() + ""));

            if (mImageObjectSelectedList.contains(integer)) {
                unChecked(loadAllImageHolder);
                int i = mImageObjectSelectedList.indexOf(integer);
                mImageObjectSelectedList.remove(integer);
                removeImageToSharePreferences(integer);
                if (mIsDisableAll) {
                    notifyDataSetChanged();
                    mIsDisableAll = false;
                } else {
                    for (; i < mImageObjectSelectedList.size(); i++) {
                        notifyItemChanged(mImageObjectList.indexOf(mImageObjectSelectedList.get(i)));
                    }
                }
            } else {
                if (mImageObjectSelectedList.size() <= mLimit - 1 ) {
                    mImageObjectSelectedList.add(integer);
                    checked(loadAllImageHolder, integer);
                    saveImageToSharePreferences(integer);
                    if (mImageObjectSelectedList.size() == mLimit) {
                        if (mLimit == 1) {
                            mBaseActivity.onBackPressed();
                        } else {
                            mIsDisableAll = true;
                            notifyDataSetChanged();
                        }
                    }
                }
            }

        }
    }

}
