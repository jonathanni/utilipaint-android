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
import android.graphics.Rect;
import android.util.Log;

public class PaintCache {

	private RandomAccessFile file;
	private File cacheFile;
	private MappedByteBuffer buffer;
	private FileChannel channel;
	final int WIDTH, HEIGHT;

	private boolean success = true;

	public PaintCache(Context context, File file) throws IOException {
		cacheFile = File.createTempFile("layer00", null);
		this.file = new RandomAccessFile(cacheFile, "rw");

		{
			BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(
					file.getAbsolutePath(), true);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(file.getAbsolutePath());

			WIDTH = options.outHeight;
			HEIGHT = options.outWidth;

			options.inJustDecodeBounds = false;
			options.inPreferQualityOverSpeed = true;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;

			if (WIDTH * 4 + 1024 > ((PaintActivity) context)
					.getAvailableMemory())
				success = false;

			for (int i = 0; i < HEIGHT; i++) {
				Bitmap slice = decoder.decodeRegion(
						new Rect(0, i, WIDTH, i + 1), options);
				ByteBuffer tempBuffer = ByteBuffer.allocate(WIDTH * 4);
				slice.copyPixelsToBuffer(tempBuffer);
				this.file.write(tempBuffer.array());
			}
		}

		if (success) {
			channel = this.file.getChannel();
			channel.lock();
			buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0,
					this.file.length());
			Log.d("com.bytecascade.utilipaint", "Buffer size: " + WIDTH
					* HEIGHT + " " + this.file.length());
		}
	}

	public MappedByteBuffer getBuffer() {
		return buffer;
	}

	public Bitmap getBitmap(int x, int y, int width, int height, float scale) {

		int space = scale <= 1 ? 1 : (int) Math.floor(scale);
		int[] buf = new int[4 * (width / space) * (height / space)];

		for (int row = 0; row < height; row += space) {
			byte[] temp = new byte[4 * width];
			buffer.get(temp, 4 * ((row + y) * WIDTH + x), 4 * width);
			for (int col = 0; col < width; col += space) {
				int index = (row / space) * (width / space) + col / space;

				buf[4 * index] = 0xff & temp[4 * col];
				buf[4 * index + 1] = 0xff & temp[4 * col + 1];
				buf[4 * index + 2] = 0xff & temp[4 * col + 2];
				buf[4 * index + 3] = 0xff & temp[4 * col + 3];
			}
		}

		return Bitmap.createBitmap(buf, width / space, height / space,
				Bitmap.Config.ARGB_8888);
	}

	public boolean getSuccess() {
		return success;
	}

	public void close() throws IOException {
		channel.close();
		this.file.close();
		cacheFile.delete();
		System.gc();
	}
}
