package com.example.android.booklistingapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class ItemsListAdapter extends RecyclerView.Adapter<ItemsListAdapter.ItemsViewHolder> {
    public static final String LOG_TAG = ItemsListAdapter.class.getName();

    private ArrayList<ItemsList> mItemList;
    private Context mContext;

    //Constructor of ListAdapter
    public ItemsListAdapter(Activity context, ArrayList<ItemsList> items, ItemOnClickHandler itemOnClickHandler) {
        // The second argument is used when the ArrayAdapter is populating and it can be any value. So it is used 0.
        //super(context, 0, items);
        mContext = context;
        mItemList = items;
        mOnClick = itemOnClickHandler;
    }

    public final ItemOnClickHandler mOnClick;
    public interface ItemOnClickHandler{
        void onClickItem(ItemsList itemsLists);
    }


    ///////ViewHolder contained as inner class///////////////////////////////////////////////////////////////////////////////
    /**
     * Cache of the children views for a list item.
     */
    class ItemsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        // Will display the position in the list, ie 0 through getItemCount() - 1
        //TextView listItemNumberView;
        TextView tw_title;
        TextView tw_authors;
        TextView tw_publishedDate;
        TextView tw_price;
        TextView rating_tw;
        RatingBar ratingBar;
        ImageView imageView;
        TextView itemPopupMenu;


        public ItemsViewHolder(View itemView) {
            super(itemView);

            tw_title = (TextView) itemView.findViewById(R.id.title_text_view);
            tw_authors = (TextView) itemView.findViewById(R.id.author_text_view);
            tw_publishedDate = (TextView) itemView.findViewById(R.id.publishedDate);
            tw_price = (TextView) itemView.findViewById(R.id.price_text_view);
            rating_tw = (TextView) itemView.findViewById(R.id.averageRating);
            ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBar);
            imageView = (ImageView) itemView.findViewById(R.id.image);
            itemPopupMenu = (TextView) itemView.findViewById(R.id.item_popupmenu);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            mOnClick.onClickItem(mItemList.get(position));
        }
    }

    @Override
    public ItemsViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdList = R.layout.list;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean attachImmediatelly = false;

        View view = inflater.inflate(layoutIdList, viewGroup, attachImmediatelly);
        ItemsViewHolder viewHolder = new ItemsViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ItemsViewHolder holder, int position) {
        final ItemsList currentItem = mItemList.get(position);
        holder.tw_title.setText(currentItem.getTitle());
        holder.tw_authors.setText(currentItem.getAuthors());
        if (currentItem.isFree()) {
                holder.tw_price.setText(mContext.getString(R.string.isFree));
            } else if (currentItem.isForSale()) {
                String currency = currentItem.getCurrencyCode();
                switch (currency) {
                    case "USD":
                        currency = "$";
                        break;
                    case "EUR":
                        currency = "€";
                        break;
                    case "GBP":
                        currency = "£";
                        break;
                    default:
                        currency = currentItem.getCurrencyCode();
                        break;
                }
                String price_value = formatValue(currentItem.getPrice());
                String price = mContext.getString(R.string.price, currency, price_value);
                holder.tw_price.setText(price);
                holder.tw_price.setVisibility(View.VISIBLE);
            } else {
                holder.tw_price.setVisibility(View.INVISIBLE);
            }
        if (currentItem.hasRating()) {
                holder.rating_tw.setText(String.valueOf(currentItem.getRating()));
                holder.rating_tw.setVisibility(View.VISIBLE);
                holder.ratingBar.setRating((float) currentItem.getRating());
                holder.ratingBar.setVisibility(View.VISIBLE);
            } else {
                holder.rating_tw.setVisibility(View.INVISIBLE);
                holder.ratingBar.setVisibility(View.INVISIBLE);
            }
            holder.tw_publishedDate.setText(formatDate(currentItem.getPublishedDate()));
        //Load images in cache from remote url
        Picasso.with(mContext).load(currentItem.getImageUrl()).into(holder.imageView);
        //here the views are not recycled. It avoids to see the previous image on the next view during the loading.
        holder.setIsRecyclable(false);

        holder.itemPopupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //creating a popup menu
                PopupMenu popup = new PopupMenu(mContext, holder.itemPopupMenu);
                //inflating menu from xml resource
                popup.inflate(R.menu.overflow_menu);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.buy_item:
                                //handle menu1 click
                                Uri webpage;
                                String title = currentItem.getTitle();
                                if (currentItem.getBuyLink() != null) {
                                    webpage = Uri.parse(currentItem.getBuyLink());
                                    Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                                    if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                                        mContext.startActivity(intent);
                                    }
                                } else { //if the items have no buy link, a toast message is displayed
                                    Toast.makeText(mContext, "\"" + title + "\" " + mContext.getString(R.string.not_available), Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case R.id.preview:
                                //handle menu2 click
                                Log.e(LOG_TAG, "Preview: " + currentItem.getTitle());
                                break;
                        }
                        return false;
                    }
                });
                //displaying the popup
                popup.show();

            }
        });

    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }
    ///End ViewHolder///////////////////////////////////////////////////////////////////////////////

    public void setItemList(ArrayList<ItemsList> items){
        mItemList = items;
        this.notifyDataSetChanged();
    }

    //for getting decimal values, based on the string, here can be shown decimal with different digits , 0.000000 and so on
    private String formatValue(double value) {
        DecimalFormat format = new DecimalFormat("0.00");
        return format.format(value);
    }

    //for getting the date conversion
    public String formatDate(String date) {
        String newFormatData = "";
        if (date.length() >= 10) {
            // Splits the string after 10 char, because the date obtained from server is like this "2017-07-15T21:30:35Z", so this method will give 2017-07-15
            CharSequence splittedDate = date.subSequence(0, 10);
            try {
                Date formatDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(splittedDate.toString());
                newFormatData = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(formatDate);
            } catch (ParseException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        } else {
            newFormatData = date;
        }
        return newFormatData;
    }
}
