package com.bytecascade.utilipaint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;

import com.example.utilipaint.R;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

public class PaintActivity extends MenuActivity implements
		PopupMenu.OnMenuItemClickListener, Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4565354057203995257L;

	public volatile boolean isRunning;

	private static final int DEFAULT_IMG_WIDTH = 800, DEFAULT_IMG_HEIGHT = 600;

	private UpdateAsyncTask task;
	private PaintCache cache, selCache;

	private PaintTool currentTool = PaintTool.PAN_ZOOM;

	private Point p1 = new Point(), p2 = new Point();

	private View[] sidebarLayouts = new View[PaintTool.values().length];

	private LinkedBlockingQueue<PaintAction> paintEvents = new LinkedBlockingQueue<PaintAction>();

	private int primaryColor = Color.argb(1, 0, 0, 0), secondaryColor = Color
			.argb(1, 1, 1, 1);

	@Override
	public void onSaveInstanceState(Bundle frozenState)
	{
		// etc. until you have everything important stored in the bundle
	}

	public void onRestoreInstanceState(Bundle savedInstanceState)
	{
		// Always call the superclass so it can restore the view hierarchy
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		boolean ret = super.onCreateOptionsMenu(menu);

		// Inflate the menu; this adds items to the action bar if it is present.
		this.getMenuInflater().inflate(R.menu.paint_activity_menu, menu);

		for (int i = 0; i < menu.size(); i++)
			menu.getItem(i).setVisible(true);

		LayoutInflater layoutInflater = this.getLayoutInflater();

		for (int i = 0; i < PaintTool.values().length; i++)
			sidebarLayouts[i] = layoutInflater.inflate(
					PaintTool.values()[i].getLayoutID(), null);

		Spinner spinner = (Spinner) menu.findItem(R.id.action_tool_select)
				.getActionView();
		spinner.setAdapter(new IconAdapter(this, R.layout.row, IconAdapter
				.getStrings()));

		final Activity activity = this;
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> adapter, View view,
					int position, long id)
			{
				PaintGLSurfaceView glsv = (PaintGLSurfaceView) activity
						.findViewById(R.id.graphics_view);
				float[] transforms = glsv.getPSInfo();

				currentTool = PaintTool.values()[position];
				ScrollView sidebar = (ScrollView) activity
						.findViewById(R.id.sidebar);

				sidebar.removeAllViews();
				sidebar.addView(sidebarLayouts[position]);

				switch (currentTool)
				{
				case MOVE_PIXELS:
					TextView t = (TextView) activity
							.findViewById(R.id.move_pixels_status_desc_text);
					if (p1.equals(p2))
						t.setText("Please make a selection first.");
					else
					{
						t.setText("Ready.");

						glsv.storeCoords();

						int x1 = Math.min(p1.x, p2.x), x2 = Math
								.max(p1.x, p2.x), y1 = Math.min(p1.y, p2.y), y2 = Math
								.max(p1.y, p2.y);

						try
						{
							selCache = new PaintCache(cache, x1, y1, x2, y2);
						} catch (IOException e)
						{
							e.printStackTrace();
						}

						try
						{
							for (int row = Math.min(p1.y, p2.y); row < Math
									.max(p1.y, p2.y); row++)
							{
								Log.i("com.bytecascade.utilipaint", "" + row);

								int[] colors = new int[x2 - x1];

								paintEvents
										.put(new PaintAction(
												PaintAction.PaintActionType.REPLACE_PIXELS,
												x1, y1, x2, y2, new Object[] {
														new int[] { x1, row,
																x2, row + 1 },
														colors }));
							}
						} catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
					break;
				default:
					break;
				}

				if (currentTool != PaintTool.MOVE_PIXELS && selCache != null)
				{
					int dx, dy;

					dx = -(int) transforms[2];
					dy = (int) transforms[3];

					for (int row = Math.min(p1.y, p2.y); row < Math.max(p1.y,
							p2.y); row++)
					{

						if (row + dy < 0 || row + dy >= cache.HEIGHT)
							continue;

						int x1 = Math.min(p1.x, p2.x), x2 = Math
								.max(p1.x, p2.x), y1 = Math.min(p1.y, p2.y), y2 = Math
								.max(p1.y, p2.y);

						if ((x1 + dx < 0 && x2 + dx < 0)
								|| (x1 + dx >= cache.WIDTH && x2 + dx >= cache.WIDTH))
							continue;

						Bitmap b = selCache.getBitmap(x1, row, x2, row + 1, 1);

						int[] colors = new int[x2 - x1];

						b.getPixels(colors, 0, b.getWidth(), 0, 0,
								b.getWidth(), b.getHeight());

						Log.e("com.bytecascade.utilipaint", "" + colors[0]);

						x1 = Math.max(x1 + dx, 0);
						x2 = Math.min(x2 + dx, cache.WIDTH - 1);

						y1 = Math.max(y1 + dy, 0);
						y2 = Math.min(y2 + dy, cache.HEIGHT - 1);

						Log.i("com.bytecascade.utilipaint", "CACHE: " + x1
								+ "," + x2 + ":" + cache.WIDTH + ","
								+ cache.HEIGHT + ":"
								+ cache.getBuffer().capacity());

						paintEvents.add(new PaintAction(
								PaintAction.PaintActionType.REPLACE_PIXELS, x1,
								y1, x2, y2, new Object[] {
										new int[] { x1, row + dy, x2,
												row + dy + 1 }, colors }));

						b.recycle();
					}

					try
					{
						selCache.close();
					} catch (IOException e)
					{
						e.printStackTrace();
					}
					selCache = null;

					p1 = new Point();
					p2 = new Point();

					glsv.restoreCoords();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapter)
			{
			}
		});

		return ret;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		isRunning = true;

		this.setContentView(R.layout.activity_action_paint);

		WindowManager.LayoutParams attrs = this.getWindow().getAttributes();
		attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
		this.getWindow().setAttributes(attrs);

		final BitmapFactory.Options op = new BitmapFactory.Options();
		op.inScaled = false;

		final Resources res = this.getResources();

		Bitmap test, defaultImage;
		test = BitmapFactory.decodeResource(res, R.drawable.test, op);
		defaultImage = Bitmap.createBitmap(DEFAULT_IMG_WIDTH,
				DEFAULT_IMG_HEIGHT, Config.ARGB_8888);

		Timer update = new Timer();

		update.schedule(task = new UpdateAsyncTask(this), 0, 200);

		try
		{
			File testFile = File.createTempFile("testIMG", ".png",
					this.getCacheDir());
			FileOutputStream testOut = new FileOutputStream(testFile);
			test.compress(Bitmap.CompressFormat.PNG, 100, testOut);
			testOut.flush();
			testOut.close();

			cache = new PaintCache(this, testFile, "testIMG");
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		((PaintGLSurfaceView) this.findViewById(R.id.graphics_view)).setImage(
				defaultImage, this.getCache().WIDTH, this.getCache().HEIGHT);

		new Thread(cache.new PaintCacheUpdater()).start();
	}

	File testFile;

	@Override
	protected void onStart()
	{
		super.onStart();
		isRunning = true;
		task.isRunning = true;
		PaintGLSurfaceView glsv = ((PaintGLSurfaceView) findViewById(R.id.graphics_view));
		if (glsv.getRenderer() != null)
			glsv.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		isRunning = false;
		task.isRunning = false;
		((PaintGLSurfaceView) findViewById(R.id.graphics_view))
				.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		try
		{
			if (cache != null)
				cache.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();

		switch (id)
		{
		case R.id.action_file:
		case R.id.action_help:
			showPopup(this, findViewById(id), true);
			return true;
		}

		return false;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item)
	{
		int id = item.getItemId();

		switch (id)
		{
		case R.id.action_new:
			final Context context = this;

			getButtonDialogBox(
					getDialogBox(R.string.dialog_new_title,
							R.string.dialog_new_message), R.string.button_ok,
					R.string.button_cancel,
					new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{
							Log.d("com.bytecascade.utilipaint", "New Document");

							Intent intent = new Intent(context,
									PaintActivity.class);
							startActivity(intent);

							finish();
						}
					}, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{
						}
					}).show();

			return true;

		case R.id.action_exit:
			finish();
			return true;

		case R.id.action_about:
			getDialogBox(R.string.dialog_about_title,
					R.string.dialog_about_message).show();
			return true;
		}

		return false;
	}

	public long getAvailableMemory()
	{
		return task.getAvailableMemory();
	}

	public PaintCache getCache()
	{
		return cache;
	}

	public PaintTool getCurrentTool()
	{
		return currentTool;
	}

	public Point[] getRectSelectionPoints()
	{
		return new Point[] { p1, p2 };
	}

	public void setRectSelectionPoints(int x1, int y1, int x2, int y2)
	{
		p1.x = x1;
		p1.y = y1;

		p2.x = x2;
		p2.y = y2;
	}

	public LinkedBlockingQueue<PaintAction> getPaintEvents()
	{
		return paintEvents;
	}

	public int getPrimaryColor()
	{
		return primaryColor;
	}

	public void setPrimaryColor(int primaryColor)
	{
		this.primaryColor = primaryColor;
	}

	public int getSecondaryColor()
	{
		return secondaryColor;
	}

	public void setSecondaryColor(int secondaryColor)
	{
		this.secondaryColor = secondaryColor;
	}
}
