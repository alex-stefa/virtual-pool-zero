package virtualPool.loader;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;

import virtualPool.loader.geometry.Bounds;
import virtualPool.loader.geometry.Face;
import virtualPool.loader.geometry.Group;
import virtualPool.loader.geometry.Material;
import virtualPool.loader.geometry.Mesh;
import virtualPool.loader.geometry.Model;
import virtualPool.loader.geometry.Vec4;
import virtualPool.scenegraph.Geometry;
import virtualPool.util.Logs;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;


public class WaveFrontRenderer extends Geometry
{
	protected HashMap<Integer, Texture> texture;
	protected int modelBoundsList;
	protected Model model;

	public WaveFrontRenderer(Model model)
	{
		this.model = model;
		this.modelBoundsList = -1;
	}

	@Override
	public void render(GL gl)
	{
		if ((gl == null) || (model == null)) return;

		int displayList = DisplayListCache.get(model);

		if (displayList < 0) displayList = initialize(gl);

		// save current state variables
		gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS);

		// check wireframe
		if (model.renderAsWireframe)
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
		else
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);

		gl.glDisable(GL.GL_COLOR_MATERIAL);

		gl.glPushMatrix();

		// check for unit size model
		if (model.unitizeSize)
		{
			float scale = 1.0f / model.bounds.getRadius();
			gl.glScalef(scale, scale, scale);
		}

		if (model.centerModel)
		{
			Vec4 center = model.bounds.getCenterPoint();
			gl.glTranslatef(-center.x, -center.y, -center.z);
		}

		if (model.renderModel) gl.glCallList(displayList);

		if (model.renderModelBounds)
		{
			// Disabled lighting for drawing the boundary lines so they are all white (or whatever I chose)
			gl.glDisable(GL.GL_LIGHTING);
			gl.glCallList(modelBoundsList);
		}

		gl.glPopMatrix();

		// restore current state variables
		gl.glPopAttrib();
	}

	/**
	 * Load the model and associated materials, etc
	 */
	protected int initialize(GL gl)
	{
		int displayList = DisplayListCache.get(model);
		if (displayList >= 0) return displayList;

		Logs.log.info("Initialize Model: " + model.modelFilename);

		int numMaterials = model.materials.size();

		if (numMaterials > 0) Logs.log.info("\n    Loading " + numMaterials + " Materials:");

		texture = new HashMap<Integer, Texture>();

		for (int i = 0; i < numMaterials; i++)
		{
			if (model.materials.get(i).strFile != null)
			{
				String status = "        Material:  " + model.materials.get(i).strFile;

				URL result;
				try
				{
					result = ResourceRetriever.getResourceAsUrl(model.materials.get(i).strFile);
				}
				catch (IOException e)
				{
					status += " ... failed";
					continue;
				}

				if (result != null && !result.getPath().endsWith("/") && !result.getPath().endsWith("\\"))
				{
					loadTexture(result, i);
					model.materials.get(i).textureId = i;
					status += " ... done. Texture ID: " + i;
				}
				else
					status += " ... failed (no result for material)";

				Logs.log.info(status);
			}
		}

		if (numMaterials > 0) Logs.log.info("    Load Materials: Done");

		Logs.log.info("\n    Generate Lists:");
		int compiledList = DisplayListCache.generateList(model, gl, 2);

		Logs.log.info("        Model List");
		gl.glNewList(compiledList, GL.GL_COMPILE);
			genModelList(gl);
		gl.glEndList();

		modelBoundsList = compiledList + 1;

		Logs.log.info("        Boundary List");
		gl.glNewList(modelBoundsList, GL.GL_COMPILE);
		genModelBoundsList(gl);
		gl.glEndList();

		Logs.log.info("    Generate Lists: Done");
		Logs.log.info("Load Model: Done");

		return compiledList;
	}

	/**
	 * Load a texture given by the specified URL and assign it to the texture id
	 * that is passed in.
	 * 
	 * @param url
	 * @param id
	 */
	private void loadTexture(URL url, int id)
	{
		if (url != null)
		{
			BufferedImage bufferedImage = null;

			try
			{
				bufferedImage = ImageIO.read(url);
			}
			catch (Exception e)
			{
				Logs.log.warning(" ... FAILED loading texture with exception: " + e.getMessage());
				return;
			}

			texture.put(id, TextureIO.newTexture(bufferedImage, true));
		}
	}
	
	protected void genMeshList(GL gl, Group group, Mesh mesh)
	{
		if (mesh.faces.size() == 0)
		{
			Logs.log.info("Mesh with no faces in group " + group.name + ".");
			return;
		}

		if (mesh.materialID < model.materials.size())
		{
			float[] rgba = new float[4];

			Material material = model.materials.get(mesh.materialID);
			gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, material.diffuseColor.getRGBComponents(rgba), 0);
			gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, material.ambientColor.getRGBComponents(rgba), 0);
			gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, material.specularColor.getRGBComponents(rgba), 0);
			gl.glMaterialf(GL.GL_FRONT, GL.GL_SHININESS, material.shininess);
			gl.glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION, material.emissiveColor.getRGBComponents(rgba), 0);
		}

		if (mesh.hasTexture && texture.get(mesh.materialID) != null)
		{
			Texture t = texture.get(mesh.materialID);

			// switch to texture mode and push a new matrix on the stack
			gl.glMatrixMode(GL.GL_TEXTURE);
			gl.glPushMatrix();

			// check to see if the texture needs flipping
			if (t.getMustFlipVertically())
			{
				gl.glScaled(1, -1, 1);
				gl.glTranslated(0, -1, 0);
			}

			// switch to modelview matrix and push a new matrix on the stack
			gl.glMatrixMode(GL.GL_MODELVIEW);
			gl.glPushMatrix();

			// enable, bind and get texture coordinates
			t.enable();
			t.bind();

			// This is required to repeat textures..
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
			gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		    gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		    gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
		}
		
		for (Face face : mesh.faces)
		{
			int indexType = 0;
			int vertexIndex = 0;
			int normalIndex = 0;
			int textureIndex = 0;

			gl.glBegin(GL.GL_POLYGON);

			// TODO: the number of vertices for a face is not always 3
			for (int whichVertex = 0; whichVertex < face.vertIndex.length; whichVertex++)
			{
				vertexIndex = face.vertIndex[whichVertex];

				try
				{
					normalIndex = face.normalIndex[whichVertex];

					indexType = 0;
					gl.glNormal3f(model.normals.get(normalIndex).x, model.normals.get(normalIndex).y,
							model.normals.get(normalIndex).z);

					if (mesh.hasTexture)
					{
						if (model.texCoords.size() > 0)
						{
							textureIndex = face.coordIndex[whichVertex];
							indexType = 1;
							gl.glTexCoord2f(model.texCoords.get(textureIndex).u, model.texCoords.get(textureIndex).v);
						}
					}
					indexType = 2;
					gl.glVertex3f(model.vertices.get(vertexIndex).x, model.vertices.get(vertexIndex).y,
							model.vertices.get(vertexIndex).z);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					switch (indexType)
					{
					case 0:
						Logs.log.severe("Normal index " + normalIndex + " is out of bounds");
						break;

					case 1:
						Logs.log.severe("Texture index " + textureIndex + " is out of bounds");
						break;

					case 2:
						Logs.log.severe("Vertex index " + vertexIndex + " is out of bounds");
						break;
					}
				}
			}
			gl.glEnd();
		}

		if (mesh.hasTexture && texture.get(mesh.materialID) != null)
		{
			Texture t = texture.get(mesh.materialID);
			t.disable();

			gl.glMatrixMode(GL.GL_TEXTURE);
			gl.glPopMatrix();

			gl.glMatrixMode(GL.GL_MODELVIEW);
			gl.glPopMatrix();
		}
	}

	protected void genGroupList(GL gl, Group group)
	{
		for (Mesh mesh : group.meshes)
			genMeshList(gl, group, mesh);
	}

	/**
	 * Generate the model display list
	 */
	protected void genModelList(GL gl)
	{
		for (Group g : model.groups)
			genGroupList(gl, g);
		
		// Try this clearing of color so it won't use the previous color
		// gl.glColor3f(1.0f, 1.0f, 1.0f);
	}

	/**
	 * Draw the boundary of the model (the large box representing the entire
	 * model and not the object in it)
	 */
	protected void genModelBoundsList(GL gl)
	{
		drawBounds(gl, model.bounds);
	}

	/**
	 * Draws the bounding box of the object using the max and min extrema
	 * points.
	 * 
	 * @param bounds
	 */
	private void drawBounds(GL gl, Bounds bounds)
	{
		// Front Face
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3f(bounds.min.x, bounds.min.y, bounds.min.z);
		gl.glVertex3f(bounds.max.x, bounds.min.y, bounds.min.z);
		gl.glVertex3f(bounds.max.x, bounds.max.y, bounds.min.z);
		gl.glVertex3f(bounds.min.x, bounds.max.y, bounds.min.z);
		gl.glEnd();

		// Back Face
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3f(bounds.min.x, bounds.min.y, bounds.max.z);
		gl.glVertex3f(bounds.max.x, bounds.min.y, bounds.max.z);
		gl.glVertex3f(bounds.max.x, bounds.max.y, bounds.max.z);
		gl.glVertex3f(bounds.min.x, bounds.max.y, bounds.max.z);
		gl.glEnd();

		// Connect the corners between the front and back face.
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3f(bounds.min.x, bounds.min.y, bounds.min.z);
		gl.glVertex3f(bounds.min.x, bounds.min.y, bounds.max.z);

		gl.glVertex3f(bounds.max.x, bounds.min.y, bounds.min.z);
		gl.glVertex3f(bounds.max.x, bounds.min.y, bounds.max.z);

		gl.glVertex3f(bounds.max.x, bounds.max.y, bounds.min.z);
		gl.glVertex3f(bounds.max.x, bounds.max.y, bounds.max.z);

		gl.glVertex3f(bounds.min.x, bounds.max.y, bounds.min.z);
		gl.glVertex3f(bounds.min.x, bounds.max.y, bounds.max.z);
		gl.glEnd();
	}

}
