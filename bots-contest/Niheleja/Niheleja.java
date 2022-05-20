package Niheleja;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.Color;
import dev.robocode.tankroyale.botapi.IBot;
import dev.robocode.tankroyale.botapi.events.Condition;
import dev.robocode.tankroyale.botapi.events.CustomEvent;
import dev.robocode.tankroyale.botapi.events.HitBotEvent;
import dev.robocode.tankroyale.botapi.events.HitWallEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

public class Niheleja extends Bot {

    boolean movingForward;
    Point2D.Double knownDestination = new Point2D.Double(Math.random()*500, Math.random()*500);
    int locationRange = 50;
    int spinCount = 0;

    // The main method starts our bot
    public static void main(String[] args) {
        new Niheleja().start();
    }

    // Constructor, which loads the bot config file
    Niheleja() {
        super(BotInfo.fromFile("Niheleja.json"));
    }

    // Called when a new round is started -> initialize and do some movement
    public void run() {
        // Set colors
        setBodyColor(Color.BLACK);   // lime
        setGunColor(Color.BLACK);    // green
        setRadarColor(Color.RED);  // dark cyan
        setBulletColor(Color.fromString("#FFFF64")); // yellow
        setScanColor(Color.fromString("#FFC8C8"));   // light red

        addCustomEvent(
                new Condition("in-limbo") {
                    public boolean test() {
                        return Math.abs(knownDestination.x - getX()) <= locationRange && Math.abs(knownDestination.y - getY()) <= locationRange;
                    }
                });

        addCustomEvent(
                new Condition("too-close-to-wall") {
                    public boolean test() {
                        return (Math.abs(getArenaWidth() - getX()) <= locationRange
                                || Math.abs(getArenaHeight() - getY()) <= locationRange
                                || getX() <= locationRange || getY() <= locationRange);
                    }
                });

        // Loop while as long as the bot is running
        while (isRunning()) {
            // Tell the game we will want to move ahead 40000 -- some large number

            decideWhereToGoAndGo(knownDestination.getX(), knownDestination.getY());
            movingForward = true;
            if (spinCount < 500) {
                setTurnRight(10);
                spinCount++;
                waitFor(new TurnCompleteCondition(this));
            }
            rescan();
        }
    }

    @Override
    public void onCustomEvent(CustomEvent e) {
        // Check if our custom event "trigger-hit" went off
        if (e.getCondition().getName().equals("in-limbo")) {

            // Move around a bit
            decideWhereToGoAndGo(Math.random()*getArenaWidth(), Math.random()*getArenaWidth());
        }
        if (e.getCondition().getName().equals("too-close-to-wall")){
            knownDestination.setLocation(350,350);
        }
    }



    // We collided with a wall -> reverse the direction
    @Override
    public void onHitWall(HitWallEvent e) {
        // Bounce off!
        reverseDirection();
    }

    // ReverseDirection: Switch from ahead to back & vice versa
    public void reverseDirection() {
        if (movingForward) {
            setBack(200);
            turnLeft(getRandomSign()*280L);
            movingForward = false;
        } else {
            setForward(200);
            turnLeft(getRandomSign()*280L);
            movingForward = true;
        }
    }

    // We scanned another bot -> fire!
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        fire(1);
        var x = getX();
        var y = getY();
        knownDestination.setLocation(x, y);
    }

    private void decideWhereToGoAndGo(double x, double y) {
        var location = new Point2D.Double();
        location.setLocation(this.getX(), this.getY());
        var destination = new Point2D.Double();
        destination.setLocation(x,y);
        int circleDirection = 1;
        destination = wallSmoothing(location, destination, circleDirection, 200);
        spinCount = 0;
        goTo(destination.getX(), destination.getY());
    }

    public int getRandomSign() {
        java.util.Random rand = new Random();
        if(rand.nextBoolean())
            return -1;
        else
            return 1;
    }

    // We hit another bot -> back up!
    @Override
    public void onHitBot(HitBotEvent e) {
        if (e.isRammed()) {
            turnLeft(100);
            var x = getX();
            var y = getY();
            knownDestination.setLocation(x, y);
        }
    }

    // Condition that is triggered when the turning is complete
    public static class TurnCompleteCondition extends Condition {

        private final IBot bot;

        public TurnCompleteCondition(IBot bot) {
            this.bot = bot;
        }

        @Override
        public boolean test() {
            // turn is complete when the remainder of the turn is zero
            return bot.getTurnRemaining() == 0;
        }
    }

    /**
     * Fast Exact Wall Smoothing.
     * @param location the robot's current location point
     * @param destination the destination point you want to go to
     * @param circleDirection 1 or -1, for clock-wise or counter-clockwise wall smoothing
     * @param wallStick the length of the wall stick
     * @return the new wall smoothed destination point
     */
    public Point2D.Double wallSmoothing(Point2D.Double location, Point2D.Double destination, int circleDirection, double wallStick) {
        double battleFieldWidth = getArenaWidth();
        double battleFieldHeight = getArenaHeight();
        Rectangle2D.Double battleField = new Rectangle2D.Double(18, 18, battleFieldWidth - 36, battleFieldHeight - 36);
        Point2D.Double p = new Point2D.Double(destination.x, destination.y);
        for (int i = 0; !battleField.contains(p) && i < 4; i++) {
            if (p.x < 18) {
                p.x = 18;
                double a = location.x - 18;
                p.y = location.y + circleDirection * Math.sqrt(wallStick * wallStick - a * a);
            } else if (p.y > battleFieldHeight - 18) {
                p.y = battleFieldHeight - 18;
                double a = battleFieldHeight - 18 - location.y;
                p.x = location.x + circleDirection * Math.sqrt(wallStick * wallStick - a * a);
            } else if (p.x > battleFieldWidth - 18) {
                p.x = battleFieldWidth - 18;
                double a = battleFieldWidth - 18 - location.x;
                p.y = location.y - circleDirection * Math.sqrt(wallStick * wallStick - a * a);
            } else if (p.y < 18) {
                p.y = 18;
                double a = location.y - 18;
                p.x = location.x - circleDirection * Math.sqrt(wallStick * wallStick - a * a);
            }
        }
        return p;
    }

    private void goTo(double x, double y) {
        double a;

        var heading = directionTo(x, y);
        turnRight(Math.tan(
                a = Math.atan2(x -= (int) getX(), y -= (int) getY())*57
                        - heading));
        forward(Math.hypot(x, y) * Math.cos(a));
    }
}
