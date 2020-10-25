package com.leydevelopment.sunibcloud.ui;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.storage.StorageReference;
import com.leydevelopment.sunibcloud.R;
import com.leydevelopment.sunibcloud.adapter.FilesArrayAdapter;
import com.leydevelopment.sunibcloud.models.SharedPref;
import com.leydevelopment.sunibcloud.utils.BottomSheet;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.Quota;
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation;

import java.io.File;

public class TaskFragment extends Fragment implements OnRemoteOperationListener, OnDatatransferProgressListener {
    private static final String CHANNEL_ID = "22";
    Uri fileUri;
    ImageView buttonOpenDialog;
    ProgressBar pb;
    TextView progressView;
    private static String filename;
    private StorageReference mStorageRef;
    @SuppressLint("StaticFieldLeak")
    private static TextView filechoosen;
    @SuppressLint("StaticFieldLeak")
    private static Button buttonUpload;
    @SuppressLint("StaticFieldLeak")
    private static TableLayout fileInfo;
    private StorageReference mStorage;
    //fileInfoString
    @SuppressLint("StaticFieldLeak")
    private static TextView fileName;
    @SuppressLint("StaticFieldLeak")
    private static TextView extensionView;
    @SuppressLint("StaticFieldLeak")
    private static TextView fileSizeView , storageSize , usedSize;
    private static String extension;
    private static String fileSize;
    private static File uriFile;
    private OwnCloudClient client;
    private Handler handler;
    private FilesArrayAdapter mFilesAdapter;

