package com.bytecascade.utilipaint;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.TimerTask;

import com.example.utilipaint.R;

import android.app.Activity;

public class UpdateAsyncTask extends TimerTask {

	private Activity activity;
	private PaintGLSurfaceView glsv;

	public UpdateAsyncTask(Activity activity) {
		this.activity = activity;
		glsv = (PaintGLSurfaceView) activity.findViewById(R.id.graphics_view);
	}

	@Override
	public void run() {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// Update zoom info
				activity.setTitle(activity.getString(R.string.app_name)
						+ " "
						+ DecimalFormat.getPercentInstance(Locale.getDefault())
								.format(glsv.getPSInfo()[4]));
			}
		});
	}

}
