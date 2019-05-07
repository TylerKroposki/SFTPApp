package com.kroposki.sftpapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.kroposki.sftpapp.R;

import java.util.Vector;

/**
 * @author Tyler Kroposki
 * RecyclerView adapter to add the File information for each file in a User's SFTP directory.
 */
public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder>{

    private Vector<LsEntry> fileEntries;
    private LayoutInflater inflator;

    FileAdapter(Context context, Vector<LsEntry> data) {
        this.inflator = LayoutInflater.from(context);
        this.fileEntries = data;
    }

    //Inflate row from fileitem xml
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflator.inflate(R.layout.fileitem, parent, false);
        return new ViewHolder(view);
    }

    //Put the text into the TextView
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        String fileName = fileEntries.get(position).getFilename();
        String fileSize = String.valueOf(fileEntries.get(position).getAttrs().getSize());
        String filePerms = String.valueOf(fileEntries.get(position).getAttrs().getPermissionsString());
        String fileTime = String.valueOf(fileEntries.get(position).getAttrs().getAtimeString());

        holder.fileName.setText(fileName);
        holder.fileSize.setText("Size: " + fileSize + " Bytes");
        holder.fileTime.setText("Last Accessed: " + fileTime);
        holder.filePerms.setText("Permissions: " +filePerms);

    }

    @Override
    public int getItemCount() {
        return fileEntries.size();
    }

    //Stores the views
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;
        TextView fileSize;
        TextView fileTime;
        TextView filePerms;

        ViewHolder(View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.recycleFileName);
            fileSize = itemView.findViewById(R.id.recycleFileSize);
            fileTime = itemView.findViewById(R.id.recycleFileLastAccessed);
            filePerms = itemView.findViewById(R.id.recycleFilePermissions);
        }

    }
}
