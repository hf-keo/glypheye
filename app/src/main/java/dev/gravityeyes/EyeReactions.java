package dev.gravityeyes;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;

/** Shared notification reaction state. No message content is saved. */
public final class EyeReactions {
    private static final String PREF="eye_reactions";
    public static final String CURIOUS="curious", SURPRISED="surprised", SLEEPY="sleepy";
    public static boolean enabled(Context c){return c.getSharedPreferences(PREF,0).getBoolean("enabled",false);}
    public static void setEnabled(Context c,boolean value){c.getSharedPreferences(PREF,0).edit().putBoolean("enabled",value).apply();}
    public static boolean respectDnd(Context c){return c.getSharedPreferences(PREF,0).getBoolean("dnd",true);}
    public static void setRespectDnd(Context c,boolean value){c.getSharedPreferences(PREF,0).edit().putBoolean("dnd",value).apply();}
    public static int duration(Context c){return c.getSharedPreferences(PREF,0).getInt("duration",1800);}
    public static void setDuration(Context c,int value){c.getSharedPreferences(PREF,0).edit().putInt("duration",value).apply();}
    public static int cooldown(Context c){return c.getSharedPreferences(PREF,0).getInt("cooldown",5000);}
    public static void setCooldown(Context c,int value){c.getSharedPreferences(PREF,0).edit().putInt("cooldown",value).apply();}
    public static String allowlist(Context c){return c.getSharedPreferences(PREF,0).getString("allowlist","");}
    public static void setAllowlist(Context c,String value){c.getSharedPreferences(PREF,0).edit().putString("allowlist",value).apply();}
    public static boolean allowed(Context c,String pkg){
        String names=allowlist(c).trim();
        if(names.isEmpty())return true;
        for(String name:names.split("[,\\s]+"))if(name.equalsIgnoreCase(pkg))return true;
        return false;
    }
    public static void trigger(Context c,String type,boolean test){
        if(!test&&!enabled(c))return;
        SharedPreferences p=c.getSharedPreferences(PREF,0);
        long now=SystemClock.elapsedRealtime();
        if(!test&&now-p.getLong("last",0)<cooldown(c))return;
        p.edit().putString("type",type).putLong("start",now).putLong("last",now).apply();
        // The widget can preview independently even if the Glyph Toy is not active.
        GlyphEyesWidget.showReaction(c,type);
    }
    public static String current(Context c,long now){
        SharedPreferences p=c.getSharedPreferences(PREF,0);
        long start=p.getLong("start",0);
        return start>0&&now>=start&&now-start<duration(c)?p.getString("type",""):"";
    }
    public static int[] apply(EyePhysics eyes,EyeSettings settings,float baseOpen,String type,long elapsed){
        if(type==null||type.isEmpty())return EyeRenderer.pixels(eyes,baseOpen,settings.closing,settings.sleeping,settings.tilt);
        float t=Math.max(0,elapsed)/1000f;
        EyePhysics pose=new EyePhysics();
        if(CURIOUS.equals(type)){pose.x[0]=pose.x[1]=t<0.25f?0f:(t<1.1f?1f:0f);pose.y[0]=pose.y[1]=0;}
        else if(SURPRISED.equals(type)){pose.y[0]=pose.y[1]=-0.5f;}
        else if(SLEEPY.equals(type)){pose.x[0]=pose.x[1]=0.6f;}
        float open=baseOpen;
        if(SURPRISED.equals(type))open=t<0.2f?1f:(t<0.55f||t>0.72f&&t<0.9f?0.06f:1f);
        else if(SLEEPY.equals(type))open=0.52f;
        return EyeRenderer.pixels(pose,open,settings.closing,settings.sleeping,settings.tilt);
    }
}
