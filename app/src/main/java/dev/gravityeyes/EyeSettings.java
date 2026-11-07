package dev.gravityeyes;
import android.content.Context;
import android.content.SharedPreferences;

public final class EyeSettings {
    private static final String PREFS = "glyph_eye_settings";
    public final int duration, brightness, tilt;
    public final String closing, sleeping;
    private EyeSettings(int d,int b,int t,String c,String s){duration=d;brightness=b;tilt=t;closing=c;sleeping=s;}
    public static EyeSettings load(Context context){
        SharedPreferences p=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        return new EyeSettings(p.getInt("duration",400),p.getInt("brightness",70),p.getInt("tilt",0),
            p.getString("closing","center"),p.getString("sleeping","line"));
    }
    public static void save(Context context,int duration,int brightness,int tilt,String closing,String sleeping){
        context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit()
            .putInt("duration",duration).putInt("brightness",brightness).putInt("tilt",tilt)
            .putString("closing",closing).putString("sleeping",sleeping).apply();
    }
}
