package com.example.utilipaint;

import android.os.Bundle;
import android.app.Activity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

public class PaintActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_paint);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		this.getMenuInflater().inflate(R.menu.paint, menu);

		return super.onCreateOptionsMenu(menu);
	}

	public boolean showPopup(View v) {
		PopupMenu popup = new PopupMenu(this, v);
		MenuInflater inflater = this.getMenuInflater();

		switch (v.getId()) {
		case R.id.action_file:
			inflater.inflate(R.menu.file_popup_menu, popup.getMenu());
			break;
		}
		
		popup.show();

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_file)
			showPopup(findViewById(id));

		return true;
	}

}
