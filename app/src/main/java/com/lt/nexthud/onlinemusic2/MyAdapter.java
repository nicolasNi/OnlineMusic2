package com.lt.nexthud.onlinemusic2;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by nickolas on 2017/3/29.
 */

public class MyAdapter extends BaseAdapter {
    private Context context;
    private int resourceId;
    private List<HashMap<String, Object>> listItem;

    public MyAdapter(Context context, int resourceId, List<HashMap<String,Object>> listItem){
        this.context = context;
        this.resourceId = resourceId;
        this.listItem = listItem;
    }

    @Override
    public int getCount() {
        return listItem == null? 0 : listItem.size();
    }

    @Override
    public Object getItem(int position) {
        return listItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HashMap<String, Object> item = (HashMap<String, Object>)getItem(position);
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(resourceId, null);
            holder.tv_search_list_title = (TextView) convertView.findViewById(R.id.tv_search_list_title);
            holder.tv_search_list_airtist = (TextView) convertView.findViewById(R.id.tv_search_list_airtist);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tv_search_list_title.setText((String) item.get("tv_search_list_title"));
        holder.tv_search_list_airtist.setText((String) item.get("tv_search_list_airtist"));

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

    public int selectItem = 0;

    class ViewHolder {
        public TextView tv_search_list_title;
        public TextView tv_search_list_airtist;
    }
}

