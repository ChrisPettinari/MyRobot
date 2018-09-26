package CPHD;


import robocode.HitByBulletEvent;
import robocode.AdvancedRobot;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import java.util.Random;

import java.awt.*;


/**
 * PaintingRobot - a sample robot that demonstrates the onPaint() and
 * getGraphics() methods.
 * Also demonstrate feature of debugging properties on RobotDialog
 * <p/>
 * Moves in a seesaw motion, and spins the gun around at each end.
 * When painting is enabled for this robot, a red circle will be painted
 * around this robot.
 *
 * @author Stefan Westen (original SGSample)
 * @author Pavel Savara (contributor)
 */
public class CPHD extends AdvancedRobot {

    /**
     * PaintingRobot's run method - Seesaw
     */

    int moveDirection=2;//which way to move


    public void run() {
        while (true) {
            //ahead(100);
            setBodyColor(Color.blue);
            setGunColor(Color.red);
            setRadarColor(Color.white);

            setAdjustGunForRobotTurn(true); // Keep the gun still when we turn
            turnRadarRightRadians(Double.POSITIVE_INFINITY);//keep turning radar right


        }
    }

    /**
     * Fire when we see a robot
     */
    // _bfWidth and _bfHeight set to battle field width and height
    private static double WALL_STICK = 140;
    int _bfHeight = 600;
    int _bfWidth = 800;
    private java.awt.geom.Rectangle2D.Double _fieldRect = new java.awt.geom.Rectangle2D.Double(18, 18, _bfWidth-36, _bfHeight-36);

// ...
    /**
     * x/y = current coordinates
     * startAngle = absolute angle that tank starts off moving - this is the angle
     *   they will be moving at if there is no wall smoothing taking place.
     * orientation = 1 if orbiting enemy clockwise, -1 if orbiting counter-clockwise
     * smoothTowardEnemy = 1 if smooth towards enemy, -1 if smooth away
     * NOTE: this method is designed based on an orbital movement system; these
     *   last 2 arguments could be simplified in any other movement system.
     */


    public double wallSmoothing(double x, double y, double startAngle,
                                int orientation, int smoothTowardEnemy) {

        double angle = startAngle;

        // in Java, (-3 MOD 4) is not 1, so make sure we have some excess
        // positivity here
        angle += (4*Math.PI);

        double testX = x + (Math.sin(angle)*WALL_STICK);
        double testY = y + (Math.cos(angle)*WALL_STICK);
        double wallDistanceX = Math.min(x - 18, _bfWidth - x - 18);
        double wallDistanceY = Math.min(y - 18, _bfHeight - y - 18);
        double testDistanceX = Math.min(testX - 18, _bfWidth - testX - 18);
        double testDistanceY = Math.min(testY - 18, _bfHeight - testY - 18);

        double adjacent = 0;
        int g = 0; // because I'm paranoid about potential infinite loops

        while (!_fieldRect.contains(testX, testY) && g++ < 25) {
            if (testDistanceY < 0 && testDistanceY < testDistanceX) {
                // wall smooth North or South wall
                angle = ((int)((angle + (Math.PI/2)) / Math.PI)) * Math.PI;
                adjacent = Math.abs(wallDistanceY);
            } else if (testDistanceX < 0 && testDistanceX <= testDistanceY) {
                // wall smooth East or West wall
                angle = (((int)(angle / Math.PI)) * Math.PI) + (Math.PI/2);
                adjacent = Math.abs(wallDistanceX);
            }

            // use your own equivalent of (1 / POSITIVE_INFINITY) instead of 0.005
            // if you want to stay closer to the wall ;)
            angle += smoothTowardEnemy*orientation*
                    (Math.abs(Math.acos(adjacent/WALL_STICK)) + 0.005);

            testX = x + (Math.sin(angle)*WALL_STICK);
            testY = y + (Math.cos(angle)*WALL_STICK);
            testDistanceX = Math.min(testX - 18, _bfWidth - testX - 18);
            testDistanceY = Math.min(testY - 18, _bfHeight - testY - 18);

            if (smoothTowardEnemy == -1) {
                // this method ended with tank smoothing away from enemy... you may
                // need to note that globally, or maybe you don't care.
            }
        }

        return angle; // you may want to normalize this
    }

    public void onScannedRobot(ScannedRobotEvent e) {

        double absBearing=e.getBearingRadians()+getHeadingRadians();//enemies absolute bearing
        double latVel=e.getVelocity() * Math.sin(e.getHeadingRadians() -absBearing);//enemies later velocity
        double gunTurnAmt;//amount to turn our gun
        setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//lock on the radar
        if(Math.random()>.9){
            setMaxVelocity((12*Math.random())+12);//randomly change speed
        }
        if (e.getDistance() > 250) {//if distance is greater than 150
            gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing- getGunHeadingRadians()+latVel/18);//amount to turn our gun, lead just a little bit
            setTurnGunRightRadians(gunTurnAmt); //turn our gun

            double startAngle = 0;

            if (Math.random() * 2 > 1){
                startAngle = robocode.util.Utils.normalRelativeAngle(absBearing-getHeadingRadians()+latVel/getVelocity()) + 40;//drive towards the enemies predicted future location
            }
            else
            {
                startAngle = robocode.util.Utils.normalRelativeAngle(absBearing-getHeadingRadians()+latVel/getVelocity()) - 40;//drive towards the enemies predicted future location
            }

            double newAngle = wallSmoothing(getX(), getY(), getHeadingRadians(), -1, 1);
            setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(newAngle));

            setAhead((e.getDistance() - 140)*moveDirection);//move forward
            setFire(2);//fire
        }
        else {//if we are close enough...
            gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + latVel / 10);//amount to turn our gun, lead just a little bit
            setTurnGunRightRadians(gunTurnAmt);//turn our gun
            setTurnLeft(-90 - e.getBearing()); //turn perpendicular to the enemy
            setAhead((e.getDistance() - 200) * moveDirection);//move forward
            setFire(3);//fire
        }
    }


    public void onHitByBullet(HitByBulletEvent e) {
        // demonstrate feature of debugging properties on RobotDialog
        setDebugProperty("lastHitBy", e.getName() + " with power of bullet " + e.getPower() + " at time " + getTime());

        // show how to remove debugging property
        setDebugProperty("lastScannedRobot", null);

        // gebugging by painting to battle view
        Graphics2D g = getGraphics();

        g.setColor(Color.orange);
        g.drawOval((int) (getX() - 55), (int) (getY() - 55), 110, 110);
        g.drawOval((int) (getX() - 56), (int) (getY() - 56), 112, 112);
        g.drawOval((int) (getX() - 59), (int) (getY() - 59), 118, 118);
        g.drawOval((int) (getX() - 60), (int) (getY() - 60), 120, 120);


    }

    /*public void onHitWall(HitWallEvent e) {
        moveDirection = -moveDirection;
    }*/


    /**
     * Paint a red circle around our PaintingRobot
     */
    public void onPaint(Graphics2D g) {
        g.setColor(Color.red);
        g.drawOval((int) (getX() - 50), (int) (getY() - 50), 100, 100);
        g.setColor(new Color(0, 0xFF, 0, 30));
        g.fillOval((int) (getX() - 60), (int) (getY() - 60), 120, 120);
    }
}
