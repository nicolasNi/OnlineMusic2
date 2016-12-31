package com.lt.nexthud.onlinemusic2;

import java.util.List;

/**
 * Created by nikaihua on 16/12/2.
 */

public interface OnLoadSearchFinishListener {

    void onLoadSucess(List<Music> musicList);

    void onLoadFiler();
}
