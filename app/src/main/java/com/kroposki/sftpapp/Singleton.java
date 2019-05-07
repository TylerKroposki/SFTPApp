package com.kroposki.sftpapp;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

//Singleton Object for Volley's RequestQueue
public class Singleton {

    private static Singleton singletonObj;
    private RequestQueue requestQueue;
    private static Context context;

    private Singleton(Context context) {
        this.context = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized Singleton getInstance(Context context) {
        if (singletonObj == null) {
            singletonObj = new Singleton(context);
        }
        return singletonObj;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request ,String tag) {
        request.setTag(tag);
        getRequestQueue().add(request);
    }
}