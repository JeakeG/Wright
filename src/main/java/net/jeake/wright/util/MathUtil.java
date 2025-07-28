package net.jeake.wright.util;

import org.joml.Quaternionf;

public class MathUtil {

    public static Quaternionf euler2Quaternion(float pitch, float yaw, float roll) {
        // Convert Euler angles (in degrees) to a quaternion
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);
        float rollRad = (float) Math.toRadians(roll);

        // Calculate quaternion components
        float cy = (float) Math.cos(yawRad * 0.5);
        float sy = (float) Math.sin(yawRad * 0.5);
        float cp = (float) Math.cos(pitchRad * 0.5);
        float sp = (float) Math.sin(pitchRad * 0.5);
        float cr = (float) Math.cos(rollRad * 0.5);
        float sr = (float) Math.sin(rollRad * 0.5);
        
        // Create quaternion from Euler angles
        return new Quaternionf(
            cr * cp * cy + sr * sp * sy,
            sr * cp * cy - cr * sp * sy,
            cr * sp * cy + sr * cp * sy,
            cr * cp * sy - sr * sp * cy
        );
    }

    public static Quaternionf euler2Quaternion(float[] angles) {
        if (angles.length != 3) {
            throw new IllegalArgumentException("Angles array must contain exactly 3 elements: [pitch, yaw, roll]");
        }
        return euler2Quaternion(angles[0], angles[1], angles[2]);
    }

    public static float[] quaternion2Euler(Quaternionf q) {
        double qx = q.x;
        double qy = q.y;
        double qz = q.z;
        double qw = q.w;

        float y, p, r;

        // Test for singularity
        double test = qx * qy + qz * qw;
        if (test > 0.499999) { // singularity at north pole
            y = (float) Math.toDegrees(2 * Math.atan2(qx, qw));
            p = (float) Math.toDegrees(Math.PI / 2);
            r = 0;
        } else if (test < -0.499999) { // singularity at south pole
            y = (float) Math.toDegrees(-2 * Math.atan2(qx, qw));
            p = (float) Math.toDegrees(-Math.PI / 2);
            r = 0;
        } else {
            double sqx = qx * qx;
            double sqy = qy * qy;
            double sqz = qz * qz;
            y = (float) Math.toDegrees(Math.atan2(2 * qy * qw - 2 * qx * qz, 1 - 2 * sqy - 2 * sqz));
            p = (float) Math.toDegrees(Math.asin(2 * test));
            r = (float) Math.toDegrees(Math.atan2(2 * qx * qw - 2 * qy * qz, 1 - 2 * sqx - 2 * sqz));
        }

        return new float[]{y, p, r};
    }

}
