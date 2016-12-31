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



    /** 搜索关键字地址 */
    public static String KEY_SEARCH_URL = "http://www.xiami.com/search/song?key=";
    /** ID接口地址 */
    public static String ID_SEARCH_URL = "http://www.xiami.com/song/playlist/id/";


    public static void getIds(String input, OnLoadSearchFinishListener listener) {
//        List<String> allIds = new ArrayList<String>();
//        String key = deCondeKey(input);// 解析用户输入关键字为 UTF-8
//        Document document = null;
//        try {
//            document = Jsoup.connect(KEY_SEARCH_URL + key).get();// jsoup连接最终拼接而成的请求字符串
//            Elements elements = document.getElementsByClass("track_list");// 选择类标签
//            if (elements.size() != 0) {
//                Elements all = elements.get(0).getElementsByClass("chkbox");
//                int size = all.size();
//                for (int i = 0; i < size; i++) {
//                    String id = all.get(i).select("input").attr("value");
//                    if (!StringUtils.isEmpty(id)) {
//                        allIds.add(id);// 不为空的话加入id list中，便于初次抓取完以后统一请求
//                    }
//                }
//                if (listener != null) {
//                    if (allIds.size() == 0) {
//                        listener.onLoadFiler();// id list大小为0 说明没有获取到数据，抓取失败
//                    } else {
//                        // 统一请求id接口地址进行再次抓取
//                        listener.onLoadSucess(getOnlineSearchList(allIds));
//                    }
//                }
//            }
//
//        } catch (IOException e) {
//            listener.onLoadFiler();
//            e.printStackTrace();
//        }


        HttpURLConnection connection = null;
        String result;
        try {
            String s = "http://s.music.qq.com/fcgi-bin/music_search_new_platform?t=0&n=10&aggr=1&cr=1&loginUin=0&format=json&inCharset=GB2312&outCharset=utf-8&notice=0&platform=jqminiframe.json&needNewCode=0&p=1&catZhida=0&remoteplace=sizer.newclient.next_song&w=";
            input = URLEncoder.encode(input, "utf-8");
            s = s + input;
//            s = URLEncoder.encode(s,"UTF-8");
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
//                result = getStringFromInputStream(is);


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
                    String path = "http://ws.stream.qqmusic.qq.com/C200"+musicId+".m4a?vkey=BC7D971169A6FF737840E77762959D1F5CC6B1753096CDC097C8D4C5D4198D7CCA13B64015888E39BDDFC8665488C9A78FA38A710A0B8C65&guid=7524721365&fromtag=30";



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
