package BotName;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.Color;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;

// ------------------------------------------------------------------
// BotName
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
// Ported to Robocode Tank Royale by Flemming N. Larsen.
//
// Probably the first bot you will learn about.
// Moves in a seesaw motion, and spins the gun around at each end.
// ------------------------------------------------------------------
public class BotName extends Bot {

    int turnDirection = 1; // clockwise (-1) or counterclockwise (1)
    int moveAmount = 0;

    // The main method starts our bot
    public static void main(String[] args) {
        new BotName().start();
    }

    // Constructor, which loads the bot config file
    BotName() {
        super(BotInfo.fromFile("BotName.json"));
    }

    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
        setBodyColor(Color.fromString("#63C5DA"));
        setGunColor(Color.fromString("#C06C84"));
        setTurretColor(Color.fromString("#FFFF00"));
        setRadarColor(Color.fromString("#800000"));
        setBulletColor(Color.fromString("#FFFF00"));
        setScanColor(Color.fromString("#FFFFFF"));
        // Repeat while the bot is running
        boolean shouldTurnRight = true;
        boolean isWallMode = false;
        moveAmount = Math.min(getArenaWidth(), getArenaHeight()) - 150;

        while (isRunning()) {
            if (getEnemyCount() == 1) {
                beRamBot();
                continue;
            }

            if (getEnergy() < 20) {
                if (!isWallMode) {
                    transformIntoWall();
                    isWallMode = true;
                }
                moveNearWall();
                continue;
            }

            forward(200);


            if (shouldTurnRight) {
                turnGunRight(80);
            } else {
                turnGunLeft(80);
            }
            shouldTurnRight = !shouldTurnRight;

            turnRight(90);
        }
    }

    private void beRamBot() {
        turnLeft(180 * turnDirection);
    }


    private void transformIntoWall() {

        // Initialize peek to false

        // turn to face a wall.
        // `getDirection() % 90` means the remainder of getDirection() divided by 90.
        turnRight(getDirection() % 90);
        forward(moveAmount);
        turnRight(90);
    }

    private void moveNearWall() {
        // Move up the wall
        forward(moveAmount);

        // Turn to the next wall
        if (getDirection() % 90 != 0) {
            turnRight(getDirection() % 90);
        } else {
            turnRight(90);
        }

    }


    @Override
    public void onScannedBot(ScannedBotEvent e) {
        if (getEnemyCount() == 1) {
            getEvents().clear();
            if (e.getTurnNumber() == getTurnNumber()) {
                turnToFaceTarget(e.getX(), e.getY());

                double distance = distanceTo(e.getX(), e.getY());
                if (getEnergy() > 5) {
                    fire(1);
                } else {
                    fire(0.3);
                }

                forward(distance);

                rescan(); // Might want to move forward again!
            }
            return;
        }

        double distance = distanceTo(e.getX(), e.getY());

        if (getEnergy() > 15) {
            if (distance < 100) {
                fire(3);
                return;
            }

            fire(1);
            rescan();

            return;
        }

        if (distance < 200 && getEnergy() > 5) {
            fire(1);
            return;
        }
    }

    // Method that turns the bot to face the target at coordinate x,y, but also sets the
    // default turn direction used if no bot is being scanned within in the run() method.
    private void turnToFaceTarget(double x, double y) {
        var bearing = bearingTo(x, y);

        if (bearing >= 0) {
            turnDirection = 1;
        } else {
            turnDirection = -1;
        }

        /*double directionDif = getDirection() - getGunDirection();
        if (directionDif > 1) {
            turn
        }*/


        turnLeft(bearing);
    }
}
