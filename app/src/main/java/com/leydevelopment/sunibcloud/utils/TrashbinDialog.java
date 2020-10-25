package com.leydevelopment.sunibcloud.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.leydevelopment.sunibcloud.R;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.trashbin.RemoveTrashbinFileRemoteOperation;
import com.owncloud.android.lib.resources.trashbin.RestoreTrashbinFileRemoteOperation;

public class TrashbinDialog extends BottomSheetDialogFragment implements OnRemoteOperationListener {

    LinearLayout restore , delete;
    protected Context mContext;
    protected String restorePath , fileName;
    protected Handler mHandler;
    protected OwnCloudClient mClient;
    private TrashbinDialog.TrashbinDialogListener mListener;

    public TrashbinDialog(Context mContext , String restorePath , String fileName) {
        this.mContext = mContext;
        this.fileName = fileName;
        this.restorePath = restorePath;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.trashbin_dialog , container , false);
        restore = (LinearLayout) v.findViewById(R.id.restore);
        delete = (LinearLayout) v.findViewById(R.id.delete);
        Uri serverUri = Uri.parse("https://indofolks.com");
        mClient = OwnCloudClientFactory.createOwnCloudClient(serverUri, getActivity(), true);
        mClient.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials("leonard", "gurame442"));
        mHandler = new Handler();
        Log.e("test" , restorePath);
        restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRestore();
                mListener.onActionTaken(restorePath);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDelete();
                mListener.onActionTaken(restorePath);
            }
        });

        return v;
    }

    private void startDelete() {
        RemoveTrashbinFileRemoteOperation removeFile = new RemoveTrashbinFileRemoteOperation(restorePath);
        removeFile.execute(mClient, this , mHandler);
    }

    private void startRestore() {
        RestoreTrashbinFileRemoteOperation restoreFile = new RestoreTrashbinFileRemoteOperation(restorePath, fileName, "leonard");
        restoreFile.execute(mClient ,this, mHandler);
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation caller, RemoteOperationResult result) {
        if (caller instanceof RestoreTrashbinFileRemoteOperation) {
            onSuccessfulRestore(caller , result);
        } else if (caller instanceof RemoveTrashbinFileRemoteOperation) {
            onSuccessfulRemove(caller , result);
        }
    }

    private void onSuccessfulRemove(RemoteOperation caller, RemoteOperationResult result) {
        Toast.makeText(mContext , "File deleted!" , Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void onSuccessfulRestore(RemoteOperation caller, RemoteOperationResult result) {
        Toast.makeText(mContext , "File restored!" , Toast.LENGTH_SHORT).show();
        dismiss();
    }

    public interface TrashbinDialogListener {
        void onActionTaken(String name);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (TrashbinDialog.TrashbinDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement BottomSheetListener");
        }
    }
}
