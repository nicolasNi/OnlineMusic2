package com.lt.nexthud.onlinemusic2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView lvSearchReasult;
    private ProgressDialog dialog;
    private List<Music> listSearchResult;
    private ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String,     Object>>();
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        btSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                dialog.show();// 进入加载状态，显示进度条
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        SearchUtils.getIds(edtKey.getText().toString(),
                                new OnLoadSearchFinishListener() {

                                    @Override
                                    public void onLoadSucess(
                                            List<Music> musicList) {
                                        dialog.dismiss();// 加载完成，取消进度条
                                        Message msg = new Message();
                                        msg.what = 0;
                                        mHandler.sendMessage(msg);
                                        listSearchResult = musicList;
                                    }

                                    @Override
                                    public void onLoadFiler() {
                                        dialog.dismiss();// 加载失败，取消进度条
                                        Toast.makeText(MainActivity.this,
                                                "加载失败", Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                });

                    }
                }).start();


            }
        });

    }


    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    for (int i =0; i<listSearchResult.size(); i++   )
                    {
                        HashMap<String, Object> map = new HashMap<String, Object>();
                        map.put("tv_search_list_title", listSearchResult.get(i).getMusciName());
                        map.put("tv_search_list_airtist", listSearchResult.get(i).getAirtistName()+"   《"+ listSearchResult.get(i).getAlbumName() + "》");
                        listItem.add(map);
                    }

                    adapter = new MyAdapter(MainActivity.this);
                    lvSearchReasult.setAdapter(adapter);
//                    Music m = listSearchResult.get(0);
//                    String keyword = m.getMusciName();
//                    Toast.makeText(MainActivity.this,
//                            keyword, Toast.LENGTH_SHORT)
//                            .show();
                    break;
            }
        };
    };


    public void click(View v){
        Intent intent = new Intent(MainActivity.this, MusicService.class);//实例化一个Intent对象
        String[] param={"-1",""};
        int op = -1;//设置中间变量op
        String url = listSearchResult.get(adapter.selectItem).getPath();
        switch (v.getId()) {
            case R.id.imageButton://当点击的为上一首按钮时
                adapter.selectItem -=1;
                adapter.setSelectItem(adapter.selectItem);
                adapter.notifyDataSetInvalidated();
                op = 1;
                break;
            case R.id.imageButton2://当点击播放按钮按钮时
                op = 2;
                param[0]="1";
                param[1]="http://ws.stream.qqmusic.qq.com/C200001OyHbk2MSIi4.m4a?vkey=BC7D971169A6FF737840E77762959D1F5CC6B1753096CDC097C8D4C5D4198D7CCA13B64015888E39BDDFC8665488C9A78FA38A710A0B8C65&guid=7524721365&fromtag=30";
                param[1]=url;
                break;
            case R.id.imageButton3://当点击暂停按钮时
                op = 3;
                param[0]="2";
                break;
            case R.id.imageButton4://当点击下一首按钮
                adapter.selectItem +=1;
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

    //ViewHolder静态类
    static class ViewHolder
    {
        public TextView tv_search_list_title;
        public TextView tv_search_list_airtist;
    }

    public class MyAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater = null;
        private int currentItemIndex = 0;
        private MyAdapter(Context context)
        {
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
            if(convertView == null)
            {
                holder = new ViewHolder();
                //根据自定义的Item布局加载布局
                convertView = mInflater.inflate(R.layout.item_online_search_list, null);
                holder.tv_search_list_title = (TextView)convertView.findViewById(R.id.tv_search_list_title);
                holder.tv_search_list_airtist = (TextView)convertView.findViewById(R.id.tv_search_list_airtist);
                //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
                convertView.setTag(holder);
            }else
            {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.tv_search_list_title.setText((String)listItem.get(position).get("tv_search_list_title"));
            holder.tv_search_list_airtist.setText((String)listItem.get(position).get("tv_search_list_airtist"));


            if (position == selectItem) {
                convertView.setBackgroundColor(Color.CYAN);
            }
            else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }

            return convertView;
        }

        public  void setSelectItem(int selectItem) {
            this.selectItem = selectItem;
        }

        private int  selectItem=0;

    }


}
