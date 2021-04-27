package com.ariyo.chatapp;

import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.UI>{
    Context mContext;
    private List<UserAccount> displayList;
    private int itemLayout;

    public RVAdapter(Context mContext, List<UserAccount> displayList, int itemLayout) {
        this.mContext = mContext;
        this.displayList = displayList;
        this.itemLayout = itemLayout;
    }


    @NonNull
    @Override
    public RVAdapter.UI onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(mContext);
        View itemView=inflater.inflate(itemLayout, parent, false);
        UI ui=new UI(itemView);
        return ui;
    }

    @Override
    public void onBindViewHolder(@NonNull RVAdapter.UI holder, int position) {
        UserAccount account=displayList.get(position);
        holder.uiName.setText(account.getName());
        if (account.getImage()!=null){
            Picasso.get().load(account.getImage()).placeholder(R.drawable.account).into(holder.uiImage);
        }
        holder.itemView.setOnClickListener(v -> {
            Intent intent=new Intent(mContext, ChatDetailActivity.class);
            intent.putExtra(ConstantKeys.KEY_NAME, account.getName());
            intent.putExtra(ConstantKeys.KEY_IMAGE, account.getImage());
            intent.putExtra(ConstantKeys.KEY_UID, account.getUid());

            mContext.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    public class UI extends RecyclerView.ViewHolder {
        CircleImageView uiImage;
        TextView uiName;
        public UI(@NonNull View itemView) {
            super(itemView);
            uiImage=itemView.findViewById(R.id.display_img);
            uiName=itemView.findViewById(R.id.display_name);
        }
    }
}
