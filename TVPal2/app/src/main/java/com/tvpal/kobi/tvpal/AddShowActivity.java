package com.tvpal.kobi.tvpal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import com.tvpal.kobi.tvpal.Model.Model;
import com.tvpal.kobi.tvpal.Model.Post;
import com.tvpal.kobi.tvpal.Model.TVShow;
import com.tvpal.kobi.tvpal.Model.User;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class AddShowActivity extends Activity {

    private static final int REQUEST_CAMERA=1;
    private static final int SELECT_FILE=2;
    ImageView showImage ;
    AutoCompleteTextView showName;
    EditText famousActors;
    EditText numberOfEpisodes;
    EditText categories;
    Button save;
    Button cancel;
    TVShow show;
    EditText seasonText;
    Post post;
    String fileName = Model.Constant.getDefaultShowPic();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_show);
        showImage = (ImageView)findViewById(R.id.activity_addShow_imageView);
        Resources res = getResources();
        final int color = res.getColor(android.R.color.white);
        famousActors = (EditText) findViewById(R.id.activity_addShow_famousActors);
        numberOfEpisodes = (EditText) findViewById(R.id.activity_addShow_NumberOfEpisodes);
        categories = (EditText) findViewById(R.id.activity_addShow_Categories);
        save = (Button) findViewById(R.id.activity_add_Show_Save);
        cancel = (Button) findViewById(R.id.activity_add_show_cancel);
        seasonText = (EditText) findViewById(R.id.activity_addShow_NumberOfSeasons);
        showImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int episodes = new Integer(numberOfEpisodes.getText().toString().trim());
                int season = new Integer(seasonText.getText().toString().trim());
                show = new TVShow(showName.getText().toString(),famousActors.getText().toString(),season,episodes, categories.getText().toString(),Model.Constant.getCurrentDate(),fileName);
                User user = Model.instance().getCurrentUser();
                int currentPart = 0;
                int currentGrade=0;
                post = new Post(show.getName(), user.getEmail(),user.displayName()+" Started "+show.getName(), Model.Constant.getCurrentDate(),currentPart,currentGrade,show);
                Model.instance().createShow(((BitmapDrawable) showImage.getDrawable()).getBitmap(), show,post ,new Model.showCreatorListener() {
                    @Override
                    public void onDone() {
                        setResult(1);
                        finish();
                    }

                    @Override
                    public void onError(String error) {
                        Log.d("ERROR","Catch the Exception : "+error);
                    }
                });
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        showName = (AutoCompleteTextView)findViewById(R.id.autoComplete);
        Model.instance().getAutoCompletePosts(Model.instance().getCurrentUser().getEmail(), new Model.ShowListener() {
            @Override
            public void onDone(final ArrayList<TVShow> shows) {
                Log.d("TAG:","GOT "+shows.size()+"shows");
                String[] showListArr = new String[shows.size()];
                for (int i = 0; i < shows.size(); i++) {
                    showListArr[i] = shows.get(i).getName();
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MyApplication.getAppContext(), R.layout.style, showListArr);
                showName.setAdapter(adapter);
                showName.setTextColor(color);
                showName.setThreshold(1);

                showName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
                        String showNameSelected = (String) parent.getItemAtPosition(position);
                        Log.d("TAG", showNameSelected);
                        for(TVShow s : shows){
                            if(s.getName().equals(showNameSelected)){
                                showName.setText(showNameSelected);
                                famousActors.setText(s.getMainActor());
                                seasonText.setText(Integer.toString(s.getSeason()));
                                numberOfEpisodes.setText(s.getEpisode()+"");
                                categories.setText(s.getCategory());
                                showImage.setClickable(false);
                                Log.d("TAG","NEED IMAGE "+ s.getImagePath());
                                if(!Model.Constant.isDefaultShowPic(s.getImagePath())){
                                    Model.instance().loadImage(s.getImagePath(), new Model.LoadImageListener() {
                                        @Override
                                        public void onResult(Bitmap imageBmp) {
                                            showImage.setImageBitmap(imageBmp);
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {}
        });
    }


    private void selectImage() {

        final CharSequence[] items;
        if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
        {
            items= new CharSequence[3];
            items[0] = "Take Photo";
            items[1] = "Choose from Library";
            items[2] = "Cancel";
        }
        else
        {
            items= new CharSequence[2];
            items[0] = "Choose from Library";
            items[1] = "Cancel";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Model.Constant.logOut)
        {
            setResult(Model.Constant.logOut);
            finish();

        }
        else{
            if (resultCode == RESULT_OK) {
                if (requestCode == REQUEST_CAMERA) {
                    Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                    fileName="Profile_Pic_"+Model.Constant.getCurrentDate()+ ".jpg";
                    showImage.setImageBitmap(thumbnail);
                } else if (requestCode == SELECT_FILE) {
                    Uri selectedImageUri = data.getData();
                    String[] projection = {MediaStore.MediaColumns.DATA};
                    CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null, null);
                    Cursor cursor = cursorLoader.loadInBackground();
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    cursor.moveToFirst();
                    String selectedImagePath = cursor.getString(column_index);
                    Bitmap bm;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(selectedImagePath, options);
                    final int REQUIRED_SIZE = 200;
                    int scale = 1;
                    while (options.outWidth / scale / 2 >= REQUIRED_SIZE && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                        scale *= 2;
                    options.inSampleSize = scale;
                    options.inJustDecodeBounds = false;
                    bm = BitmapFactory.decodeFile(selectedImagePath, options);
                    fileName="Profile_Pic_"+Model.Constant.getCurrentDate()+ ".jpg";
                    showImage.setImageBitmap(bm);
                }
            }
        }

    }
}
