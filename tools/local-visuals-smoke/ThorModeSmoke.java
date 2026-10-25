package dev.lelonio.square.visualsmoke;

import android.app.*;
import android.content.*;
import android.os.*;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/** Exercises real display switch buttons. Never issues audio, queue, seek, or preference commands. */
public final class ThorModeSmoke extends Instrumentation {
    @Override public void onCreate(Bundle args) { super.onCreate(args); start(); }
    private Class<?> cls(String name) throws Exception { return Class.forName(name,true,getTargetContext().getClassLoader()); }
    private boolean lower() throws Exception {
        Class<?> c=cls("dev.lelonio.square.thor.display.ThorPresentation");
        Object flow=c.getMethod("getMode").invoke(c.getField("INSTANCE").get(null));
        return "LOWER_ONLY".equals(cls("kotlinx.coroutines.flow.StateFlow").getMethod("getValue").invoke(flow).toString());
    }
    private AccessibilityNodeInfo find(AccessibilityNodeInfo n,String label,int minimumY) {
        if(n==null) return null;
        if(n.isVisibleToUser() && "dev.lelonio.square".contentEquals(n.getPackageName()==null?"":n.getPackageName()) &&
            (label.contentEquals(n.getContentDescription()==null?"":n.getContentDescription()) || label.contentEquals(n.getText()==null?"":n.getText()))) {
            android.graphics.Rect bounds=new android.graphics.Rect(); n.getBoundsInScreen(bounds);
            if(bounds.centerY()>=minimumY) return n;
        }
        for(int i=0;i<n.getChildCount();i++){AccessibilityNodeInfo r=find(n.getChild(i),label,minimumY);if(r!=null)return r;}
        return null;
    }
    private void click(String es,String en) throws Exception {
        long until=SystemClock.uptimeMillis()+20000;
        while(SystemClock.uptimeMillis()<until) {
            android.util.SparseArray<List<AccessibilityWindowInfo>> windows=getUiAutomation().getWindowsOnAllDisplays();
            for(int i=0;i<windows.size();i++) for(AccessibilityWindowInfo w:windows.valueAt(i)) {
                android.graphics.Rect windowBounds=new android.graphics.Rect();w.getBoundsInScreen(windowBounds);
                boolean modeButton=en.contains("screen");
                int minimumY=modeButton?0:windowBounds.top+(int)(windowBounds.height()*0.7f);
                AccessibilityNodeInfo n=find(w.getRoot(),es,minimumY); if(n==null)n=find(w.getRoot(),en,minimumY);
                while(n!=null) { if(n.isClickable()&&n.performAction(AccessibilityNodeInfo.ACTION_CLICK))return; n=n.getParent(); }
            }
            SystemClock.sleep(200);
        }
        throw new AssertionError("Switch button not reachable: "+en);
    }
    private Object controller;
    private Object invoke(String method) throws Exception {return cls("androidx.media3.session.MediaController").getMethod(method).invoke(controller);}
    private String queue() throws Exception {
        AtomicReference<String> value=new AtomicReference<>();AtomicReference<Throwable> failure=new AtomicReference<>();
        runOnMainSync(()->{try{
            int count=(Integer)invoke("getMediaItemCount");StringBuilder s=new StringBuilder();
            for(int i=0;i<count;i++) {
                Object item=cls("androidx.media3.session.MediaController").getMethod("getMediaItemAt",int.class).invoke(controller,i);
                s.append(item.getClass().getField("mediaId").get(item)).append('|');
            }
            s.append("index=").append(invoke("getCurrentMediaItemIndex"));value.set(s.toString());
        }catch(Throwable t){failure.set(t);}});
        if(failure.get()!=null)throw new AssertionError(failure.get());return value.get();
    }
    private long position() { AtomicReference<Long> v=new AtomicReference<>();runOnMainSync(()->{try{v.set((Long)invoke("getCurrentPosition"));}catch(Exception e){throw new RuntimeException(e);}});return v.get();}
    private boolean playing() { AtomicReference<Boolean> v=new AtomicReference<>();runOnMainSync(()->{try{v.set((Boolean)invoke("getPlayWhenReady"));}catch(Exception e){throw new RuntimeException(e);}});return v.get();}
    @Override public void onStart() {
        Bundle result=new Bundle();
        try {
            java.util.Map<String,java.util.Set<Activity>> active = new java.util.concurrent.ConcurrentHashMap<>();
            ((Application)getTargetContext().getApplicationContext()).registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                public void onActivityCreated(Activity a,Bundle b){active.computeIfAbsent(a.getClass().getName(),k->java.util.concurrent.ConcurrentHashMap.newKeySet()).add(a);}
                public void onActivityDestroyed(Activity a){java.util.Set<Activity> set=active.get(a.getClass().getName());if(set!=null)set.remove(a);}
                public void onActivityStarted(Activity a){} public void onActivityResumed(Activity a){} public void onActivityPaused(Activity a){} public void onActivityStopped(Activity a){} public void onActivitySaveInstanceState(Activity a,Bundle b){}
            });
            AccessibilityServiceInfo info=getUiAutomation().getServiceInfo(); info.flags|=AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;getUiAutomation().setServiceInfo(info);
            startActivitySync(new Intent().setClassName("dev.lelonio.square","dev.lelonio.square.ui.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            AtomicReference<Object> future=new AtomicReference<>();
            runOnMainSync(()->{try{
                Object token=cls("androidx.media3.session.SessionToken").getConstructor(Context.class,ComponentName.class).newInstance(getTargetContext(),new ComponentName("dev.lelonio.square","dev.lelonio.square.playback.PlaybackService"));
                Object builder=cls("androidx.media3.session.MediaController$Builder").getConstructor(Context.class,cls("androidx.media3.session.SessionToken")).newInstance(getTargetContext(),token);
                future.set(builder.getClass().getMethod("buildAsync").invoke(builder));
            }catch(Exception e){throw new RuntimeException(e);}});
            controller=((Future<?>)future.get()).get(20,TimeUnit.SECONDS);
            SystemClock.sleep(5000);
            java.util.Map<String,Activity> originalActivities=new java.util.HashMap<>();
            for(String name:new String[]{"dev.lelonio.square.ui.MainActivity","dev.lelonio.square.thor.ui.ThorActivity"}) {
                if(active.get(name)==null || active.get(name).size()!=1)throw new AssertionError("Initial activity count: "+name);
                originalActivities.put(name,active.get(name).iterator().next());
            }
            String queue=queue();if(queue.startsWith("index="))throw new AssertionError("A restored queue is required");long start=SystemClock.uptimeMillis(),at=position();boolean play=playing();
            click("Reproductor en pantalla inferior","Player on lower screen");SystemClock.sleep(2500);
            if(!lower())throw new AssertionError("Lower-only mode not selected");
            if(!queue.equals(queue()))throw new AssertionError("Queue changed in lower mode");
            for(String name:new String[]{"dev.lelonio.square.ui.MainActivity","dev.lelonio.square.thor.ui.ThorActivity"})
                if(active.get(name)==null || active.get(name).size()!=1 || !active.get(name).contains(originalActivities.get(name)))throw new AssertionError("Activity identity in lower mode: "+name);
            click("Cola","Queue");
            SystemClock.sleep(1000);
            click("Efectos","Effects");
            SystemClock.sleep(1000);
            click("Créditos","Credits");
            SystemClock.sleep(1000);
            click("Letra","Lyrics");
            SystemClock.sleep(1000);
            click("Cola","Queue");
            SystemClock.sleep(3000);
            Bundle lowerStatus=new Bundle();lowerStatus.putString("stream","PASS lower mode: one MainActivity, one ThorActivity; queue preserved; touch Queue/Effects/Credits/Lyrics\n");sendStatus(0,lowerStatus);
            SystemClock.sleep(8000);
            android.graphics.Bitmap shot=getUiAutomation().takeScreenshot();
            if(shot!=null)try(java.io.FileOutputStream f=new java.io.FileOutputStream(new java.io.File(getTargetContext().getCacheDir(),"thor-lower-mode.png"))){shot.compress(android.graphics.Bitmap.CompressFormat.PNG,100,f);}
            click("Restaurar modo dual-screen","Restore dual-screen mode");SystemClock.sleep(2500);
            if(lower())throw new AssertionError("Dual mode not restored");
            for(String name:new String[]{"dev.lelonio.square.ui.MainActivity","dev.lelonio.square.thor.ui.ThorActivity"})
                if(active.get(name)==null || active.get(name).size()!=1 || !active.get(name).contains(originalActivities.get(name)))throw new AssertionError("Activity identity in dual mode: "+name);
            if(!queue.equals(queue()))throw new AssertionError("Queue changed after restoration");
            long delta=position()-at;long expected=play?SystemClock.uptimeMillis()-start:0;
            if(Math.abs(delta-expected)>5000)throw new AssertionError("Playback position jumped");
            if(play!=playing())throw new AssertionError("Playback intent changed");
            Class<?> diagnostic=cls("dev.lelonio.square.data.local.LocalVisualDiagnostics");
            Class<?> stage=cls("dev.lelonio.square.data.local.LocalVisualDiagnostics$Stage");
            Object firstFrame=java.lang.Enum.valueOf((Class)stage,"FIRST_FRAME");
            Object delivered=java.lang.Enum.valueOf((Class)stage,"URL_DELIVERED");
            Object diagnosticObject=diagnostic.getField("INSTANCE").get(null);
            long deadline=SystemClock.uptimeMillis()+15000;
            while((Integer)diagnostic.getMethod("count",stage).invoke(diagnosticObject,firstFrame)==0 && SystemClock.uptimeMillis()<deadline)SystemClock.sleep(200);
            if((Integer)diagnostic.getMethod("count",stage).invoke(diagnosticObject,firstFrame)==0 ||
                (Integer)diagnostic.getMethod("count",stage).invoke(diagnosticObject,delivered)==0)throw new AssertionError("Normal UI did not deliver and render Motion");
            result.putString("stream","PASS dual -> lower -> dual via real buttons; one activity per role; queue/index, position and play intent preserved; normal UI URL_DELIVERED and FIRST_FRAME\n");finish(Activity.RESULT_OK,result);
        } catch(Throwable error) {result.putString("stream","FAIL "+error+"\n");finish(Activity.RESULT_CANCELED,result);}
        finally { if(controller!=null)runOnMainSync(()->{try{invoke("release");}catch(Exception ignored){}}); }
    }
}
