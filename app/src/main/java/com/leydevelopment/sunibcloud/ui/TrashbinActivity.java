package com.leydevelopment.sunibcloud.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.leydevelopment.sunibcloud.MainActivity;
import com.leydevelopment.sunibcloud.R;
import com.leydevelopment.sunibcloud.adapter.TrashbinAdapter;
import com.leydevelopment.sunibcloud.utils.TrashbinDialog;
import com.owncloud.android.lib.common.OwnCloudBasicCredentials;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.lib.resources.trashbin.EmptyTrashbinRemoteOperation;
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

public class TrashbinActivity extends AppCompatActivity implements OnRemoteOperationListener , TrashbinAdapter.OnItemListener, TrashbinDialog.TrashbinDialogListener {
    private Handler mHandler;
    private OwnCloudClient mClient;
    private OwnCloudBasicCredentials cred;
    private String username;

    private RecyclerView trashbinList;

    private  List<TrashbinFile> listFile = new ArrayList<TrashbinFile>();

    String TRASHBIN_CACHE_KEY = "trashbin_";
    String path , keypath;
    Context mContext;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this , MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trashbin);
        mContext = this;
        trashbinList = (RecyclerView) findViewById(R.id.trashbinList);
        List<TrashbinFile> listFile = null;
        TrashbinAdapter trashbinAdapter = new TrashbinAdapter(listFile,this , this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.trashbin_menu , menu);
        MenuItem menuItem = menu.findItem(R.id.searchMenu);
        MenuItem deleteAll = menu.findItem(R.id.deleteMenu);
        ImageView deleteall = (ImageView) deleteAll.getActionView();
        deleteall.setImageResource(R.drawable.ic_baseline_delete_24);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<TrashbinFile> result = listFile;
                List<TrashbinFile> newResult = new ArrayList<TrashbinFile>();
                for (TrashbinFile x:result) {
                    Log.e("res" , x.getFileName());
                    if(x.getFileName().toLowerCase().contains(newText.toLowerCase())) {
                        if (x.getRemotePath().equals("/")){
                            continue;
                        }else{
                            newResult.add(x);
                        }
                    }
                }
                setAdapter(newResult);
                return false;
            }
        });
        deleteall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emptyTrashbin();
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void emptyTrashbin() {
        EmptyTrashbinRemoteOperation emptyTrashbin = new EmptyTrashbinRemoteOperation("leonard");
        emptyTrashbin.execute(mClient , this , mHandler);
    }

    private void setAdapter(List<TrashbinFile> mewRes) {
        TrashbinAdapter trashbinAdapter = new TrashbinAdapter(mewRes,mContext , this);
        trashbinList.setAdapter(trashbinAdapter);
        trashbinList.setLayoutManager(new LinearLayoutManager(mContext));
    }

    private void getTrashbin() {
        keypath = TRASHBIN_CACHE_KEY + path;
        if(keypath.contains("/")) {
            keypath = keypath.replaceAll("/" , "%10");
        }
        ReadTrashbinFolderRemoteOperation readTrashbin = new ReadTrashbinFolderRemoteOperation(path , username);
        readTrashbin.execute(mClient , this , mHandler);
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation caller, RemoteOperationResult result) {
        if (caller instanceof  ReadTrashbinFolderRemoteOperation) {
            OnSuccessfulReadTrashbin(caller , result);
        } else if (caller instanceof  EmptyTrashbinRemoteOperation) {
            OnSuccesfulEmptyTrashbin(caller, result);
        }
    }

    private void OnSuccesfulEmptyTrashbin(RemoteOperation caller, RemoteOperationResult result) {
        Toast.makeText(this ,"Trashbin removed succesfuly!" , Toast.LENGTH_SHORT).show();
        getTrashbin();
    }

    private void OnSuccessfulReadTrashbin(RemoteOperation caller, RemoteOperationResult result) {
        listFile.clear();
        for (Object obj: result.getData()) {
            listFile.add((TrashbinFile) obj);
        }
        if (listFile != null){
            TrashbinAdapter trashbinAdapter = new TrashbinAdapter(listFile,this , this);
            trashbinList.setAdapter(trashbinAdapter);
            trashbinList.setLayoutManager(new LinearLayoutManager(this));
            createCachedFile(this , keypath ,listFile);
        }
        Log.e("interface " , "test");
    }

    private void checkCached() throws IOException, ClassNotFoundException {
        keypath = TRASHBIN_CACHE_KEY + path;
        Log.e("kp1" , keypath);
        if(keypath.contains("/")) {
            keypath = keypath.replaceAll("/" , "%10");
        }
        List<TrashbinFile> apkCacheList = (ArrayList<TrashbinFile>)readCachedFile  (Objects.requireNonNull(this), keypath);
        if(apkCacheList == null) {
            Log.e("Cache " , "Cache null");
        } else {
            TrashbinAdapter trashbinAdapter = new TrashbinAdapter(apkCacheList,this , this);
            trashbinList.setAdapter(trashbinAdapter);
            trashbinList.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    public static void createCachedFile (Context context, String key, List<TrashbinFile> fileName) {
        try{
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

    @Override
    public void OnItemClick(int position) {
        listFile.get(position);
        if (listFile.get(position).getMimeType().equals("DIR")) {
            path = FileUtils.PATH_SEPARATOR + getFileName(listFile.get(position).getRemotePath());
            getTrashbin();
        } else {
            TrashbinDialog bottomDialog = new TrashbinDialog(this ,listFile.get(position).getRemotePath(),listFile.get(position).getFileName());
            bottomDialog.show(getSupportFragmentManager(), "trashbindialog");
        }
    }

    private String getFileName(String paths) {
        String Filename = "";
        int idx = 0;

        for (int i = paths.length()-2; i >= 0; i--) {
            if (paths.charAt(i) == '/') {
                idx = i+1;
                break;
            }
        }
        Filename = paths.substring(idx , paths.length());
        return Filename;
    }

    @Override
    public void onActionTaken(String rpath) {
        Log.e("rpath " , rpath);
        path = FileUtils.PATH_SEPARATOR;
        getTrashbin();
    }
}