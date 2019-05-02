package com.example.win.easy.view;

import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.example.win.easy.Constants;
import com.example.win.easy.R;
import com.example.win.easy.activity.MainActivity;
import com.example.win.easy.persistence.component.FileSongMapConfigurationPersistence;
import com.example.win.easy.persistence.component.SongListConfigurationPersistence;
import com.example.win.easy.song.Song;
import com.example.win.easy.song.SongManagerImpl;
import com.example.win.easy.song.interfaces.SongManager;
import com.example.win.easy.songList.SongListMangerImpl;
import com.example.win.easy.songList.interfaces.SongListManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SongPanelView {

    private SongListManager songListManager= SongListMangerImpl.getInstance();
    private SongManager songManager= SongManagerImpl.getInstance();
    private int songListToAddTheSong;

    private static SongPanelView instance=new SongPanelView();
    public static SongPanelView getInstance(){return instance;}
    private SongPanelView() {
        Button btnAddSong = MainActivity.mainActivity.findViewById(R.id.AddSong);
        btnAddSong.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                performFileSearch();
            }
        });
    }

    static {
        Button buttonSeeAllSongs=MainActivity.mainActivity.findViewById(R.id.SeeAllSongs);
        buttonSeeAllSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instance.createDialogSeeAllSongs();
            }
        });
    }

    public void createDialogSeeAllSongs(){
        List<Song> allSongs=songManager.getAllSongs();
        List<String> songNames=new ArrayList<>();
        for (Song song:allSongs)
            songNames.add(song.getName());
        new AlertDialog.Builder(MainActivity.mainActivity)
                .setItems(
                        songNames.toArray(new String[songNames.size()]),
                        null
                )
                .show();
    }

    /**
     * 展示一个页面，让用户选择将音乐添加到哪一个歌单
     * @param uri 将添加的音乐文件的URI
     */
    public void createDialogAddSongToSongList(final Uri uri){
        List<String> songListNames=songListManager.getNameOfAllSelfDefinedSongLists();
        final String[] songListNamesInArray = songListNames.toArray(new String[songListNames.size()]);//所有歌单名字的列表

        songListToAddTheSong = -1;//默认加入第一个歌单

        new AlertDialog.Builder(MainActivity.mainActivity)
                .setTitle("加入歌单")
                .setSingleChoiceItems(songListNamesInArray, songListToAddTheSong, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        songListToAddTheSong = which;
                    }
                })//选择歌单
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //获得歌曲名字和绝对路径，初始化一个歌，并放进歌单
                            File songFile=new File(getPathByUri4kitkat(MainActivity.mainActivity,uri));
                            songManager.add(songFile);
                            Toast.makeText(MainActivity.mainActivity,"添加成功", Toast.LENGTH_SHORT).show();
                            if (songListToAddTheSong>=0){
                                songListManager.getAllSongLists().get(songListToAddTheSong+1).add(songManager.toSong(songFile));
                            }
                            FileSongMapConfigurationPersistence.getInstance()
                                    .save(SongManagerImpl.getInstance().getMap());
                            SongListConfigurationPersistence.getInstance()
                                    .save(SongListMangerImpl.getInstance().getAllSongLists());
                        }
                    })
                .show();
    }

    /**
     * 跳转到文件浏览页面，用于选择要添加的音乐文件
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        MainActivity.mainActivity.startActivityForResult(intent, Constants.READ_REQUEST_CODE);
    }

    //以下是我百度到的对URI的处理函数们，我对他的机制不是完全掌握，不要乱动。
    //但有挺多有用的信息。

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static String getPathByUri4kitkat(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {// ExternalStorageProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {// DownloadsProvider
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null); } else if (isMediaDocument(uri)) {// MediaProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {// MediaStore(and general)
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {// File
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context
     *            The context.
     * @param uri
     *            The Uri to query.
     * @param selection
     *            (Optional) Filter used in the query.
     * @param selectionArgs
     *            (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


}
