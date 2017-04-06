package com.lt.nexthud.onlinemusic2;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by nickolas on 2017/4/6.
 */

public class VolleySingleton {
    private static VolleySingleton volleySingleton;
    private RequestQueue mRequestQueue;
    private Context mContext;
    private VolleySingleton(Context context){
        mContext = context;
    }

    public static synchronized VolleySingleton getInstance(Context context){
        if(volleySingleton == null)
        {
            volleySingleton = new VolleySingleton(context);
        }
        return volleySingleton;
    }

    public RequestQueue getmRequestQueue()
    {
        if(mRequestQueue == null){
            mRequestQueue = Volley.newRequestQueue(mContext);
        }
        return mRequestQueue;
    }

    public void addToRequestQueue(Request request)
    {
        getmRequestQueue().add(request);
    }

}
