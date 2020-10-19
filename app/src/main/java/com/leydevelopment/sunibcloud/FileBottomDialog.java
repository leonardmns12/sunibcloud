package com.leydevelopment.sunibcloud;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.DownloadFileRemoteOperation;
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation;
import com.owncloud.android.lib.resources.files.RenameFileRemoteOperation;

import java.io.File;

public class FileBottomDialog extends BottomSheetDialogFragment implements OnRemoteOperationListener , OnDatatransferProgressListener {
    private static final String CHANNEL_ID = "23";
    private LinearLayout downloadBtn , copyBtn , moveBtn , deleteBtn , renameBtn , actionMenu , renameMenu;

    private String path;

    private FileDialogListner mListener;

    private EditText rename;

    private Handler mHandler;

    private Button submitRename;

    private OwnCloudClient mClient;
    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder builder;
    private int PROGRESS_MAX = 100;
    private int PROGRESS_CURRENT = 0;

    private String IsFolder = "false";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.file_dialog, container , false);
        path = this.getArguments().getString("actionpath");
        IsFolder = this.getArguments().getString("isfolder");
        mHandler = new Handler();
        Uri serverUri = Uri.parse("https://indofolks.com");
        mClient = OwnCloudClientFactory.createOwnCloudClient(serverUri, getActivity(), true);
        mClient.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials("leonard", "gurame442"));
        downloadBtn = (LinearLayout) v.findViewById(R.id.downloadBtn);
        copyBtn = (LinearLayout) v.findViewById(R.id.copyBtn);
        moveBtn = (LinearLayout) v.findViewById(R.id.moveBtn);
        deleteBtn = (LinearLayout) v.findViewById(R.id.deleteBtn);
        renameBtn = (LinearLayout) v.findViewById(R.id.renameBtn);
        actionMenu = (LinearLayout) v.findViewById(R.id.actionMenu);
        renameMenu = (LinearLayout) v.findViewById(R.id.renameMenu);
        submitRename = (Button) v.findViewById(R.id.submitRename);
        rename = (EditText) v.findViewById(R.id.rename);
        if(IsFolder.equals("true")) {
            downloadBtn.setVisibility(View.GONE);
        }
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//               mListener.onActionTaken("Download" , path);
                startDownload(path);
               dismiss();
            }
        });
        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onActionTaken("Copy" , path);
                dismiss();
            }
        });
        moveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onActionTaken("Move" , path);
                dismiss();
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mListener.onActionTaken("Delete" , path);
                startRemove(path);
                dismiss();
            }
        });
        renameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionMenu.setVisibility(View.GONE);
                renameMenu.setVisibility(View.VISIBLE);
                String Filename = getFileName(path);
                rename.setText(Filename);
            }
        });
        submitRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRename(path);
                dismiss();
            }
        });
        return v;
    }

    private void startRename(String path) {
       String newName = rename.getText().toString();
       if (newName.isEmpty()) {
           rename.setError("New name cannot empty!");
       } else if (newName.contains("/")) {
           rename.setError("New name cannot contain symbol!");
       } else{
           RenameFileRemoteOperation renameFile = new RenameFileRemoteOperation(getFileName(path) , path, newName, isFolder());
           renameFile.execute(mClient, this, mHandler);
       }
    }

    private boolean isFolder() {
        if (IsFolder.equals("true")) {
            return true;
        } else {
            return false;
        }
    }


    private void startRemove(String path) {
        RemoveFileRemoteOperation removeFile = new RemoveFileRemoteOperation(path);
        removeFile.execute(mClient,this,mHandler);
    }

    private void startDownload(String paths) {
        try{
            notificationManager = NotificationManagerCompat.from(getActivity());

            builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_baseline_cloud_download_24)
                    .setContentTitle("Downloading Files")
                    .setContentText("0%")
                    .setPriority(NotificationCompat.PRIORITY_LOW).setOnlyAlertOnce(true);
            Intent ii = new Intent(getContext(), TaskFragment.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, ii, 0);

            // === Removed some obsoletes
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                String channelId = "23";
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
                builder.setChannelId(channelId);
            }
            builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
            notificationManager.notify(0, builder.build());
        }catch (Exception e) {
            e.printStackTrace();
        }
        File downFolder = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
        downFolder.mkdir();
        String remotePath = paths;
        DownloadFileRemoteOperation downloadOperation = new DownloadFileRemoteOperation(remotePath, downFolder.getAbsolutePath());
        downloadOperation.addDatatransferProgressListener(this);
        downloadOperation.execute(mClient, this, mHandler);
    }

    @Override
    public void onTransferProgress(long progressRate, long totalTransferredSoFar, long totalToTransfer, String fileAbsoluteName) {
        final long percentage = (totalToTransfer > 0 ? totalTransferredSoFar * 100 / totalToTransfer : 0);
        Log.d("TAG", "progressRate " + percentage);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                PROGRESS_CURRENT = (int)percentage;
                builder.setContentText(Integer.toString(PROGRESS_CURRENT) + "%").setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
                notificationManager.notify(0, builder.build());
            }
        });
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation caller, RemoteOperationResult result) {
        if (caller instanceof DownloadFileRemoteOperation) {
            onSuccessfulDownload(caller, result);
        } else if (caller instanceof  RemoveFileRemoteOperation) {
            onSuccessfulRemove(caller, result);
        } else if (caller instanceof  RenameFileRemoteOperation) {
            onSuccessfulRename(caller, result);
        }
    }

    private void onSuccessfulRename(RemoteOperation caller, RemoteOperationResult result) {
        mListener.onActionTaken("Rename" , path);
    }

    private void onSuccessfulRemove(RemoteOperation caller, RemoteOperationResult result) {
        mListener.onActionTaken("Remove" , path);
    }

    public void onSuccessfulDownload(RemoteOperation caller, RemoteOperationResult result) {
        Log.e("FInish" , "DOwnload finuish");
//        File downFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/Nextcloud");
//        File downloadedFile = downFolder.listFiles()[0];
        builder.setContentText("Download complete")
                .setProgress(0,0,false);
        notificationManager.notify(0, builder.build());

//        mFrame.setBackgroundDrawable(bDraw);
    }

    private String getFileName(String paths) {
        String Filename = "";
        int idx = 0;

        for (int i = paths.length()-1; i >= 0; i--) {
            if (paths.charAt(i) == '/') {
                if (i == paths.length()-1){
                    continue;
                }else{
                    idx = i+1;
                    break;
                }
            }
        }
        Filename = paths.substring(idx , paths.length());

        if (Filename.endsWith("/")){
            Filename = Filename.substring(0 , Filename.length()-1);
        }

        return Filename;
    }

    public interface FileDialogListner {
        void onActionTaken(String name , String rPath);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (FileBottomDialog.FileDialogListner) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement BottomSheetListener");
        }
    }
}
