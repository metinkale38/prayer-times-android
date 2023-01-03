/*
 * Copyright (c) 2013-2023 Metin Kale
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

package com.metinkale.prayer.utils;

import android.app.Activity;
import android.app.Dialog;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.Arrays;

public class FileChooser {
    private static final String PARENT_DIR = "..";

    @NonNull
    private final Activity activity;
    private final ListView list;
    private final Dialog dialog;
    private File currentPath;

    // filter on file extension
    @Nullable
    private String extension;

    public void setExtension(@Nullable String extension) {
        this.extension = extension == null ? null :
                extension.toLowerCase();
    }

    // file selection event handling
    public interface FileSelectedListener {
        void fileSelected(File file);
    }

    @NonNull
    public FileChooser setFileListener(FileSelectedListener fileListener) {
        this.fileListener = fileListener;
        return this;
    }

    private FileSelectedListener fileListener;

    public FileChooser(@NonNull Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity);
        list = new ListView(activity);
        list.setOnItemClickListener((adapterView, view, which, l) -> {
            String fileChosen = (String) list.getItemAtPosition(which);
            File chosenFile = getChosenFile(fileChosen);
            if (chosenFile.isDirectory()) {
                refresh(chosenFile);
            } else {
                if (fileListener != null) {
                    fileListener.fileSelected(chosenFile);
                }
                dialog.dismiss();
            }
        });
        dialog.setContentView(list);
        dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        refresh(Environment.getExternalStorageDirectory());
    }

    public void showDialog() {
        dialog.show();
    }


    /**
     * Sort, filter and display the files for the given path.
     */
    private void refresh(@NonNull File path) {
        currentPath = path;
        if (path.exists()) {
            File[] dirs = path.listFiles(file -> file.isDirectory() && file.canRead());
            File[] files = path.listFiles(file -> {
                if (file.isDirectory()) {
                    return false;
                } else {
                    if (!file.canRead()) {
                        return false;
                    } else if (extension == null) {
                        return true;
                    } else {
                        return file.getName().toLowerCase().endsWith(extension);
                    }
                }
            });

            if (files == null) files = new File[0];
            if (dirs == null) dirs = new File[0];

            // convert to an array
            int i = 0;
            String[] fileList;
            if (path.getParentFile() == null) {
                fileList = new String[dirs.length + files.length];
            } else {
                fileList = new String[dirs.length + files.length + 1];
                fileList[i++] = PARENT_DIR;
            }
            Arrays.sort(dirs);
            Arrays.sort(files);
            for (File dir : dirs) {
                fileList[i++] = dir.getName();
            }
            for (File file : files) {
                fileList[i++] = file.getName();
            }

            // refresh the user interface
            dialog.setTitle(currentPath.getPath());
            list.setAdapter(new ArrayAdapter<String>(activity,
                    android.R.layout.simple_list_item_1, fileList) {
                @NonNull
                @Override
                public View getView(int pos, View view, @NonNull ViewGroup parent) {
                    view = super.getView(pos, view, parent);
                    ((TextView) view).setSingleLine(true);
                    return view;
                }
            });
        }
    }


    /**
     * Convert a relative filename into an actual File object.
     */
    private File getChosenFile(@NonNull String fileChosen) {
        if (fileChosen.equals(PARENT_DIR)) {
            return currentPath.getParentFile();
        } else {
            return new File(currentPath, fileChosen);
        }
    }
}