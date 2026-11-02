package dev.gravityeyes;

import android.app.Activity;
import android.os.Bundle;
import android.content.*;
import android.widget.*;

public final class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int)(24*getResources().getDisplayMetrics().density);
        layout.setPadding(padding,padding,padding,padding);
        TextView text = new TextView(this);
        text.setTextSize(20);
        text.setText("GRAVITY EYES\n\nTwo curious pupils. Tilt, bounce, settle.\n\nMade for Nothing Phone (4a) Pro.\n\nChoose Gravity Eyes in Settings → Glyph Interface → Flip to Glyph → Always-on Glyph Toy.\n\nThis experimental build still needs testing on a real phone. Nothing OS may limit animation or sensor updates while the screen is off.");
        layout.addView(text);
        Button button = new Button(this); button.setText("Open Glyph Toys");
        button.setOnClickListener(v -> {
            try { startActivity(new Intent().setComponent(new ComponentName("com.nothing.thirdparty", "com.nothing.thirdparty.matrix.toys.manager.ToysManagerActivity"))); }
            catch (ActivityNotFoundException | SecurityException e) { Toast.makeText(this,"Open Settings → Glyph Interface → Flip to Glyph manually.",Toast.LENGTH_LONG).show(); }
        });
        layout.addView(button); setContentView(layout);
    }
}
