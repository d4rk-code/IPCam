package com.ma.ipcam;

import android.graphics.PixelFormat;
import android.graphics.YuvImage;
import android.graphics.Rect;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.util.List;

public class CameraHandler {

    private Camera cam;
    private SimpleHttpServer server = new SimpleHttpServer();

    private final Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {

        private byte[] toJpeg(byte[] data, Camera camera) {
            Camera.Size size = camera.getParameters().getPreviewSize();

            YuvImage image = new YuvImage(
                    data,
                    ImageFormat.NV21,
                    size.width,
                    size.height,
                    null
            );

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, size.width, size.height), 50, out);

            return out.toByteArray();
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (server != null && server.isAlive()) {
                server.setFrame(toJpeg(data, camera));
            }
        }
    };

    public void startCamera(SurfaceHolder holder) {
        try {

            if (!server.isAlive()) {
                server.start();
            }

            cam = Camera.open();

            Camera.Parameters params = cam.getParameters();
            params.setPreviewFormat(ImageFormat.NV21);

            List<Camera.Size> sizes = params.getSupportedPreviewSizes();
            Camera.Size best = sizes.get(0);

            for (Camera.Size s : sizes) {
                if (s.width <= 640 && s.height <= 480) {
                    best = s;
                    break;
                }
            }

            params.setPreviewSize(best.width, best.height);
            cam.setParameters(params);

            cam.setDisplayOrientation(270);
            cam.setPreviewDisplay(holder);
            cam.setPreviewCallback(previewCallback);

            cam.startPreview();

        } catch (Exception e) {
            Log.e("CAMERA", "error", e);
        }
    }

    public void stopCamera() {
        try {
            if (cam != null) {
                cam.stopPreview();
                cam.release();
                cam = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
