package virtualPool.graphics;

import javax.media.opengl.GL;

import virtualPool.loader.WaveFrontRenderer;
import virtualPool.loader.geometry.Model;


public class Chair extends WaveFrontRenderer
{
	public static final float HEIGHT = 115.0f;
	
	public Chair(Model model)
	{
		super(model);

		model.center();
		model.scaleY(Chair.HEIGHT);
		model.centerAt(0, model.bounds.getSize().y / 2, 0);
	}

	@Override
	public void render(GL gl)
	{
		super.render(gl);
	}
}
