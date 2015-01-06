package com.bytecascade.utilipaint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Timer;

import com.example.utilipaint.R;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.Spinner;

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
	private PaintCache cache;

	private PaintTool currentTool = PaintTool.PAN_ZOOM;

	private Point topLeft = new Point(), bottomRight = new Point();

	private View[] sidebarLayouts = new View[PaintTool.values().length];

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
		{
			sidebarLayouts[i] = layoutInflater.inflate(
					PaintTool.values()[i].getLayoutID(), null);
			Log.i("com.bytecascade.utilipaint", "" + sidebarLayouts[i]);
		}

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
				currentTool = PaintTool.values()[position];
				ScrollView sidebar = (ScrollView) activity
						.findViewById(R.id.sidebar);

				sidebar.removeAllViews();
				sidebar.addView(sidebarLayouts[position]);
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

			cache = new PaintCache(this, testFile);
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		((PaintGLSurfaceView) this.findViewById(R.id.graphics_view)).setImage(
				defaultImage, this.getCache().WIDTH, this.getCache().HEIGHT);
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
		return new Point[] { topLeft, bottomRight };
	}

	public void setRectSelectionPoints(int x1, int y1, int x2, int y2)
	{
		topLeft.x = x1;
		topLeft.y = y1;

		bottomRight.x = x2;
		bottomRight.y = y2;
	}
}
