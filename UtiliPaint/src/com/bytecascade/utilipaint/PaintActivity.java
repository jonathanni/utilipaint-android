package com.bytecascade.utilipaint;

import com.example.utilipaint.R;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.PopupMenu;

public class PaintActivity extends MenuActivity implements
		PopupMenu.OnMenuItemClickListener {

	private int dialogResult;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		this.getMenuInflater().inflate(R.menu.paint_activity_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_action_paint);

		WindowManager.LayoutParams attrs = this.getWindow().getAttributes();
		attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
		this.getWindow().setAttributes(attrs);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
		case R.id.action_file:
		case R.id.action_help:
			showPopup(this, findViewById(id));
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
