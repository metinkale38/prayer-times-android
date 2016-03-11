package com.metinkale.prayerapp.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.BaseActivity;
import com.metinkale.prayerapp.PermissionUtils;
import com.metinkale.prayerapp.vakit.Main;

import java.io.*;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BackupRestoreActivity extends BaseActivity implements OnItemClickListener {

    private MyAdapter mAdapter;
    private File mFolder;

    @Override
    public void onCreate(Bundle bdl) {
        super.onCreate(bdl);
        PermissionUtils.get(this).needStorage(this);
        this.setContentView(R.layout.settings_backuprestore);
        ListView list = (ListView) findViewById(R.id.listView1);
        mFolder = new File(Environment.getExternalStorageDirectory(), "backups/com.metinkale.prayer");
        mFolder.mkdirs();
        mAdapter = new MyAdapter(this);
        list.setAdapter(mAdapter);
        list.setOnItemClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!PermissionUtils.get(this).pStorage) finish();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
        final File file = mAdapter.getFile(pos);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(file.getName()).setItems(new String[]{getString(R.string.restore), getString(R.string.delete)}, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    File files = getFilesDir();
                    for (File file : files.listFiles()) {
                        file.delete();
                    }
                    files.delete();
                    files = new File(files.getParentFile(), "databases");
                    for (File file : files.listFiles()) {
                        file.delete();
                    }
                    files.delete();
                    files = new File(files.getParentFile(), "shared_prefs");
                    for (File file : files.listFiles()) {
                        file.delete();
                    }
                    files.delete();

                    files = files.getParentFile();

                    Zip.unzip(file.getAbsolutePath(), files.getAbsolutePath() + "/");
                    System.exit(0);
                    Intent i = new Intent(BackupRestoreActivity.this, Main.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                } else {
                    file.delete();
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
        builder.show();

    }

    public void backup(View v) {

        if (!mFolder.exists()) mFolder.mkdirs();

        File zipFile = new File(mFolder, DateFormat.format("yyyy-MM-dd HH.mm.ss", new Date()) + ".zip");

        try {
            Zip zip = new Zip(zipFile.getAbsolutePath());
            File files = this.getFilesDir();
            for (String file : files.list()) {
                if (file.contains(".Fabric")) continue;
                zip.addFile("files", files.getAbsolutePath() + "/" + file);
            }
            files = new File(files.getParentFile(), "databases");
            for (String file : files.list()) {
                zip.addFile("databases", files.getAbsolutePath() + "/" + file);
            }
            files = new File(files.getParentFile(), "shared_prefs");
            for (String file : files.list()) {
                zip.addFile("shared_prefs", files.getAbsolutePath() + "/" + file);
            }
            zip.closeZip();
        } catch (IOException e) {
            Crashlytics.logException(e);
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
            zipFile.delete();
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean setNavBar() {
        return false;
    }

    public static class Zip {
        static final int BUFFER = 2048;

        ZipOutputStream out;
        byte data[];

        public Zip(String name) throws FileNotFoundException {
            FileOutputStream dest = new FileOutputStream(name);
            out = new ZipOutputStream(new BufferedOutputStream(dest));
            data = new byte[BUFFER];
        }

        public static boolean unzip(String zip, String to) {
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
                Crashlytics.logException(e);
                return false;
            }

            return true;
        }

        public void addFile(String folder, String name) throws IOException {
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

        public MyAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1, android.R.id.text1);
        }

        @Override
        public int getCount() {
            if (mFolder.listFiles() == null) return 0;
            return mFolder.listFiles().length;
        }

        @Override
        public String getItem(int pos) {
            return mFolder.listFiles()[pos].getName();
        }

        public File getFile(int pos) {
            return mFolder.listFiles()[pos];
        }

    }

}
