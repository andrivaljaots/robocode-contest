package Jev;

import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

import java.util.*;

public class Jev extends Bot {

    boolean movingForward;

    // The main method starts our bot
    public static void main(String[] args) {
        new Jev().start();
    }

    // Constructor, which loads the bot config file
    Jev() {
        //super(BotInfo.fromFile("/Users/jevgeni/work/robocode-contest/bots-contest/Jev/Jev.json"));
        super(BotInfo.fromFile("Jev.json"));
    }

    // Called when a new round is started -> initialize and do some movement
    public void run() {
        // Set colors
        setBodyColor(Color.fromString("#800080"));   // lime
        setGunColor(Color.fromString("#009632"));    // green
        setRadarColor(Color.fromString("#006464"));  // dark cyan
        setBulletColor(Color.fromString("#FFFF64")); // yellow
        setScanColor(Color.fromString("#800080"));   // light red

        while (isRunning()) {
            System.out.println("Running");
            reverseDirection(200);
            double turn = Math.random() * 400;
            turnLeft(turn);
        }
    }

    // We collided with a wall -> reverse the direction
    @Override
    public void onHitWall(HitWallEvent e) {
        System.out.println("Hit wall");
        // Bounce off!
        reverseDirection(100);
    }

    @Override
    public void onBulletHitWall(BulletHitWallEvent e) {
    }

    @Override
    public void onTick(TickEvent tickEvent) {
        setColor();

        if (tickEvent.getEvents().size() <= 1) {
            return;
        }
        tickEvent.getEvents().stream()
            .filter(ScannedBotEvent.class::isInstance)
            .map(ScannedBotEvent.class::cast)
            .findFirst().ifPresent(e -> {
                double distance = distanceTo(e.getX(), e.getY());
                System.out.println("Extra fire");
                smartFire(distance);
            });
    }

    private void setColor() {
        Random randomService = new Random();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < 6) {
            sb.append(Integer.toHexString(randomService.nextInt()));
        }
        sb.setLength(6);
        Color color = Color.fromHex(sb.toString());
        setBodyColor(color);
        setRadarColor(color);
        setScanColor(color);
    }

    // ReverseDirection: Switch from ahead to back & vice versa
    public void reverseDirection(double distance) {
        System.out.println("Changing direction");
        if (movingForward) {
            setBack(distance);
            System.out.println("back");
            movingForward = false;
        } else {
            System.out.println("forward");
            setForward(distance);
            movingForward = true;
        }
    }

    // We scanned another bot -> fire!
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        System.out.println(String.format("Scanned bot %s, direction %s", e.getScannedBotId(), e.getDirection()));
        stop();
        var distance = distanceTo(e.getX(), e.getY());
        smartFire(distance);
        movingForward = true;
        forward(distance + 10);
        rescan();
        resume();
    }

    @Override
    public void onHitByBullet(BulletHitBotEvent e) {
        // Calculate the bearing to the direction of the bullet
        var bearing = calcBearing(e.getBullet().getDirection());
        // Turn 90 degrees to the bullet direction based on the bearing
        turnLeft(90 - bearing);
        reverseDirection(200);
    }

    private void smartFire(double distance) {
        if (distance > 50) {
            fire(2);
        } else {
            fire(Constants.MAX_FIREPOWER);
        }
    }

    // We hit another bot -> back up!
    @Override
    public void onHitBot(HitBotEvent e) {
        System.out.println(String.format("Hit bot %s", e.getVictimId()));
        setMaxSpeed(Constants.MAX_SPEED);
        stop();
        reverseDirection(200);
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

    public static class DistanceCompleteCondition extends Condition {

        private final IBot bot;

        public DistanceCompleteCondition(IBot bot) {
            this.bot = bot;
        }

        @Override
        public boolean test() {
            return bot.getDistanceRemaining() == 0;
        }
    }

}
