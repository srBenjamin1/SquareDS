package dev.lelonio.square.visualsmoke;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityNodeInfo;

/** Read-only visual probe: resolves Mama and renders MotionCover, without audio/session commands. */
public final class MotionSmoke extends Instrumentation {
    @Override public void onCreate(Bundle args) { super.onCreate(args); start(); }
    private boolean contains(AccessibilityNodeInfo n, String text) {
        if (n == null) return false;
        if (n.getText() != null && n.getText().toString().contains(text)) return true;
        for (int i=0;i<n.getChildCount();i++) if (contains(n.getChild(i),text)) return true;
        return false;
    }
    @Override public void onStart() {
        Activity activity = null;
        Bundle result = new Bundle();
        try {
            activity = startActivitySync(new Intent().setClassName("dev.lelonio.square", "dev.lelonio.square.debug.MotionProbeActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            long end=SystemClock.uptimeMillis()+120000;
            while (SystemClock.uptimeMillis()<end) {
                AccessibilityNodeInfo root=getUiAutomation().getRootInActiveWindow();
                if (contains(root,"PASS Motion first frame")) {
                    SystemClock.sleep(750);
                    android.graphics.Bitmap shot=getUiAutomation().takeScreenshot();
                    if(shot!=null)try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(getTargetContext().getCacheDir(),"motion-first-frame.png"))){shot.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);}
                    result.putString("stream","PASS Mama album -> policy -> MotionCover -> FIRST_FRAME\n"); finish(Activity.RESULT_OK,result); return; }
                if (contains(root,"FAIL Motion")) throw new AssertionError("Visual probe reported failure");
                SystemClock.sleep(250);
            }
            throw new AssertionError("No first frame within 120 seconds");
        } catch (Throwable error) { result.putString("stream","FAIL "+error+"\n"); finish(Activity.RESULT_CANCELED,result); }
        finally { if (activity!=null) { final Activity a=activity; runOnMainSync(a::finish); } }
    }
}
