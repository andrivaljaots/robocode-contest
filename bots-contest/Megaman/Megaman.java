package Megaman;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.Color;
import dev.robocode.tankroyale.botapi.IBot;
import dev.robocode.tankroyale.botapi.events.Condition;
import dev.robocode.tankroyale.botapi.events.HitBotEvent;
import dev.robocode.tankroyale.botapi.events.HitWallEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;

import static dev.robocode.tankroyale.botapi.Color.*;
import static java.lang.Math.abs;
import static java.lang.Math.random;

public class Megaman extends Bot{

    // The main method starts our bot
    public static void main( String[] args ){
        new Megaman().start();
    }

    // Constructor, which loads the bot config file
    Megaman(){
        super( BotInfo.fromFile( "Megaman.json" ) );
    }

    int targetId;
    boolean hasTarget;
    double distanceToTarget;
    double targetX;
    double targetY;
    double targetSpeed;
    double targetDirection;

    int targetLastSeen;

    int turnDirection = 1; // clockwise (-1) or counterclockwise (1)

    // Called when a new round is started -> initialize and do some movement
    public void run(){
        // Set colors
        setBodyColor( CYAN );
        setGunColor( NAVY );
        setRadarColor( PURPLE );
        setBulletColor( ORANGE );

        // Loop while as long as the bot is running
        while( isRunning() ){

            if( getTurnNumber() - targetLastSeen > 15 ){
                hasTarget = false;
            }
            setColor();
            System.out.println( "TargetLastSeen: " + targetLastSeen + ", turnNumber: " + getTurnNumber() );
            if( hasTarget ){
                turnGunLeft( gunBearingTo( targetX, targetY ) );
                fire();
            }
            else {
                turnGunLeft( 30 );
            }
        }
    }

    private void fire(){
        if( abs( distanceToTarget ) < 30 ){
            fire( 3 );
        }
        else if( abs( targetSpeed ) > 4 ){
            fire(1);
            turnLeft( bearingTo( targetX, targetY ) );
            forward( 2 );
        }
     /*   else if( distanceToTarget > 300 ){
            turnLeft( bearingTo( targetX, targetY ) );
            forward( 30 );
        }*/
        else {
            fire( 3 );
            turnLeft( bearingTo( targetX, targetY ) );
            forward( 2 );
        }
    }

    public void setColor(){
        if( hasTarget ){
            setScanColor( RED );
        }
        else {
            setScanColor( GREEN );
        }
    }

    // We collided with a wall -> reverse the direction
    @Override
    public void onHitWall( HitWallEvent e ){
        // Bounce off!
        reverseDirection();
    }

    // We scanned another bot -> fire!
    @Override
    public void onScannedBot( ScannedBotEvent e ){
        targetLastSeen = this.getTurnNumber();
        if( hasTarget ){
            distanceToTarget = distanceTo( e.getX(), e.getY() );
            targetX = e.getX();
            targetY = e.getY();
            targetSpeed = e.getSpeed();
            targetDirection = e.getDirection();
        }
        if( !hasTarget ){
            hasTarget = true;
            distanceToTarget = distanceTo( e.getX(), e.getY() );
            targetSpeed = e.getSpeed();
            targetDirection = e.getDirection();
            targetId = e.getScannedBotId();
        }
        System.out.println( "TARGET SPEED: " + e.getSpeed() );
        System.out.println( "TARGET DIRECTION: " + e.getDirection() );
        System.out.println( "TARGET DISTANCE: " + distanceToTarget );
    }

    public void reverseDirection(){
        back( 100 );
    }
}
