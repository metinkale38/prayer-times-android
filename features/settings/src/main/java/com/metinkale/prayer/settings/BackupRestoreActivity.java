/*
 * Copyright (c) 2013-2019 Metin Kale
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metinkale.prayer.settings;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.metinkale.prayer.Module;
import com.metinkale.prayer.utils.PermissionUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BackupRestoreActivity extends AppCompatActivity implements OnItemClickListener {
    
    private MyAdapter mAdapter;
    private File mFolder;
    
    @Override
    public void onCreate(Bundle bdl) {
        super.onCreate(bdl);
        PermissionUtils.get(this).needStorage(this);
        setContentView(R.layout.settings_backuprestore);
        ListView list = findViewById(R.id.listView1);
        mFolder = new File(Environment.getExternalStorageState(), "backups/com.metinkale.prayer");
        mFolder.mkdirs();
        mAdapter = new MyAdapter(this);
        list.setAdapter(mAdapter);
        list.setOnItemClickListener(this);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.get(this).onRequestPermissionResult(permissions, grantResults);
        if (!PermissionUtils.get(this).pStorage) {
            finish();
        }
    }
    
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
        final File file = mAdapter.getFile(pos);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(file.getName())
                .setItems(new String[]{getString(R.string.restore), getString(R.string.delete)}, (dialogInterface, which) -> {
                    if (which == 0) {
                        boolean success = true;
                        File files = getFilesDir();
                        for (File file1 : files.listFiles()) {
                            success &= file1.delete();
                        }
                        success &= files.delete();
                        files = new File(files.getParentFile(), "databases");
                        for (File file1 : files.listFiles()) {
                            success &= file1.delete();
                        }
                        success &= files.delete();
                        files = new File(files.getParentFile(), "shared_prefs");
                        for (File file1 : files.listFiles()) {
                            success &= file1.delete();
                        }
                        success &= files.delete();

                        if (!success)
                            Toast.makeText(BackupRestoreActivity.this, R.string.error, Toast.LENGTH_LONG).show();
                        files = files.getParentFile();

                        Zip.unzip(file.getAbsolutePath(), files.getAbsolutePath() + "/");
                        System.exit(0);
                        Module.TIMES.launch(BackupRestoreActivity.this);
                    } else {
                        file.delete();
                        mAdapter.notifyDataSetChanged();
                    }
                });
        builder.show();
        
    }
    
    public void backup(View v) {
        
        if (!mFolder.exists()) {
            mFolder.mkdirs();
        }
        
        File zipFile = new File(mFolder, DateFormat.format("yyyy-MM-dd HH.mm.ss", new Date()) + ".zip");
        
        try {
            Zip zip = new Zip(zipFile.getAbsolutePath());
            
            File files = getFilesDir();
            if (files.exists() && files.isDirectory())
                for (String file : files.list()) {
                    if (new File(file).isDirectory() || file.contains(".Fabric") || file.contains("leakcanary") || file.contains("ion")) {
                        continue;
                    }
                    zip.addFile("files", files.getAbsolutePath() + "/" + file);
                }
            
            files = new File(files.getParentFile(), "databases");
            if (files.exists() && files.isDirectory())
                for (String file : files.list()) {
                    if (file.contains("evernote"))
                        continue;
                    zip.addFile("databases", files.getAbsolutePath() + "/" + file);
                }
            
            files = new File(files.getParentFile(), "shared_prefs");
            if (files.exists() && files.isDirectory())
                for (String file : files.list()) {
                    if (file.contains("evernote"))
                        continue;
                    zip.addFile("shared_prefs", files.getAbsolutePath() + "/" + file);
                }
            zip.closeZip();
        } catch (IOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
            zipFile.delete();
        }
        mAdapter.notifyDataSetChanged();
    }
    
    
    public static class Zip {
        static final int BUFFER = 2048;
        
        ZipOutputStream out;
        byte[] data;
        
        public Zip(@NonNull String name) throws FileNotFoundException {
            FileOutputStream dest = new FileOutputStream(name);
            out = new ZipOutputStream(new BufferedOutputStream(dest));
            data = new byte[BUFFER];
        }
        
        public static boolean unzip(@NonNull String zip, String to) {
            InputStream is;
            ZipInputStream zis;
            try {
                is = new FileInputStream(zip);
                zis = new ZipInputStream(new BufferedInputStream(is));
                ZipEntry ze;
                
                while ((ze = zis.getNextEntry()) != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int count;
                    
                    String filename = ze.getName();
                    new File(to + filename).getParentFile().mkdirs();
                    FileOutputStream fout = new FileOutputStream(to + filename);
                    
                    // reading and writing
                    while ((count = zis.read(buffer)) != -1) {
                        baos.write(buffer, 0, count);
                        byte[] bytes = baos.toByteArray();
                        fout.write(bytes);
                        baos.reset();
                    }
                    
                    fout.close();
                    zis.closeEntry();
                }
                
                zis.close();
            } catch (IOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                return false;
            }
            
            return true;
        }
        
        public void addFile(@Nullable String folder, @NonNull String name) throws IOException {
            FileInputStream fi = new FileInputStream(name);
            BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
            ZipEntry entry = new ZipEntry((folder == null ? "" : folder + "/") + name.substring(name.lastIndexOf("/") + 1));
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();
        }
        
        public void closeZip() throws IOException {
            out.close();
        }
    }
    
    class MyAdapter extends ArrayAdapter<String> {
        
        public MyAdapter(@NonNull Context context) {
            super(context, android.R.layout.simple_list_item_1, android.R.id.text1);
        }
        
        @Override
        public int getCount() {
            if (mFolder.listFiles() == null) {
                return 0;
            }
            return mFolder.listFiles().length;
        }
        
        @Override
        public String getItem(int pos) {
            return mFolder.listFiles()[pos].getName();
        }
        
        File getFile(int pos) {
            return mFolder.listFiles()[pos];
        }
        
    }
    
}
