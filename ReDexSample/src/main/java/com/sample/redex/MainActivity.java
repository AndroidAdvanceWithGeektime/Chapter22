package com.sample.redex;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("test", getStackTraceString(new Throwable()));

    }

    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        Throwable t0 = tr;
        while (t0 != null) {
            if (t0 instanceof UnknownHostException) {
                return "";
            }
            t0 = t0.getCause();
        }

        StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, false);
        Set<Throwable> dejaVu =
          Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
        dejaVu.add(tr);

        pw.println(tr);
        StackTraceElement[] trace = tr.getStackTrace();
        for (StackTraceElement traceElement : trace)
            pw.println("\tat " + traceElement);

        Throwable ourCause = tr.getCause();
        while (ourCause != null) {
            printEnclosedStackTrace(ourCause, pw, trace, "Caused by: ", "", dejaVu);
            ourCause = ourCause.getCause();
        }

        pw.flush();
        return sw.toString();
    }

    private static void printEnclosedStackTrace(Throwable t, PrintWriter s,
                                                StackTraceElement[] enclosingTrace,
                                                String caption,
                                                String prefix,
                                                Set<Throwable> dejaVu) {
        if (dejaVu.contains(t)) {
            s.println("\t[CIRCULAR REFERENCE:" + t + "]");
        } else {
            dejaVu.add(t);
            StackTraceElement[] trace = t.getStackTrace();
            int m = trace.length - 1;
            int n = enclosingTrace.length - 1;
            while (m >= 0 && n >=0 && trace[m].equals(enclosingTrace[n])) {
                m--; n--;
            }
            int framesInCommon = trace.length - 1 - m;

            s.println(prefix + caption + t);
            for (int i = 0; i <= m; i++)
                s.println(prefix + "\tat " + trace[i]);
            if (framesInCommon != 0)
                s.println(prefix + "\t... " + framesInCommon + " more");
        }
    }
}
