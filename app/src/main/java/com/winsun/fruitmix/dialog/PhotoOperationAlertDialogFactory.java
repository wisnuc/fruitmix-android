package com.winsun.fruitmix.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.command.AbstractCommand;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/5/18.
 */

public class PhotoOperationAlertDialogFactory implements DialogFactory {

    private List<String> commandNames;
    private List<AbstractCommand> commands;

    private Dialog dialog;

    public PhotoOperationAlertDialogFactory(List<String> commandNames, List<AbstractCommand> commands) {
        this.commandNames = Collections.unmodifiableList(commandNames);
        this.commands = Collections.unmodifiableList(commands);
    }

    @Override
    public Dialog createDialog(Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setView(createView(context));

        dialog = builder.create();

        return dialog;
    }

    private View createView(Context context) {

        View view = View.inflate(context, R.layout.list_dialog_layout, null);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);

        PhotoOperationRecyclerViewAdapter adapter = new PhotoOperationRecyclerViewAdapter(context);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        return view;
    }

    private class PhotoOperationRecyclerViewAdapter extends RecyclerView.Adapter<PhotoOperationRecyclerViewHolder> {

        private Context context;

        public PhotoOperationRecyclerViewAdapter(Context context) {
            this.context = context;
        }

        @Override
        public PhotoOperationRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(context).inflate(R.layout.photo_operation_list_item, parent, false);

            return new PhotoOperationRecyclerViewHolder(view);
        }


        @Override
        public void onBindViewHolder(PhotoOperationRecyclerViewHolder holder, int position) {
            holder.refreshView(position);
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return commandNames.size();
        }
    }

    class PhotoOperationRecyclerViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_textview)
        TextView itemTextView;
        @BindView(R.id.item_layout)
        ViewGroup itemLayout;

        PhotoOperationRecyclerViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        public void refreshView(int position) {

            String commandName = commandNames.get(position);

            final AbstractCommand command = commands.get(position);

            itemTextView.setText(commandName);

            itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    command.execute();

                    dialog.dismiss();
                }
            });
        }

    }


}
