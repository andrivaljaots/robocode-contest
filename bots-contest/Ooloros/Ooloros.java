package Ooloros;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.Color;
import dev.robocode.tankroyale.botapi.events.Condition;
import dev.robocode.tankroyale.botapi.events.HitWallEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;

import java.util.Objects;

public class Ooloros extends Bot {
    int scanTurn = 0;

    public static void main(String[] args) {
        new Ooloros().start();
    }

    Ooloros() {
        super(BotInfo.fromFile("Ooloros.json"));
    }

    @Override
    public void run() {
        // Set colors - wave the white flag
        setBodyColor(Color.WHITE);
        setGunColor(Color.WHITE);
        setRadarColor(Color.WHITE);
        setBulletColor(Color.WHITE);
        setScanColor(Color.RED);
        setGunTurnRate(100);

        goToCentre();
        while (isRunning()) {
            setForward(10_000);
            setTurnRight(10_000);
            waitFor(new Ooloros.TurnCompleteCondition(this));
        }
    }

    private void goToCentre() {
        double x;
        double y;
        while (isRunning()) {
            setAdjustGunForBodyTurn(true);
            setTurnLeft(bearingTo(getArenaWidth() * 1.0 / 2, getArenaHeight() * 1.0 / 2));
            setForward(distanceTo(getArenaWidth() * 1.0 / 2, getArenaHeight() * 1.0 / 2));
            x = getX();
            y = getY();
            rescan();
            waitFor(new Ooloros.TurnCompleteCondition(this));
            var targetX = Math.abs(x - getArenaWidth() - 2) / getArenaWidth();
            var targetY = Math.abs(y - getArenaHeight() - 2) / getArenaHeight();
            if (Math.abs(0.5 - targetX) < 0.1 && Math.abs(0.5 - targetY) < 0.1) {
                break;
            }
        }
    }

    private ScannedBotEvent prevEvent = null;

    @Override
    public void onScannedBot(ScannedBotEvent e) {
        scanTurn = e.getTurnNumber();
        setGunTurnRate(100);

        var bearingFromGun = gunBearingTo(e.getX(), e.getY());
        setTurnGunLeft(bearingFromGun);
        if (prevEvent == null || !Objects.equals(prevEvent.getScannedBotId(), e.getScannedBotId())) {
            prevEvent = e;
            return;
        }

        if (Math.abs(bearingFromGun) <= 1 && getGunHeat() == 0) {
            fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
        }

        if (bearingFromGun < 5) {
            rescan();
        }
    }

    @Override
    public void onHitWall(HitWallEvent botHitWallEvent) {
        goToCentre();
    }

    public static class TurnCompleteCondition extends Condition {

        private final Ooloros bot;

        public TurnCompleteCondition(Ooloros bot) {
            this.bot = bot;
        }

        @Override
        public boolean test() {
            if (bot.getTurnNumber() - bot.scanTurn < 20 ) {
                bot.scanTurn = bot.getTurnNumber();
                bot.setTurnGunLeft(90);
                bot.setGunTurnRate(5);
                bot.rescan();
            }
            return bot.getDistanceRemaining() == 0;
        }
    }
}
