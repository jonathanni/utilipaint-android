package com.example.utilipaint;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

public class PaintActivity extends FragmentActivity implements
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

	public boolean showDialogBox(View v) {
		new NoticeDialogFragment(v).show(getSupportFragmentManager(), "about");

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
		case R.id.action_about:
			showDialogBox(findViewById(id));
			return true;
		}

		return false;
	}

	private class NoticeDialogFragment extends DialogFragment {

		View v;

		public NoticeDialogFragment(View v) {
			this.v = v;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

			switch (v.getId()) {
			case R.id.action_about:
				builder.setMessage(R.string.dialog_about_message).setTitle(
						R.string.dialog_about_title);
				break;
			}

			return builder.create();
		}
	}
}
