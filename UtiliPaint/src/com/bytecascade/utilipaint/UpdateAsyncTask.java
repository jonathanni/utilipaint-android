package com.bytecascade.utilipaint;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.TimerTask;

import com.example.utilipaint.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.widget.TextView;

public class UpdateAsyncTask extends TimerTask
{

	private Activity activity;
	private PaintGLSurfaceView glsv;
	private MemoryInfo mi = new MemoryInfo();
	private ActivityManager activityManager;
	private TextView bottom;

	public UpdateAsyncTask(Activity activity)
	{
		this.activity = activity;
		glsv = (PaintGLSurfaceView) activity.findViewById(R.id.graphics_view);

		activityManager = (ActivityManager) activity
				.getSystemService(Activity.ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(mi);
		bottom = (TextView) activity.findViewById(R.id.info_content);
	}

	@Override
	public void run()
	{
		final long MEM = this.getAvailableMemory();

		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				// Update zoom info
				bottom.setText(""
						+ DecimalFormat.getPercentInstance(Locale.getDefault())
								.format(glsv.getPSInfo()[4])
						+ " "
						+ DecimalFormat.getNumberInstance(Locale.getDefault())
								.format(MEM)
						+ " bytes free "
						+ DecimalFormat.getNumberInstance(Locale.getDefault())
								.format((int) (1000. / glsv.getRenderer()
										.getFrameTime())) + " fps");
			}
		});

		// TODO fix scaling

		// final BitmapFactory.Options op = new BitmapFactory.Options();
		// op.inSampleSize = (int) Math.round(1.0 / glsv.getPSInfo()[4]);

		// final Resources res = activity.getResources();

		// PaintImage.loadTexture(activity,
		// BitmapFactory.decodeResource(res, R.drawable.test, op));
	}

	public long getAvailableMemory()
	{
		activityManager.getMemoryInfo(mi);
		return mi.availMem;
	}

}
