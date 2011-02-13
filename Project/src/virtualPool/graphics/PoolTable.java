package virtualPool.graphics;

import javax.media.opengl.GL;

import virtualPool.loader.WaveFrontRenderer;
import virtualPool.loader.geometry.Model;


public class PoolTable extends WaveFrontRenderer
{
	public static final float MAX_X = 116.84f - PoolBall.RADIUS; // official WPA 8x4 foot table length (centimeters)
	public static final float MAX_Z = 58.42f - PoolBall.RADIUS; // official WPA 8x4 foot table width (centimeters)
	public static final float TOP_Y = 77.674f; // automagically obtained using the println below
	
	public PoolTable(Model model)
	{
		super(model);
		
		// resize and center table. 
		model.centerAt(-(model.bounds.getSize().x - 71.844f)/2, 0, 0);
		model.scaleZ(58.554f * PoolBall.RADIUS);
		model.centerAt(model.bounds.getCenterPoint().x, model.bounds.getSize().y / 2, 0);
		// PoolTable.TOP_Y = model.bounds.getSize().y * 0.9166f; 
		// System.out.println(PoolTable.TOP_Y);
	}
	
	@Override
	public void render(GL gl)
	{
		super.render(gl);
	}
}
