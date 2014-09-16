package com.bytecascade.utilipaint;

import com.example.utilipaint.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

public class PaintWelcomeActivity extends Activity implements
		PopupMenu.OnMenuItemClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_paint);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		this.getMenuInflater().inflate(R.menu.paint_welcome_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	public boolean showPopup(View v) {
		PopupMenu popup = new PopupMenu(this, v);
		MenuInflater inflater = this.getMenuInflater();

		popup.setOnMenuItemClickListener(this);

		switch (v.getId()) {
		case R.id.action_file:
			inflater.inflate(R.menu.file_popup_menu, popup.getMenu());
			break;
		case R.id.action_help:
			inflater.inflate(R.menu.help_popup_menu, popup.getMenu());
			break;
		}

		popup.show();

		return true;
	}

	public boolean showDialogBox(int title, int message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message).setTitle(title);
		builder.show();

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
		case R.id.action_file:
		case R.id.action_help:
			showPopup(findViewById(id));
			return true;
		}

		return false;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
		case R.id.action_new:
			Intent intent = new Intent(this, PaintActivity.class);
			return true;
		case R.id.action_about:
			showDialogBox(R.string.dialog_about_title,
					R.string.dialog_about_message);
			return true;
		}

		return false;
	}
}
