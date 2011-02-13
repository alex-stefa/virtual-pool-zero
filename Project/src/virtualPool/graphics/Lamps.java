package virtualPool.graphics;

import javax.media.opengl.GL;

import virtualPool.graphics.PoolWindow.GameState;
import virtualPool.loader.WaveFrontRenderer;
import virtualPool.loader.geometry.Face;
import virtualPool.loader.geometry.Group;
import virtualPool.loader.geometry.Mesh;
import virtualPool.loader.geometry.Model;
import virtualPool.loader.geometry.Vec4;
import virtualPool.scenegraph.SpotLight;
import virtualPool.util.Logs;


public class Lamps extends WaveFrontRenderer
{
	public static SpotLight[] lights = {
			new  SpotLight(GL.GL_LIGHT0),
			new  SpotLight(GL.GL_LIGHT1),
			new  SpotLight(GL.GL_LIGHT2),
			new  SpotLight(GL.GL_LIGHT3)
	};

	public Lamps(Model model)
	{
		super(model);

		// resize and center lamp above pool table
		model.center();
		model.scaleX(PoolTable.MAX_X * 1.9f);
		model.centerAt(0, PoolRoom.ROOM_TOP - model.bounds.getSize().y /2, 0);
		
		// lights are initialized later, so disable them
		for (SpotLight l : lights) l.is_on = false;
	}
	
	@Override
	protected void genMeshList(GL gl, Group group, Mesh mesh)
	{
		// treat the mesh as usual
		super.genMeshList(gl, group, mesh);
		
		// if it is a light bulb, compute center point and initialize spotlight
		if (group.name.startsWith("bulb") && model.materials.get(mesh.materialID).strName.equals("mat_light"))
		{
			try
			{
				// get light id N-1 from group name (= "bulbN")
				int lightId = Integer.parseInt(group.name.substring(4)) - 1;
				
				double lightX = 0;
				double lightY = 0;
				double lightZ = 0;
				int vertexCount = 0;
				
				for (Face face : mesh.faces)
				{
					for (int vi : face.vertIndex)
					{
						Vec4 vertex = model.vertices.get(vi);
						lightX += vertex.x;
						lightY += vertex.y;
						lightZ += vertex.z;
					}
					vertexCount += face.vertIndex.length;
				}
				
				lightX /= vertexCount;
				lightY /= vertexCount;
				lightZ /= vertexCount;
				
				SpotLight light = lights[lightId];
				
				light.setPosition((float) lightX, (float) lightY, (float) lightZ);
				light.setDirection(0.0f, -1.0f, 0.0f);
				light.spot_cutoff = 60.0f;
				light.spot_exp = 60.0f;
				
				// init done, we can enable it
				light.is_on = true;
			}
			catch (Exception ex)
			{
				Logs.log.severe("Error creating light object for " + group.name + ": " + ex.getMessage());
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void render(GL gl)
	{
		// render lights first
		for (SpotLight light : lights) light.render(gl);
		
		// then the lamps
		if (PoolWindow.gameState != GameState.ABOVE) super.render(gl);
	}
}
