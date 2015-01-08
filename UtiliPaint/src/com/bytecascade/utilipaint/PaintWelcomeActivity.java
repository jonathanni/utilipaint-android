package com.bytecascade.utilipaint;

import com.example.utilipaint.R;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;

public class PaintWelcomeActivity extends MenuActivity implements
		PopupMenu.OnMenuItemClickListener
{

	private int dialogResult;

	private static final String FILE_URI_STRING = "com.bytecascade.utilipaint.FILE_URI";

	private static final int PICKFILE_REQUEST_CODE = 0x1;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
		case PICKFILE_REQUEST_CODE:
			if (resultCode != RESULT_CANCELED)
			{
				Intent intent = new Intent(this, PaintActivity.class);
				intent.putExtra(FILE_URI_STRING, data.getData());
				startActivity(intent);

				finish();
			}
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_paint);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		this.getMenuInflater().inflate(R.menu.paint_activity_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();

		switch (id)
		{
		case R.id.action_file:
		case R.id.action_help:
			showPopup(this, findViewById(id), false);
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

		{
			Intent intent = new Intent(this, PaintActivity.class);
			startActivity(intent);

			finish();
		}
			return true;

		case R.id.action_open:
		{
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			startActivityForResult(intent, PICKFILE_REQUEST_CODE);
		}
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

	protected void setDialogResult(int selection)
	{
		dialogResult = selection;
	}
}
