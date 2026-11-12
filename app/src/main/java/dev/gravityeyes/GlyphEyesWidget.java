package dev.gravityeyes;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.RemoteViews;

/** Home-screen mirror. Updated only while GlyphToy is sending frames. */
public final class GlyphEyesWidget extends AppWidgetProvider {
    private static final String STORE="glyph_widget_frame";
    private static final int SIZE=260;
    private static final int[] EMPTY=EyeRenderer.pixels(new EyePhysics());
    public static void show(Context context,int[] pixels){
        if(pixels==null||pixels.length!=169)return;
        // Store a compact frame so widget can restore after launcher/process restart.
        StringBuilder data=new StringBuilder(169);
        for(int p:pixels)data.append(p==Color.BLACK?'0':'1');
        context.getSharedPreferences(STORE,Context.MODE_PRIVATE).edit().putString("pixels",data.toString()).apply();
        refresh(context,pixels);
    }
    public static void showReaction(Context context,String type){
        EyeSettings settings=EyeSettings.load(context);
        EyePhysics pose=new EyePhysics();
        int[] frame=EyeReactions.apply(pose,settings,1f,type,300);
        refresh(context,frame);
        // A test remains visible until the next Glyph frame or widget refresh.
    }
    public static void refresh(Context context,int[] frame){
        AppWidgetManager manager=AppWidgetManager.getInstance(context);
        ComponentName name=new ComponentName(context,GlyphEyesWidget.class);
        int[] ids=manager.getAppWidgetIds(name);
        if(ids.length==0)return;
        Bitmap image=render(frame);
        Intent open=new Intent(context,MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent tap=PendingIntent.getActivity(context,0,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        for(int id:ids){
            RemoteViews views=new RemoteViews(context.getPackageName(),R.layout.widget_glyph_eyes);
            views.setImageViewBitmap(R.id.widget_eyes,image);
            views.setOnClickPendingIntent(R.id.widget_eyes,tap);
            manager.updateAppWidget(id,views);
        }
    }
    private static Bitmap render(int[] pixels){
        Bitmap bitmap=Bitmap.createBitmap(SIZE,SIZE,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);
        canvas.drawColor(Color.rgb(8,9,9));
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        for(int y=0;y<13;y++)for(int x=0;x<13;x++){
            float cx=x*20+10,cy=y*20+10;
            p.setColor(0xff26282a);canvas.drawCircle(cx,cy,3.2f,p);
            if(pixels[y*13+x]!=Color.BLACK){
                p.setColor(0x33e6f8d9);canvas.drawCircle(cx,cy,8,p);
                p.setColor(0xffe9f5df);canvas.drawCircle(cx,cy,5.5f,p);
            }
        }
        return bitmap;
    }
    @Override public void onUpdate(Context context,AppWidgetManager manager,int[] ids){
        String saved=context.getSharedPreferences(STORE,Context.MODE_PRIVATE).getString("pixels","");
        int[] pixels=EMPTY.clone();
        if(saved.length()==169)for(int i=0;i<169;i++)pixels[i]=saved.charAt(i)=='1'?Color.WHITE:Color.BLACK;
        refresh(context,pixels);
    }
}
