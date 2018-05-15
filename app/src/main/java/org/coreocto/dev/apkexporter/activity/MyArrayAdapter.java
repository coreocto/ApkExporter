package org.coreocto.dev.apkexporter.activity;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.coreocto.dev.apkexporter.R;
import org.coreocto.dev.apkexporter.util.AndroidUtil;

import java.util.List;

public class MyArrayAdapter extends ArrayAdapter<ApplicationInfo> {
    private static final String TAG = "MyArrayAdapter";
    private LayoutInflater mInflater;
    private List<ApplicationInfo> mListItem;

    public MyArrayAdapter(Context context, List<ApplicationInfo> listItem) {
        super(context, 0, listItem);
        this.mInflater = LayoutInflater.from(context);
        this.mListItem = listItem;
    }

    @Override
    public int getCount() {
        return mListItem.size();
    }

    @Override
    public ApplicationInfo getItem(int position) {
        return mListItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        public TextView text;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item, null);
            holder.text = (TextView) convertView.findViewById(R.id.ItemText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();

        }
        MainActivity activity = (MainActivity) getContext();
        ApplicationInfo appInfo = mListItem.get(position);
        String displayName = null;
        try {
            displayName = AndroidUtil.getAppName(activity.getPackageManager(), appInfo) + " " + AndroidUtil.getAppVer(activity.getPackageManager(), appInfo.packageName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "package not found", e);
        }
        holder.text.setText(displayName);

        return convertView;
    }
}
