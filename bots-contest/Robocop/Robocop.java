package Robocop;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.Color;
import dev.robocode.tankroyale.botapi.IBot;
import dev.robocode.tankroyale.botapi.events.Condition;
import dev.robocode.tankroyale.botapi.events.CustomEvent;
import dev.robocode.tankroyale.botapi.events.HitBotEvent;
import dev.robocode.tankroyale.botapi.events.HitWallEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;

public class Robocop extends Bot {

	boolean movingForward;
	boolean isScanning; // flag set when scanning
	double lastReverse = 0;

	// The main method starts our bot
	public static void main(String[] args) {
		new Robocop().start();
	}

	// Constructor, which loads the bot config file
	Robocop() {
		super(BotInfo.fromFile("Robocop.json"));
	}

	// Called when a new round is started -> initialize and do some movement
	public void run() {
		// Set colors
		setBodyColor(Color.fromString("#FFFF00"));
		setGunColor(Color.fromString("#FFFFFF"));    // green
		setRadarColor(Color.fromString("#FFFF00"));  // dark cyan
		setBulletColor(Color.fromString("#FFFF00")); // yellow
		setScanColor(Color.fromString("#FFFF00"));   // light red

		//		setAdjustRadarForGunTurn(false);
		//		setAdjustRadarForBodyTurn(false);
		setMaxRadarTurnRate(30.0);

		//		addCustomEvent(
		//				new Condition("near-wall") {
		//					public boolean test() {
		//						return System.currentTimeMillis() - lastReverse > 3000 && getX() < 40 && getDirection() > 90 && getDirection() < 270
		//								|| getArenaHeight() - getY() < 40 && getDirection() > 0 && getDirection() < 180
		//								|| getArenaWidth() - getX() < 40 && (getDirection() > 0 && getDirection() < 90 || getDirection() > 270 )
		//								|| getY() < 40 && getDirection() > 180 && getDirection() < 360;
		//					}
		//				});

		// Loop while as long as the bot is running
		while (isRunning()) {
			// Tell the game we will want to move ahead 40000 -- some large number
			setForward(5000 + Math.random() * 30000);
			movingForward = true;
			// Tell the game we will want to turn right 90
			setTurnRight(90);
			//turn gun left if not scanning enemy
			// At this point, we have indicated to the game that *when we do something*,
			// we will want to move ahead and turn right. That's what "set" means.
			// It is important to realize we have not done anything yet!
			// In order to actually move, we'll want to call a method that takes real time, such as
			// waitFor.
			// waitFor actually starts the action -- we start moving and turning.
			// It will not return until we have finished turning.
			if (!isScanning) {
				setTurnGunLeft(90);
			}
			waitFor(new TurnCompleteCondition(this));
			// Note: We are still moving ahead now, but the turn is complete.
			// Now we'll turn the other way...
			setTurnLeft(190);
			if (!isScanning) {
				setTurnGunLeft(190);
			}
			// ... and wait for the turn to finish ...
			waitFor(new TurnCompleteCondition(this));
			// ... then the other way ...
			setTurnRight(190);
			if (!isScanning) {
				setTurnGunRight(190);
			}
			// ... and wait for that turn to finish.
			waitFor(new TurnCompleteCondition(this));
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
			setBack(5000 + Math.random() * 30000);
			movingForward = false;
		} else {
			setForward(5000 + Math.random() * 30000);
			movingForward = true;
		}
	}

	// We scanned another bot -> we have a target, so go get it
	@Override
	public void onScannedBot(ScannedBotEvent e) {
		isScanning = true; // we started scanning

		var yPotentialMoved = 0.0;
		var xPotentialMoved = 0.0;

		double speedAbsolute = Math.abs(e.getSpeed());
		if (speedAbsolute > 0) {
			int isReversingModifier = e.getSpeed() >= 0 ? 1 : -1;
			if (e.getDirection() >= 0 && e.getDirection() < 90) {
				yPotentialMoved = isReversingModifier * speedAbsolute * Math.sin(e.getDirection());
				xPotentialMoved = isReversingModifier * Math.sqrt(Math.pow(speedAbsolute, 2) + Math.pow(yPotentialMoved, 2));
			} else if (e.getDirection() >= 90 && e.getDirection() < 180) {
				yPotentialMoved = isReversingModifier * speedAbsolute * Math.sin(180 - e.getDirection());
				xPotentialMoved = isReversingModifier * Math.sqrt(Math.pow(speedAbsolute, 2) + Math.pow(yPotentialMoved, 2));
			} else if (e.getDirection() >= 180 && e.getDirection() < 270) {
				yPotentialMoved = isReversingModifier * speedAbsolute * Math.sin(270 - e.getDirection());
				xPotentialMoved = isReversingModifier * Math.sqrt(Math.pow(speedAbsolute, 2) + Math.pow(yPotentialMoved, 2));
			} else {
				yPotentialMoved = isReversingModifier * speedAbsolute * Math.sin(360 - e.getDirection());
				xPotentialMoved = isReversingModifier * Math.sqrt(Math.pow(speedAbsolute, 2) + Math.pow(yPotentialMoved, 2));
			}
		}
		var bearingFromGun = gunBearingTo(e.getX() + xPotentialMoved, e.getY() + yPotentialMoved);
		//		var bearingFromRadar = radarBearingTo(e.getX(), e.getY() );

		// Turn the gun toward the scanned bot
		turnGunLeft(bearingFromGun);

		//		turnRadarLeft(bearingFromRadar);

		// If it is close enough, fire!
		if (Math.abs(bearingFromGun) <= 3 && getGunHeat() == 0) {
			fire(0.4);
		}

		// Generates another scan event if we see a bot.
		// We only need to call this if the gun (and therefore radar)
		// are not turning. Otherwise, scan is called automatically.
		if (Math.abs(bearingFromGun) < 5 && Math.random() > 0.5) {
			if (this.getTurnRemaining() == 0) {
				setTurnLeft(190);
			}
			rescan();
		}

		isScanning = false; // we stopped scanning

	}

	// We hit another bot -> back up!
	@Override
	public void onHitBot(HitBotEvent e) {
		// If we're moving into the other bot, reverse!
		if (e.isRammed()) {
			reverseDirection();
		}
	}

	// A custom event occurred
	@Override
	public void onCustomEvent(CustomEvent e) {
		if (e.getCondition().getName().equals("near-wall")) {
			reverseDirection();
			lastReverse = System.currentTimeMillis();
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

}
