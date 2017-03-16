package com.lt.nexthud.onlinemusic2;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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

    private MediaPlayer mediaPlayer;
    private String downloadMusicPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialDB();
        init();

        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
    }

    private void init() {
        listSearchResult = new ArrayList<Music>();
        dialog = new ProgressDialog(this);
        dialog.setTitle("加载中。。。");
        lvSearchReasult = (ListView) findViewById(R.id.lv_search_list);
        lvSearchReasult.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        Button btSearch = (Button) findViewById(R.id.bt_online_search);
        final EditText edtKey = (EditText) findViewById(R.id.edt_search);

        btSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayMusicHistory = false;
                dialog.show();// 进入加载状态，显示进度条
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SearchUtils.getIds(edtKey.getText().toString(),
                                new OnLoadSearchFinishListener() {
                                    @Override
                                    public void onLoadSucess(List<Music> musicList) {
                                        dialog.dismiss();// 加载完成，取消进度条
                                        Message msg = new Message();
                                        msg.what = 0;
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
    }

    private void displayMusicHistory() {
        downloadMusicPath = this.getPackageName() + "/myDownLoadMusic/";
        Message msg = new Message();
        msg.what = 0;
        mHandler.sendMessage(msg);
        listSearchResult = getMusicListFromDB();
//        deleteTable();
    }


    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case 0:
                listItem.clear();
                for (int i = 0; i < listSearchResult.size(); i++) {
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("tv_search_list_title", listSearchResult.get(i).getMusciName());
                    map.put("tv_search_list_airtist", listSearchResult.get(i).getAirtistName() + "   《" + listSearchResult.get(i).getAlbumName() + "》");
                    listItem.add(map);
                }

                adapter = new MyAdapter(MainActivity.this);
                lvSearchReasult.setAdapter(adapter);
                break;
        }
        }

        ;
    };


    private void initialDB() {
        db = openOrCreateDatabase("test.db", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE if not exists music (_id INTEGER PRIMARY KEY AUTOINCREMENT, musciName VARCHAR,  airtistName VARCHAR,  albumName VARCHAR, No SMALLINT)");
    }

    public void click(View v) {
        Intent intent = new Intent(MainActivity.this, MusicService.class);//实例化一个Intent对象
        String[] param = {"-1", ""};
        int op = -1;//设置中间变量op
        String url = listSearchResult.get(adapter.selectItem).getPath();
        switch (v.getId()) {
            case R.id.imageButton://当点击的为上一首按钮时
                adapter.selectItem -= 1;
                adapter.setSelectItem(adapter.selectItem);
                adapter.notifyDataSetInvalidated();
                op = 1;
                break;
            case R.id.imageButton2://当点击播放按钮按钮时
                Music musicPlayed = listSearchResult.get(adapter.selectItem);
                if (displayMusicHistory) {
                    File rootFile = android.os.Environment.getExternalStorageDirectory();
                    File file = new File(rootFile.getPath() + "/com.lt.nexthud.onlinemusic2/myDownLoadMusic/" + musicPlayed.getMusciName() + "-" + musicPlayed.getAirtistName() + "-" + musicPlayed.getAlbumName() + ".m4a");
                    url = file.getPath();
                }
                op = 2;
                param[0] = "1";
                param[1] = "http://ws.stream.qqmusic.qq.com/C200001OyHbk2MSIi4.m4a?vkey=BC7D971169A6FF737840E77762959D1F5CC6B1753096CDC097C8D4C5D4198D7CCA13B64015888E39BDDFC8665488C9A78FA38A710A0B8C65&guid=7524721365&fromtag=30";
                param[1] = url;
//                download(musicPlayed);
                if (!isExist(musicPlayed)) {
                    getANewSong(musicPlayed);
                    download(musicPlayed);
                }
                break;
            case R.id.imageButton3://当点击暂停按钮时
                op = 3;
                param[0] = "2";
                break;
            case R.id.imageButton4://当点击下一首按钮
                adapter.selectItem += 1;
                adapter.setSelectItem(adapter.selectItem);
                adapter.notifyDataSetInvalidated();
                break;
            default:
                break;
        }

        Bundle bundle = new Bundle();//实例化一个Bundle对象
        bundle.putInt("msg", op);//把op的值放入到bundle对象中
        bundle.putStringArray("param", param);
        intent.putExtras(bundle);//再把bundle对象放入intent对象中
        startService(intent);//开启这个服务


    }

    private boolean isExist(Music music) {
        boolean exits = false;
        Cursor c = db.rawQuery("SELECT * FROM music WHERE musciName = ? and airtistName = ? and albumName = ?", new String[]{music.getMusciName(), music.getAirtistName(), music.getAlbumName()});
        while (c.moveToNext()) {
            int No = c.getInt(c.getColumnIndex("No"));
            if (No > 0) {
                exits = true;
            }
        }
        return exits;
    }

    private void getANewSong(Music newMusic) {
        deletThe50thSong();
        updateMusicList();
        addTheNewSong(newMusic);

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
            //更新数据
            db.update("music", cv, "No = ?", new String[]{Integer.toString(i)});
        }
    }

    private void addTheNewSong(Music newMusic) {
        newMusic.No = 1;
        //插入数据
        db.execSQL("INSERT INTO music VALUES (NULL, ?, ?, ? , ?)", new Object[]{newMusic.getMusciName(), newMusic.getAirtistName(), newMusic.getAlbumName(), newMusic.No});

    }

    private List<Music> getMusicListFromDB() {
        List<Music> musicList = new ArrayList<Music>();
        Cursor c = db.rawQuery("SELECT * FROM music WHERE No >= ? ORDER BY No ASC", new String[]{"0"});
        while (c.moveToNext()) {
            int _id = c.getInt(c.getColumnIndex("_id"));
            String musciName = c.getString(c.getColumnIndex("musciName"));
            String airtistName = c.getString(c.getColumnIndex("airtistName"));
            String albumName = c.getString(c.getColumnIndex("albumName"));
            int No = c.getInt(c.getColumnIndex("No"));
            String s = "No:" + No + ", 歌名:" + musciName + ", 歌手:" + airtistName + ", 专辑:" + albumName;
            Music music = new Music();
            music.setMusciName(musciName);
            music.setAirtistName(airtistName);
            music.setAlbumName(albumName);
            musicList.add(music);
        }
//        c.close();
//        db.close();
        return musicList;
    }

    private void download(Music downloadMusic) {
        downloadUrl = downloadMusic.getPath();
        Uri resource = Uri.parse(downloadUrl);
        DownloadManager.Request request = new DownloadManager.Request(
                resource);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE
                | DownloadManager.Request.NETWORK_WIFI);
        request.setAllowedOverRoaming(false);
        // 设置文件类型
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap
                .getMimeTypeFromExtension(MimeTypeMap
                        .getFileExtensionFromUrl(downloadUrl));
        request.setMimeType(mimeString);
        // // 在通知栏中隐藏,在小米miui中会出现安全异常
        // request.setNotificationVisibility(View.GONE);
        // request.setVisibleInDownloadsUi(false);

        // 在通知栏中显示
        request.setNotificationVisibility(View.VISIBLE);
        request.setVisibleInDownloadsUi(true);
        // sdcard的目录下的download文件夹
        String musicName = downloadMusic.getMusciName() + "-" + downloadMusic.getAirtistName() + "-" + downloadMusic.getAlbumName() + ".m4a";
