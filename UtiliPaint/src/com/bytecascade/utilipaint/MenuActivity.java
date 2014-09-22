package com.bytecascade.utilipaint;

import com.example.utilipaint.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.MenuInflater;
import android.view.View;
import android.widget.PopupMenu;

public abstract class MenuActivity extends Activity {
	boolean showPopup(PopupMenu.OnMenuItemClickListener listener, View v,
			boolean showAll) {
		PopupMenu popup = new PopupMenu(this, v);
		MenuInflater inflater = this.getMenuInflater();

		popup.setOnMenuItemClickListener(listener);

		switch (v.getId()) {
		case R.id.action_file:
			inflater.inflate(R.menu.file_popup_menu, popup.getMenu());
			break;
		case R.id.action_help:
			inflater.inflate(R.menu.help_popup_menu, popup.getMenu());
			break;
		}

		if (showAll)
			for (int i = 0; i < popup.getMenu().size(); i++)
				popup.getMenu().getItem(i).setVisible(true);

		popup.show();

		return true;
	}

	AlertDialog.Builder getDialogBox(int title, int message) {
		return new AlertDialog.Builder(this).setMessage(message)
				.setTitle(title);
	}

	AlertDialog.Builder getButtonDialogBox(AlertDialog.Builder builder, int ok,
			int cancel, DialogInterface.OnClickListener okListener,
			DialogInterface.OnClickListener cancelListener) {
		return builder.setPositiveButton(ok, okListener).setNegativeButton(
				cancel, cancelListener);
	}
}
