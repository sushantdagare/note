/* Copyright 2014 Braden Farmer
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

package com.farmerbb.notepad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class ImportActivity extends Activity implements CompoundButton.OnCheckedChangeListener {

    ListView listView;
    Button button;
    ArrayList<String> listOfFiles;
    ArrayList<String> notesToImport;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        // Set TextView contents
        TextView textView = (TextView) findViewById(R.id.importTextView);
        textView.setText(getResources().getString(R.string.import_notes_instructions) + getExternalFilesDir(null));

        // Set OnClickListener for the button
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(notesToImport != null && notesToImport.size() > 0) {
                    try {
                        for(Object file : notesToImport.toArray()) {
                            File fileToImport = new File(getExternalFilesDir(null), file.toString());
                            File importedFile = new File(getFilesDir(), Long.toString(fileToImport.lastModified()));
                            long suffix = 0;

                            // Handle cases where a note may have a duplicate title
                            while(importedFile.exists()) {
                                suffix++;
                                importedFile = new File(getFilesDir(), Long.toString(fileToImport.lastModified() + suffix));
                            }

                            InputStream is = new FileInputStream(fileToImport);
                            OutputStream os = new FileOutputStream(importedFile);
                            byte[] data = new byte[is.available()];

                            is.read(data);
                            os.write(data);
                            is.close();
                            os.close();
                        }

                        // Send broadcast to NoteListFragment to refresh list of notes
                        Intent listNotesIntent = new Intent();
                        listNotesIntent.setAction("com.farmerbb.notepad.LIST_NOTES");
                        sendBroadcast(listNotesIntent);

                        // Show toast notification
                        showToast(R.string.notes_imported_successfully);
                    } catch (IOException e) {
                        showToast(R.string.error_importing_notes);
                    }
                }

                finish();
            }
        });

        try {
            // Get array of file names
            String[] allFiles = getExternalFilesDir(null).list();
            int numOfFiles = 0;

            // Sort array
            Arrays.sort(allFiles);

            listOfFiles = new ArrayList<String>(allFiles.length);

            for(String note : allFiles) {
                if(note.endsWith(".txt")) {
                    listOfFiles.add(note);
                    numOfFiles++;
                }
            }

            // Declare ListView and ArrayList
            listView = (ListView) findViewById(R.id.listView2);
            notesToImport = new ArrayList<String>(numOfFiles);

            // Create the custom adapter to bind the array to the ListView
            final ImportListAdapter adapter = new ImportListAdapter(this, listOfFiles);

            // Display the ListView
            listView.setAdapter(adapter);
        } catch (NullPointerException e) {
            // Throws a NullPointerException if no external storage is present
            finish();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked)
            notesToImport.add(listOfFiles.get(listView.getPositionForView(buttonView)));
        else
            notesToImport.remove(listOfFiles.get(listView.getPositionForView(buttonView)));

        if(notesToImport != null && notesToImport.size() > 0) {
            if(button.getText().equals(getResources().getString(R.string.action_close)))
                button.setText(getResources().getString(R.string.action_import));
        } else
            button.setText(getResources().getString(R.string.action_close));
    }

    // Method used to generate toast notifications
    private void showToast(int message) {
        Toast toast = Toast.makeText(this, getResources().getString(message), Toast.LENGTH_SHORT);
        toast.show();
    }
}
