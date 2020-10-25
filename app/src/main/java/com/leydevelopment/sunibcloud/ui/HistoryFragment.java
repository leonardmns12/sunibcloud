//Copyright @Leonardmnss
//All codes belongs to all vendors and developers
package com.leydevelopment.sunibcloud.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.leydevelopment.sunibcloud.utils.FileBottomDialog;
import com.leydevelopment.sunibcloud.R;
import com.leydevelopment.sunibcloud.adapter.FilesArrayAdapter;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.CopyFileRemoteOperation;
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.lib.resources.files.MoveFileRemoteOperation;
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.model.RemoteFile;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class HistoryFragment extends Fragment implements OnRemoteOperationListener {
    private static OwnCloudClient mClient;
    private static Handler mHandler;


    private FilesArrayAdapter mFilesAdapter;

    private View mFrame;

    private String cachePath , backDir, path;

    private ListView moreList;

    private View v;

    private Button backBtn;

    private ArrayList<RemoteFile> files;

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    @SuppressLint("StaticFieldLeak")
    private static Button actionBtn;

    private static String actionPath = "";

    private static String actionName = "folder";

    private EditText folderName;


    @SuppressLint({"RestrictedApi", "ResourceAsColor"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.fragment_history , container , false);
        actionBtn = v.findViewById(R.id.actionBtn);
        folderName = v.findViewById(R.id.folderName);
        mContext = getContext();
        files = new ArrayList<RemoteFile>();
        Objects.requireNonNull(((AppCompatActivity) (Objects.requireNonNull(getActivity()))).getSupportActionBar()).setShowHideAnimationEnabled(false);
        Objects.requireNonNull(((AppCompatActivity) (getActivity())).getSupportActionBar()).show();
        initServer();
        path = (FileUtils.PATH_SEPARATOR);
        startRefresh(path);
        try {
            checkCached();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        moreList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView nextPath = (TextView) view.findViewById(R.id.viewId);
                String onHoldPath = nextPath.getText().toString();
                Bundle bundle = new Bundle();
                bundle.putString("actionpath", onHoldPath );
                if (onHoldPath.endsWith("/")) {
                    bundle.putString("isfolder", "true" );
                } else {
                    bundle.putString("isfolder", "false" );
                }
                FileBottomDialog bottomDialog = new FileBottomDialog();
                bottomDialog.setArguments(bundle);
                bottomDialog.show(getFragmentManager() , "string");
                return true;
            }
        });
        moreList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView nextPath = (TextView) view.findViewById(R.id.viewId);
                backDir = path;
                path = nextPath.getText().toString();
                refreshRemotePath(path);
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backDir = getBackDir(path);
                path = backDir;
               refreshRemotePath(backDir);
            }
        });
        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionName.equals("Copy")) {
                    startCopy();
                }
                if(actionName.equals("Move")) {
                    startMove();
                }
                if(actionName.equals("folder")){
                  if (folderName.getText().toString().isEmpty()) {
                      folderName.setError("Name cannot empty!");
                  } else if (folderName.getText().toString().contains("/")) {
                      folderName.setError("Name cannot contains symbol!");
                  } else{
                      createFolder(folderName.getText().toString());
                  }
                }
            }
        });
        return v;
    }

    private void createFolder(String foldername) {
        String folderPath = path + foldername;
        CreateFolderRemoteOperation createFolder = new CreateFolderRemoteOperation(folderPath,true);
        createFolder.execute(mClient, this ,mHandler);
    }

    private void startMove() {
        String pastePath = path + getFileName(actionPath);;
        MoveFileRemoteOperation moveFile = new MoveFileRemoteOperation(actionPath , pastePath , true);
        moveFile.execute(mClient , this , mHandler);
    }

    private void startCopy() {
        String pastePath = path + getFileName(actionPath);;
        CopyFileRemoteOperation copyFile = new CopyFileRemoteOperation(actionPath , pastePath , true);
        copyFile.execute(mClient , this , mHandler);
    }

    private void initServer() {
        backBtn = v.findViewById(R.id.backDir);
        backBtn.setVisibility(View.GONE);
        mFilesAdapter = new FilesArrayAdapter(getActivity(), R.layout.file_in_list);
        moreList = (ListView) v.findViewById(R.id.list_view);
        moreList.setAdapter(mFilesAdapter);
        mHandler = new Handler();
        Uri serverUri = Uri.parse("https://indofolks.com");
        mClient = OwnCloudClientFactory.createOwnCloudClient(serverUri, getActivity(), true);
        mClient.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials("leonard", "gurame442"));
        AssetManager assets = getActivity().getAssets();
    }

    private void checkCached() throws IOException, ClassNotFoundException {
        List<RemoteFile> apkCacheList = (ArrayList<RemoteFile>)readCachedFile  (Objects.requireNonNull(getContext()), cachePath);
        if(apkCacheList == null) {
            Log.e("Cache " , "Cache null");
        } else {
            mFilesAdapter.clear();
            Log.e("Cache " , "Cache not null");
            Iterator<RemoteFile> it = apkCacheList.iterator();
            while (it.hasNext()) {
                mFilesAdapter.add(it.next());
            }
            mFilesAdapter.remove(mFilesAdapter.getItem(0));
            mFilesAdapter.notifyDataSetChanged();
        }
    }

    private void startRefresh(String path) {
            if (path.contains("/")) {
                cachePath = path.replaceAll("/" , "%10");
            } else {
                cachePath = path;
            }
        ReadFolderRemoteOperation refreshOperation = new ReadFolderRemoteOperation(path);
        refreshOperation.execute(mClient, this, mHandler);
    }

    @SuppressLint("SetTextI18n")
    public void onRemoteOperationFinish(RemoteOperation operation, RemoteOperationResult result) {
        if (!result.isSuccess()) {
            Log.e("ERROR ", result.getLogMessage(), result.getException());
            actionName = "folder";
        } else if (operation instanceof ReadFolderRemoteOperation) {
            try {
                if (!path.equals(FileUtils.PATH_SEPARATOR) && backBtn.getVisibility() != View.VISIBLE) {
                    backBtn.setVisibility(View.VISIBLE);
                }
                    onSuccessfulRefresh((ReadFolderRemoteOperation) operation, result);
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    checkCached();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        } else if(operation instanceof CopyFileRemoteOperation) {
            actionName = "folder";
            actionBtn.setText("Create Folder");
            Toast.makeText(getActivity() , "File copied successfully!",Toast.LENGTH_SHORT).show();
            startRefresh(path);
        } else if (operation instanceof MoveFileRemoteOperation){
            actionName = "folder";
            actionBtn.setText("Create Folder");
            Toast.makeText(getActivity() , "File moved successfully!",Toast.LENGTH_SHORT).show();
            startRefresh(path);
        } else if (operation instanceof CreateFolderRemoteOperation) {
            folderName.setText("");
            Toast.makeText(getActivity() , "Folder created!" , Toast.LENGTH_SHORT).show();
            startRefresh(path);
        }
    }

    public void refreshRemotePath(String path) {
        if(path.equals(FileUtils.PATH_SEPARATOR)) {
            backBtn.setVisibility(View.GONE);
        }
        if (path.endsWith("/")) {
             startRefresh(path);
        } else {
//            startDownload(path);
            Bundle bundle = new Bundle();
            bundle.putString("actionpath", path );
            bundle.putString("isfolder", "false" );
            FileBottomDialog bottomDialog = new FileBottomDialog();
            bottomDialog.setArguments(bundle);
            assert getFragmentManager() != null;
            bottomDialog.show(getFragmentManager(), "string");

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void onSuccessfulRefresh(ReadFolderRemoteOperation operation, RemoteOperationResult result) throws IOException {
            mFilesAdapter.clear();
            files = new ArrayList<RemoteFile>();
            for(Object obj: result.getData()) {
                files.add((RemoteFile) obj);
            }
            if (files != null) {
                Iterator<RemoteFile> it = files.iterator();
                while (it.hasNext()) {
//                Log.e("Data => " , it.next().getRemotePath());
                    mFilesAdapter.add(it.next());
                }
                mFilesAdapter.remove(mFilesAdapter.getItem(0));
            }
            mFilesAdapter.notifyDataSetChanged();
            createCachedFile (getContext(),cachePath,files);
    }

    public static void createCachedFile (Context context, String key, List<RemoteFile> fileName) {
            try{
                String tempFile = null;
                for (RemoteFile file : fileName) {
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

    private String getBackDir(String paths) {
        String backdir = paths;
        int idx = 0;
        for(int i = paths.length()-1; i >= 1; i--) {
            if(i == 1) {
                backdir = "/";
            }
            if(paths.charAt(i) == '/' && i != paths.length()-1) {
                idx = i+1;
                backdir = paths.substring(0 , idx);
                break;
            }
        }

        return backdir;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        Log.e("Option Menu" , "Menu called");
        inflater.inflate(R.menu.my_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.searchMenu);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mFilesAdapter.clear();
                List<RemoteFile> result = new ArrayList<RemoteFile>();
                for (RemoteFile x:files) {
                    if(x.getRemotePath().toLowerCase().contains(newText.toLowerCase())) {
                        if (x.getRemotePath().equals("/")){
                            continue;
                        }else{
                            result.add(x);
                        }
                    }
                }
                mFilesAdapter.addAll(result);
                mFilesAdapter.notifyDataSetChanged();
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressLint("SetTextI18n")
    public static void receiverMethod(String action, String rPath) {
//        if( action.equals("Download")) {
//            startDownload(rPath);
        if(action.equals("Remove")) {
            Toast.makeText(mContext , "File successfully deleted!" , Toast.LENGTH_SHORT).show();
        } else if (action.equals("Copy") || action.equals("Move")) {
            if(action.equals("Copy")){
                actionBtn.setText("Paste here");
            } else {
                actionBtn.setText("Move here");
            }
            actionName = action;
            actionPath = rPath;
            actionBtn.setVisibility(View.VISIBLE);
        } else if (action.equals("Rename")) {
            Toast.makeText(mContext , "Rename success!", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileName(String paths) {
        String Filename = "";
        int idx = 0;

        for (int i = paths.length()-1; i >= 0; i--) {
            if (paths.charAt(i) == '/') {
                idx = i+1;
                break;
            }
        }
        Filename = paths.substring(idx , paths.length());
        return Filename;
    }
}
