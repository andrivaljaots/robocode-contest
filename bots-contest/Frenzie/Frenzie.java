package Frenzie;

import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

public class Frenzie extends Bot {

	int turnDirection = 1; // clockwise (-1) or counterclockwise (1)
	int noticeCount = 0;
	int hitCount = 0;
	boolean movingForward;
	boolean hasDetected = false;

	// The main method starts our bot
	public static void main(String[] args) {
		new Frenzie().start();
	}

	// Constructor, which loads the bot config file
	Frenzie() {
		super(BotInfo.fromFile("Frenzie.json"));
	}

	// Called when a new round is started -> initialize and do some movement
	@Override
	public void run() {
		// Set colors
		setBodyColor(Color.CYAN);
		setGunColor(Color.WHITE);
		setRadarColor(Color.WHITE);
		setBulletColor(Color.CYAN);
		setScanColor(Color.fromString("#006464"));

		// Repeat while the bot is running
		while (isRunning()) {
			setScanColor(Color.fromString("#006464"));
			setForward(150);
			movingForward = true;
			setTurnGunRight(180);
			setTurnRight(90);
			waitFor(new TurnCompleteCondition(this));
			setTurnGunLeft(180);
			setTurnLeft(180);
			setForward(50);
			waitFor(new TurnCompleteCondition(this));
			setBack(200);
			movingForward = false;
			setTurnGunRight(180);
			setTurnRight(180);
			waitFor(new TurnCompleteCondition(this));
		}
	}

	// We scanned another bot -> fire and go ram it
	@Override
	public void onScannedBot(ScannedBotEvent e) {
		noticeCount = noticeCount + 1;
		setScanColor(Color.ORANGE);
		// Calculate direction of the scanned bot and bearing to it for the gun
		var bearingFromGun = gunBearingTo(e.getX(), e.getY());
		var distance = distanceTo(e.getX(), e.getY());

		if (noticeCount > 0) {
			setTurnGunLeft(bearingFromGun);
			noticeCount = 0;
		}

		// If it is close enough, fire!
		if (Math.abs(bearingFromGun) <= 3 && getGunHeat() == 0) {
			if (distance > 50) {
				setFire(1);
				turnToFaceTarget(e.getX(), e.getY());
				setForward(100);
			} else {
				setFire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
				turnToFaceTarget(e.getX(), e.getY());
				setForward(distance + 5);
			}
			waitFor(new TurnCompleteCondition(this));
		} else {
			// Else move closer
			turnToFaceTarget(e.getX(), e.getY());
			if (distance > 100) {
				setForward(100);
			} else {
				setForward(distance + 5);
			}
			waitFor(new TurnCompleteCondition(this));
		}
		rescan();
	}

	// We hit another bot -> back up, shoot and move!
	@Override
	public void onHitBot(HitBotEvent e) {
		// If we're moving into the other bot, reverse!
		if (e.isRammed()) {
			reverseDirection(e.getX(), e.getY());
			rescan();

			turnLeft(90);
			forward(100);
		}
	}

	// We hit another bot -> back up, shoot and move!
	@Override
	public void onHitWall(HitWallEvent e) {
		reverseDirection();
		turnLeft(90);
		forward(20);
	}

	// ReverseDirection: Switch from ahead to back & vice versa
	private void reverseDirection(double x, double y) {
		var bearing = bearingTo(x, y);
		if (bearing > -90 && bearing < 90) {
			back(100);
		} else { // else he's in back of us, so set ahead a bit.
			forward(100);
		}
	}

	// ReverseDirection: Switch from ahead to back & vice versa
	private void reverseDirection() {
		if (movingForward) {
			setBack(70);
			movingForward = false;
		} else {
			setForward(70);
			movingForward = true;
		}
	}

	// We were hit by a bullet -> turn perpendicular to the bullet
	@Override
	public void onHitByBullet(BulletHitBotEvent e) {
		// Calculate the bearing to the direction of the bullet
		var bearing = calcBearing(e.getBullet().getDirection());

		// Turn 90 degrees to the bullet direction based on the bearing
		setTurnLeft(90 - bearing);
		setForward(30);
		waitFor(new TurnCompleteCondition(this));
	}

	// Method that turns the bot to face the target at coordinate x,y, but also sets
	// the
	// default turn direction used if no bot is being scanned within in the run()
	// method.
	private void turnToFaceTarget(double x, double y) {
		var bearing = bearingTo(x, y);
		if (bearing >= 0) {
			turnDirection = 1;
		} else {
			turnDirection = -1;
		}
		setTurnLeft(bearing);
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
}
