package com.lt.nexthud.onlinemusic2;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    public static String UUID_URL = "http://c.y.qq.com/base/fcgi-bin/fcg_musicexpress.fcg";

    @SuppressLint("NewApi")
    public static String getMusicKey(){
        try {
            StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);

            HttpPost httpPost = new HttpPost(UUID_URL);

            ArrayList<NameValuePair> params = new ArrayList <NameValuePair>();
            params.add(new BasicNameValuePair("json", "3"));
            params.add(new BasicNameValuePair("guid", "7524721365"));
            httpPost.setEntity(new UrlEncodedFormEntity(params, org.apache.http.protocol.HTTP.UTF_8));

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpPost);
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent()));
            for (String s = reader.readLine(); s != null; s = reader.readLine()) {
                builder.append(s);
            }
            String[] msgs= split(builder.toString(), ":");

            String keyStr= msgs[ msgs.length-1];
            keyStr=keyStr.replace("\"", "");
            keyStr=keyStr.replace("});", "");
            keyStr=keyStr.replace(" ", "");
            Log.e("nexthud", "key:"+keyStr);
            return keyStr;
        } catch (Exception e) {
            Log.e("nexthud", e.getMessage());
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
            // return false;
        }
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

    public static void getIds(String input, OnLoadSearchFinishListener listener) {
        String key=getMusicKey();

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
                        String path = "http://ws.stream.qqmusic.qq.com/C200"+musicId+".m4a?vkey="+key+"&guid=7524721365&fromtag=30";
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