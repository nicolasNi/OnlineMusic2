package com.lt.nexthud.onlinemusic2;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import java.io.IOException;

public class MusicService extends Service {

    private MediaPlayer player;//声明一个MediaPlayer对象

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO 自动生成的方法存根
        return null;
    }

    //创建服务
    @Override
    public void onCreate() {
        // 当player对象为空时
        if (player == null) {
//			player = MediaPlayer.create(MusicService.this, Uri
//					.parse("http://ws.stream.qqmusic.qq.com/C200001OyHbk2MSIi4.m4a?vkey=E0E1E16F18FA8F082FA0B9E82C448C13DA5ABE950F1BCA55957C877FE54909CFB8744BE9A2BFAA6076A0A519579F118520DC2C45D416F062&guid=7524721365&fromtag=30"));//实例化对象，通过播放本机服务器上的一首音乐
//			player.setLooping(false);//设置不循环播放
        }
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

    //开始服务
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO 自动生成的方法存根
        Bundle b = intent.getExtras();//获取到从MainActivity类中传递过来的Bundle对象
        String[] para = b.getStringArray("param");
        int op = Integer.parseInt(para[0]);

//		int op = b.getInt("msg");//再获取到MainActivity类中op的值
        switch (op) {
            case 1://当op为1时，即点击播放按钮时
                play(para[1]);//调用play()方法
                break;
            case 2://当op为2时，即点击暂停按钮时
                pause();//调用pause()方法
                break;
            case 3://当op为3时，即点击停止按钮时
                stop();//调用stop()方法
                break;
            default:
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    //停止播放音乐方法
    private void stop() {
        // 当player对象不为空时
        if (player != null) {
            player.seekTo(0);//设置从头开始
            player.stop();//停止播放
            try {
                player.prepare();//预加载音乐
            } catch (IllegalStateException e) {
                // TODO 自动生成的 catch 块
                e.printStackTrace();
            } catch (IOException e) {
                // TODO 自动生成的 catch 块
                e.printStackTrace();
            }
        }
    }

    //暂停播放音乐方法
    private void pause() {
        // 当player对象正在播放时并且player对象不为空时
        if (player.isPlaying() && player != null) {
            player.pause();//暂停播放音乐
        }
    }

    //播放音乐方法
    private void play(String url) {
        stop();
        player = MediaPlayer.create(MusicService.this, Uri
                .parse(url));//实例化对象，通过播放本机服务器上的一首音乐
        player.setLooping(false);//设置不循环播放

        // 当player对象不为空并且player不是正在播放时
        if (player != null && !player.isPlaying()) {
            player.start();//开始播放音乐
        }
    }

}
