package fr.leabar.leatools.maths;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class VectorUtils {
    public static Vector between(Location from, Location to) {
        return to.toVector().subtract(from.toVector());
    }

    public static Vector parabola(Location from, Location to, double gravity) {
        Vector start = from.toVector();
        Vector end = to.toVector();
        Vector delta = end.clone().subtract(start);
        Vector horizontal = delta.clone();
        horizontal.setY(0);
        double distance = horizontal.length();
        if (distance == 0) return new Vector(0, 0, 0);
        double height = delta.getY();
        double angle = Math.toRadians(45);
        double denominator = 2 * (height - distance * Math.tan(angle));
        if (denominator <= 0) {
            double speed = Math.sqrt(gravity * distance) * 1.5;
            Vector flatXZ = horizontal.normalize().multiply(speed);
            return new Vector(flatXZ.getX(), speed, flatXZ.getZ());
        }
        double velocity = Math.sqrt(gravity*distance*distance/denominator);
        Vector xz = horizontal.normalize().multiply(velocity * Math.cos(angle));
        double y = velocity * Math.sin(angle);
        return new Vector(xz.getX(), y, xz.getZ());
    }

}