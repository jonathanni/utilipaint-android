package com.bytecascade.utilipaint;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.TimerTask;

import com.example.utilipaint.R;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.BitmapFactory;

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

		// TODO fix scaling
		
		final BitmapFactory.Options op = new BitmapFactory.Options();

		final Resources res = activity.getResources();

		PaintImage.loadTexture(activity,
				BitmapFactory.decodeResource(res, R.drawable.test, op));
	}

}
