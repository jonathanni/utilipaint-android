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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.provider.MediaStore;
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

	private int primaryColor = Color.argb(255, 0, 0, 0), secondaryColor = Color
			.argb(255, 255, 255, 255);
	
	private int brushRadius = 8, eraserRadius = 8;

	private static final String FILE_URI_STRING = "com.bytecascade.utilipaint.FILE_URI";

	private static final int PICKFILE_REQUEST_CODE = 0x1,
			SAVEFILE_REQUEST_CODE = 0x2;

	private Uri saveUri = null;

	private Menu optionMenu;

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
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode != RESULT_CANCELED)
		{
			switch (requestCode)
			{
			case PICKFILE_REQUEST_CODE:
				Intent intent = new Intent(this, PaintActivity.class);
				intent.putExtra(FILE_URI_STRING, data.getData());
				startActivity(intent);

				finish();
				break;
			case SAVEFILE_REQUEST_CODE:
				saveUri = data.getData();
				break;
			}
		}
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

				PaintTool lastTool = currentTool;

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

						x1 = Math.max(x1 + dx, 0);
						x2 = Math.min(x2 + dx, cache.WIDTH - 1);

						y1 = Math.max(y1 + dy, 0);
						y2 = Math.min(y2 + dy, cache.HEIGHT - 1);

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
				}

				if (currentTool == PaintTool.PAN_ZOOM)
					glsv.restoreCoords();
				else if (lastTool == PaintTool.PAN_ZOOM)
					glsv.storeCoords();
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapter)
			{
			}
		});

		this.optionMenu = menu;

		return ret;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Uri fileOpenUri;

		if (savedInstanceState == null)
		{
			Bundle extras = getIntent().getExtras();
			if (extras == null)
				fileOpenUri = null;
			else
				fileOpenUri = extras.getParcelable(FILE_URI_STRING);
		} else
			fileOpenUri = savedInstanceState.getParcelable(FILE_URI_STRING);

		Log.e("com.bytecascade.utilipaint", "" + fileOpenUri);

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

		File openFile = null;
		if (fileOpenUri == null)
			try
			{
				openFile = File.createTempFile("testIMG", ".png",
						this.getCacheDir());
				FileOutputStream testOut = new FileOutputStream(openFile);

				test.compress(Bitmap.CompressFormat.PNG, 100, testOut);

				testOut.flush();
				testOut.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		else
			openFile = new File(getPath(fileOpenUri));

		try
		{
			cache = new PaintCache(this, openFile, "testIMG");
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		((PaintGLSurfaceView) this.findViewById(R.id.graphics_view)).setImage(
				defaultImage, this.getCache().WIDTH, this.getCache().HEIGHT);

		new Thread(cache.new PaintCacheUpdater()).start();
	}

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
							Log.i("com.bytecascade.utilipaint", "New Document");

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

		case R.id.action_open:
		{
			Log.i("com.bytecascade.utilipaint", "Open Document");

			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			startActivityForResult(intent, PICKFILE_REQUEST_CODE);
		}
			return true;

		case R.id.action_save_as:
		{
			Intent intent = new Intent(Intent.ACTION_PICK);
			intent.setType("image/*");
			startActivityForResult(intent, SAVEFILE_REQUEST_CODE);

			//MenuItem save = this.optionMenu.findItem(R.id.action_file)
			//		.getSubMenu().findItem(R.id.action_save);
			//save.setEnabled(true);
		}

		// leave no return

		case R.id.action_save:

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

	public String getPath(Uri uri)
	{
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		startManagingCursor(cursor);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
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

	public int getBrushRadius()
	{
		return brushRadius;
	}

	public void setBrushRadius(int brushRadius)
	{
		this.brushRadius = brushRadius;
	}

	public int getEraserRadius()
	{
		return eraserRadius;
	}

	public void setEraserRadius(int eraserRadius)
	{
		this.eraserRadius = eraserRadius;
	}
}
