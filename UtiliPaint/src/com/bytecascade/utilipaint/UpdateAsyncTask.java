package com.bytecascade.utilipaint;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.TimerTask;

import com.example.utilipaint.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.graphics.Bitmap;
import android.widget.TextView;

public class UpdateAsyncTask extends TimerTask
{

	private Activity activity;
	private final PaintGLSurfaceView glsv;
	private MemoryInfo mi = new MemoryInfo();
	private ActivityManager activityManager;
	private TextView bottom;
	private static long diffTime;
	public volatile boolean isRunning;

	public UpdateAsyncTask(Activity activity)
	{
		this.activity = activity;
		glsv = (PaintGLSurfaceView) activity.findViewById(R.id.graphics_view);

		activityManager = (ActivityManager) activity
				.getSystemService(Activity.ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(mi);
		bottom = (TextView) activity.findViewById(R.id.info_content);

		isRunning = true;
	}

	@Override
	public void run()
	{
		if (!isRunning)
			return;

		if (glsv.getRenderer() == null || glsv.getWidth() == 0
				|| glsv.getHeight() == 0)
			return;

		float[] info = glsv.getPSInfo();

		final int cx = (int) (info[2] + glsv.getRenderer().getImageWidth() / 2), cy = (int) (info[3] + glsv
				.getRenderer().getImageHeight() / 2), w = (int) ((1.0f / info[4]) * glsv
				.getWidth()), h = (int) ((1.0f / info[4]) * glsv.getHeight());

		if (((PaintActivity) activity).getCache() != null
				&& ((PaintActivity) activity).getCache().isSuccessful())
		{
			final Bitmap image = ((PaintActivity) activity).getCache()
					.getBitmap(
							Math.max(0, cx - w / 2),
							Math.max(0,
									(glsv.getRenderer().getImageHeight() - cy)
											- h / 2),
							Math.min(glsv.getRenderer().getImageWidth(), cx
									+ (w - w / 2)),
							Math.min(glsv.getRenderer().getImageHeight(), (glsv
									.getRenderer().getImageHeight() - cy)
									+ (h - h / 2)), glsv.getPSInfo()[4]);

			glsv.queueEvent(new Runnable()
			{
				@Override
				public void run()
				{
					final long time = System.currentTimeMillis();
					PaintImage.loadTexture(activity, image);
					glsv.getRenderer().getImage().updateCoords();
					UpdateAsyncTask.diffTime = System.currentTimeMillis()
							- time;
				}
			});
		}

		final long MEM = this.getAvailableMemory();

		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				// Update zoom info

				bottom.setText(String.format(
						"%s %s bytes free %s fps x: %.2f y: %.2f upd: %d",
						DecimalFormat.getPercentInstance(Locale.getDefault())
								.format(glsv.getPSInfo()[4]),
						DecimalFormat.getNumberInstance(Locale.getDefault())
								.format(MEM),
						DecimalFormat.getNumberInstance(Locale.getDefault())
								.format((int) (1000.0 / glsv.getRenderer()
										.getFrameTime())), glsv.getPSInfo()[2]
								+ (float) glsv.getRenderer().getImageWidth()
								/ 2, glsv.getPSInfo()[3]
								+ (float) glsv.getRenderer().getImageHeight()
								/ 2, diffTime));
			}
		});
	}

	public long getAvailableMemory()
	{
		activityManager.getMemoryInfo(mi);
		return mi.availMem;
	}

}
