package dev.gravityeyes;
import android.content.Context;
import android.content.SharedPreferences;

public final class EyeSettings {
    private static final String PREFS = "glyph_eye_settings";
    public final int duration, brightness, tilt, wanderInterval, blinkInterval, blinkDuration;
    public final boolean randomBlink;
    public final String closing, sleeping, motion, lockBehavior;
    private EyeSettings(int d,int b,int t,int w,int bi,int bd,boolean rb,String c,String s,String m,String l){
        duration=d;brightness=b;tilt=t;wanderInterval=w;blinkInterval=bi;blinkDuration=bd;randomBlink=rb;closing=c;sleeping=s;motion=m;lockBehavior=l;
    }
    public static EyeSettings load(Context context){
        SharedPreferences p=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        return new EyeSettings(p.getInt("duration",400),p.getInt("brightness",70),p.getInt("tilt",0),
            p.getInt("wanderInterval",1800),p.getInt("blinkInterval",5000),p.getInt("blinkDuration",180),p.getBoolean("randomBlink",false),p.getString("closing","center"),p.getString("sleeping","line"),
            p.getString("motion","gravity"),p.getString("lockBehavior","sleep"));
    }
    public static void save(Context context,int duration,int brightness,int tilt,String closing,String sleeping,
                            String motion,String lockBehavior,int wanderInterval,boolean randomBlink,int blinkInterval,int blinkDuration){
        context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit()
            .putInt("duration",duration).putInt("brightness",brightness).putInt("tilt",tilt)
            .putString("closing",closing).putString("sleeping",sleeping)
            .putString("motion",motion).putString("lockBehavior",lockBehavior)
            .putInt("wanderInterval",wanderInterval).putBoolean("randomBlink",randomBlink)
            .putInt("blinkInterval",blinkInterval).putInt("blinkDuration",blinkDuration).apply();
    }
}