//        request.setDestinationInExternalPublicDir("/Download/", musicName);
        request.setDestinationInExternalPublicDir(this.getPackageName() + "/myDownLoadMusic", musicName);
        request.setTitle("新版本");
        downloadManager.enqueue(request);
        registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }


    private void deleteTable() {
        db.execSQL("DROP TABLE IF EXISTS music");
    }

    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    protected void onResume() {
        super.onResume();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
        }
    };

    //ViewHolder静态类
    static class ViewHolder {
        public TextView tv_search_list_title;
        public TextView tv_search_list_airtist;
    }

    public class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater = null;
        private int currentItemIndex = 0;

        private MyAdapter(Context context) {
            //根据context上下文加载布局，这里的是Demo17Activity本身，即this
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            //How many items are in the data set represented by this Adapter.
            //在此适配器中所代表的数据集中的条目数
            return listItem.size();
        }

        @Override
        public Object getItem(int position) {
            // Get the data item associated with the specified position in the data set.
            //获取数据集中与指定索引对应的数据项
            return position;
        }

        @Override
        public long getItemId(int position) {
            //Get the row id associated with the specified position in the list.
            //获取在列表中与指定索引对应的行id
            return position;
        }

        //Get a View that displays the data at the specified position in the data set.
        //获取一个在数据集中指定索引的视图来显示数据
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            //如果缓存convertView为空，则需要创建View
            if (convertView == null) {
                holder = new ViewHolder();
                //根据自定义的Item布局加载布局
                convertView = mInflater.inflate(R.layout.item_online_search_list, null);
                holder.tv_search_list_title = (TextView) convertView.findViewById(R.id.tv_search_list_title);
                holder.tv_search_list_airtist = (TextView) convertView.findViewById(R.id.tv_search_list_airtist);
                //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_search_list_title.setText((String) listItem.get(position).get("tv_search_list_title"));
            holder.tv_search_list_airtist.setText((String) listItem.get(position).get("tv_search_list_airtist"));


            if (position == selectItem) {
                convertView.setBackgroundColor(Color.CYAN);
            } else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }

            return convertView;
        }

        public void setSelectItem(int selectItem) {
            this.selectItem = selectItem;
        }

        private int selectItem = 0;

    }


}
