package org.indywidualni.pictureflip.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.indywidualni.pictureflip.Constant;
import org.indywidualni.pictureflip.R;
import org.indywidualni.pictureflip.activity.PopupActivity;

import java.io.File;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    // Because RecyclerView.Adapter in its current form doesn't natively
    // support cursors, we wrap a CursorAdapter that will do all the job
    private CursorAdapter mCursorAdapter;

    private Context mContext;

    public CursorAdapter getCursorAdapter() {
        return mCursorAdapter;
    }

    public RecyclerAdapter(Context context, Cursor cursor) {

        mContext = context;
        mCursorAdapter = new CursorAdapter(mContext, cursor, 0) {

            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.item_grid, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView title = (TextView) view.findViewById(R.id.text);
                ImageView image = (ImageView) view.findViewById(R.id.image);

                String name, path;

                name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                title.setText(name);

                int sizeInPixels = Util.getDimensionInPixels(context, R.dimen.thumbnail_size);

                if (!TextUtils.isEmpty(path)) {
                    Picasso.with(context).load(new File(path))
                            .resize(sizeInPixels, sizeInPixels)
                            .centerCrop()
                            .into(image);
                }
            }

        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View v1, v2, v3;
        public IViewHolderClicks mListener;

        public ViewHolder(View itemView, IViewHolderClicks listener) {
            super(itemView);
            mListener = listener;
            v1 = itemView.findViewById(R.id.text);
            v2 = itemView.findViewById(R.id.image);
            v3 = itemView.findViewById(R.id.item_container);
            v3.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v instanceof LinearLayout)
                mListener.onClick((LinearLayout) v, getAdapterPosition());
        }

        public interface IViewHolderClicks {
            void onClick(LinearLayout caller, int position);
        }
    }

    @Override
    public int getItemCount() {
        return mCursorAdapter.getCount();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Passing the binding operation to cursor loader
        mCursorAdapter.getCursor().moveToPosition(position);
        mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Passing the inflater job to the cursor-adapter
        View v = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
        return new ViewHolder(v, new ViewClicks());
    }

    private class ViewClicks implements ViewHolder.IViewHolderClicks {
        public void onClick(LinearLayout caller, int position) {
            Cursor cursor = (Cursor) mCursorAdapter.getItem(position);
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            Intent intent = new Intent(mContext.getApplicationContext(), PopupActivity.class);
            intent.putExtra(Constant.TITLE_EXTRA, name);
            intent.putExtra(Constant.PATH_EXTRA, path);
            ((Activity) mContext).startActivityForResult(intent, Constant.ACTIVITY_POPUP_REQUEST);
        }
    }

}