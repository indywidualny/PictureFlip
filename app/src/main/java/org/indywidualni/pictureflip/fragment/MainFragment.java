package org.indywidualni.pictureflip.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.indywidualni.pictureflip.Constant;
import org.indywidualni.pictureflip.R;
import org.indywidualni.pictureflip.activity.MainActivity;
import org.indywidualni.pictureflip.util.PermissionUtil;
import org.indywidualni.pictureflip.util.RecyclerAdapter;

public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        MainActivity.IMainActivityToFragment {

    public static final String TAG = MainFragment.class.getSimpleName();
    public static final String TAG_FRAGMENT = "main_fragment";
    public static final String CURRENT_POSITION = "current_position";

    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerViewAdapter;
    private int currentPosition;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int columnsNumber = getResources().getInteger(R.integer.columns_number);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columnsNumber));
        recyclerViewAdapter = new RecyclerAdapter(getContext(), null);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (PermissionUtil.hasStoragePermission(getContext()))
            startLoader();
        else
            PermissionUtil.requestStoragePermission(getActivity());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_POSITION, ((GridLayoutManager) recyclerView.getLayoutManager())
                .findFirstCompletelyVisibleItemPosition());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null)
            currentPosition = savedInstanceState.getInt(CURRENT_POSITION, 0);
    }

    private void restoreScrollPosition() {
        recyclerView.getLayoutManager().scrollToPosition(currentPosition);
        currentPosition = 0;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
    }

    @Override
    public void startLoader() {
        if (getLoaderManager().getLoader(Constant.LOADER_PHOTO_LIST) != null
                && getLoaderManager().getLoader(Constant.LOADER_PHOTO_LIST).isStarted()) {
            getLoaderManager().restartLoader(Constant.LOADER_PHOTO_LIST, null, this);
        } else {
            getLoaderManager().initLoader(Constant.LOADER_PHOTO_LIST, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        CursorLoader loader = new CursorLoader(getActivity());
        switch (id) {
            case Constant.LOADER_PHOTO_LIST:
                loader.setUri(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                loader.setSelection(MediaStore.Images.Media.DATA + " LIKE ?");
                loader.setSelectionArgs(new String[] {"%.jpg"});
                loader.setSortOrder(MediaStore.Images.Media.DATE_MODIFIED + " DESC");
                break;
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == Constant.LOADER_PHOTO_LIST) {
            recyclerViewAdapter.getCursorAdapter().swapCursor(data);
            restoreScrollPosition();
            // It's not necessary for a cursor swap of a standard adapter.
            // Mine is quite a special one so it's needed though.
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        recyclerViewAdapter.getCursorAdapter().swapCursor(null);
    }

}
