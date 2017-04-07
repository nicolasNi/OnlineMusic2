package com.lt.nexthud.onlinemusic2;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nikaihua on 16/12/2.
 */

public class SearchUtils {
    public static String UUID_URL = "http://c.y.qq.com/base/fcgi-bin/fcg_musicexpress.fcg";
    private  String musicKey;
    private Context mContext;
    private VolleySingleton mVolleySingleton;

    public SearchUtils(Context context){
        mContext = context;
        mVolleySingleton = VolleySingleton.getInstance(mContext);
    }

    private void getMusicKey(){
        try {
            StringRequest musicKeyStringRequest = new StringRequest(Request.Method.POST,
                    UUID_URL,
                    new Response.Listener<String>(){
                        @Override
                        public void onResponse(String s) {
                            musicKey = parseFromMusicKeyResponse(s);
                        }
                    },
                    new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                        }
                    }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> param = new HashMap<String,String>();
                    param.put("json", "3");
                    param.put("guid", "7524721365");
                    return param;
                }
            };
            mVolleySingleton.addToRequestQueue(musicKeyStringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String parseFromMusicKeyResponse(String musicKeyResponse)
    {
        String[] msgs= split(musicKeyResponse.toString(), ":");

        String keyStr= msgs[ msgs.length-1];
        keyStr=keyStr.replace("\"", "");
        keyStr=keyStr.replace("});", "");
        keyStr=keyStr.replace(" ", "");
        return keyStr;
    }

    public static String[] split(String str, String splitsign) {
        int index;
        if (str == null || splitsign == null)
            return null;
        ArrayList al = new ArrayList();
        while ((index = str.indexOf(splitsign)) != -1) {
            al.add(str.substring(0, index));
            str = str.substring(index + splitsign.length());
        }
        al.add(str);
        return (String[]) al.toArray(new String[0]);
    }

    private void parseFromSearchMusicResult(JSONObject result, OnLoadSearchFinishListener listener)
    {
        getMusicKey();
        String key=musicKey;
        try{
            JSONObject songData = result.getJSONObject("data");
            JSONObject song = songData.getJSONObject("song");
            JSONArray listObj = (JSONArray)song.getJSONArray("list");

            List<Music> musicList = new ArrayList<>();
            for(int i=0; i < listObj.length()-1; i++)
            {
                JSONObject music =(JSONObject)listObj.get(i);
                String musciName = music.getString("fsong");
                String airtistName = music.getString("fsinger");
                String albumName = music.getString("albumName_hilight");
                if (albumName.contains(">"))
                {
                    albumName = albumName.substring(albumName.indexOf(">")+1);
                    albumName = albumName.substring(0, albumName.indexOf("<"));
                }
                String f = music.getString("f");
                String[] fForSongID = f.split("\\u007C");
                String musicId = fForSongID[20];
                String path = "http://ws.stream.qqmusic.qq.com/C200"+musicId+".m4a?vkey="+key+"&guid=7524721365&fromtag=30";
                musicList.add(new Music(musciName,airtistName,path,albumName,musicId));
            }
            listener.onLoadSucess(musicList);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void getMusics(String input, final OnLoadSearchFinishListener listener) {
        try {
            String s = "http://s.music.qq.com/fcgi-bin/music_search_new_platform?t=0&n=10&aggr=1&cr=1&loginUin=0&format=json&inCharset=GB2312&outCharset=utf-8&notice=0&platform=jqminiframe.json&needNewCode=0&p=1&catZhida=0&remoteplace=sizer.newclient.next_song&w=";
            input = URLEncoder.encode(input, "utf-8");
            s = s + input;
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(s,null,
                    new Response.Listener<JSONObject>(){
                        @Override
                        public void onResponse(JSONObject reponse) {
                            parseFromSearchMusicResult(reponse, listener);
                        }
                    },
                    new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });
            mVolleySingleton.addToRequestQueue(jsonObjectRequest);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}