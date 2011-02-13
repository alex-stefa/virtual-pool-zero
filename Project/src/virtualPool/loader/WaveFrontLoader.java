package virtualPool.loader;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import virtualPool.loader.geometry.Face;
import virtualPool.loader.geometry.Group;
import virtualPool.loader.geometry.Material;
import virtualPool.loader.geometry.Mesh;
import virtualPool.loader.geometry.Model;
import virtualPool.loader.geometry.TexCoord;
import virtualPool.loader.geometry.Vec4;
import virtualPool.util.Logs;


public class WaveFrontLoader
{
	private static final String VERTEX_DATA = "v ";
	private static final String NORMAL_DATA = "vn ";
	private static final String TEXTURE_DATA = "vt ";
	private static final String FACE_DATA = "f ";
	private static final String SMOOTHING_GROUP = "s ";
	private static final String GROUP = "g ";
	private static final String OBJECT = "o ";
	private static final String COMMENT = "#";

	private WaveFrontLoader()
	{
	}

	public static Model load(String modelFile, String textureFolder) throws ModelLoadException
	{
		String baseDir = "";
		String tokens[] = modelFile.split("/");
		for (int i = 0; i < tokens.length - 1; i++)
		{
			baseDir += tokens[i] + "/";
		}

		String tex = textureFolder;
		if (tex == null) tex = baseDir;
		if (!tex.endsWith("/")) tex += "/";

		Model model = new Model(modelFile, tex);

		Group group = null;
		int materialID = -1;

		InputStream stream = null;
		try
		{
			stream = ResourceRetriever.getResourceAsInputStream(model.modelFilename);
			if (stream == null)
			{
				throw new ModelLoadException("Stream is null");
			}
		}
		catch (IOException e)
		{
			throw new ModelLoadException("Caught IO exception: " + e);
		}

		try
		{
			// Open a file handle and read the models data
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));

