package virtualPool.graphics;

import javax.media.opengl.GL;

import virtualPool.VirtualPool;
import virtualPool.graphics.CollisionEvents.BallCollisionEvent;
import virtualPool.graphics.CollisionEvents.CollisionEvent;
import virtualPool.graphics.CollisionEvents.RailCollisionEvent;
import virtualPool.loader.ModelLoadException;
import virtualPool.loader.WaveFrontLoader;
import virtualPool.scenegraph.Geometry;

public class PoolGame extends Geometry
{
	private PoolBall[] balls;
	private PoolTable table;
	private PoolStick stick;
	
	private float tableTop; 
	private float aimX, aimZ;
	private float ballDistance;
	private float buttAngle;
	private float rotX, rotY;
		
	public PoolGame()
	{
		tableTop = PoolTable.TOP_Y + PoolBall.RADIUS;
		ballDistance = PoolStick.IDLE_DISTANCE;
		buttAngle = PoolStick.DEFAULT_BUTT_ANGLE;
		
		try
		{
			table = new PoolTable(WaveFrontLoader.load("models/pool/pool-table.obj", "textures"));
			stick = new PoolStick(WaveFrontLoader.load("models/pool/pool-stick.obj", "textures"));
		}
		catch (ModelLoadException e)
		{
			e.printStackTrace();
		}

		balls = new PoolBall[16];
		for (int i = 0; i < 16; i++)
		{
			balls[i] = new PoolBall(i);
			balls[i].isPocketed = true;
			this.addChild(balls[i]);
		}
	}
	
	public void aimAt(float aimX, float aimZ)
	{
		this.aimX = aimX;
		this.aimZ = aimZ;
	}
	
	public void aimAtCue()
	{
		if (balls[0].isPocketed)
			aimAt(0.0f, 0.0f);
		else
            aimAt(balls[0].posX, balls[0].posZ);
	}
	
	public void setRotation(float rotX, float rotY)
	{
		this.rotY = rotY;
		this.rotX = rotX;
		
		buttAngle = PoolStick.DEFAULT_BUTT_ANGLE;
		
		if (rotX >= 0.85f * PoolStick.DEFAULT_BUTT_ANGLE && rotX < PoolStick.DEFAULT_BUTT_ANGLE)
			buttAngle = rotX + 10.0f;
		
		if (rotX <= 1.15f * PoolStick.DEFAULT_BUTT_ANGLE && rotX >= PoolStick.DEFAULT_BUTT_ANGLE)
			buttAngle = rotX - 10.0f;
	}
	
	@Override
	public void render(GL gl)
	{
		table.render(gl);

		if (!isMoving())
		{
			gl.glPushMatrix();
				gl.glTranslatef(+aimX, +tableTop, +aimZ);
				gl.glRotatef(-rotY, 0.0f, 1.0f, 0.0f);
				gl.glRotatef(-90, 0.0f, 1.0f, 0.0f);
				gl.glRotatef(+buttAngle, 0.0f, 0.0f, 1.0f);
				gl.glTranslatef(+ballDistance, 0.0f, 0.0f);
				stick.render(gl);
			gl.glPopMatrix();
		}
		
		animate(1.0f / VirtualPool.RunningConfig.SystemFps * 
				(float) VirtualPool.RunningConfig.GameSpeed);
	}

	public float getAimX()
	{
		return aimX;
	}

	public float getAimZ()
	{
		return aimZ;
	}

	public void moveCue(float dx, float dz)
	{
		PoolBall cue = balls[0];
		
		cue.posX += dx;
		cue.posZ += dz;
		
		if (cue.posX > +PoolTable.MAX_X) cue.posX = +PoolTable.MAX_X;
		if (cue.posX < -PoolTable.MAX_X) cue.posX = -PoolTable.MAX_X;
		if (cue.posZ > +PoolTable.MAX_Z) cue.posZ = +PoolTable.MAX_Z;
		if (cue.posZ < -PoolTable.MAX_Z) cue.posZ = -PoolTable.MAX_Z;
	}

	public void resetBallDistance()
	{
		this.ballDistance = PoolStick.IDLE_DISTANCE;
	}
	
	public boolean adjustBallDistance(float addition)
	{
		ballDistance += addition;
		if (ballDistance < PoolBall.RADIUS)
		{
			PoolBall cue = balls[0];
			cue.vX = (float) -Math.sin(Math.toRadians(rotY)) * addition * 100;
			cue.vZ = (float) Math.cos(Math.toRadians(rotY)) * addition * 100;
			resetBallDistance();
			return true;
		}
		return false;
	}
	
	public void resetBalls()
	{
		for (PoolBall ball : balls)
		{
			ball.isPocketed = false;
			ball.posX = (float) Math.random() * PoolTable.MAX_X * 2 - PoolTable.MAX_X;
			ball.posZ = (float) Math.random() * PoolTable.MAX_Z * 2 - PoolTable.MAX_Z;
			ball.vX = ball.vZ = 0;
		}
	}
	
	public boolean isMoving()
	{
		for (PoolBall ball : balls)
			if (ball.vX != 0 || ball.vZ != 0)
				return true;
		return false;
	}
	
