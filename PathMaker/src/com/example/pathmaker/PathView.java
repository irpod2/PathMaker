
package com.example.pathmaker;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.widget.ImageView;

public class PathView extends ImageView
{
	private final long DOUBLE_TAP_TIME = 500L;
	private final float MIN_DIST = 30.0f;
	private final float MAX_DIST = 50.0f;
	private final Context context;
	private Paint thinPaint = new Paint();
	private Paint thickPaint = new Paint();
	private ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
	private MapBundle bundle;
	private Waypoint lastPoint;
	private Waypoint lastParent;
	private int cameraZeroX;
	private int cameraZeroY;
	private int cameraX;
	private int cameraY;
	private int cameraWidth;
	private int cameraHeight;
	private int imageWidth;
	private int imageHeight;

	private long lastTapTime = 0;
	private float lastX;
	private float lastY;
	private boolean dragging = false;

	public PathView(Context baseContext, int resourceID, int camWidth,
			int camHeight)
	{
		super(baseContext);

		cameraWidth = camWidth;
		cameraHeight = camHeight;

		context = baseContext;

		setPaints();

		setScaleType(ScaleType.CENTER);
		Drawable bg = getResources().getDrawable(resourceID);
		setImageDrawable(bg);
		imageWidth = bg.getIntrinsicWidth();
		imageHeight = bg.getIntrinsicHeight();
		cameraZeroX = -imageWidth / 2 + cameraWidth / 2;
		cameraZeroY = -imageHeight / 2 + cameraHeight / 2;
		cameraX = cameraZeroX;
		cameraY = cameraZeroY;
		scrollTo((int) cameraX, (int) cameraY);

		bundle = new MapBundle();
	}

	// thinPaint is used for lines
	// thickPaint is used for points
	private void setPaints()
	{
		thinPaint.setColor(Color.BLUE);
		thinPaint.setStyle(Paint.Style.STROKE);
		thinPaint.setStrokeWidth(8);
		thickPaint.setColor(Color.BLUE);
		thickPaint.setStyle(Paint.Style.STROKE);
		thickPaint.setStrokeWidth(16);
	}

	// Remove all paths and points
	public void clearPath()
	{
		cameraX = cameraZeroX;
		cameraY = cameraZeroY;
		scrollTo(cameraX, cameraY);
		lastPoint = null;
		lastParent = null;
		bundle.paths.clear();
		waypoints.clear();
	}

	// Save all paths using bundle's save method
	public void savePaths(String filename)
	{
		if (!bundle.paths.isEmpty())
		{
			MapSaveService.save(context, bundle, filename);
		}
	}

	// Load paths
	public void loadPaths(String filename)
	{
		MapBundle newPaths = null;
		newPaths = MapSaveService.load(context, filename);
		if (newPaths != null)
		{
			bundle = newPaths;
			// Add waypoints to View's list (for drawing & searching)
			for (MapPath p : bundle.paths)
			{
				// p.resize(cameraWidth, cameraHeight);
				for (int i = 0; i < p.size(); i++)
				{
					waypoints.add(p.getWaypoint(i));
				}
			}
			// Make sure any leftover searches are cleaned up
			unvisitNodes();
		}
	}

	// Used to avoid TSP when searching
	private void unvisitNodes()
	{
		for (Waypoint wp : waypoints)
		{
			wp.visited = false;
		}
	}

