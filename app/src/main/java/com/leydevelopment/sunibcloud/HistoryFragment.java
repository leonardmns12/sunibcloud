//Copyright @Leonardmnss
//All codes belongs to all vendors and developers
package com.helloworld.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.DownloadFileRemoteOperation;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.model.RemoteFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HistoryFragment extends Fragment implements OnRemoteOperationListener , OnDatatransferProgressListener {
    private Handler mHandler;

    private OwnCloudClient mClient;

    private FilesArrayAdapter mFilesAdapter;

    private View mFrame;

    private String cachePath , backDir, path;

    private ListView moreList;

    private View v;

    private Button backBtn;

    private ArrayList<RemoteFile> files;

    @SuppressLint("RestrictedApi")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.fragment_history , container , false);
        files = new ArrayList<RemoteFile>();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setShowHideAnimationEnabled(false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        initServer();
        path = (FileUtils.PATH_SEPARATOR);
        startRefresh(path);
        try {
            checkCached();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        return v;
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

    private void checkCached() throws IOException, ClassNotFoundException, InterruptedException {
        List<RemoteFile> apkCacheList = (ArrayList<RemoteFile>)readCachedFile  (getContext(), cachePath);
        if(apkCacheList == null) {
            Log.e("Cache " , "Cache null");
        } else {
            mFilesAdapter.clear();
            Log.e("Cache " , "Cache not null");
            if (apkCacheList != null) {
                Iterator<RemoteFile> it = apkCacheList.iterator();
                while (it.hasNext()) {
                    mFilesAdapter.add(it.next());
                }
                mFilesAdapter.remove(mFilesAdapter.getItem(0));
            }
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

    public void onRemoteOperationFinish(RemoteOperation operation, RemoteOperationResult result) {
        if (!result.isSuccess()) {
            Log.e("ERROR ", result.getLogMessage(), result.getException());

        } else if (operation instanceof ReadFolderRemoteOperation) {
            try {
                if (path != FileUtils.PATH_SEPARATOR && backBtn.getVisibility() != View.VISIBLE) {
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
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        } else if (operation instanceof DownloadFileRemoteOperation) {
            onSuccessfulDownload((DownloadFileRemoteOperation) operation, result);
        }
    }

    @SuppressWarnings("deprecation")
    private void onSuccessfulDownload(DownloadFileRemoteOperation operation, RemoteOperationResult result) {
        File downFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/Nextcloud");
        File downloadedFile = downFolder.listFiles()[0];
        BitmapDrawable bDraw = new BitmapDrawable(getResources(), downloadedFile.getAbsolutePath());
//        mFrame.setBackgroundDrawable(bDraw);
    }

    public void refreshRemotePath(String path) {
        if(path == FileUtils.PATH_SEPARATOR) {
            backBtn.setVisibility(View.GONE);
        }
        if (path.endsWith("/")) {
             startRefresh(path);
        } else {
            startDownload(path);
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

    private void startDownload(String path) {
        File downFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/Nextcloud");
        downFolder.mkdir();
        String remotePath = path;
        DownloadFileRemoteOperation downloadOperation = new DownloadFileRemoteOperation(remotePath, downFolder.getAbsolutePath());
        downloadOperation.addDatatransferProgressListener(this);
        downloadOperation.execute(mClient, this, mHandler);
    }


    @Override
    public void onTransferProgress(long progressRate, long totalTransferredSoFar, long totalToTransfer, String fileAbsoluteName) {
        final long percentage = (totalToTransfer > 0 ? totalTransferredSoFar * 100 / totalToTransfer : 0);
        Log.d("TAG", "progressRate " + percentage);
    }

    public static void createCachedFile (Context context, String key, List<RemoteFile> fileName) throws IOException {
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
        Object object = ois.readObject ();
        return object;
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
                        result.add(x);
                        Log.e("File" , x.getRemotePath());
                    }
                }
              mFilesAdapter.addAll(result);
                mFilesAdapter.notifyDataSetChanged();

                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }
}
