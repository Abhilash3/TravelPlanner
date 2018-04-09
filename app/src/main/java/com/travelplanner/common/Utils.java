package com.travelplanner.common;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import java.util.function.Supplier;

public class Utils {

    private Utils() {
        throw new RuntimeException();
    }

    public static void toast(Context context, String msg, int duration) {
        new Handler(context.getMainLooper()).post(() -> Toast.makeText(context, msg, duration).show());
    }

    public static <E> AsyncTask<String, Void, E> fakeRequest(Supplier<E> supplier) {
        return new AsyncTask<String, Void, E>() {
            @Override
            protected E doInBackground(String... strings) {
                return supplier.get();
            }
        }.execute();
    }
}
