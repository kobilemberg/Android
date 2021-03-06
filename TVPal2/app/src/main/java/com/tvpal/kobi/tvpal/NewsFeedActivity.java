package com.tvpal.kobi.tvpal;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import com.tvpal.kobi.tvpal.Model.Model;
import com.tvpal.kobi.tvpal.Model.Post;
import com.tvpal.kobi.tvpal.Model.User;
import java.util.LinkedList;

public class NewsFeedActivity extends Activity
{
    static class ViewHolder{
        TextView userNameText;
        TextView userEvent;
        RatingBar rated;
        TextView ratedText;
        TextView episode;
        TextView comment;
        ImageView image;
        ProgressBar imageProgressBar;


    }



    ListView listView;
    CustomAdapter adapter;
    LinkedList<Post> data = new LinkedList<Post>();
    Bitmap defaultBitmap= BitmapFactory.decodeResource(MyApplication.getAppContext().getResources(), R.drawable.default_profile_pic);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_feed);
        Log.d("TAG", "In NewsFeed Activity");
        setTitle("News Feed");
        listView = (ListView) findViewById(R.id.news_feed_listView);
        adapter = new CustomAdapter();
        listView.setAdapter(adapter);
        Model.instance().getAllPosts(new Model.EventPostsListener() {
            @Override
            public void onResult(LinkedList<Post> o) {
                if(o!=null) {
                    data = o;
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onError(String error) {Log.d("Error", "Error: " + error);}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.news_feed_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        setResult(Model.Constant.backBtn);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {

            case R.id.go_to_profile_from_menu:
                intent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivityForResult(intent, 0);
                return true;

            case R.id.go_to_add_show_from_menu:
                intent = new Intent(getApplicationContext(), AddShowActivity.class);
                startActivityForResult(intent, 0);
                return true;

            case R.id.sign_out_from_menu:
                setResult(Model.Constant.logOut);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    static class ViewHolderItem {

        TextView userNameText;
        TextView userEvent;
        RatingBar rated;
        TextView ratedText;
        TextView episode;
        TextView comment;
        ImageView image;
        ProgressBar imageProgressBar;
    }


    class CustomAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {return data.size();}

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {return position;}

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final Post currentPost;
            final ViewHolderItem viewHolder;
            final ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                viewHolder = new ViewHolderItem();
                convertView = inflater.inflate(R.layout.news_feed_row_layout,null);
                //viewHolder.userNameText = (TextView) convertView.findViewById(R.id.news_feed_raw_profile_displayName);
                viewHolder.userNameText = (TextView) convertView.findViewById(R.id.news_feed_raw_profile_displayName);
                viewHolder.userEvent = (TextView) convertView.findViewById(R.id.news_feed_raw_show_event);
                viewHolder.image = (ImageView) convertView.findViewById(R.id.news_feed_raw_profile_image);
                viewHolder.episode = (TextView)convertView.findViewById(R.id.news_feed_raw_episode);
                viewHolder.imageProgressBar = (ProgressBar) convertView.findViewById(R.id.news_feed_raw_user_image_progressBar);
                viewHolder.rated = (RatingBar)convertView.findViewById(R.id.news_feed_raw_ratingBar);
                viewHolder.comment = (TextView)convertView.findViewById(R.id.news_feed_raw_post);
                viewHolder.ratedText = (TextView) convertView.findViewById(R.id.news_feed_raw_rated);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolderItem) convertView.getTag();
            }
            currentPost = (Post) getItem(position);
            Model.instance().getUserByEmail(currentPost.getUserEmail(), new Model.UserEventPostsListener() {
                @Override
                public void onResult(final User u) {
                    Log.d("TAG", u.displayName());
                    if(u!=null){
                        viewHolder.userNameText.setText(u.getFirstName()+" "+u.getLastName());
                        viewHolder.userNameText.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (u.equals(Model.instance().getCurrentUser())) {
                                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                                    startActivityForResult(intent, 0);
                                } else {
                                    Intent intent = new Intent(getApplicationContext(), UserDisplayerActivity.class);
                                    intent.putExtra("user", u.displayName());
                                    intent.putExtra("email", u.getEmail());
                                    startActivityForResult(intent, 0);
                                }
                            }
                        });
                    }
                    else{
                        viewHolder.userNameText.setText("nir");
                    }

                    if(!Model.Constant.isDefaultProfilePic(u.getProfilePic())){
                        Log.d("TAG",u.displayName()+" profilePic "+u.getProfilePic());
                        viewHolder.imageProgressBar.setVisibility(View.VISIBLE);
                        Model.instance().loadImage(u.getProfilePic(), new Model.LoadImageListener() {
                            @Override
                            public void onResult(Bitmap imageBmp) {
                                if (imageBmp != null) {
                                    viewHolder.image.setImageBitmap(imageBmp);
                                }
                                else{
                                    viewHolder.image.setImageBitmap(defaultBitmap);
                                }
                                viewHolder.imageProgressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                    else {
                        viewHolder.image.setImageBitmap(defaultBitmap);
                        viewHolder.imageProgressBar.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onError(String error) {
                    viewHolder.userNameText.setText(error);
                }
            });
            String event = currentPost.getEvent();
            viewHolder.userEvent.setText(event+" "+currentPost.getShowName());
            viewHolder.userEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ShowDisplayerActivity.class);
                    intent.putExtra("showName", currentPost.getShowName());
                    startActivityForResult(intent,0);
                }
            });

            if(!event.equals("Is On")){
                viewHolder.ratedText.setVisibility(View.GONE);
                viewHolder.rated.setVisibility(View.GONE);
                viewHolder.episode.setVisibility(View.GONE);
                viewHolder.comment.setVisibility(View.GONE);
            }
            else {
                viewHolder.ratedText.setVisibility(View.VISIBLE);
                viewHolder.rated.setVisibility(View.VISIBLE);
                viewHolder.episode.setVisibility(View.VISIBLE);
                Log.d("TAG","set starts for "+currentPost.getShow()+": "+currentPost.getGrade());
                viewHolder.rated.setRating(currentPost.getGrade());
                viewHolder.episode.setText(" episode: "+currentPost.getCurrentPart());
                String comment = currentPost.getText();
                if(comment!=null&&!comment.equals("")) {
                    viewHolder.comment.setVisibility(View.VISIBLE);
                    viewHolder.comment.setText("\""+comment+"\"");
                }
                else {
                    viewHolder.comment.setVisibility(View.GONE);
                }
            }
            return convertView;

        }
    }

    @Override
    protected void onResume() {

        Model.instance().getAllPosts(new Model.EventPostsListener() {
            @Override
            public void onResult(LinkedList<Post> o) {
                if(o!=null) {
                    data = o;
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onError(String error) {Log.d("Error", "Error: " + error);}
        });
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);
        if(resultCode== Model.Constant.logOut){
            setResult(Model.Constant.logOut);
            finish();
        }



    }
}

