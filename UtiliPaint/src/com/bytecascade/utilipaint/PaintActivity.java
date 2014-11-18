package com.bytecascade.utilipaint;

import java.io.File;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Timer;

import com.example.utilipaint.R;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.PopupMenu;

public class PaintActivity extends MenuActivity implements
		PopupMenu.OnMenuItemClickListener, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4565354057203995257L;
	private int dialogResult;

	public volatile boolean isRunning;

	@Override
	public void onSaveInstanceState(Bundle frozenState) {
		// etc. until you have everything important stored in the bundle
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Always call the superclass so it can restore the view hierarchy
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		this.getMenuInflater().inflate(R.menu.paint_activity_menu, menu);

		for (int i = 0; i < menu.size(); i++)
			menu.getItem(i).setVisible(true);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		isRunning = true;

		this.setContentView(R.layout.activity_action_paint);

		WindowManager.LayoutParams attrs = this.getWindow().getAttributes();
		attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
		this.getWindow().setAttributes(attrs);

		final BitmapFactory.Options op = new BitmapFactory.Options();
		op.inScaled = false;

		final PaintGLSurfaceView glsv = (PaintGLSurfaceView) findViewById(R.id.graphics_view);
		final Resources res = this.getResources();

		// Renderer is actually created here
		glsv.setImage(BitmapFactory.decodeResource(res, R.drawable.test, op));

		Timer update = new Timer();

		update.schedule(new UpdateAsyncTask(this), 0, 500);
	}

	File testFile;

	@Override
	protected void onStart() {
		super.onStart();
		isRunning = true;
	}

	@Override
	protected void onStop() {
		super.onStop();
		isRunning = false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
		case R.id.action_file:
		case R.id.action_help:
			showPopup(this, findViewById(id), true);
			return true;
		}

		return false;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
		case R.id.action_new:
			getButtonDialogBox(
					getDialogBox(R.string.dialog_new_title,
							R.string.dialog_new_message), R.string.button_ok,
					R.string.button_cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							setDialogResult(1);
						}
					}, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							setDialogResult(0);
						}
					}).show();

			if (dialogResult == 0)
				return false;

			Intent intent = new Intent(this, PaintActivity.class);
			startActivity(intent);

			finish();
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

	protected void setDialogResult(int selection) {
		dialogResult = selection;
	}
}
