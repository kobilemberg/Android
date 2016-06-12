package com.tvpal.kobi.tvpal;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tvpal.kobi.tvpal.Model.Model;
import com.tvpal.kobi.tvpal.Model.Post;
import com.tvpal.kobi.tvpal.Model.User;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by nir on 05/06/2016.
 */
public class ProfileActivity  extends Activity{
    Button editProfile;
    List<Post> data = new LinkedList<Post>();
    TextView displayName;
    TextView email;
    TextView birthDate;
    ImageView profilePic;
    ProgressBar imageProgressBar;
    User user;
    Button addShowButton;
    ListView listView;
    CustomAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //06-07 10:48:12.518 11759-11759/com.tvpal.kobi.tvpal D/TAG: User have been changed : User{email='m@n.com', firstName='q', lastName='p', birthDate='5/6/2016', password='m', profilePic='null', lastUpdateDate='06/05/2016 21:41:54'}

        setContentView(R.layout.activity_profile);


        user = Model.instance().getCurrentUser();
        listView = (ListView) findViewById(R.id.listView_activity_profile);
        adapter = new CustomAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*Toast.makeText(getApplicationContext(), "item click " + position, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(),StudentDetailsActivity.class);
                intent.putExtra("id",data.get(position).getShowName());
                startActivity(intent);*/
            }
        });

        Model.instance().getAllPostsPerUser(user.getEmail(), new Model.EventPostsListener() {
            @Override
            public void onResult(LinkedList<Post> o) {
                if(o!=null) {
                    data = o;
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String error) {
                Log.d("Error", "Error: " + error);
            }
        });


        Log.d("TAG", "In Profile activity.");
        Log.d("TAG","USER: "+user.toString());
        Model.instance().getUpdateDate();
        Log.d("TAG", "Finished.");
        imageProgressBar = (ProgressBar) findViewById(R.id.UserImageProgressBar);

        displayName = (TextView) findViewById(R.id.activity_profile_name);
        email = (TextView) findViewById(R.id.activity_profile_Email);
        birthDate = (TextView) findViewById(R.id.activity_profile_Birh_Date);
        profilePic = (ImageView) findViewById(R.id.activity_profile_imageView);
        if(!Model.instance().isDefaultProfilePic(user.getProfilePic())){
            imageProgressBar.setVisibility(View.VISIBLE);
            Model.instance().loadImage(user.getProfilePic(), new Model.LoadImageListener() {
                @Override
                public void onResult(Bitmap imageBmp) {
                    if(imageBmp!=null)
                        profilePic.setImageBitmap(imageBmp);
                    imageProgressBar.setVisibility(View.INVISIBLE);
                }
            });
        }
        //put the variables.
        displayName.setText(user.displayName());
        email.setText(user.getEmail());
        birthDate.setText(user.getBirthDate());
        addShowButton = (Button) findViewById(R.id.button_profile_activity_add_Show);
        addShowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG", "Move to ADD SHOW ACTIVITY");
                Intent intent = new Intent(getApplicationContext(), AddShowActivity.class);
                startActivity(intent);
            }
        });



        //profilePic.setImageBitmap(user.getProfilePic());

        editProfile = (Button) findViewById(R.id.edit_profile_button);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG", "Clicked on Edit profile button, MOVING TO Edit PROFILE ACTIVITY");
                Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
                startActivityForResult(intent,0);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUser();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==1) {
            user = Model.instance().getCurrentUser();
            Log.d("TAG","User Edited: "+user.toString());
            displayName.setText(user.displayName());
            email.setText(user.getEmail());
            birthDate.setText(user.getBirthDate());
/*            if(!Model.instance().isDefaultProfilePic(user.getProfilePic())){
                imageProgressBar.setVisibility(View.VISIBLE);
                Model.instance().loadImage(user.getProfilePic(), new Model.LoadImageListener() {
                    @Override
                    public void onResult(Bitmap imageBmp) {
                        if(imageBmp!=null)
                            profilePic.setImageBitmap(imageBmp);
                        imageProgressBar.setVisibility(View.INVISIBLE);

                    }
                });
            }*/
            if(!Model.instance().isDefaultProfilePic(user.getProfilePic()))
            {
                imageProgressBar.setVisibility(View.VISIBLE);
                Log.d("TAG","Profile Pic is different");
                profilePic.setImageBitmap(Model.instance().loadImageFromFile(user.getProfilePic()));
                imageProgressBar.setVisibility(View.INVISIBLE);
            }
            //updateUser();



        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateUser()
    {
        user = Model.instance().getCurrentUser();
        displayName.setText(user.displayName());
        email.setText(user.getEmail());
        birthDate.setText(user.getBirthDate());
        if(!Model.instance().isDefaultProfilePic(user.getProfilePic())){
            imageProgressBar.setVisibility(View.VISIBLE);
            Model.instance().loadImage(user.getProfilePic(), new Model.LoadImageListener() {
                @Override
                public void onResult(Bitmap imageBmp) {
                    if(imageBmp!=null)
                        profilePic.setImageBitmap(imageBmp);
                    imageProgressBar.setVisibility(View.INVISIBLE);
                }
            });
        }
    }


    class CustomAdapter extends BaseAdapter {

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
            if(convertView == null){
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.row_layout,null);
                /*CheckBox cb1 = (CheckBox) convertView.findViewById(R.id.checkBox);
                cb1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("LISTAPP", "my tag is: " + v.getTag());
                        Student st = data.get((Integer) v.getTag());
                        st.setChecked(!st.isChecked());
                    }
                });*/
            }

            final ImageView image = (ImageView) convertView.findViewById(R.id.image_rowlayout_post);
            TextView name = (TextView) convertView.findViewById(R.id.nameTextView);
            ProgressBar pb = (ProgressBar) convertView.findViewById(R.id.TVShow_Progress_Bar);
            Post post = data.get(position);
            pb.setProgress(post.getProgress());
            /*TextView id = (TextView) convertView.findViewById(R.id.idTextView);
            final CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkBox);
            cb.setTag(new Integer(position));*/
            convertView.setTag(position);
            name.setText(post.getShowName()+" ("+post.getProgress()+"%)");
            //id.setText(st.getId());
            //cb.setChecked(st.isChecked());

            if (!post.getImagePath().equals("default_show_pic")){
                Log.d("TAG","list gets image " + post.getImagePath());
                final ProgressBar progress = (ProgressBar) convertView.findViewById(R.id.TVShow_Progress_Bar);
                progress.setVisibility(View.VISIBLE);
                Model.instance().loadImage(post.getImagePath(), new Model.LoadImageListener() {
                    @Override
                    public void onResult(Bitmap imageBmp) {
                        if (imageBmp != null) {
                            image.setImageBitmap(imageBmp);
                            progress.setVisibility(View.GONE);
                        }
                    }
                });
            }
            return convertView;
        }
    }

}


