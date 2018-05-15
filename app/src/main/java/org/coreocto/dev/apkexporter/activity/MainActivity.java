package org.coreocto.dev.apkexporter.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.*;

import org.apache.commons.io.FilenameUtils;
import org.coreocto.dev.apkexporter.R;
import org.coreocto.dev.apkexporter.util.AndroidUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";
    public static String PACKAGE_NAME = null;
    private List<ApplicationInfo> mAppList = null;
    private ArrayAdapter mArrayAdapter = null;
    private EditText etKeyword = null;
    private Button bSearch = null;
    private ProgressDialog progressDialog = null;
    private PackageManager mPackageManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PACKAGE_NAME = getApplicationContext().getPackageName();

        this.mAppList = new ArrayList<ApplicationInfo>();

        this.mArrayAdapter = new MyArrayAdapter(this, mAppList);

        this.bSearch = (Button) findViewById(R.id.bSearch);
        this.etKeyword = (EditText) findViewById(R.id.etKeyword);
        ListView leAppList = (ListView) findViewById(R.id.lvAppList);

        leAppList.setAdapter(mArrayAdapter);

        bSearch.setText(R.string.bSearch_text);
        etKeyword.setText("");

        mPackageManager = getPackageManager();

        bSearch.setOnClickListener(this);

        bSearch.performClick();

        leAppList.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View view) {

        //get a list of installed apps.
        List<ApplicationInfo> packages = mPackageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        mAppList.clear();

        for (ApplicationInfo packageInfo : packages) {
            if (!AndroidUtil.isSystemPackage(packageInfo) && !packageInfo.packageName.equals(PACKAGE_NAME)) {
                if (AndroidUtil.getAppName(mPackageManager, packageInfo).contains(etKeyword.getText())) {
                    mAppList.add(packageInfo);
                }

            }
            mArrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        Object tmp = adapterView.getItemAtPosition(i);
        if (tmp != null && tmp instanceof ApplicationInfo) {
            ApplicationInfo packageInfo = (ApplicationInfo) tmp;
            File srcApk = new File(packageInfo.publicSourceDir);
            File appDir = new File(Environment.getExternalStorageDirectory(), PACKAGE_NAME);
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            File newApk = new File(appDir, FilenameUtils.getBaseName(packageInfo.publicSourceDir) + ".apk");
            if (!newApk.exists()) {
                try {
                    newApk.createNewFile();
                } catch (IOException e) {
                    Log.e(TAG, "failed to create new file", e);
                }
            }
            progressDialog = ProgressDialog.show(this, null, "Copying " + packageInfo.packageName + " to external storage.", true);
            new CopyFileTask().execute(srcApk, newApk);
        }
    }

    private class CopyFileTask extends AsyncTask<File, Integer, Boolean> {

        private File srcFile = null;
        private File targetFile = null;

        protected Boolean doInBackground(File... params) {

            boolean ok = false;

            if (params != null && params.length > 0) {

                srcFile = params[0];
                targetFile = params[1];

                long copiedLength = 0;
                long totalLength = srcFile.length();

                InputStream in = null;
                OutputStream out = null;

                try {

                    in = new FileInputStream(srcFile);
                    out = new FileOutputStream(targetFile);

                    byte[] buf = new byte[1024];
                    int len = -1;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);

                        copiedLength += len;

                        publishProgress((int) ((copiedLength / (float) totalLength) * 100));
                    }

                    ok = true;

                } catch (Exception e) {
                    Log.e(TAG, "file not found", e);
                    ok = false;
                }

                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                }
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                }

            }

            return ok;
        }

        protected void onProgressUpdate(Integer... progress) {
            progressDialog.setProgress(progress[0]);
        }

        protected void onPostExecute(Boolean result) {

            String message = null;

            if (result) {
                message = getResources().getString(R.string.export_success_msg);
            } else {
                message = getResources().getString(R.string.export_failed_msg);
            }

            progressDialog.dismiss();

            new AlertDialog.Builder(MainActivity.this)
                    .setMessage(message)
                    .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setPositiveButton("Share", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intentShareFile = new Intent(Intent.ACTION_SEND);
                            intentShareFile.setType("application/vnd.android.package-archive");
                            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(targetFile));
                            startActivity(Intent.createChooser(intentShareFile, getString(R.string.choose_an_action)));
                        }
                    }).show();
        }
    }
}
