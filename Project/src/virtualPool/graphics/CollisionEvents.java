package virtualPool.graphics;


public class CollisionEvents
{
	public static abstract class CollisionEvent
	{
		private float time;
		
		public CollisionEvent(float time)
		{
			this.time = time;
		}
		
		public float getTime()
		{
			return time;
		}
		
		public abstract void solve();
	}
	
	public static class BallCollisionEvent extends CollisionEvent
	{
		private PoolBall ball1, ball2;

		public BallCollisionEvent(float time, PoolBall ball1, PoolBall ball2)
		{
			super(time);
			this.ball1 = ball1;
			this.ball2 = ball2;
		}

		@Override
		public void solve()
		{
		    // displacement from i to j
	        float y = (ball2.posZ - ball1.posZ);
	        float x = (ball2.posX - ball1.posX);

	        // distance squared
	        float d2 = x * x + y * y;

	        // dividing by 0 is bad
	        if (d2 == 0) d2 = 0.01f;

            float kii, kji, kij, kjj;

            kji = (x * ball1.vX + y * ball1.vZ) / d2; // k of j due to i
            kii = (x * ball1.vZ - y * ball1.vX) / d2; // k of i due to i
            kij = (x * ball2.vX + y * ball2.vZ) / d2; // k of i due to j
            kjj = (x * ball2.vZ - y * ball2.vX) / d2; // k of j due to j

            // set velocity of i
            ball1.vZ = kij * y + kii * x;
            ball1.vX = kij * x - kii * y;

            // set velocity of j
            ball2.vZ = kji * y + kjj * x;
            ball2.vX = kji * x - kjj * y;
	        
	        ball1.applyEnergyLoss();
	        ball2.applyEnergyLoss();
		}
		
		public PoolBall getBall1()
		{
			return ball1;
		}
		
		public PoolBall getBall2()
		{
			return ball2;
		}
	}
	
	public static class RailCollisionEvent extends CollisionEvent
	{
		public static enum Rail { HIGH_X, HIGH_Z, LOW_X, LOW_Z };
		
		private PoolBall ball;
		private Rail railType;

		public RailCollisionEvent(float time, PoolBall ball, RailCollisionEvent.Rail railType)
		{
			super(time);
			this.ball = ball;
			this.railType = railType;
		}

		@Override
		public void solve()
		{
			if (railType == Rail.HIGH_X || railType == Rail.LOW_X) ball.vX = -ball.vX;
			if (railType == Rail.HIGH_Z || railType == Rail.LOW_Z) ball.vZ = -ball.vZ;
			ball.applyEnergyLoss();
		}
		
		public PoolBall getBall()
		{
			return ball;
		}
		
		public Rail getRailType()
		{
			return railType;
		}
	}
}
