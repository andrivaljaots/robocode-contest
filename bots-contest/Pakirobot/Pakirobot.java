package Pakirobot;

import java.util.HashMap;
import java.util.Map;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.Color;
import dev.robocode.tankroyale.botapi.IBot;
import dev.robocode.tankroyale.botapi.events.Condition;
import dev.robocode.tankroyale.botapi.events.DeathEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;

public class Pakirobot extends Bot {

	boolean isScanning;

	// The main method starts our bot
	public static void main(String[] args) {
		new Pakirobot().start();
	}

	// Constructor, which loads the bot config file
	Pakirobot() {
		super(BotInfo.fromFile("Pakirobot.json"));
	}

	// Called when a new round is started -> initialize and do some movement
	public void run() {
		botDistance.clear();
		isScanning = false;
		// Set colors
		setBodyColor(Color.fromString("#FFA500")); // lime
		setGunColor(Color.fromString("#FFA500")); // green
		setRadarColor(Color.fromString("#FFA500")); // dark cyan
		setBulletColor(Color.fromString("#FFA500")); // yellow
		setScanColor(Color.fromString("#FFA500")); // light red
		setTurretColor(Color.fromString("#FFA500"));
		setTracksColor(Color.fromString("#FFA500"));

		// Loop while as long as the bot is running
		while (isRunning()) {

			if (isScanning) {
				go();
			} else {
				turnRadarLeft(180D);

				setRadarTurnRate(5D);

				botDistance.values().stream().min((a, b) -> Double.compare(distanceTo(a.x, a.y), distanceTo(b.x, b.y)))
						.ifPresentOrElse(pos -> {
							setTurnLeft(bearingTo(pos.x, pos.y));
							setTurnGunLeft(gunBearingTo(pos.x, pos.y));
							waitFor(new TurnCompleteCondition(this));
							setForward(distanceTo(pos.x, pos.y));
							firee();
						}, () -> firee());
			}
		}
	}

	static class Pos {
		double x;
		double y;
		double speed;

		Pos(double x, double y, double speed) {
			this.x = x;
			this.y = y;
			this.speed = speed;
		}
	}

	Map<Integer, Pos> botDistance = new HashMap<>();

	@Override
	public void onScannedBot(ScannedBotEvent e) {
		isScanning = true;
		botDistance.put(e.getScannedBotId(), new Pos(e.getX(), e.getY(), e.getSpeed()));

		Pos pos = botDistance.values().stream()
				.min((a, b) -> Double.compare(distanceTo(a.x, a.y), distanceTo(b.x, b.y))).get();
		setTurnLeft(bearingTo(pos.x, pos.y));
		setTurnGunLeft(gunBearingTo(pos.x, pos.y));

		setForward(distanceTo(pos.x, pos.y));

		firee();

		isScanning = false;
	}

	void firee() {
		botDistance.values().stream().min((a, b) -> Double.compare(distanceTo(a.x, a.y), distanceTo(b.x, b.y)))
				.filter(a -> {
					return distanceTo(a.x, a.y) < 300d && Math.abs(gunBearingTo(a.x, a.y)) < 10;
				}).ifPresent(pos -> {
					if (distanceTo(pos.x, pos.y) < 40 && Math.abs(pos.speed) <= 2) {
						fire(4);
					} else if (distanceTo(pos.x, pos.y) < 50 && Math.abs(pos.speed) <= 2) {
						fire(3);
					} else if (distanceTo(pos.x, pos.y) < 60 && getEnergy() > 40 && Math.abs(pos.speed) <= 2) {
						fire(2);
					} else if (distanceTo(pos.x, pos.y) < 100 && Math.abs(pos.speed) <= 4) {
						fire(1);
					} else if (distanceTo(pos.x, pos.y) < 200) {
						fire(1);
					}
				});
	}

	@Override
	public void onBotDeath(DeathEvent e) {
		botDistance.remove(e.getVictimId());
		this.getEvents().clear();
	}

	@Override
	public void onDeath(DeathEvent e) {
		botDistance.remove(e.getVictimId());
		this.getEvents().clear();
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
