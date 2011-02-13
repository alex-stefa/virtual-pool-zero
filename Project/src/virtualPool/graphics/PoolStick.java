package virtualPool.graphics;

import javax.media.opengl.GL;

import virtualPool.loader.WaveFrontRenderer;
import virtualPool.loader.geometry.Model;


public class PoolStick extends WaveFrontRenderer
{
	public static final float LENGTH = 150.0f; // 1.5 meter stick
	public static final float IDLE_DISTANCE = PoolBall.RADIUS * 2f; // how far away from ball center
	public static final float DEFAULT_BUTT_ANGLE = 15.0f;

	public PoolStick(Model model)
	{
		super(model);
		
		model.center();
		model.scaleX(PoolStick.LENGTH);
		model.centerAt(+model.bounds.getSize().x / 2, 0.0f, 0.0f);
	}

	@Override
	public void render(GL gl)
	{
		super.render(gl);
	}
}
