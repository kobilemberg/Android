package com.tvpal.kobi.tvpal;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import com.tvpal.kobi.tvpal.Model.Model;
import com.tvpal.kobi.tvpal.Model.Post;

public class UpdateShowProgressActivity extends Activity {
    Post current;
    ImageView showImageView;
    TextView showNameText;
    TextView seasonText;
    Spinner episodeSpinner;
    Integer[] episodeArr;
    TextView fromText;
    RatingBar rate;
    EditText opinion;
    Button save;
    Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_show_progress);
        final String showName = getIntent().getExtras().getString("showName");
        Resources res = getResources();
        final int color = res.getColor(android.R.color.white);
        String date = getIntent().getExtras().getString("date");
        String text = getIntent().getExtras().getString("text");
        setTitle("Update "+showName);
        Model.instance().getPostByParamsAsync(showName, date, text, new Model.PostListener() {
            @Override
            public void onResult(Post post) {
                current = post;
                if (!Model.Constant.isDefaultShowPic(current.getImagePath())) {
                    final ProgressBar imagePb = (ProgressBar) findViewById(R.id.activity_updatePost_show_ImageProgressBar);
                    imagePb.setVisibility(View.VISIBLE);
                    showImageView = (ImageView) findViewById(R.id.activity_updatePost_Show_image);
                    Model.instance().loadImage(current.getImagePath(), new Model.LoadImageListener() {
                        @Override
                        public void onResult(Bitmap imageBmp) {
                            if (imageBmp != null) {
                                showImageView.setImageBitmap(imageBmp);
                                imagePb.setVisibility(View.GONE);
                            }
                        }
                    });
                }
                showNameText = (TextView) findViewById(R.id.activity_updatePost_movieName);
                showNameText.setText(current.getShowName());
                seasonText = (TextView) findViewById(R.id.activity_updatePost_seasson);
                seasonText.setText("Season " + current.getShow().getSeason());
                episodeSpinner = (Spinner) findViewById(R.id.activity_updatePost_dropdown);
                episodeArr = new Integer[current.getShow().getEpisode()];
                for (int i = 0; i < episodeArr.length; i++) {
                    episodeArr[i] = i + 1;
                }
                ArrayAdapter<Integer> episodeSpinnerAdapter = new ArrayAdapter<Integer>(getApplicationContext(),  R.layout.style, episodeArr);
                episodeSpinner.setAdapter(episodeSpinnerAdapter);
                episodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        TextView selectedText = (TextView) parent.getChildAt(0);
                        if (selectedText != null) {
                            selectedText.setTextColor(color);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        TextView selectedText = (TextView) parent.getChildAt(0);
                        selectedText.setTextColor(color);
                    }
                });
                        fromText = (TextView) findViewById(R.id.activity_updatePost_from_Number_Of_Episodes);
                fromText.setText("from " + current.getShow().getEpisode());

                rate = (RatingBar) findViewById(R.id.activity_updatePost_ratingBar);
                rate.setNumStars(4);
                opinion = (EditText) findViewById(R.id.activity_updatePost_activity_summary);
                save = (Button) findViewById(R.id.activity_updatePost_Save);
                cancel = (Button) findViewById(R.id.activity_updatePost_cancel);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int seen  = Integer.parseInt(episodeSpinner.getSelectedItem().toString().trim());
                        Post updated = new Post(current.getShowName(),current.getUserEmail(),opinion.getText().toString(), Model.Constant.getCurrentDate(),seen,(int)rate.getRating(),current.getShow());
                        Model.instance().addPost(updated, new Model.PostListener() {
                            @Override
                            public void onResult(Post post) {
                                setResult(2);
                                finish();
                            }

                            @Override
                            public void onError(String error) {}
                        });
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setResult(0);
                        finish();
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.d("TAG",error);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== Model.Constant.logOut) {
            setResult(Model.Constant.logOut);
            finish();
        }
    }
}