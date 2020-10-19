package com.leydevelopment.sunibcloud;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.owncloud.android.lib.common.OwnCloudBasicCredentials;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.lib.resources.trashbin.ReadTrashbinFolderRemoteOperation;
import com.owncloud.android.lib.resources.trashbin.model.TrashbinFile;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TrashbinActivity extends AppCompatActivity implements OnRemoteOperationListener {
    private Handler mHandler;
    private OwnCloudClient mClient;
    private OwnCloudBasicCredentials cred;
    private String username;

    private RecyclerView trashbinList;

    String TRASHBIN_CACHE_KEY = "trashbin_";
    String path , keypath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trashbin);
        trashbinList = (RecyclerView) findViewById(R.id.trashbinList);
        List<TrashbinFile> listFile = null;
        TrashbinAdapter trashbinAdapter = new TrashbinAdapter(listFile,this);
        trashbinList.setAdapter(trashbinAdapter);
        Uri serverUri = Uri.parse("https://indofolks.com");
        username = "leonard";
        mHandler = new Handler();
        path = FileUtils.PATH_SEPARATOR;
        mClient = OwnCloudClientFactory.createOwnCloudClient(serverUri, this, true);
        mClient.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials(username, "gurame442"));
        try {
            checkCached();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        getTrashbin();
    }

    private void getTrashbin() {
        ReadTrashbinFolderRemoteOperation readTrashbin = new ReadTrashbinFolderRemoteOperation(path , username);
        readTrashbin.execute(mClient , this , mHandler);
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation caller, RemoteOperationResult result) {
        if (caller instanceof  ReadTrashbinFolderRemoteOperation) {
            OnSuccessfulReadTrashbin(caller , result);
        }
    }

    private void OnSuccessfulReadTrashbin(RemoteOperation caller, RemoteOperationResult result) {
        List<TrashbinFile> listFile = new ArrayList<TrashbinFile>();
        for (Object obj: result.getData()) {
            listFile.add((TrashbinFile) obj);
        }
        if (listFile != null){
            TrashbinAdapter trashbinAdapter = new TrashbinAdapter(listFile,this);
            trashbinList.setAdapter(trashbinAdapter);
            trashbinList.setLayoutManager(new LinearLayoutManager(this));
            createCachedFile(this , keypath ,listFile);
        }
    }

    private void checkCached() throws IOException, ClassNotFoundException {
        keypath = TRASHBIN_CACHE_KEY + path;
        if(keypath.contains("/")) {
            keypath = keypath.replaceAll("/" , "%10");
        }
        List<TrashbinFile> apkCacheList = (ArrayList<TrashbinFile>)readCachedFile  (Objects.requireNonNull(this), keypath);
        if(apkCacheList == null) {
            Log.e("Cache " , "Cache null");
        } else {
            TrashbinAdapter trashbinAdapter = new TrashbinAdapter(apkCacheList,this);
            trashbinList.setAdapter(trashbinAdapter);
            trashbinList.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    public static void createCachedFile (Context context, String key, List<TrashbinFile> fileName) {
        try{
            String tempFile = null;
            for (TrashbinFile file : fileName) {
                FileOutputStream fos = context.openFileOutput (key, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream (fos);
                oos.writeObject (fileName);
                oos.close ();
                fos.close ();
            }
        }catch (Exception e){
            Log.e("Exit" , "Fragment closed~");
        }
    }

    public static Object readCachedFile (Context context, String key) throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput (key);
        ObjectInputStream ois = new ObjectInputStream (fis);
        return ois.readObject ();
    }
}