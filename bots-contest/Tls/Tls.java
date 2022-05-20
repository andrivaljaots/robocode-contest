package Tls;

import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

// ------------------------------------------------------------------
// Tls
// Simply a wonderful tank
public class Tls extends Bot {

	private static double MAX_TARGET_DISTANCE = 100.00;

	public static void main(String[] args) {
		new Tls().start();
	}

	// Constructor, which loads the bot config file
	Tls() {
		super(BotInfo.fromFile("Tls.json"));
	}

	// Called when a new round is started -> initialize and do some movement
	@Override
	public void run() {
		setBodyColor(Color.GREEN);
		setTurretColor(Color.RED);
		setRadarColor(Color.BLUE);
		setScanColor(Color.ORANGE);
		setBulletColor(Color.RED);

		// Repeat while the bot is running
		while (isRunning()) {
			patrol();
		}
	}

	private void patrol() {
		setTurnLeft(14_000);
		setMaxSpeed(6);
		forward(14_000);
	}

	@Override
	public void onHitWall(HitWallEvent e) {
		turnLeft(45);
		setForward(200);
	}

	@Override
	public void onScannedBot(ScannedBotEvent e) {
		double targetDistance = distanceTo(e.getX(), e.getY());
		if (targetDistance <= MAX_TARGET_DISTANCE) {
			fire(3);
			turnToFaceTarget(e.getX(), e.getY());
			forward(targetDistance + 5);
			rescan();
		} else {
			double direction = directionTo(e.getX(), e.getY());
			double bearing = calcBearing(direction);
			if (bearing > -10 && bearing < 10) {
				fire(3);
			}
			patrol();
		}
	}

	private void turnToFaceTarget(double x, double y) {
		double bearing = bearingTo(x, y);
		turnLeft(bearing);
	}

	@Override
	public void onHitBot(HitBotEvent e) {
		turnToFaceTarget(e.getX(), e.getY());
		fire(3);
		double targetDistance = distanceTo(e.getX(), e.getY());
		forward(targetDistance + 5);
		patrol();
	}
}
