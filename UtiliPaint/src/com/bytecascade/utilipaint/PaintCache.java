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
import android.graphics.Rect;

public class PaintCache
{

	private RandomAccessFile file;
	private File cacheFile;
	private MappedByteBuffer buffer;
	private FileChannel channel;
	final int WIDTH, HEIGHT;

	private volatile boolean success;

	public PaintCache(Context context, File file) throws IOException
	{
		cacheFile = File.createTempFile("layer00", null, context.getCacheDir());
		this.file = new RandomAccessFile(cacheFile, "rw");

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file.getAbsolutePath(), options);

		WIDTH = options.outWidth;
		HEIGHT = options.outHeight;

		options.inJustDecodeBounds = false;
		options.inPreferQualityOverSpeed = true;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;

		if (WIDTH * 4 + 1024 < ((PaintActivity) context).getAvailableMemory())
		{
			BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(
					file.getAbsolutePath(), true);

			for (int i = 0; i < HEIGHT; i++)
			{
				// ARGB
				Bitmap slice = decoder.decodeRegion(
						new Rect(0, i, WIDTH, i + 1), options);
				ByteBuffer tempBuffer = ByteBuffer.allocate(WIDTH * 4);
				slice.copyPixelsToBuffer(tempBuffer);
				this.file.write(tempBuffer.array());
			}

			channel = this.file.getChannel();
			channel.lock();
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
			buffer.get(temp, 0, 4 * WIDTH);

			for (int col = 0; col < WIDTH; col += SPACE)
			{
				int index = (row / SPACE)
						* (int) Math.ceil((float) WIDTH / SPACE) + col / SPACE;

				// ARGB -> ARGB
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

	public void processEventQueue()
	{

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
}
