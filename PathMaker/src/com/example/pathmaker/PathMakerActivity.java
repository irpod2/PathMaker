
package com.example.pathmaker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

@SuppressLint("NewApi")
public class PathMakerActivity extends Activity
{
	private PathView pathView;

	private int cameraWidth;
	private int cameraHeight;
	private String recentFilename = "";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Fullscreen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Set cameraWidth and cameraHeight
		detectDisplaySize();

		// Change background image from ic_launcher to desired image in
		// drawables folder
		pathView = new PathView(this, R.drawable.worldmap, cameraWidth,
				cameraHeight);

		setContentView(pathView);

		MapSaveService.init(getBaseContext());
	}

	@SuppressWarnings("deprecation")
	private void detectDisplaySize()
	{
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE))
				.getDefaultDisplay();

		// For Android API 13+ getWidth() and getHeight() are deprecated
		if (android.os.Build.VERSION.SDK_INT >= 13)
		{
			Point size = new Point();
			display.getSize(size);
			cameraWidth = size.x;
			cameraHeight = size.y;
		}
		// For APIs < 13, getSize(Point) doesn't exist.
		else
		{
			cameraWidth = display.getWidth();
			cameraHeight = display.getHeight();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_path_maker, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		// Clear all paths displayed
		case R.id.clear:
			pathView.clearPath();
			break;
		// Save all paths displayed
		case R.id.save:
			// Dialog to designate save file name
			AlertDialog.Builder saveBuilder = new AlertDialog.Builder(this);
			saveBuilder.setTitle("Save File (.map)");
			final EditText saveInput = new EditText(this);
			saveInput.setText(recentFilename);
			saveBuilder.setView(saveInput);

			saveBuilder.setPositiveButton("Save",
					new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog,
								int whichButton)
						{
							String filename = saveInput.getText().toString();
							recentFilename = filename;
							// If user specifies file extension, let it be
							if (filename.indexOf(".") == -1)
								recentFilename += ".map";
							pathView.savePaths(recentFilename);
						}
					});

			saveBuilder.setNegativeButton("Cancel", null);

			saveBuilder.setCancelable(true);

			saveBuilder.show();
			break;
		// Load file from storage
		case R.id.load:
			// Dialog to designate load file name
			AlertDialog.Builder loadBuilder = new AlertDialog.Builder(this);
			loadBuilder.setTitle("Load File");
			final EditText loadInput = new EditText(this);
			loadInput.setText(recentFilename);
			loadBuilder.setView(loadInput);

			loadBuilder.setPositiveButton("Load",
					new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog,
								int whichButton)
						{
							String filename = loadInput.getText().toString();

							recentFilename = filename;
							// If user specifies file extension, let it be
							if (filename.indexOf(".") == -1)
								recentFilename += ".map";

							pathView.loadPaths(recentFilename);
						}
					});

			loadBuilder.setNegativeButton("Cancel", null);

			loadBuilder.setCancelable(true);

			loadBuilder.show();
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);

		// Redetect cameraWidth and cameraHeight
		detectDisplaySize();

		// Resize paths
		pathView.onOrientationChanged(cameraWidth, cameraHeight);
	}
}
