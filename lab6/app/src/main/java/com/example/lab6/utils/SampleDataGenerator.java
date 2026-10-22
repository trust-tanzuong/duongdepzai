package com.example.lab6.utils;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.Surface;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class SampleDataGenerator {

    public static void generateSampleContacts(Context context) {
        String[][] contacts = {
                {"abc def", "0912345678"},
                {"xyz", "0987654321"},
                {"hhh", "0901234567"},
                {"jjjj", "0911223344"},
                {"ssss", "0922334455"},
                {"kkkk", "0933445566"}
        };

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        for (String[] contact : contacts) {
            String name = contact[0];
            String phone = contact[1];

            int rawContactInsertIndex = ops.size();

            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                    .build());

            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());
        }

        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            Toast.makeText(context, "Đã tạo 6 liên hệ mẫu thành công!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi tạo danh bạ mẫu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void generateSampleMedia(Context context) {
        String[] imageNames = {
                "photo_abc_def.jpg",
                "sunset_view.jpg",
                "mountain_landscape.jpg",
                "city_night.jpg",
                "beach_vacation.jpg",
                "cat_pet.jpg"
        };
        int[] colors = {
                Color.parseColor("#6200EE"),
                Color.parseColor("#FF5722"),
                Color.parseColor("#4CAF50"),
                Color.parseColor("#00BCD4"),
                Color.parseColor("#E91E63"),
                Color.parseColor("#FF9800")
        };

        int createdCount = 0;
        long now = System.currentTimeMillis() / 1000;

        for (int i = 0; i < imageNames.length; i++) {
            Bitmap bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(colors[i]);

            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(50);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(imageNames[i].replace(".jpg", ""), 400, 400, paint);

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, imageNames[i]);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DATE_ADDED, now - (i * 3600));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Lab6Sample");
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
            }

            Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream os = context.getContentResolver().openOutputStream(uri)) {
                    if (os != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, os);
                        createdCount++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    context.getContentResolver().update(uri, values, null, null);
                }
            }
        }

        // Generate real playable MP4 video files using MediaCodec & MediaMuxer
        createRealSampleVideo(context, "sample_video_clip.mp4", 3);
        createRealSampleVideo(context, "vacation_short.mp4", 2);

        Toast.makeText(context, "Đã tạo " + createdCount + " hình ảnh & 2 video mẫu!", Toast.LENGTH_SHORT).show();
    }

    private static void createRealSampleVideo(Context context, String fileName, int durationSeconds) {
        try {
            File outputFile = new File(context.getCacheDir(), fileName);
            int width = 320;
            int height = 240;
            int fps = 15;
            int numFrames = durationSeconds * fps;

            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE, 400000);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, fps);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

            MediaCodec encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            Surface inputSurface = encoder.createInputSurface();
            encoder.start();

            MediaMuxer muxer = new MediaMuxer(outputFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int trackIndex = -1;
            boolean muxerStarted = false;

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            for (int i = 0; i < numFrames; i++) {
                Canvas canvas = inputSurface.lockCanvas(null);
                if (canvas != null) {
                    canvas.drawColor((i % 6 < 3) ? Color.parseColor("#6200EE") : Color.parseColor("#00BCD4"));
                    Paint paint = new Paint();
                    paint.setColor(Color.WHITE);
                    paint.setTextSize(24);
                    paint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText(fileName, width / 2f, height / 2f - 20, paint);
                    canvas.drawText("Playing Frame " + (i + 1) + "/" + numFrames, width / 2f, height / 2f + 20, paint);
                    inputSurface.unlockCanvasAndPost(canvas);
                }

                boolean done = false;
                while (!done) {
                    int encoderStatus = encoder.dequeueOutputBuffer(bufferInfo, 10000);
                    if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        done = true;
                    } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (!muxerStarted) {
                            MediaFormat newFormat = encoder.getOutputFormat();
                            trackIndex = muxer.addTrack(newFormat);
                            muxer.start();
                            muxerStarted = true;
                        }
                    } else if (encoderStatus >= 0) {
                        ByteBuffer encodedData = encoder.getOutputBuffer(encoderStatus);
                        if (encodedData != null) {
                            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                                bufferInfo.size = 0;
                            }
                            if (bufferInfo.size != 0 && muxerStarted) {
                                encodedData.position(bufferInfo.offset);
                                encodedData.limit(bufferInfo.offset + bufferInfo.size);
                                bufferInfo.presentationTimeUs = (i * 1_000_000L) / fps;
                                muxer.writeSampleData(trackIndex, encodedData, bufferInfo);
                            }
                            encoder.releaseOutputBuffer(encoderStatus, false);
                        }
                    }
                }
            }

            encoder.signalEndOfInputStream();
            boolean endOfStream = false;
            while (!endOfStream) {
                int encoderStatus = encoder.dequeueOutputBuffer(bufferInfo, 10000);
                if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    endOfStream = true;
                } else if (encoderStatus >= 0) {
                    ByteBuffer encodedData = encoder.getOutputBuffer(encoderStatus);
                    if (encodedData != null) {
                        if (bufferInfo.size != 0 && muxerStarted) {
                            encodedData.position(bufferInfo.offset);
                            encodedData.limit(bufferInfo.offset + bufferInfo.size);
                            muxer.writeSampleData(trackIndex, encodedData, bufferInfo);
                        }
                        encoder.releaseOutputBuffer(encoderStatus, false);
                    }
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        endOfStream = true;
                    }
                }
            }

            encoder.stop();
            encoder.release();
            if (muxerStarted) {
                muxer.stop();
                muxer.release();
            }

            saveVideoToMediaStore(context, outputFile, fileName, durationSeconds * 1000L);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveVideoToMediaStore(Context context, File sourceFile, String videoName, long durationMs) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME, videoName);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Video.Media.DURATION, durationMs);
        values.put(MediaStore.Video.Media.SIZE, sourceFile.length());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/Lab6Sample");
            values.put(MediaStore.Video.Media.IS_PENDING, 1);
        }

        Uri uri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (InputStream is = new FileInputStream(sourceFile);
                 OutputStream os = context.getContentResolver().openOutputStream(uri)) {
                if (os != null) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = is.read(buf)) > 0) {
                        os.write(buf, 0, len);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear();
                values.put(MediaStore.Video.Media.IS_PENDING, 0);
                context.getContentResolver().update(uri, values, null, null);
            }
        }
    }
}