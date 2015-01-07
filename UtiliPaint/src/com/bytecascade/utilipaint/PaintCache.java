package com.bytecascade.utilipaint;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

public class PaintCache
{

	private final Context context;
	private RandomAccessFile file;
	private File cacheFile;
	private MappedByteBuffer buffer;
	private FileChannel channel;
	final int WIDTH, HEIGHT;

	private volatile boolean success;

	public PaintCache(Context context, File file, String id) throws IOException
	{
		this.context = context;

		this.cacheFile = File.createTempFile(id + ".layer00", null,
				this.context.getCacheDir());
		this.file = new RandomAccessFile(this.cacheFile, "rw");

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file.getAbsolutePath(), options);

		this.WIDTH = options.outWidth;
		this.HEIGHT = options.outHeight;

		options.inJustDecodeBounds = false;
		options.inPreferQualityOverSpeed = true;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;

		if (this.WIDTH * 4 + 1024 < ((PaintActivity) context)
				.getAvailableMemory())
		{
			BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(
					file.getAbsolutePath(), true);

			for (int i = 0; i < this.HEIGHT; i++)
			{
				// ARGB
				Bitmap slice = decoder.decodeRegion(new Rect(0, i, this.WIDTH,
						i + 1), options);
				ByteBuffer tempBuffer = ByteBuffer.allocate(this.WIDTH * 4);
				slice.copyPixelsToBuffer(tempBuffer);
				this.file.write(tempBuffer.array());
			}

			this.channel = this.file.getChannel();
			this.channel.lock();
			this.buffer = this.channel.map(FileChannel.MapMode.READ_WRITE, 0,
					this.file.length());

			this.success = true;
		}
	}

	public PaintCache(PaintCache other, int x1, int y1, int x2, int y2)
			throws IOException
	{
		if (x1 > x2 || y1 > y2)
			throw new IllegalArgumentException(
					"This constructure requires x1 < x2 and y1 < y2; i.e. it must be top left and bottom right.");

		this.context = other.context;

		this.cacheFile = File.createTempFile("sel", null,
				this.context.getCacheDir());
		this.file = new RandomAccessFile(this.cacheFile, "rw");

		this.WIDTH = other.WIDTH;
		this.HEIGHT = other.HEIGHT;

		if (this.WIDTH * 4 + 1024 < ((PaintActivity) this.context)
				.getAvailableMemory())
		{
			for (int i = 0; i < y1; i++)
				this.file.write(new byte[4 * this.WIDTH]);
			for (int i = y1; i < y2; i++)
			{
				byte[] buf = new byte[4 * this.WIDTH];

				other.buffer.position(4 * (i * this.WIDTH + x1));
				other.buffer.get(buf, 4 * x1, 4 * (x2 - x1));

				this.file.write(buf);
			}
			for (int i = y2; i < this.HEIGHT; i++)
				this.file.write(new byte[4 * this.WIDTH]);

			this.channel = this.file.getChannel();
			this.channel.lock();
			buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0,
					this.file.length());

			success = true;
		}
	}

	public MappedByteBuffer getBuffer()
	{
		return buffer;
	}

	public Bitmap getBitmap(int x1, int y1, int x2, int y2, float scale)
	{
		final int SPACE = scale >= 1 ? 1 : (int) Math.floor(1 / scale);
		final int WIDTH = x2 - x1, HEIGHT = y2 - y1;
		int[] buf = new int[(int) (Math.ceil((float) WIDTH / SPACE) * Math
				.ceil((float) HEIGHT / SPACE))];

		for (int row = 0; row < HEIGHT; row += SPACE)
		{
			byte[] temp = new byte[4 * WIDTH];

			buffer.position(4 * ((row + y1) * this.WIDTH + x1));
			buffer.get(temp);

			for (int col = 0; col < WIDTH; col += SPACE)
			{
				int index = (row / SPACE)
						* (int) Math.ceil((float) WIDTH / SPACE) + col / SPACE;

				// RGBA -> ARGB
				buf[index] = Color.argb(0xff & temp[4 * col + 3],
						0xff & temp[4 * col], 0xff & temp[4 * col + 1],
						0xff & temp[4 * col + 2]);
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(buf,
				(int) Math.ceil((float) WIDTH / SPACE),
				(int) Math.ceil((float) HEIGHT / SPACE),
				Bitmap.Config.ARGB_8888);

		if (bitmap == null)
			throw new NullPointerException();

		return bitmap;
	}

	private void setBitmap(Bitmap bitmap, int x, int y)
	{
		Log.i("com.bytecascade.utilipaint", ":" + x + "," + y);

		int[] buf = new int[bitmap.getWidth() * bitmap.getHeight()];
		bitmap.getPixels(buf, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(),
				bitmap.getHeight());

		for (int row = 0; row < bitmap.getHeight(); row++)
		{
			byte[] temp = new byte[4 * bitmap.getWidth()];

			for (int col = 0; col < bitmap.getWidth(); col++)
			{
				temp[4 * col] = (byte) Color.red(buf[row * bitmap.getWidth()
						+ col]);
				temp[4 * col + 1] = (byte) Color.green(buf[row
						* bitmap.getWidth() + col]);
				temp[4 * col + 2] = (byte) Color.blue(buf[row
						* bitmap.getWidth() + col]);
				temp[4 * col + 3] = (byte) Color.alpha(buf[row
						* bitmap.getWidth() + col]);
			}

			buffer.position(4 * ((row + y) * this.WIDTH + x));
			buffer.put(temp);
		}
	}

	public boolean isSuccessful()
	{
		return success;
	}

	public void close() throws IOException
	{
		channel.close();
		this.file.close();
		cacheFile.delete();
		System.gc();
	}

	class PaintCacheUpdater implements Runnable
	{

		@Override
		public void run()
		{
			try
			{
				Bitmap bitmap = null;
				Point tl = null, br = null;

				while (true)
				{
					if (!((PaintActivity) context).isRunning)
						Thread.sleep(100);

					if (((PaintActivity) context).getPaintEvents().peek() == null
							&& bitmap != null)
					{
						setBitmap(bitmap, tl.x, tl.y);
						bitmap.recycle();

						bitmap = null;
						tl = null;
						br = null;
					}

					PaintAction action = ((PaintActivity) context)
							.getPaintEvents().take();

					if (tl == null || br == null
							|| !action.getBounds()[0].equals(tl)
							|| !action.getBounds()[1].equals(br))
					{
						if (bitmap != null)
						{
							setBitmap(bitmap, tl.x, tl.y);
							bitmap.recycle();
						}

						tl = action.getBounds()[0];
						br = action.getBounds()[1];

						Bitmap b = getBitmap(tl.x, tl.y, br.x, br.y, 1);
						bitmap = b.copy(Bitmap.Config.ARGB_8888, true);
						b.recycle();
					}

					switch (action.getActionType())
					{
					case REPLACE_PIXEL:
						bitmap.setPixel(tl.x, tl.y, (Integer) action.getData());
						break;
					case REPLACE_PIXELS:
						int[] bounds = (int[]) ((Object[]) action.getData())[0];
						int[] pixels = (int[]) ((Object[]) action.getData())[1];

						Log.i("com.bytecascade.utilipaint", "" + bounds[0]
								+ "," + bounds[1] + ":" + bounds[2] + ","
								+ bounds[3]);

						bitmap.setPixels(pixels, 0, bounds[2] - bounds[0],
								bounds[0] - tl.x, bounds[1] - tl.y, bounds[2]
										- bounds[0], bounds[3] - bounds[1]);
						break;
					default:
						break;
					}
				}
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

	}
}
