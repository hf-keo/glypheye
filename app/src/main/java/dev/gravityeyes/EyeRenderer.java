package dev.gravityeyes;

public final class EyeRenderer {
    private EyeRenderer() {}
    /** Open fraction: 1 = fully open, 0 = sleeping. */
    public static int[] pixels(EyePhysics p, float openness) {
        int[] out = new int[13*13];
        java.util.Arrays.fill(out, 0xff000000);
        float o = Math.max(0f, Math.min(1f, openness));
        for (int i=0; i<2; i++) {
            int cx = i == 0 ? 3 : 9;
            if (o < 0.13f) {
                // One-pixel resting eyelid.
                for (int x=cx-2; x<=cx+2; x++) out[7*13+x] = 0xffffffff;
                continue;
            }
            int height = Math.max(1, Math.round(6f*o));
            int top = 7 - height/2;
            int bottom = top + height - 1;
            for (int y=top; y<=bottom; y++) for (int x=cx-2; x<=cx+2; x++) {
                if (height >= 4 && (y==top || y==bottom) && Math.abs(x-cx)==2) continue;
                out[y*13+x] = 0xffffffff;
            }
            if (height >= 4) {
                int px = Math.max(cx-2, Math.min(cx+2, cx + Math.round(p.x[i])));
                int py = Math.max(top, Math.min(bottom, 6 + Math.round(p.y[i])));
                out[py*13+px] = 0xff000000;
                if (py+1 <= bottom) out[(py+1)*13+px] = 0xff000000;
            }
        }
        return out;
    }
    public static int[] pixels(EyePhysics p) { return pixels(p, 1f); }
}
