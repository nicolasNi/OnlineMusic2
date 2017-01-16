package com.lt.nexthud.onlinemusic2;

import java.io.Serializable;

/**
 * Created by nikaihua on 16/12/2.
 */

public class Music implements Serializable {
    /** 歌名 */
    private String musciName;
    /** 歌手名 */
    private String airtistName;
    /** 歌曲路径 */
    private String path;
    /** 专辑名 */
    private String albumName;
    /** 歌曲id */
    private String musicId;

    public int No;


    public Music(String musciName,String airtistName,String path, String albumName,String musicId)
    {
        this.musciName = musciName;
        this.airtistName = airtistName;
        this.path = path;
        this.albumName = albumName;
        this.musicId = musicId;
    }

    public Music()
    {
    }


    public String getMusciName() {
        return musciName;
    }

    public void setMusciName(String musciName) {
        this.musciName = musciName;
    }

    public String getAirtistName() {
        return airtistName;
    }

    public void setAirtistName(String airtistName) {
        this.airtistName = airtistName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }


    public String getMusicId() {
        return musicId;
    }

    public void setMusicId(String musicId) {
        this.musicId = musicId;
    }
}
