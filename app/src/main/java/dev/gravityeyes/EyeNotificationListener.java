package dev.gravityeyes;

import android.app.Notification;
import android.app.NotificationManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

/** Reacts only to notification metadata; never reads or saves message text. */
public final class EyeNotificationListener extends NotificationListenerService {
    @Override public void onNotificationPosted(StatusBarNotification sbn){
        if(!EyeReactions.enabled(this)||sbn==null||getPackageName().equals(sbn.getPackageName()))return;
        if(!EyeReactions.allowed(this,sbn.getPackageName()))return;
        Notification n=sbn.getNotification();
        if(n==null||(n.flags&Notification.FLAG_ONGOING_EVENT)!=0)return;
        NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(EyeReactions.respectDnd(this)&&nm!=null&&nm.getCurrentInterruptionFilter()!=NotificationManager.INTERRUPTION_FILTER_ALL)return;
        int importance=NotificationManager.IMPORTANCE_DEFAULT;
        Ranking ranking=new Ranking();
        if(getCurrentRanking().getRanking(sbn.getKey(),ranking))importance=ranking.getImportance();
        if(importance<NotificationManager.IMPORTANCE_DEFAULT)return;
        String reaction=importance>=NotificationManager.IMPORTANCE_HIGH?EyeReactions.SURPRISED:EyeReactions.CURIOUS;
        if((n.flags&Notification.FLAG_INSISTENT)!=0)reaction=EyeReactions.SURPRISED;
        if("sleep".equals(EyeSettings.load(this).lockBehavior)){
            android.os.PowerManager pm=(android.os.PowerManager)getSystemService(POWER_SERVICE);
            if(pm!=null&&!pm.isInteractive())reaction=EyeReactions.SLEEPY;
        }
        EyeReactions.trigger(this,reaction,false);
    }
}
