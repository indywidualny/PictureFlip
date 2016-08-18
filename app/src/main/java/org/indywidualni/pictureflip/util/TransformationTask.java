package org.indywidualni.pictureflip.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.IdRes;

import org.indywidualni.pictureflip.R;

import java.io.File;
import java.io.FileOutputStream;

import dmax.dialog.SpotsDialog;

public class TransformationTask extends AsyncTask<Void, Void, Boolean> {

    public static final String TEMPORARY_FILE_NAME;
    public static final int FLIP_VERTICAL = 1;
    public static final int FLIP_HORIZONTAL = 2;

    private final Context context;

    private final File imageFile;
    private final File outFile;
    private final File temporaryFile;

    private final int transformation;
    private Dialog progress;

    static {
        TEMPORARY_FILE_NAME = "pflip_temp.jpg";
    }

    public TransformationTask(Context context, File imageFile, File outFile, @IdRes int transformation) {
        this.context = context;
        this.imageFile = imageFile;
        this.outFile = outFile;
        this.transformation = transformationChooser(transformation);
        temporaryFile = new File(Environment.getExternalStorageDirectory() + File.separator + TEMPORARY_FILE_NAME);
        progress = new SpotsDialog(context, R.style.CustomProgressDialog);
    }

    private static int transformationChooser(@IdRes int transformation) {
        switch (transformation) {
            case R.id.flip_v:
                return FLIP_VERTICAL;
            case R.id.flip_h:
                return FLIP_HORIZONTAL;
            default:
                return -1;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (context instanceof Activity)
            Util.lockOrientation((Activity) context);
        if (context instanceof TaskStatus)
            ((TaskStatus) context).processingStarted();
        progress.show();
    }

    @Override
    protected void onPostExecute(Boolean status) {
        super.onPostExecute(status);
        if (context instanceof TaskStatus)
            ((TaskStatus) context).processingDone(status);
        if (context instanceof Activity)
            Util.unlockOrientation((Activity) context);
        progress.dismiss();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            Bitmap bMap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            bMap = flip(bMap, transformation);
            if (bMap != null) {
                bMap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(temporaryFile));
                Util.moveFile(temporaryFile, outFile);
            }
            return true;
        } catch (OutOfMemoryError e) {
            if (context instanceof TaskStatus)
                ((TaskStatus) context).showDebugInfo(new RuntimeException("java.lang.OutOfMemoryError"));
        } catch (Exception e) {
            e.printStackTrace();
            if (context instanceof TaskStatus)
                ((TaskStatus) context).showDebugInfo(e);
        }
        return false;
    }

    private static Bitmap flip(Bitmap src, int type) {
        Matrix matrix = new Matrix();
        if (type == FLIP_VERTICAL) {
            matrix.preScale(1.0f, -1.0f);
        } else if (type == FLIP_HORIZONTAL) {
            matrix.preScale(-1.0f, 1.0f);
        } else {
            return null;
        }

        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    public interface TaskStatus {
        void processingDone(boolean status);
        void showDebugInfo(final Exception exception);
        void processingStarted();
    }

}