			String line = null;
			while ((line = br.readLine()) != null)
			{
				while (true)
				{
					if (lineIs(COMMENT, line) || /* ignore comments */
						lineIs(OBJECT, line) || /* ignore object lines */
						(line.length() == 0)) /* ignore empty lines */
					{
						break;
					}

					if (lineIs("mtllib ", line))
					{
						processMaterialLib(model, baseDir, line);
						break;
					}

					if (lineIs(GROUP, line))
					{
						if (group != null)
						{
							model.groups.add(group);
						}
						group = new Group();
						group.name = parseName(line);
						break;
					}

					if (lineIs(VERTEX_DATA, line))
					{
						line = getPoints(model, VERTEX_DATA, line, br);
						continue;
					}

					if (lineIs(TEXTURE_DATA, line))
					{
						line = getTexCoords(model, TEXTURE_DATA, line, br);
						continue;
					}

					if (lineIs(NORMAL_DATA, line))
					{
						line = getPoints(model, NORMAL_DATA, line, br);
						continue;
					}

					if (lineIs("usemtl ", line))
					{
						materialID = processMaterialType(model, line);
						break;
					}

					if (lineIs(FACE_DATA, line))
					{
						if (group == null) group = new Group();

						Mesh mesh = new Mesh();

						mesh.materialID = materialID;
						mesh.hasTexture = (mesh.materialID >= 0)
								&& (model.materials.get(mesh.materialID).strFile != null);

						line = getFaces(line, mesh, br);

						group.meshes.add(mesh);
						continue;
					}
					
					break; // unconditionally
				}
			}
		}
		catch (IOException e)
		{
			throw new ModelLoadException("Failed to find or read OBJ: " + stream);
		}

		if (group != null) model.groups.add(group);

		model.updateBounds();
		Logs.log.info("Model bounds: " + model.bounds.toString());
		Logs.log.info("Model #v " + model.vertices.size() + " / #n " + model.normals.size() + " / #vt "
				+ model.texCoords.size());

		return model;
	}

	private static boolean lineIs(String type, String line)
	{
		return line.startsWith(type);
	}

	private static String getPoints(Model model, String prefix, String currLine, BufferedReader br) throws IOException
	{
		boolean isVertices = prefix.equals(VERTEX_DATA);

		// we've already read in the first line (currLine)
		// so go ahead and parse it

		if (isVertices)
			model.vertices.add(parsePoint(currLine));
		else
			model.normals.add(parsePoint(currLine));

		// parse through the rest of the points
		String line = null;
		while ((line = br.readLine()) != null)
		{
			if (!lineIs(prefix, line)) break;
			Vec4 point = parsePoint(line);
			if (isVertices)
				model.vertices.add(point);
			else
				model.normals.add(point);
		}

		return line;
	}

	private static String getTexCoords(Model model, String prefix, String currLine, BufferedReader br)
			throws IOException
	{
		String s[] = currLine.split("\\s+");
		TexCoord texCoord = new TexCoord();
		texCoord.u = Float.parseFloat(s[1]);
		texCoord.v = Float.parseFloat(s[2]);

		model.texCoords.add(texCoord);

		// parse through the rest of the points
		String line = null;
		while ((line = br.readLine()) != null)
		{
			if (!lineIs(prefix, line)) break;

			s = line.split("\\s+");

			texCoord = new TexCoord();
			texCoord.u = Float.parseFloat(s[1]);
			texCoord.v = Float.parseFloat(s[2]);

			model.texCoords.add(texCoord);
		}

		return line;
	}

	private static String getFaces(String currLine, Mesh mesh, BufferedReader br) throws IOException
	{
		mesh.faces.add(parseFace(currLine));

		// parse through the rest of the faces
		String line = null;
		while ((line = br.readLine()) != null)
		{
			// if (lineIs(SMOOTHING_GROUP, line) || lineIs(COMMENT, line) ||
			// (line.length() < 1))
			if (lineIs(SMOOTHING_GROUP, line))
			{
				continue;
			}
			else if (lineIs(FACE_DATA, line))
			{
				mesh.faces.add(parseFace(line));
			}
			else
				break;
		}

		return line;
	}

	private static Face parseFace(String line)
	{
		String s[] = line.split("\\s+");
		if (line.contains("//"))
		{ // Pattern is present if obj has no texture
			for (int loop = 1; loop < s.length; loop++)
			{
				s[loop] = s[loop].replaceAll("//", "/-1/"); // insert -1 for
															// missing vt data
			}
		}

		Face face = new Face(s.length - 1);

		for (int loop = 1; loop < s.length; loop++)
		{
			String s1 = s[loop];
			String[] temp = s1.split("/");

			if (temp.length > 0)
			{ // we have vertex data
				if (Integer.valueOf(temp[0]) < 0)
				{
					// TODO handle relative vertex data
				}
				else
				{
					face.vertIndex[loop - 1] = Integer.valueOf(temp[0]) - 1;
				}
			}

			if (temp.length > 1)
			{ // we have texture data
				if (Integer.valueOf(temp[1]) < 0)
				{
					face.coordIndex[loop - 1] = 0;
				}
				else
				{
					face.coordIndex[loop - 1] = Integer.valueOf(temp[1]) - 1;
				}
			}

			if (temp.length > 2)
			{ // we have normal data
				face.normalIndex[loop - 1] = Integer.valueOf(temp[2]) - 1;
			}
		}

		return face;
	}

	private static Vec4 parsePoint(String line)
	{
		Vec4 point = new Vec4();

		final String s[] = line.split("\\s+");

		point.x = Float.parseFloat(s[1]);
		point.y = Float.parseFloat(s[2]);
		point.z = Float.parseFloat(s[3]);

		return point;
	}

	private static String parseName(String line)
	{
		final String s[] = line.split("\\s+");
		return s[1];
	}

	private static void processMaterialLib(Model model, String baseDir, String mtlData)
	{
		String s[] = mtlData.split("\\s+");

		InputStream stream = null;
		try
		{
			stream = ResourceRetriever.getResourceAsInputStream(baseDir + s[1]);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}

		if (stream == null)
		{
			try
			{
				stream = new FileInputStream(baseDir + s[1]);
			}
			catch (FileNotFoundException ex)
			{
				ex.printStackTrace();
				return;
			}
		}
		loadMaterialFile(model, stream);
	}

	private static int processMaterialType(Model model, String line)
	{
		String s[] = line.split("\\s+");

		for (int i = 0; i < model.materials.size(); i++)
			if (model.materials.get(i).strName.equals(s[1])) return i;

		return -1;
	}

	private static void loadMaterialFile(Model model, InputStream stream)
	{
		Material mat = null;
		int texId = 0;

		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			String line;

			while ((line = br.readLine()) != null)
			{

				String parts[] = line.trim().split("\\s+");

				if (parts[0].equals("newmtl"))
				{
					if (mat != null) model.materials.add(mat);

					mat = new Material();
					mat.strName = parts[1];
					mat.textureId = texId++;

				}
				else if (parts[0].equals("Ks"))
					mat.specularColor = parseColor(line);

				else if (parts[0].equals("Ns"))
				{
					if (parts.length > 1) mat.shininess = Float.valueOf(parts[1]);
				}
				else if (parts[0].equals("d"))
					;
				else if (parts[0].equals("illum"))
					;
				else if (parts[0].equals("Ka"))
					mat.ambientColor = parseColor(line);
				else if (parts[0].equals("Kd"))
					mat.diffuseColor = parseColor(line);
				else if (parts[0].equals("Ke"))
					mat.emissiveColor = parseColor(line);
				else if (parts[0].equals("map_Kd"))
				{
					if (parts.length > 1) mat.strFile = model.textureBasePath + parts[1];
				}

				else if (parts[0].equals("map_Ka"))
				{
					if (parts.length > 1) mat.strFile = model.textureBasePath + parts[1];
				}
			}

			br.close();
			model.materials.add(mat);

		}
		catch (FileNotFoundException ex)
		{
			ex.printStackTrace();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	private static Color parseColor(String line)
	{
		String parts[] = line.trim().split("\\s+");

		Color color = new Color(Float.valueOf(parts[1]), Float.valueOf(parts[2]), Float.valueOf(parts[3]));

		return color;
	}
}