	// Returns (Manhattan) distance between two waypoints
	private float getDistance(Waypoint a, Waypoint b)
	{
		cameraZeroX = -imageWidth / 2 + cameraWidth / 2;
		if (a == null || b == null)
			return Float.MAX_VALUE;
		float dx = b.x - a.x;
		float dy = b.y - a.y;
		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	// Gets closest point already placed
	private Waypoint getClosestWaypoint(Waypoint wp)
	{
		float minDist = MAX_DIST;
		Waypoint retPt = null;
		for (Waypoint candidate : waypoints)
		{
			if (candidate == lastPoint || candidate == lastParent)
				continue;
			float dist = getDistance(candidate, wp);
			if (dist < minDist)
			{
				retPt = candidate;
				minDist = dist;
			}
		}
		return retPt;
	}

	// Connects two waypoints, and integrates a onto b's path if not already the
	// same. a should always be the new waypoint.
	private void connectPoints(Waypoint a, Waypoint b)
	{
		if (a.path != b.path)
		{
			bundle.paths.remove(a.path);
			// Switch all waypoints in a's path to b's path
			b.path.integrate(a.path);
		}
		a.addConnection(b);
		b.addConnection(a);
	}

	@Override
	public boolean onTouchEvent(MotionEvent e)
	{
		Waypoint wp = new Waypoint((int) (e.getX() + cameraX - cameraZeroX),
				(int) (e.getY() + cameraY - cameraZeroY));

		// On touch down, either place a new root or prepare to continue an
		// existing path
		if (e.getAction() == MotionEvent.ACTION_DOWN)
		{
			Waypoint closest = getClosestWaypoint(wp);
			if (closest != null)
			{
				lastPoint = closest;
			}
			else
			{
				long time = SystemClock.elapsedRealtime();
				long dt = time - lastTapTime;
				if (dt < DOUBLE_TAP_TIME)
				{
					bundle.paths.add(new MapPath(wp));
					lastPoint = wp;
					waypoints.add(wp);
				}
				else
				{
					dragging = true;
					lastX = e.getX();
					lastY = e.getY();
				}
				lastTapTime = time;
			}
		}
		// On move, connect new point to previous point once minimum distance
		// between points has been reached
		else if (e.getAction() == MotionEvent.ACTION_MOVE)
		{
			if (dragging)
			{
				float dx = lastX - e.getX();
				float dy = lastY - e.getY();
				// Keep camera in bounds: between 0 and image bounds
				// (X & Y move independently)
				cameraX = Math.max(
						-imageWidth / 2 + cameraWidth / 2,
						Math.min((int) (cameraX + dx), imageWidth / 2
								- cameraWidth / 2));
				cameraY = Math.max(
						-imageHeight / 2 + cameraHeight / 2,
						Math.min((int) (cameraY + dy), imageHeight / 2
								- cameraHeight / 2));
				scrollTo((int) cameraX, (int) cameraY);

				// Set new X,Y position
				lastX = e.getX();
				lastY = e.getY();
			}
			else if (getDistance(wp, lastPoint) > MIN_DIST)
			{
				lastPoint.path.addWaypoint(wp);
				connectPoints(wp, lastPoint);

				lastParent = lastPoint;
				lastPoint = wp;
				waypoints.add(wp);
			}

		}
		// On touch up, connect to closest point (under Max distance), not
		// including last two points
		else if (e.getAction() == MotionEvent.ACTION_UP)
		{
			if (!dragging)
			{
				Waypoint closest = getClosestWaypoint(wp);
				if (closest != null)
				{
					connectPoints(lastPoint, closest);
				}
				lastPoint = null;
				lastParent = null;
			}
			dragging = false;
		}
		return true;
	}

	// Used for color picking: path number determines color
	private int maskBytes(int numBytes)
	{
		int mask = 0xff000000;
		// Ones
		if ((numBytes & 0x00000001) != 0)
			mask |= 0x000000ff;
		// Twos
		if ((numBytes & 0x00000002) != 0)
			mask |= 0x0000ff00;
		// Fours
		if ((numBytes & 0x00000004) != 0)
			mask |= 0x00ff0000;
		return mask;
	}

	// Draws lines between points by following the path
	private void recursiveDrawLine(Canvas canvas, Waypoint wp)
	{
		// Don't visit the same node twice
		wp.visited = true;
		canvas.drawPoint(wp.x + cameraZeroX, wp.y + cameraZeroY, thickPaint);
		MapPath path = wp.path;
		for (Edge childEdge : wp.getConnections())
		{
			// Drawing the same line twice is pretty harmless, so I'm not TOO
			// careful here, and it doesn't seem to affect performance
			Waypoint child = path.getWaypoint(childEdge.edgeToId);
			canvas.drawLine(wp.x + cameraZeroX, wp.y + cameraZeroY, child.x
					+ cameraZeroX, child.y + cameraZeroY, thinPaint);
			if (!child.visited)
			{
				recursiveDrawLine(canvas, child);
			}
		}
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		// Draw each path
		for (int i = 0; i < bundle.paths.size(); i++)
		{
			int color = maskBytes(i % 7 + 1);
			thinPaint.setColor(color);
			thickPaint.setColor(color);
			recursiveDrawLine(canvas, bundle.paths.get(i).getRoot());
		}
		// Reset nodes for next draw cycle
		unvisitNodes();

		// Not sure what this does, but it was in all the online example code
		// snippets for drawing on a view
		invalidate();
	}

	// When the phone is rotated, resize the paths
	public void onOrientationChanged(int camWidth, int camHeight)
	{
		cameraWidth = camWidth;
		cameraHeight = camHeight;
		cameraZeroX = -imageWidth / 2 + cameraWidth / 2;
		cameraZeroY = -imageHeight / 2 + cameraHeight / 2;
		cameraX = cameraZeroX;
		cameraY = cameraZeroY;
		scrollTo(cameraX, cameraY);
		// bundle.resize(camWidth, camHeight);
	}
}
