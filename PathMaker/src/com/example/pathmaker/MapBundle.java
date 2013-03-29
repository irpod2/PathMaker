
package com.example.pathmaker;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

public class MapBundle implements Serializable
{
	private static final long serialVersionUID = -6917082927330285431L;

	public ArrayList<MapPath> paths = new ArrayList<MapPath>();

	public void resize(float cameraWidth, float cameraHeight)
	{
		for (MapPath p : paths)
		{
			p.resize(cameraWidth, cameraHeight);
		}
	}

	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	public void save(Context context, String filename)
	{
		try
		{
			FileOutputStream fOut = context.openFileOutput(filename,
					Activity.MODE_WORLD_READABLE);
			ObjectOutputStream osw = new ObjectOutputStream(fOut);
			osw.writeObject(this);
			osw.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public static MapBundle load(Context context, String filename)
	{
		MapBundle bundle = null;
		try
		{
			FileInputStream fin = context.openFileInput(filename);
			ObjectInputStream sin = new ObjectInputStream(fin);
			bundle = (MapBundle) sin.readObject();
			sin.close();
		}
		catch (StreamCorruptedException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}

		return bundle;
	}
}