    //notification progress
    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder builder;
    private int PROGRESS_MAX = 100;
    private int PROGRESS_CURRENT = 0;
    private String totalQuota, quotaUsed;
    private Context mContext;
    public static void receiverMethod(String name , int size , File fileUri) {
        if (size < 1000000) {
            fileSize = Integer.toString(size) + " kb";
        } else {
            float a = size/1000000.2F;
            fileSize = String.format("%.2f",a) + " mb";
         }
        extension = name.substring(name.indexOf("."));
        filechoosen.setVisibility(View.INVISIBLE);
        buttonUpload.setVisibility(View.VISIBLE);
        fileInfo.setVisibility(View.VISIBLE);
        filename = name;
        fileName.setText(name);
        extensionView.setText(extension);
        fileSizeView.setText(fileSize);
        uriFile = fileUri;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // SETS the target fragment for use later when sending results
        View v = inflater.inflate(R.layout.fragment_task , container , false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        fileName = v.findViewById(R.id.fileName);
        extensionView   = v.findViewById(R.id.fileExt);
        fileSizeView    = v.findViewById(R.id.fileSize);
        storageSize     = v.findViewById(R.id.storageSize);
        usedSize        = v.findViewById(R.id.usedSize);
        buttonUpload    = v.findViewById(R.id.buttonUpload);
        filechoosen     = v.findViewById(R.id.fileChoosen);
        fileInfo        = v.findViewById(R.id.fileInfo);
        pb              = v.findViewById(R.id.progress_bar);
        progressView    = v.findViewById(R.id.progress);
        pb.setVisibility(View.INVISIBLE);
        progressView.setVisibility(View.INVISIBLE);
        buttonUpload.setVisibility(View.INVISIBLE);
        fileInfo.setVisibility(View.INVISIBLE);
        handler = new Handler();
        Uri serverUri = Uri.parse("http://indofolks.com/");
        client = OwnCloudClientFactory.createOwnCloudClient(serverUri , getActivity() , true);
        client.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials("leonard", "gurame442"));
        buttonOpenDialog = (ImageView) v.findViewById(R.id.openDialog);
        Quota quota = new Quota();
        Long use = quota.free;
        Log.e("QUOTA" , Long.toString(use));
        mContext = getContext();
        totalQuota = getInfo("total_quota", mContext);
        quotaUsed = getInfo( "used_quota",mContext);
        usedSize.setText("used: " +quotaUsed);
        storageSize.setText(totalQuota.substring(1));
        buttonOpenDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressView.setVisibility(View.GONE);
                BottomSheet bottomSheet = new BottomSheet();
                bottomSheet.show(getFragmentManager(), "string");
            }
        });
        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uriFile != null){
                    pb.setVisibility(View.VISIBLE);
                    progressView.setVisibility((View.VISIBLE));
                    buttonUpload.setVisibility(View.GONE);
                    fileInfo.setVisibility(View.INVISIBLE);
                    fileUpload(uriFile);
                }
            }
        });
        return v;
    }

    private String getInfo(String shared_key, Context curr_context) {
        SharedPref sf = new SharedPref(shared_key , curr_context);
        return sf.getQuotaUsed();
    }

    private void fileUpload(File uriFile) {
        try{
            notificationManager = NotificationManagerCompat.from(getContext());

            builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_baseline_cloud_download_24)
                    .setContentTitle("Uploading Files")
                    .setContentText("0%")
                    .setPriority(NotificationCompat.PRIORITY_LOW).setOnlyAlertOnce(true);
            Intent ii = new Intent(getContext(), TaskFragment.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, ii, 0);

            // === Removed some obsoletes
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                String channelId = "22";
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
                builder.setChannelId(channelId);
            }
            builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
            notificationManager.notify(0, builder.build());


            Long timeStampLong = uriFile.lastModified() / 1000;
            String timeStamp = timeStampLong.toString();
            String remotePath = FileUtils.PATH_SEPARATOR + uriFile.getName();
            if ( remotePath.contains(" ")) {
                remotePath = remotePath.replaceAll("\\s" , "%20");
            }
            String mimeType = "image/png";
            UploadFileRemoteOperation uploadOperation = new UploadFileRemoteOperation(uriFile.getAbsolutePath(), remotePath, mimeType, timeStamp);
            uploadOperation.addDataTransferProgressListener(this);
            uploadOperation.execute(client, this , handler);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 9 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectFile();
        } else {
            Toast.makeText(getActivity() , "please granted permission.." , Toast.LENGTH_SHORT).show();
        }
    }

    private void selectFile() {
        Intent intent = new Intent();
        intent.setType("application/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 86);
    }

    @Override
    public void onTransferProgress(long progressRate, long totalTransferredSoFar, long totalToTransfer, String fileAbsoluteName) {
        Log.e("Tag" , "MASUK");
        final long percentage = (totalToTransfer > 0 ? totalTransferredSoFar * 100 / totalToTransfer : 0);
        handler.post(new Runnable() {
            @Override
            public void run() {
                PROGRESS_CURRENT = (int)percentage;
                pb.setVisibility(View.VISIBLE);
                pb.setProgress((int)percentage);
                builder.setContentText(Integer.toString(PROGRESS_CURRENT) + "%").setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
                notificationManager.notify(0, builder.build());
                progressView.setText(Long.toString(percentage) + " %");
            }
        });
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation caller, RemoteOperationResult result) {
        Toast.makeText(getActivity(), String.valueOf(result.getHttpCode()), Toast.LENGTH_SHORT).show();
        if (!result.isSuccess()) {
            Log.e("TAG", "Err : " + result.getLogMessage(), result.getException());
            Toast.makeText(getActivity() , "Error while uploading files!" , Toast.LENGTH_LONG).show();
            pb.setVisibility(View.GONE);
            progressView.setVisibility(View.GONE);
            progressView.setText("");
            pb.setProgress(0);
        } else if (caller instanceof UploadFileRemoteOperation) {
            onSuccessfulUpload((UploadFileRemoteOperation) caller , result);
        }
    }

    private void onSuccessfulUpload(UploadFileRemoteOperation caller, RemoteOperationResult result) {
        builder.setContentText("Upload complete")
                .setProgress(0,0,false);
        notificationManager.notify(0, builder.build());
        pb.setVisibility(View.GONE);
        progressView.setVisibility(View.GONE);
        progressView.setText("");
        pb.setProgress(0);
        Toast.makeText(getActivity() , "File successfully Uploaded" , Toast.LENGTH_LONG).show();
    }
}