	public void animate(float time)
	{
		if (!isMoving()) return;
		
		float remainingTime = time;
		CollisionEvent event = null;
		
		while (true)
		{
			event = computeNextEvent(remainingTime);
			
			if (event == null || event.getTime() > remainingTime)
			{
				for (PoolBall ball : balls) ball.move(remainingTime);
				break;
			}
			
			for (PoolBall ball : balls) ball.move(event.getTime());
			event.solve();
			remainingTime -= event.getTime();
			if (remainingTime <= 0) break;
		}
		
		for (PoolBall ball : balls) ball.applyFriction(time);
	}
	
	
	public CollisionEvent computeNextEvent(float maxTime)
	{
		CollisionEvent nextEvent = null;
		
		for (PoolBall ball : balls)
		{
			if (ball.isPocketed) continue;
			
			float timeToRail;
			
			if (ball.vX > 0)
			{
				timeToRail = (+PoolTable.MAX_X - ball.posX) / ball.vX;
				if ((timeToRail >= 0) && (nextEvent == null || nextEvent.getTime() > timeToRail))
					nextEvent = new RailCollisionEvent(timeToRail, ball, RailCollisionEvent.Rail.HIGH_X);
			}
			if (ball.vX < 0)
			{
				timeToRail = (-PoolTable.MAX_X - ball.posX) / ball.vX;
				if ((timeToRail >= 0) && (nextEvent == null || nextEvent.getTime() > timeToRail))
					nextEvent = new RailCollisionEvent(timeToRail, ball, RailCollisionEvent.Rail.LOW_X);
			}
			if (ball.vZ > 0)
			{
				timeToRail = (+PoolTable.MAX_Z - ball.posZ) / ball.vZ;
				if ((timeToRail >= 0) && (nextEvent == null || nextEvent.getTime() > timeToRail))
					nextEvent = new RailCollisionEvent(timeToRail, ball, RailCollisionEvent.Rail.HIGH_Z);
			}
			if (ball.vZ < 0)
			{
				timeToRail = (-PoolTable.MAX_Z - ball.posZ) / ball.vZ;
				if ((timeToRail >= 0) && (nextEvent == null || nextEvent.getTime() > timeToRail))
					nextEvent = new RailCollisionEvent(timeToRail, ball, RailCollisionEvent.Rail.LOW_Z);
			}
		}
		
		for (int i = 0; i < 15; i++)
			for (int j = i+1; j < 16; j++)
			{
				PoolBall ball1 = balls[i];
				PoolBall ball2 = balls[j];
				
				if (ball1.isPocketed || ball2.isPocketed) continue;
				
				if (!willIntersect(ball1, ball2)) continue;
								
				double criticalDistance = 2 * PoolBall.RADIUS; // + 0.01d;
				
                /* Breaking down the formula for t */
                double A = ball1.vX * ball1.vX + ball1.vZ * ball1.vZ - 
                	2 * ball1.vX * ball2.vX + ball2.vX * ball2.vX - 
                	2 * ball1.vZ * ball2.vZ + ball2.vZ * ball2.vZ;
                double B = -ball1.posX * ball1.vX - ball1.posZ * ball1.vZ + 
                	ball1.vX * ball2.posX + ball1.vZ * ball2.posZ + ball1.posX * ball2.vX -
                    ball2.posX * ball2.vX + ball1.posZ * ball2.vZ - ball2.posZ * ball2.vZ;
                double C = ball1.vX * ball1.vX + ball1.vZ * ball1.vZ - 
                	2 * ball1.vX * ball2.vX + ball2.vX * ball2.vX - 
                	2 * ball1.vZ * ball2.vZ + ball2.vZ * ball2.vZ;
                double D = ball1.posX * ball1.posX + ball1.posZ * ball1.posZ - 
                	criticalDistance * criticalDistance - 
                	2 * ball1.posX * ball2.posX + ball2.posX * ball2.posX - 
                	2 * ball1.posZ * ball2.posZ + ball2.posZ * ball2.posZ;
                double delta = (-2 * B) * (-2 * B) - 4 * C * D;

                /* If the discriminant if non negative, a collision will occur and 	*
                 * we must compare the time to our current time of collision. We   	*
                 * update the time if we find a collision that has occurred earlier *
                 * than the previous one.                                          	*/
                if (delta < 0) continue;
                
                float time1 = (float) (0.5 * (2 * B - Math.sqrt(delta)) / A);
                float time2 = (float) (0.5 * (2 * B + Math.sqrt(delta)) / A);
                
                if (time1 < 0 && time2 < 0) continue;
                
                float timeToCollision = Math.min(time1, time2);
                if (timeToCollision < 0) timeToCollision = Math.max(time1, time2);
                	
    			if (nextEvent == null || nextEvent.getTime() > timeToCollision)
    				nextEvent = new BallCollisionEvent(timeToCollision, ball1, ball2);
 			}
		
		return nextEvent;
	}
	
	public boolean willIntersect(PoolBall ball1, PoolBall ball2)
	{
		return (ball2.posX - ball1.posX) * (ball1.vX - ball2.vX) + 
			(ball2.posZ - ball1.posZ) * (ball1.vZ - ball2.vZ) > 0;
	}
}
