package com.example.orderManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import org.w3c.dom.Document;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ShoppingCartItemAdapter extends FirestoreRecyclerAdapter <ShoppingCartItem, ShoppingCartItemAdapter.ShoppingCartItemHolder> {
    private OnItemClickListener listener;
    public ShoppingCartItemAdapter(@NonNull FirestoreRecyclerOptions<ShoppingCartItem> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ShoppingCartItemHolder holder, int position, @NonNull ShoppingCartItem model) {
        holder.textViewName.setText(model.getProductName());
        holder.textViewQuantity.setText(model.getQuantity()+"");
    }

    @NonNull
    @Override
    public ShoppingCartItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.shoping_cart_item, parent, false);
        return new ShoppingCartItemHolder(v);
    }

    public void deleteItem(int position){
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    class ShoppingCartItemHolder extends RecyclerView.ViewHolder{
        TextView textViewName;
        TextView textViewQuantity;

        public ShoppingCartItemHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.list_product_name);
            textViewQuantity = itemView.findViewById(R.id.list_qty);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position!= RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
}
