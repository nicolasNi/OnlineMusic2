package com.lt.nexthud.onlinemusic2;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nikaihua on 16/12/2.
 */

public class SearchUtils {
    public static void getIds(String input, OnLoadSearchFinishListener listener) {
        HttpURLConnection connection = null;
        String result;
        try {
            String s = "http://s.music.qq.com/fcgi-bin/music_search_new_platform?t=0&n=10&aggr=1&cr=1&loginUin=0&format=json&inCharset=GB2312&outCharset=utf-8&notice=0&platform=jqminiframe.json&needNewCode=0&p=1&catZhida=0&remoteplace=sizer.newclient.next_song&w=";
            input = URLEncoder.encode(input, "utf-8");
            s = s + input;
            URL url = new URL(s);

            connection = (HttpURLConnection) url.openConnection();
            // 设置请求方法，默认是GET
            connection.setRequestMethod("GET");
            // 设置字符集
            connection.setRequestProperty("Charset", "UTF-8");
            // 设置文件类型
            connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
            // 设置请求参数，可通过Servlet的getHeader()获取
            connection.setRequestProperty("Cookie", "AppName=" + URLEncoder.encode("你好", "UTF-8"));
            // 设置自定义参数
            connection.setRequestProperty("MyProperty", "this is me!");

            if(connection.getResponseCode() == 200){
                InputStream is = connection.getInputStream();
                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                byte[] buff=new byte[1024];
                int len=-1;
                while((len=is.read(buff))!=-1){
                    baos.write(buff, 0, len);
                }
                is.close();
                String html=baos.toString();
                baos.close();
                result = html;

                try{
                    JSONObject dataJson = new JSONObject(result);
                    JSONObject songData = dataJson.getJSONObject("data");
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
                        String path = "http://ws.stream.qqmusic.qq.com/C200"+musicId+".m4a?vkey=1E6C21E8CD90E9CF5CE3996213A6E29C21543A6FDA7145787DFA30FA84BF008BC6F759874C80589352FD5D245225C6C483E07F2550B6C938&guid=7524721365&fromtag=30";
                        musicList.add(new Music(musciName,airtistName,path,albumName,musicId));
                    }
                    ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String,     Object>>();
                    for(int i=0;i<10;i++)
                    {
                        HashMap<String, Object> map = new HashMap<String, Object>();
                        map.put("tv_search_list_title", "第"+i+"行");
                        map.put("tv_search_list_airtist", "这是第"+i+"行");
                        listItem.add(map);
                    }
                    listener.onLoadSucess(musicList);
                }
                catch (Exception e)
                {
                    Log.e("1","2",e);
                };
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if(connection != null){
                connection.disconnect();
            }
        }
    }
}