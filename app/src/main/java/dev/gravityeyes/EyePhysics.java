package dev.gravityeyes;

/** Independent damped springs with circular collision bounds; coordinates in eye radii. */
public final class EyePhysics {
    public final float[] x = {0, 0}, y = {0, 0};
    private final float[] vx = {0, 0}, vy = {0, 0};
    public void step(float gx, float gy, float elapsed) {
        if (!Float.isFinite(gx) || !Float.isFinite(gy) || !Float.isFinite(elapsed)) return;
        float dt = Math.max(0, Math.min(elapsed, 0.05f));
        // Substeps keep collisions and springs stable after scheduling delays.
        int steps = Math.max(1, (int)Math.ceil(dt / 0.008f));
        float h = dt / steps;
        for (int s = 0; s < steps; s++) for (int i = 0; i < 2; i++) {
            float spring = i == 0 ? 24f : 28f;
            vx[i] += (Math.max(-1, Math.min(1, gx)) * 35 - spring*x[i] - 4.5f*vx[i]) * h;
            vy[i] += (Math.max(-1, Math.min(1, gy)) * 35 - spring*y[i] - 4.5f*vy[i]) * h;
            x[i] += vx[i]*h; y[i] += vy[i]*h;
            float r = (float)Math.hypot(x[i], y[i]);
            if (r > 1) {
                float nx = x[i]/r, ny = y[i]/r;
                x[i] = nx; y[i] = ny;
                float outward = vx[i]*nx + vy[i]*ny;
                if (outward > 0) { vx[i] -= 1.55f*outward*nx; vy[i] -= 1.55f*outward*ny; }
            }
        }
    }
}
