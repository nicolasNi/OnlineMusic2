package com.lt.nexthud.onlinemusic2;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity {

    private ListView lvSearchReasult;
    private ProgressDialog dialog;
    private List<Music> listSearchResult;
    private ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
    private MyAdapter adapter;
    private SQLiteDatabase db;
    private DownloadManager downloadManager;
    private String downloadUrl;
    private boolean displayMusicHistory = true;
    private Button musicHistoryButton;
    private MusicDBHelper musicDBHelper;
    private static final int REFLASH_BY_SEARCH_RESULT = 0;
    private MusicService.ControlMusicBinder musicBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialDB();
        init();
    }

    private void init() {
        listSearchResult = new ArrayList<Music>();
        dialog = new ProgressDialog(this);
        dialog.setTitle("加载中。。。");
        lvSearchReasult = (ListView) findViewById(R.id.lv_search_list);
        lvSearchReasult.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        Button btSearch = (Button) findViewById(R.id.bt_online_search);
        final EditText edtKey = (EditText) findViewById(R.id.edt_search);
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        btSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayMusicHistory = false;
                dialog.show();// 进入加载状态，显示进度条
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SearchUtils.getMusics(edtKey.getText().toString(),
                                new OnLoadSearchFinishListener() {
                                    @Override
                                    public void onLoadSucess(List<Music> musicList) {
                                        dialog.dismiss();// 加载完成，取消进度条
                                        Message msg = new Message();
                                        msg.what = REFLASH_BY_SEARCH_RESULT;
                                        listSearchResult = musicList;
                                        mHandler.sendMessage(msg);
                                    }

                                    @Override
                                    public void onLoadFiler() {
                                        dialog.dismiss();// 加载失败，取消进度条
                                        Toast.makeText(MainActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }).start();
            }
        });

        musicHistoryButton = (Button) findViewById(R.id.musicHistory);
        musicHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayMusicHistory = true;
                displayMusicHistory();
            }
        });

        Intent musicServiceIntent = new Intent(this,MusicService.class);
        bindService(musicServiceIntent,musicServiceConnection,BIND_AUTO_CREATE);
    }

    private void displayMusicHistory() {
        Message msg = new Message();
        msg.what = 0;
        listSearchResult = getMusicListFromDB();
        mHandler.sendMessage(msg);
//        deleteTable();
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case REFLASH_BY_SEARCH_RESULT:
                listItem.clear();
                for (int i = 0; i < listSearchResult.size(); i++) {
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("tv_search_list_title", listSearchResult.get(i).getMusciName());
                    map.put("tv_search_list_airtist", listSearchResult.get(i).getAirtistName() + "   《" + listSearchResult.get(i).getAlbumName() + "》");
                    listItem.add(map);
                }
                adapter = new MyAdapter(MainActivity.this, R.layout.item_online_search_list,listItem);
                lvSearchReasult.setAdapter(adapter);
                break;
        }
        }
    };


    private void initialDB() {
        musicDBHelper = new MusicDBHelper(this,"hud.db",null,1);
        db = musicDBHelper.getWritableDatabase();
    }

    public void click(View v) {
        String url = listSearchResult.get(adapter.selectItem).getPath();
        switch (v.getId()) {
            case R.id.imageButton://当点击的为上一首按钮时
                adapter.selectItem -= 1;
                adapter.notifyDataSetChanged();
                break;
            case R.id.imageButton2://当点击播放按钮按钮时
                Music musicPlayed = listSearchResult.get(adapter.selectItem);
                if (displayMusicHistory) {
                    File rootFile = android.os.Environment.getExternalStorageDirectory();
                    File file = new File(rootFile.getPath() + "/com.lt.nexthud.onlinemusic2/myDownLoadMusic/" + musicPlayed.getMusciName() + "-" + musicPlayed.getAirtistName() + "-" + musicPlayed.getAlbumName() + ".m4a");
                    url = file.getPath();
                }
//                download(musicPlayed);
                if (!isExist(musicPlayed)) {
                    getANewSong(musicPlayed);
                    download(musicPlayed);
                }
                musicBinder.playMusic(url);
                break;
            case R.id.imageButton3://当点击暂停按钮时
                musicBinder.pauseMusic();
                break;
            case R.id.imageButton4://当点击下一首按钮
                adapter.selectItem += 1;
                adapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    private boolean isExist(Music music) {
        boolean exits = false;
        Cursor cursor = db.query("music",null,"musciName = ? and airtistName = ? and albumName = ?",new String[]{music.getMusciName(), music.getAirtistName(), music.getAlbumName()},null,null,null);
        if(cursor.moveToFirst())
        {
            do{
                int No = cursor.getInt(cursor.getColumnIndex("No"));
                if (No > 0) {
                    exits = true;
                    break;
                }
            }
            while (cursor.moveToNext());
        }
        return exits;
    }

    private void getANewSong(Music newMusic) {
        deleteThe50thMusicFromFolder();
        deletThe50thSong();
        updateMusicList();
        addTheNewSong(newMusic);
    }

    private void deleteThe50thMusicFromFolder(){
        Cursor cursor = db.query("music",null,"No = ?",new String[]{"50"},null,null,null);
        if(cursor.moveToFirst()){
            String musicName = cursor.getString(cursor.getColumnIndex("musciName"));
            String airtistName = cursor.getString(cursor.getColumnIndex("airtistName"));
            String albumName = cursor.getString(cursor.getColumnIndex("albumName"));
            File rootFile = android.os.Environment.getExternalStorageDirectory();
            File file = new File(rootFile.getPath() + "/com.lt.nexthud.onlinemusic2/myDownLoadMusic/" +musicName + "-" + airtistName + "-" + albumName + ".m4a");
            if(file.isFile()){
                file.delete();
            }
        }
    }

    private void deletThe50thSong() {
        db.delete("music", "No = ?", new String[]{"50"});
    }

    private void updateMusicList() {
        Cursor cursor = db.rawQuery("select count(*)from music", null);
        //游标移到第一条记录准备获取数据
        cursor.moveToFirst();
        // 获取数据中的LONG类型数据
        int count = cursor.getInt(0);

        for (int i = count; i > 0; i--) {
            ContentValues cv = new ContentValues();
            cv.put("No", i + 1);
            db.update("music", cv, "No = ?", new String[]{Integer.toString(i)});
        }
    }

    private void addTheNewSong(Music newMusic) {
        newMusic.No = 1;
        //插入数据
        ContentValues contentValues = new ContentValues();
        contentValues.put("musciName",newMusic.getMusciName());
        contentValues.put("airtistName",newMusic.getAirtistName());
        contentValues.put("albumName",newMusic.getAlbumName());
        contentValues.put("No",newMusic.No);
        db.insert("music",null,contentValues);
    }

    private List<Music> getMusicListFromDB() {
        List<Music> musicList = new ArrayList<Music>();
        Cursor cursor = db.query("music",null,null,null,null,null,"No ASC");
        if(cursor.moveToFirst()){
            do{
                String musciName = cursor.getString(cursor.getColumnIndex("musciName"));
                String airtistName = cursor.getString(cursor.getColumnIndex("airtistName"));
                String albumName = cursor.getString(cursor.getColumnIndex("albumName"));
                Music music = new Music();
                music.setMusciName(musciName);
                music.setAirtistName(airtistName);
                music.setAlbumName(albumName);
                musicList.add(music);
            }
            while (cursor.moveToNext());
        }
        return musicList;
    }

    private void download(final Music downloadMusic) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                downloadUrl = downloadMusic.getPath();
                Uri resource = Uri.parse(downloadUrl);
                DownloadManager.Request request = new DownloadManager.Request(resource);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                request.setAllowedOverRoaming(true);
                // 设置文件类型
                MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                String mimeString = mimeTypeMap
                        .getMimeTypeFromExtension(MimeTypeMap
                                .getFileExtensionFromUrl(downloadUrl));
                request.setMimeType(mimeString);

                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                request.setVisibleInDownloadsUi(false);
                // sdcard的目录下的download文件夹
                String musicName = downloadMusic.getMusciName() + "-" + downloadMusic.getAirtistName() + "-" + downloadMusic.getAlbumName() + ".m4a";
                request.setDestinationInExternalPublicDir(MainActivity.this.getPackageName() + "/myDownLoadMusic", musicName);
                downloadManager.enqueue(request);
            }
        }).start();
    }

    private void deleteTable() {
        db.execSQL("DROP TABLE IF EXISTS music");
    }

    private ServiceConnection musicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicBinder = (MusicService.ControlMusicBinder)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
}
