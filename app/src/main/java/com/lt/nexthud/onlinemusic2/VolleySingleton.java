package com.lt.nexthud.onlinemusic2;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by nickolas on 2017/4/6.
 */

public class VolleySingleton {
    private static final VolleySingleton volleySingleton = new VolleySingleton();
    private RequestQueue mRequestQueue;
    private static Context mContext;
    private VolleySingleton(){
    }

    public static VolleySingleton getInstance(Context context){
        mContext = context;
        return volleySingleton;
    }

    public RequestQueue getmRequestQueue()
    {
        if(mRequestQueue == null){
            mRequestQueue = Volley.newRequestQueue(mContext);
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request)
    {
        getmRequestQueue().add(request);
    }

}
