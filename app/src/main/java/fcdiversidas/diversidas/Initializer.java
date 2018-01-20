package fcdiversidas.diversidas;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import net.gotev.uploadservice.BinaryUploadRequest;
import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import java.io.File;

/**
 * Created by Zachary Bys on 2018-01-19.
 */

public class Initializer extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;

        UploadService.HTTP_STACK = new OkHttpStack(); // a new client will be automatically created
    }

    public void uploadBinary(final Context context, String filePath, String fileName) {
        try {
            // starting from 3.1+, you can also use content:// URI string instead of absolute file
            String uploadId =
                    new MultipartUploadRequest(context, "http://ec2-54-236-246-164.compute-1.amazonaws.com:3000/test")
                            .addFileToUpload(filePath, "filename")
                            .setMaxRetries(2)
                            .startUpload();
        } catch (Exception exc) {
            Log.e("upload",exc.getMessage());
        }
    }

}
