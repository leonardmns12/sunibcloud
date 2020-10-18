package com.helloworld.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.io.FileOutputStream;

public class BottomSheet extends BottomSheetDialogFragment {
    Uri fileUri;
    private String type;
    private BottomSheetListener mListener;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.row_add_item , container , false);
        ImageView imageBtn = (ImageView) v.findViewById(R.id.image);
        ImageView docsBtn = (ImageView) v.findViewById(R.id.docs);
        ImageView videoBtn = (ImageView) v.findViewById(R.id.video);
        ImageView cameraBtn = (ImageView) v.findViewById(R.id.camera);

        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "image";
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    selectFile("image");
                } else {
                    ActivityCompat.requestPermissions(getActivity() , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},9);
                }
            }
        });
        docsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "application";
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    selectFile("application");
                } else {
                    ActivityCompat.requestPermissions(getActivity() , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},9);
                }
            }
        });
        videoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "video";
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    selectFile("video");
                } else {
                    ActivityCompat.requestPermissions(getActivity() , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},9);
                }
            }
        });
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "camera";
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ){
                    selectFile("camera");
                } else if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(getActivity() , new String[]{Manifest.permission.CAMERA},9);
                } else if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(getActivity() , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},9);
                }
            }
        });
        return v;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 9 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectFile(type);
        } else {
            Toast.makeText(getActivity() , "please granted permission.." , Toast.LENGTH_SHORT).show();
        }
    }

    private void selectFile(String type) {
        if (type.equals("camera")) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePictureIntent, 1);
        } else {
            Intent intent = new Intent();
            if (type.equals("application")) {
                intent.setType("application/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
            }
            if (type.equals("image")) {
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
            }
            if (type.equals("video")) {
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
            }
            startActivityForResult(intent, 86);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 86 && data != null) {
            fileUri = data.getData();
            FileUtils path = new FileUtils();
            String url = path.getPath(fileUri , getActivity());
            Cursor returnCursor = getActivity().getContentResolver().query(fileUri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            File file = new File(url);
            mListener.onFilesTaken(returnCursor.getString(nameIndex) , (int)returnCursor.getLong(sizeIndex), file);
            dismiss();
        }
        else if (requestCode == 1 && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            try {
                String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/capture.png";
                Log.e("tag", imageFilePath);
                FileOutputStream out = new FileOutputStream(imageFilePath);
                photo.compress(Bitmap.CompressFormat.JPEG, 90, out);
                Toast.makeText(getActivity() , "Photo saved to /Download/capture.png" , Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(getActivity() , "Please select a file.." , Toast.LENGTH_SHORT).show();
        }
    }

    public interface BottomSheetListener {
        void onFilesTaken(String name , int size , File fileUri);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (BottomSheetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement BottomSheetListener");
        }
    }
    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getActivity().getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}