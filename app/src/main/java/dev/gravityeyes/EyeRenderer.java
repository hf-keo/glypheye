package dev.gravityeyes;

public final class EyeRenderer {
    private EyeRenderer() {}
    /** A full 13 x 13 ARGB image, with dark pupils cut out of two white eyes. */
    public static int[] pixels(EyePhysics p) {
        int[] out = new int[13*13];
        java.util.Arrays.fill(out, 0xff000000);
        for (int i=0; i<2; i++) {
            int cx = i == 0 ? 3 : 9;
            for (int y=4; y<=9; y++) for (int x=cx-2; x<=cx+2; x++) {
                if ((y==4 || y==9) && Math.abs(x-cx)==2) continue;
                out[y*13+x] = 0xffffffff;
            }
            int px = cx + Math.round(p.x[i]), py = 6 + Math.round(p.y[i]);
            out[py*13+px] = 0xff000000;
            out[(py+1)*13+px] = 0xff000000;
        }
        return out;
    }
}
