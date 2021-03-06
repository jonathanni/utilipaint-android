package com.bytecascade.utilipaint;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.TimerTask;

import com.example.utilipaint.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.widget.TextView;

public class UpdateAsyncTask extends TimerTask
{

	private Activity activity;
	private final PaintGLSurfaceView glsv;
	private MemoryInfo mi = new MemoryInfo();
	private ActivityManager activityManager;
	private TextView bottom;
	private static long diffTime;
	private float lastX = Float.NaN, lastY = Float.NaN;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run()
	{
		if (!isRunning)
			return;

		if (glsv.getRenderer() == null || glsv.getWidth() == 0
				|| glsv.getHeight() == 0)
			return;

		float[] info = glsv.getPSInfo();

		if (((PaintActivity) activity).getCache() != null
				&& ((PaintActivity) activity).getCache().isSuccessful()
				&& ((((PaintActivity) activity).getCurrentTool() == PaintTool.PAN_ZOOM && (lastX != info[2] || lastY != info[3])) || ((PaintActivity) activity)
						.isUpdated()))
			updateImage();

		// Flip colors of selection
		if (glsv.getRenderer().getSelection() != null)
			glsv.getRenderer().getSelection().flipColors();

		final long MEM = this.getAvailableMemory();

		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				// Update zoom info
				String selInfo = " ";
				Point[] selPoints = ((PaintActivity) activity)
						.getRectSelectionPoints();
				int col1 = ((PaintActivity) activity).getPrimaryColor(), col2 = ((PaintActivity) activity)
						.getSecondaryColor();

				if (!selPoints[0].equals(selPoints[1]))
					selInfo = String.format(" sel: %d, %d %dx%d",
							Math.min(selPoints[0].x, selPoints[1].x),
							Math.min(selPoints[0].y, selPoints[1].y),
							Math.abs(selPoints[0].x - selPoints[1].x),
							Math.abs(selPoints[0].y - selPoints[1].y));

				final NumberFormat PERCENT = DecimalFormat
						.getPercentInstance(Locale.getDefault()), NUMBER = DecimalFormat
						.getNumberInstance(Locale.getDefault());

				bottom.setText(String
						.format("%s %s bytes free x: %.2f y: %.2f rupd: %d ms rgba col1: #%02X%02X%02X%02X col2: #%02X%02X%02X%02X",
								PERCENT.format(glsv.getPSInfo()[4]),
								NUMBER.format(MEM),
								NUMBER.format(glsv.getPSInfo()[2]
										+ (float) glsv.getRenderer()
												.getImageWidth() / 2),
								NUMBER.format(glsv.getPSInfo()[3]
										+ (float) glsv.getRenderer()
												.getImageHeight() / 2),
								NUMBER.format(diffTime), Color.red(col1),
								Color.green(col1), Color.blue(col1),
								Color.alpha(col1), Color.red(col2),
								Color.green(col2), Color.blue(col2),
								Color.alpha(col2))
						+ selInfo);
			}
		});
	}

	public void updateImage()
	{
		float[] info = glsv.getPSInfo();

		final int cx = (int) (info[2] + glsv.getRenderer().getImageWidth() / 2), cy = (int) (info[3] + glsv
				.getRenderer().getImageHeight() / 2), w = (int) ((1.0f / info[4]) * glsv
				.getWidth()), h = (int) ((1.0f / info[4]) * glsv.getHeight());

		((PaintActivity) activity).setUpdated(false);

		final Bitmap image = ((PaintActivity) activity).getCache()
				.getBitmap(
						Math.max(0, cx - w / 2),
						Math.max(0, (glsv.getRenderer().getImageHeight() - cy)
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
				UpdateAsyncTask.diffTime = System.currentTimeMillis() - time;
			}
		});

		lastX = info[2];
		lastY = info[3];
	}

	public long getAvailableMemory()
	{
		activityManager.getMemoryInfo(mi);
		return mi.availMem;
	}

}
