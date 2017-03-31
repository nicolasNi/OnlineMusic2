package com.lt.nexthud.onlinemusic2;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

public class MusicService extends Service {

    private MediaPlayer player;//声明一个MediaPlayer对象
    private String preUri;
    private ControlMusicBinder musicBinder = new ControlMusicBinder();

    public class ControlMusicBinder extends Binder{
        public void playMusic(final String uri)
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(preUri != null && preUri.equals(uri)){
                        if(player != null && player.isPlaying()){
                            player.pause();
                        }
                        else {
                            player.start();
                        }
                    }
                    else {
                        try{
                            pauseMusic();
                            player.reset();
                            player.setDataSource(MusicService.this,Uri.parse(uri));
                            player.prepare();
                            player.start();
                            preUri = uri;
                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        public void pauseMusic()
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(player!=null && player.isPlaying()){
                        player.pause();
                    }
                }
            }).start();

        }
    }
    @Override
    public IBinder onBind(Intent arg0) {
        // TODO 自动生成的方法存根
        return musicBinder;
    }

    //创建服务
    @Override
    public void onCreate() {
        player = new MediaPlayer();
        super.onCreate();
    }

    //销毁服务
    @Override
    public void onDestroy() {
        //当对象不为空时
        if (player != null) {
            player.stop();//停止播放
            player.release();//释放资源
            player = null;//把player对象设置为null
        }
        super.onDestroy();
    }
}
