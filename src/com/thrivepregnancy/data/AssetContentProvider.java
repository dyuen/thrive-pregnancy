package com.thrivepregnancy.data;

/**
 * A ContentProvider for files in the /assets directory. Although programmatic access
 * works for direct reads and writes works fine,  calling ImageView.setImageURI on the 
 * same uri does not allow the view to read the file 
 */
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;

public class AssetContentProvider extends ContentProvider {

    private AssetManager mAssetManager;
    public static final Uri CONTENT_URI = Uri.parse("content://com.thrivepregnancy.assetcontentprovider");

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public boolean onCreate() {
        mAssetManager = getContext().getAssets();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
        String path = uri.getPath().substring(1);
        try {
            AssetFileDescriptor afd = mAssetManager.openFd(path);
            return afd;
        } 
        catch (IOException e) {
            throw new FileNotFoundException("No asset found: " + uri);
        }
    }
}
