package Pytsekas;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.Color;
import dev.robocode.tankroyale.botapi.IBot;
import dev.robocode.tankroyale.botapi.events.BulletHitBotEvent;
import dev.robocode.tankroyale.botapi.events.Condition;
import dev.robocode.tankroyale.botapi.events.HitBotEvent;
import dev.robocode.tankroyale.botapi.events.HitWallEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;
import dev.robocode.tankroyale.botapi.events.TickEvent;

public class Pytsekas extends Bot {

    private static final long LONG_STEP = 40000;
    private static final int DEATHMATCH_COUNT = 2;
    private static final int HIT_THE_WALL_ANGLE = 135;

    private static double DRUNKEN_DRIVE_ANGLE = 90;

    boolean movingForward;

    public static void main(String[] args) {
        new Pytsekas().start();
    }

    Pytsekas() {
        super(BotInfo.fromFile("Pytsekas.json"));
    }

    public void run() {
        setColors();
        while (isRunning()) {
            debug();

            setForward(LONG_STEP);
            movingForward = true;

            //DRUNKEN_DRIVE_ANGLE = getRandomBetween(45, 135);
            setTurnRight(DRUNKEN_DRIVE_ANGLE);
            waitFor(new TurnCompleteCondition(this));
            setTurnLeft(DRUNKEN_DRIVE_ANGLE);
            waitFor(new TurnCompleteCondition(this));
        }
    }

    @Override
    public void onTick(TickEvent tickEvent) {
        paintTank();
        if (this.getEnemyCount() >= DEATHMATCH_COUNT) {
            fire(1);
        }
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        reverseDirection(Direction.LEFT, HIT_THE_WALL_ANGLE);
    }

    @Override
    public void onScannedBot(ScannedBotEvent e) {
        setForward(LONG_STEP);

        double bearing = bearingTo(e.getX(), e.getY());

        turnLeft(e.getSpeed() * bearing);

        if (distanceTo(e.getX(), e.getY()) > 200) {
            waitFor(new TurnCompleteCondition(this));
        }

        //turnGunLeft(bearing * e.getSpeed());
        //waitFor(new TurnGunCompleteCondition(this));

        hitHardWhenBearingIsGoooooooood(e.getX(), e.getY());
    }

    @Override
    public void onHitBot(HitBotEvent e) {
        hitHardWhenBearingIsGoooooooood(e.getX(), e.getY());

        System.out.println("get out of my WAYYY sucker!");
        reverseDirection(Direction.RIGHT, 45);
    }

    @Override
    public void onHitByBullet(BulletHitBotEvent bulletHitBotEvent) {
        reverseDirection(Direction.LEFT, 90);
    }

    public void reverseDirection(Direction direction, long angle) {
        if (movingForward) {
            setBack(LONG_STEP);
        } else {
            setForward(LONG_STEP);
        }
        movingForward = !movingForward;

        if (angle > 0) {
            if (direction.equals(Direction.LEFT)) {
                setTurnLeft(angle);
            } else {
                setTurnRight(angle);
            }
            waitFor(new TurnCompleteCondition(this));
        }
    }

    private void hitHardWhenBearingIsGoooooooood(double x, double y) {
        var direction = directionTo(x, y);
        var bearing = calcBearing(direction);
        var firepower = getFirepower(distanceTo(x, y));

        if (bearing > -10 && bearing < 10) {
            fire(firepower);
        }
    }

    private double getFirepower(double distance) {
        if (distance < 100) {
            return 3;
        } else if (distance < 200) {
            return 3;
        } else if (distance < 300) {
            return 2;
        } else {
            return 0.5;
        }
    }

    public static class TurnGunCompleteCondition extends Condition {

        private final IBot bot;

        public TurnGunCompleteCondition(IBot bot) {
            this.bot = bot;
        }

        @Override
        public boolean test() {
            return bot.getGunTurnRemaining() == 0;
        }
    }

    public static class TurnCompleteCondition extends Condition {

        private final IBot bot;

        public TurnCompleteCondition(IBot bot) {
            this.bot = bot;
        }

        @Override
        public boolean test() {
            return bot.getTurnRemaining() == 0;
        }
    }

    private void debug() {
        System.out.println(
            "STATUS: enemies left: " + getEnemyCount() +
                ", X: " + this.getX() +
                ", Y:" + this.getY() +
                ", energy: " + this.getEnergy() +
                ", gun head: " + this.getGunHeat() +
                ", cooling rate: " + this.getGunCoolingRate());
    }

    private void setColors() {
        setBodyColor(Color.fromString("#ff00fb"));
        setGunColor(Color.fromString("#ff00fb"));    // green
        setRadarColor(Color.fromString("#006464"));  // dark cyan
        setBulletColor(Color.fromString("#ffffff")); // yellow
        setScanColor(Color.fromString("#FFC8C8"));   // light red
    }

    private void paintTank() {
        Color bodyColor = this.getBodyColor();
        if (bodyColor != null) {
            Color color = this.getBodyColor().equals(Color.BLACK) ? Color.WHITE : Color.BLACK;
            setBodyColor(color);
            setGunColor(color);
        }
    }

    private enum Direction {
        LEFT, RIGHT;
    }
}
