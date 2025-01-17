package com.union.unionapp.views;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.union.unionapp.controllers.AdapterClubPosts;
import com.union.unionapp.R;
import com.union.unionapp.models.ModelAchievements;
import com.union.unionapp.models.ModelAchievementsScores;
import com.union.unionapp.models.ModelBuddyAndClubPost;
import com.union.unionapp.models.ModelUsers;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * This fragment shows club posts and enable the clubs create a new posts while all users can filter the posts
 *
 * @author unionTeam
 * @version 04.05.2021
 */
public class ClubsFragment extends Fragment {

    // Constants
    private static final int CAMERA_PERM_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 102;
    private static final int GALLERY_REQUEST_CODE = 105;

    // Variables
    private Dialog clubDialog;
    private DatabaseReference userDbRefPosts;
    private Spinner tagSpinner;
    private String filterTagsToUpload;
    private EditText postDetailsEt,
            postQuotaEt,
            postLocationEt,
            postTitleEt;
    private ProgressBar pb;
    private TextView postDateEt,
            postTimeEt,
            filterDateTv,
            filterTimeTv,
            clickToSeeImageTv;
    private TextView[] textViewTags;
    private AppCompatButton tag1,
            tag2,
            tag3,
            filterTag1,
            filterTag2,
            filterTag3;
    private String[] allTags;
    private AppCompatButton[] tagsArray;
    //Achievements
    private int mathScore;// = 0;
    private int careerScore;// = 0;
    private int sportScore;// = 0;
    private int technologyScore;// = 0;
    private int socialScore;// = 0;
    private int englishScore;// = 0;
    private int turkishScore;// = 0;
    private int studyScore;// = 0;
    private String title, description, point, nId, level;
    // Achievements
    private boolean[] tagsStatus = { false, false, false };
    private int[] tagTextsIndexArray = new int[3];
    private int[] i = new int[1];
    private int lastDeletedtag = 0;
    private ImageView imageIv,
            sendButtonIv,
            addPhotoIv;

    private DatabaseReference userDbRef;
    private FirebaseAuth firebaseAuth;
    private String image_uri;
    private String date;
    private String time;
    private String timestamp;
    private String tagsToUpload;
    private DatePickerDialog.OnDateSetListener setListener;
    private TimePickerDialog.OnTimeSetListener timeSetListener;
    private RecyclerView recyclerView;
    private List<ModelBuddyAndClubPost> postList;
    private AdapterClubPosts adapterClubPosts;

    private String currentPhotoPath;
    private String pUid;
    //user info
    private String username, email, uid, pp;

    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState ) {

        View view = inflater.inflate( R.layout.fragment_clubs, container, false );

        pb = view.findViewById( R.id.progressBar );

        ImageView filterImageView = (ImageView) view.findViewById( R.id.showClubFilterPopup );
        ImageView createPost = (ImageView) view.findViewById( R.id.showPopUpCreate );
        clubDialog = new Dialog( getActivity() );


        // Layoutu transparent yapıo
        clubDialog.getWindow().setBackgroundDrawable( new ColorDrawable( android.graphics.Color.TRANSPARENT ) );

        allTags = getResources().getStringArray( R.array.all_tags );


        firebaseAuth = FirebaseAuth.getInstance();

        //get some info of the current user to include in the post
        userDbRef = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Users" );
        userDbRef.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange( @NonNull DataSnapshot snapshot ) {
                for ( DataSnapshot ds : snapshot.getChildren() ) {
                    if ( ds.getKey().equals( FirebaseAuth.getInstance().getCurrentUser().getUid() ) ) {
                        ModelUsers user = ds.getValue( ModelUsers.class );
                        username = user.getUsername();
                        pp = user.getPp();
                        uid = user.getUid();
                    }
                }
            }

            @Override
            public void onCancelled( @NonNull DatabaseError error ) {

            }
        } );


        //recycler view and its properties
        recyclerView = view.findViewById( R.id.clubPostsRecyclerView );
        LinearLayoutManager layoutManager = new LinearLayoutManager( getActivity() );

        //show newest post first, for this load from last
        layoutManager.setStackFromEnd( true );
        layoutManager.setReverseLayout( true );

        //set layout to recyclerview
        recyclerView.setLayoutManager( layoutManager );

        //init post list
        postList = new ArrayList<>();
        loadPosts();

        createPost.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {

                Dialog dialog;
                clubDialog.setContentView( R.layout.custom_create_club_post );

                tagsStatus[0] = false;
                tagsStatus[1] = false;
                tagsStatus[2] = false;
                i[0] = 0;

                tagSpinner = clubDialog.findViewById( R.id.tagSpinner );
                ArrayAdapter<CharSequence> tagAdapter = ArrayAdapter.createFromResource( getActivity(), R.array.club_tags, android.R.layout.simple_spinner_item );
                tagAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
                tagSpinner.setAdapter( tagAdapter );

                clickToSeeImageTv = clubDialog.findViewById( R.id.clickToSeeImageTW );
                clickToSeeImageTv.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick( View view ) {
                        Dialog dialog = new Dialog( getContext() );
                        dialog.getWindow().setBackgroundDrawable( new ColorDrawable( android.graphics.Color.TRANSPARENT ) );
                        dialog.setCanceledOnTouchOutside( true );
                        dialog.setContentView( R.layout.custom_comment_image_view );
                        ImageView commentImageIV = dialog.findViewById( R.id.commentImageIV );
                        try {
                            //if image received, set
                            FirebaseStorage.getInstance().getReference( "BilkentUniversity/ClubPosts/" + image_uri ).getDownloadUrl().addOnFailureListener( new OnFailureListener() {
                                @Override
                                public void onFailure( @NonNull Exception e ) {
                                    Picasso.get().load( R.drawable.user_pp_template ).into( commentImageIV );
                                }
                            } ).addOnSuccessListener( new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess( Uri uri ) {
                                    Picasso.get().load( uri ).into( commentImageIV );
                                }
                            } );
                        } catch ( Exception e ) {
                            //if there is any exception while getting image then set default
                            Picasso.get().load( R.drawable.user_pp_template ).into( commentImageIV );
                        }

                        dialog.show();
                    }
                } );

                //path to store post data
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference( "BilkentUniversity" ).child( "ClubPosts" );
                pUid = reference.push().getKey();

                tagSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected( AdapterView<?> parent, View view, int position, long id ) {
                        if ( position > 0 ) {
                            String selectedItem = parent.getItemAtPosition( position ).toString();
                            if ( i[0] < tagsStatus.length ) {

                                for ( int j = 0; j < 3; j++ ) {

                                    if ( !tagsStatus[j] ) {

                                        tagsArray[j].setText( selectedItem );
                                        if ( !tagHasSelectedBefore( tag1, tag2, tag3 ) ) {
                                            tagsArray[j].setVisibility( View.VISIBLE );
                                            i[0]++;
                                            tagsStatus[j] = true;
                                        } else {
                                            tagsStatus[j] = false;
                                            tagsArray[j].setText( "" );
                                        }
                                        break;
                                    }
                                }
                            }
                        }

                        if ( i[0] == tagsStatus.length ) {

                            tagSpinner.setEnabled( false );

                        }
                    }

                    public void onNothingSelected( AdapterView<?> parent ) {
                        //TODO
                    }
                } );


                //init views
                postDetailsEt = clubDialog.findViewById( R.id.editTextPostDetails );
                postDateEt = clubDialog.findViewById( R.id.dateTextView );
                postQuotaEt = clubDialog.findViewById( R.id.editTextQuota );
                postTimeEt = clubDialog.findViewById( R.id.editTextTime );
                sendButtonIv = clubDialog.findViewById( R.id.imageViewSendButton );
                addPhotoIv = clubDialog.findViewById( R.id.uploadPhotoImageView );
                postLocationEt = clubDialog.findViewById( R.id.editTextLocation );
                postTitleEt = clubDialog.findViewById( R.id.editTextHeadLine );


                //init tags
                tag1 = clubDialog.findViewById( R.id.textViewTag1 );
                tag2 = clubDialog.findViewById( R.id.textViewTag2 );
                tag3 = clubDialog.findViewById( R.id.textViewTag3 );

                //set tag texts to empty string
                tag1.setText( "" );
                tag2.setText( "" );
                tag3.setText( "" );

                //set tags to invisible
                tag1.setVisibility( View.INVISIBLE );
                tag2.setVisibility( View.INVISIBLE );
                tag3.setVisibility( View.INVISIBLE );


                textViewTags = new TextView[]{ tag1, tag2, tag3 };
                tagsArray = new AppCompatButton[]{ tag1, tag2, tag3 };

                //set onClickListeners for tags
                tag1.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick( View v ) {
                        tag1.setVisibility( View.INVISIBLE );
                        tagsStatus[0] = false;
                        tagSpinner.setEnabled( true );
                        i[0]--;
                        lastDeletedtag = 0;
                    }
                } );

                tag2.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick( View v ) {
                        tag2.setVisibility( View.INVISIBLE );
                        tagsStatus[1] = false;
                        tagSpinner.setEnabled( true );
                        i[0]--;
                        lastDeletedtag = 1;
                    }
                } );

                tag3.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick( View v ) {
                        tag3.setVisibility( View.INVISIBLE );
                        tagsStatus[2] = false;
                        tagSpinner.setEnabled( true );
                        i[0]--;
                        lastDeletedtag = 2;
                    }
                } );


                //set the postDateEt to current date for default
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis( MainActivity.getServerDate() );
                calendarToString( calendar );
                postDateEt.setText( date );
                postTimeEt.setText( time );

                //setting up the calendar dialog
                final int year = calendar.get( Calendar.YEAR );
                final int month = calendar.get( Calendar.MONTH );
                final int day = calendar.get( Calendar.DAY_OF_MONTH );
                final int hourOfDay = calendar.get( Calendar.HOUR_OF_DAY );
                final int minute = calendar.get( Calendar.MINUTE );

                postDateEt.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick( View v ) {
                        DatePickerDialog datePickerDialog = new DatePickerDialog(
                                getActivity(), android.R.style.Theme_Holo_Light_Dialog_MinWidth, setListener, day, month, year
                        );
                        datePickerDialog.getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
                        datePickerDialog.updateDate( year, month, day );
                        datePickerDialog.show();
                    }
                } );

                postTimeEt.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick( View v ) {
                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                getActivity(), android.R.style.Theme_Holo_Light_Dialog_MinWidth, timeSetListener, hourOfDay, minute, true
                        );
                        timePickerDialog.getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
                        timePickerDialog.show();
                    }
                } );

                setListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet( DatePicker view, int year, int month, int dayOfMonth ) {
                        month = month + 1;
                        String dayOfMonthS = "";
                        String monthS = "";
                        if ( dayOfMonth < 10 ) {
                            dayOfMonthS = "0" + dayOfMonth;
                        } else {
                            dayOfMonthS = String.valueOf( dayOfMonth );
                        }
                        if ( month < 10 ) {
                            monthS = "0" + month;
                        } else {
                            monthS = "" + month;
                        }
                        String date = dayOfMonthS + "/" + monthS + "/" + year;
                        postDateEt.setText( date );

                    }
                };

                timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet( TimePicker view, int hourOfDay, int minute ) {
                        String hourOfDayS = "" + hourOfDay;
                        String minuteS = "" + minute;
                        if ( hourOfDay < 10 ) {
                            hourOfDayS = "0" + hourOfDay;
                        }
                        if ( minute < 10 ) {
                            minuteS = "0" + minute;
                        }
                        String time = hourOfDayS + ":" + minuteS;
                        postTimeEt.setText( time );
                    }
                };


                addPhotoIv.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick( View v ) {
                        //show image pick dialog
                        showImagePickDialog();
                    }
                } );

                sendButtonIv.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick( View v ) {

                        String postTitle = postTitleEt.getText().toString().trim();
                        if ( postTitle.isEmpty() ) {
                            postTitleEt.setError( "The title section cannot be left empty." );
                        } else if ( i[0] == 0 ) {
                            ( (TextView) tagSpinner.getSelectedView() ).setError( "Post must have at least 1 tag" );
                        } else {
                            String postDetails = postDetailsEt.getText().toString().trim();
                            timestamp = String.valueOf( MainActivity.getServerDate() );
                            String postDate = postDateEt.getText().toString().trim();
                            String postQuotaStr = postQuotaEt.getText().toString().trim();
                            String postTime = postTimeEt.getText().toString().trim();
                            String postLocation = postLocationEt.getText().toString().trim();


                            tagsToUpload = "";

                            for ( int k = 1; k < allTags.length; k++ ) {
                                if ( allTags[k].equals( tag1.getText().toString() ) || allTags[k].equals( tag2.getText().toString() ) || allTags[k].equals( tag3.getText().toString() ) ) {
                                    tagsToUpload = tagsToUpload + k + ",";
                                }
                            }

                            //tags to upload'un sonundaki virgülü atıyor
                            if ( tagsToUpload.length() > 0 ) {
                                StringBuilder tempString = new StringBuilder( tagsToUpload );
                                tempString.deleteCharAt( tempString.length() - 1 );
                                tagsToUpload = tempString.toString();
                            }

                            if ( TextUtils.isEmpty( postDetails ) ) {
                                Toast.makeText( getActivity(), "Enter post Details", Toast.LENGTH_SHORT );
                                return;
                            }

                            if ( image_uri == null ) {
                                //post without image
                                uploadData( postDetails, postDate, postTime, postQuotaStr, "noImage", postLocation, tagsToUpload, postTitle, reference );
                            } else {
                                //post with image
                                uploadData( postDetails, postDate, postTime, postQuotaStr, image_uri, postLocation, tagsToUpload, postTitle, reference );
                            }

                            tagsStatus[0] = false;
                            tagsStatus[1] = false;
                            tagsStatus[2] = false;
                            i[0] = 0;

                            clubDialog.dismiss();
                        }
                    }
                } );

                clubDialog.show();
            }
        } );
        checkUserStatus();

        filterImageView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                clubDialog.setContentView( R.layout.custom_club_filter );
                clubDialog.setCanceledOnTouchOutside( true );

                filterTag1 = clubDialog.findViewById( R.id.filterTag1TextView );
                filterTag2 = clubDialog.findViewById( R.id.filterTag2TextView );
                filterTag3 = clubDialog.findViewById( R.id.filterTag3TextView );


                //init views
                ImageView searchFilterImageView = clubDialog.findViewById( R.id.searchFiltersImageView );
                ImageView resetFiltersImageView = clubDialog.findViewById( R.id.cancelFilterImageView );
                TextView filterDateTv = clubDialog.findViewById( R.id.dateFilterEditText );
                TextView filterTimeTv = clubDialog.findViewById( R.id.timeFilterEditText );
                EditText filterLocationEt = clubDialog.findViewById( R.id.locationFilterEditText );
                EditText filterQuotaEt = clubDialog.findViewById( R.id.quotaFilterEditText );


                //set to 0 zero in order to prevent blocking spinner due to the previous posts.
                i[0] = 0;

                //set tags to empty strings
                filterTag1.setText( "" );
                filterTag2.setText( "" );
                filterTag3.setText( "" );

                //set tags invisible
                filterTag1.setVisibility( View.INVISIBLE );
                filterTag2.setVisibility( View.INVISIBLE );
                filterTag3.setVisibility( View.INVISIBLE );

                //set boolean array to false only
                tagsStatus[0] = false;
                tagsStatus[1] = false;
                tagsStatus[2] = false;

                tagsArray = new AppCompatButton[]{ filterTag1, filterTag2, filterTag3 };
                textViewTags = new TextView[]{ tag1, tag2, tag3 };

                tagSpinner = clubDialog.findViewById( R.id.tagSpinner );
                ArrayAdapter<CharSequence> tagAdapter = ArrayAdapter.createFromResource( getActivity(), R.array.buddy_tags, android.R.layout.simple_spinner_item );
                tagAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
                tagSpinner.setAdapter( tagAdapter );

                tagSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected( AdapterView<?> parent, View view, int position, long id ) {
                        if ( position > 0 ) {
                            String selectedItem = parent.getItemAtPosition( position ).toString();
                            if ( i[0] < tagsStatus.length ) {

                                for ( int j = 0; j < 3; j++ ) {

                                    if ( !tagsStatus[j] ) {

                                        tagsArray[j].setText( selectedItem );
                                        if ( !tagHasSelectedBefore( filterTag1, filterTag2, filterTag3 ) ) {
                                            tagsArray[j].setVisibility( View.VISIBLE );
                                            i[0]++;
                                            tagsStatus[j] = true;
                                        } else {
                                            tagsStatus[j] = false;
                                            tagsArray[j].setText( "" );
                                        }
                                        break;
                                    }
                                }
                            }
                        }

                        if ( i[0] == tagsStatus.length ) {
                            tagSpinner.setEnabled( false );

                        }
                    }

                    public void onNothingSelected( AdapterView<?> parent ) {
                        //TODO
                    }
                } );


                //set the postDateEt to current date for default
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis( MainActivity.getServerDate() );
                calendarToString( calendar );
                filterDateTv.setText( date );
                filterTimeTv.setText( time );

                //setting up the calendar dialog
                final int year = calendar.get( Calendar.YEAR );
                final int month = calendar.get( Calendar.MONTH );
                final int day = calendar.get( Calendar.DAY_OF_MONTH );
                final int hourOfDay = calendar.get( Calendar.HOUR_OF_DAY );
                final int minute = calendar.get( Calendar.MINUTE );


                filterDateTv.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick( View v ) {
                        DatePickerDialog datePickerDialog = new DatePickerDialog(
                                getActivity(), android.R.style.Theme_Holo_Light_Dialog_MinWidth, setListener, day, month, year
                        );
                        datePickerDialog.getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
                        datePickerDialog.updateDate( year, month, day );
                        datePickerDialog.show();
                    }
                } );

                filterTimeTv.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick( View v ) {
                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                getActivity(), android.R.style.Theme_Holo_Light_Dialog_MinWidth, timeSetListener, hourOfDay, minute, true
                        );
                        timePickerDialog.getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
                        timePickerDialog.show();
                    }
                } );

                setListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet( DatePicker view, int year, int month, int dayOfMonth ) {
                        String monthS = month + "";
                        String dayS = dayOfMonth + "";

                        month = month + 1;

                        if (month < 10) {
                            monthS = "0" + month;
                        }
                        if (dayOfMonth < 10) {
                            dayS = "0" + dayOfMonth;
                        }

                        String newDate = dayS + "/" + monthS + "/" + year;
                        filterDateTv.setText( newDate );


                    }
                };

                timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet( TimePicker view, int hourOfDay, int minute ) {
                        String time = hourOfDay + ":" + minute;
                        filterTimeTv.setText( time );
                    }
                };


                //set onClickListeners for tags
                filterTag1.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick( View v ) {
                        filterTag1.setVisibility( View.INVISIBLE );
                        tagsStatus[0] = false;
                        tagSpinner.setEnabled( true );
                        i[0]--;
                        lastDeletedtag = 0;
                    }
                } );

                filterTag2.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick( View v ) {
                        filterTag2.setVisibility( View.INVISIBLE );
                        tagsStatus[1] = false;
                        tagSpinner.setEnabled( true );
                        i[0]--;
                        lastDeletedtag = 1;
                    }
                } );

                filterTag3.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick( View v ) {
                        filterTag3.setVisibility( View.INVISIBLE );
                        tagsStatus[2] = false;
                        tagSpinner.setEnabled( true );
                        i[0]--;
                        lastDeletedtag = 2;
                    }
                } );

                //set on click listener for the searching filter imageView
                searchFilterImageView.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick( View v ) {

                        //save the user's filter choices
                        String filterDate = filterDateTv.getText().toString().trim();
                        String filterQuota = filterQuotaEt.getText().toString().trim();
                        if (filterQuota.isEmpty()) {
                            filterQuota = "";
                        }
                        String filterTime = filterTimeTv.getText().toString().trim();
                        String filterLocation = filterLocationEt.getText().toString().trim();
                        filterTagsToUpload = "";


                        for ( int k = 1; k < allTags.length; k++ ) {
                            if ( allTags[k].equals( filterTag1.getText().toString() ) || allTags[k].equals( filterTag2.getText().toString() ) || allTags[k].equals( filterTag3.getText().toString() ) ) {
                                filterTagsToUpload = filterTagsToUpload + k + ",";
                            }
                        }

                        if ( filterTagsToUpload.length() != 0 ) {

                            //tags to upload'un sonundaki virgülü atıyor
                            StringBuilder tempString = new StringBuilder( filterTagsToUpload );
                            filterTagsToUpload = tempString.deleteCharAt( tempString.length() - 1 ).toString(); //filterTagsToUpload = tempString.substring(0,tempString.length() - 2);
                        }

                        if ( filterTagsToUpload.equals( "" ) ) {
                            filterTagsToUpload = "0";
                        }


                        DatabaseReference queryRef = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/ClubPosts" );
                        Query query = queryRef.orderByChild( "pQuota" ).equalTo( filterQuota );

                        query.addValueEventListener( new ValueEventListener() {
                            @Override
                            public void onDataChange( @NonNull DataSnapshot snapshot ) {
                                Toast.makeText( getActivity(), "Oluyor buraya kadar", Toast.LENGTH_SHORT ).show();


                                postList.clear();
                                for ( DataSnapshot ds : snapshot.getChildren() ) {
                                    ModelBuddyAndClubPost modelBuddyPost = ds.getValue( ModelBuddyAndClubPost.class );

                                    if ( modelBuddyPost.getpQuota().isEmpty() || modelBuddyPost.getpQuota().contains( filterQuota ) ) {

                                        if ( !filterLocation.isEmpty() ) {
                                            if ( !modelBuddyPost.getpLocation().contains( filterLocation ) ) {
                                                continue;
                                            }
                                        }

                                        if ( !filterTagsToUpload.equals( "0" ) ) {
                                            String[] filterPartialTags = filterTagsToUpload.split( "," );
                                            int filterTagNumber = filterPartialTags.length;

                                            String postTags = modelBuddyPost.getpTags();
                                            String[] postPartialTags = postTags.split( "," );

                                            if ( !isMatch( postPartialTags, filterPartialTags ) ) {
                                                continue;
                                            }

                                        }

                                        if ( !filterDate.isEmpty() ) {
                                            if (!modelBuddyPost.getpDate().contains( filterDate ) ) {
                                                continue;
                                            }
                                        }

                                        if ( !filterTime.isEmpty() ) {
                                            if (!modelBuddyPost.getpHour().contains( filterTime ) ) {
                                                continue;
                                            }
                                        }

                                        postList.add( modelBuddyPost );

                                    }
                                }

                                // adapter
                                adapterClubPosts = new AdapterClubPosts( getActivity(), postList );
                                adapterClubPosts.notifyDataSetChanged();

                                // set adapter to recyclerView
                                recyclerView.setAdapter( adapterClubPosts );

                            }

                            @Override
                            public void onCancelled( @NonNull DatabaseError error ) {

                            }
                        } );

                        clubDialog.dismiss();
                    }
                } );

                //set on click listener to reset filters
                resetFiltersImageView.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick( View v ) {

                        filterDateTv.setText( "" );
                        filterQuotaEt.setText( "" );
                        filterTimeTv.setText( "" );
                        filterLocationEt.setText( "" );

                        //TODO Reset Selected filters and set the feed back to normal feed.
                        loadPosts();

                    }
                } );

                //dialog dismiss listener
                clubDialog.setOnDismissListener( new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss( DialogInterface dialog ) {

                    }
                } );

                clubDialog.show();
            }
        } );
        return view;
    }


    private void loadPosts() {
        // path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/ClubPosts" );
        ref.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange( @NonNull DataSnapshot snapshot ) {
                postList.clear();
                for ( DataSnapshot ds : snapshot.getChildren() ) {
                    ModelBuddyAndClubPost modelBuddyPost = ds.getValue( ModelBuddyAndClubPost.class );
                    postList.add( modelBuddyPost );

                    // adapter
                    adapterClubPosts = new AdapterClubPosts( getActivity(), postList );

                    // set adapter to recyclerView
                    recyclerView.setAdapter( adapterClubPosts );
                }
                pb.setVisibility( View.GONE );
            }

            @Override
            public void onCancelled( @NonNull DatabaseError error ) {
                // in case of error
                // Toast.makeText(getActivity(), "Error on load post method 214. line", Toast.LENGTH_SHORT).show();
            }


        } );
    }

    private void searchPosts( String searchQuery ) {

    }


    private void showImagePickDialog() {
        String[] options = { "Camera", "Gallery", "Delete Photo" };
        Context context;
        AlertDialog.Builder builder = new AlertDialog.Builder( getContext() );
        // set items to dialog
        builder.setTitle( "Pick Image From" );
        builder.setItems( options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick( DialogInterface dialogInterface, int i ) {
                if ( i == 0 ) {
                    //camera clicked
                    askCameraPermissions();

                } else if ( i == 1 ) {
                    // gallery clicked
                    Intent gallery = new Intent( Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI );
                    startActivityForResult( gallery, GALLERY_REQUEST_CODE );
                } else if ( i == 2 ) {
                    addPhotoIv.setBackground( ContextCompat.getDrawable( getContext(), R.drawable.ic_baseline_add_a_photo_24 ) );
                    StorageReference image = FirebaseStorage.getInstance().getReference( "BilkentUniversity/ClubPosts/" + pUid );
                    image_uri = "noImage";
                    image.delete();
                    clickToSeeImageTv.setVisibility( View.INVISIBLE );
                }
            }
        } );
        builder.show();
    }

    private void checkUserStatus() {
        //get curremt user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if ( user != null ) {
            //user is signed in
        }
    }

    private void uploadData( String postDetails, String postDate, String postTime, String postQuotaStr, String uri, String postLocation, String tagsToUpload, String postTitle, DatabaseReference reference ) {
        //for post-image name, post-id, post-publish-time
        String timeStamp = String.valueOf( MainActivity.getServerDate() );

        //post without image

        checkUserStatus();
        HashMap<Object, String> hashMap = new HashMap<>();

        //put post info
        hashMap.put( "uid", uid );
        hashMap.put( "username", username );
        hashMap.put( "uEmail", email );
        hashMap.put( "uDp", pp );
        hashMap.put( "pDetails", postDetails );
        hashMap.put( "pDate", postDate );
        hashMap.put( "pHour", postTime );
        hashMap.put( "pQuota", postQuotaStr );
        hashMap.put( "pImage", uri );
        hashMap.put( "pTime", timeStamp );
        hashMap.put( "pLocation", postLocation );
        hashMap.put( "pTitle", postTitle );

        //tagsToUpload achievements KUTAY
        String[] achsTagsToUpload = tagsToUpload.split( "," );
        for ( int i = 0; i < achsTagsToUpload.length; i++ ) {
            if ( Integer.valueOf( achsTagsToUpload[i] ) < 4 ) {

                loadProfileScoreAchievements();
                Handler handler = new Handler();
                handler.postDelayed( new Runnable() {
                    public void run() {
                        increaseOnePoints( "1" );
                    }
                }, 2000 );

            } else if ( Integer.valueOf( achsTagsToUpload[i] ) < 6 ) {

                loadProfileScoreAchievements();
                Handler handler = new Handler();
                handler.postDelayed( new Runnable() {
                    public void run() {
                        increaseOnePoints( "2" );
                    }
                }, 2000 );

            } else if ( Integer.valueOf( achsTagsToUpload[i] ) < 11 ) {

                loadProfileScoreAchievements();
                Handler handler = new Handler();
                handler.postDelayed( new Runnable() {
                    public void run() {
                        increaseOnePoints( "3" );
                    }
                }, 2000 );
            } else if ( Integer.valueOf( achsTagsToUpload[i] ) < 14 ) {

                loadProfileScoreAchievements();
                Handler handler = new Handler();
                handler.postDelayed( new Runnable() {
                    public void run() {
                        increaseOnePoints( "4" );
                    }
                }, 2000 );
            } else if ( Integer.valueOf( achsTagsToUpload[i] ) < 17 ) {

                loadProfileScoreAchievements();
                Handler handler = new Handler();
                handler.postDelayed( new Runnable() {
                    public void run() {
                        increaseOnePoints( "5" );
                    }
                }, 2000 );
            } else if ( Integer.valueOf( achsTagsToUpload[i] ) < 19 ) {

                loadProfileScoreAchievements();
                Handler handler = new Handler();
                handler.postDelayed( new Runnable() {
                    public void run() {
                        increaseOnePoints( "6" );
                    }
                }, 2000 );
            } else if ( Integer.valueOf( achsTagsToUpload[i] ) < 21 ) {

                loadProfileScoreAchievements();
                Handler handler = new Handler();
                handler.postDelayed( new Runnable() {
                    public void run() {
                        increaseOnePoints( "7" );
                    }
                }, 2000 );
            } else if ( Integer.valueOf( achsTagsToUpload[i] ) < 23 ) {

                loadProfileScoreAchievements();
                Handler handler = new Handler();
                handler.postDelayed( new Runnable() {
                    public void run() {
                        increaseOnePoints( "8" );
                    }
                }, 2000 );
            }

        }

        if ( !tagsToUpload.equals( "" ) ) {
            hashMap.put( "pTags", tagsToUpload );
        } else {
            hashMap.put( "pTags", "0" );
        }

        hashMap.put( "pId", pUid );

        //put data in this ref
        reference.child( pUid ).setValue( hashMap )
                .addOnSuccessListener( new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess( Void aVoid ) {
                        //added in database
                        addToHisLastActivities( pUid, "Published an announcement" );

                        // Sends notification to people who have same tag numbers with this post

                        //getting users who have that spesific tag
                        // for now notification will send for random tag
                        String completeTag = tagsToUpload;
                        String[] partialTag = completeTag.split( "," );

                        int randomIndex = (int) Math.random() * ( partialTag.length - 1 );
                        final String luckyOnesToBeSendNotification = partialTag[randomIndex];

                        String firstTag = "1,2,3"; //tagSplitter(tagsToUpload)[0];

                        userDbRef = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Users" );
                        //   final String luckyOnesToBeSendNotification = "2";

                        userDbRef.addValueEventListener( new ValueEventListener() {
                            @Override
                            public void onDataChange( @NonNull DataSnapshot snapshot ) {
                                for ( DataSnapshot ds : snapshot.getChildren() ) {

                                    ModelUsers modelUsers = ds.getValue( ModelUsers.class );

                                    String[] userTags = modelUsers.getTags().split( "," );
                                    String firstTag = userTags[0];
                                    String secondTag = userTags[1];
                                    String thirdTag = userTags[2];
                                    String userUI = modelUsers.getUid();
                                    String[] alltags = MainActivity.getAllTags();

                                    if ( luckyOnesToBeSendNotification.equals( firstTag ) || luckyOnesToBeSendNotification.equals( secondTag ) || luckyOnesToBeSendNotification.equals( thirdTag ) ) {
                                        if ( !userUI.equals( uid ) ) {
                                            addToHisNotifications( "" + userUI, "" + pUid, "" + username + " has a new announcement !" + " " + alltags[Integer.parseInt( luckyOnesToBeSendNotification )] );
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled( @NonNull DatabaseError error ) {

                            }
                        } );
                    }
                } )
                .addOnFailureListener( new OnFailureListener() {
                    @Override
                    public void onFailure( @NonNull Exception e ) {
                        //failed adding post in database
                        Toast.makeText( getActivity(), "Failed publishing post", Toast.LENGTH_SHORT );
                    }
                } );

    }

    private void addToHisNotifications( String hisUid, String pId, String notification ) {

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put( "pId", pId );
        hashMap.put( "timestamp", timestamp );
        hashMap.put( "pUid", hisUid );
        hashMap.put( "notification", notification );
        hashMap.put( "sUid", uid );
        hashMap.put( "sName", username );
        hashMap.put( "sTag", tagsToUpload );

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Notifications/" + hisUid ); // uid
        String nUid = ref.push().getKey();
        hashMap.put( "nId", nUid );
        ref.child( pId ).setValue( hashMap )

                .addOnSuccessListener( new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess( Void aVoid ) {

                    }
                } )
                .addOnFailureListener( new OnFailureListener() {
                    @Override
                    public void onFailure( @NonNull Exception e ) {
                        // failed
                    }
                } );

    }

    private void loadProfileScoreAchievements() {

        // getting user's scores
        DatabaseReference usersDbRefAchscore = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Users/" + uid );
        usersDbRefAchscore.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange( @NonNull DataSnapshot snapshot ) {
                for ( DataSnapshot ds : snapshot.getChildren() ) {
                    if ( ds.getKey().equals( "AchievementsScores" ) ) {

                        ModelAchievementsScores modelAchievementsScores = ds.getValue( ModelAchievementsScores.class );

                        //Parsing in database user's scores
                        mathScore = Integer.parseInt( "" + modelAchievementsScores.getMath() );
                        careerScore = Integer.parseInt( "" + modelAchievementsScores.getCareer() );
                        sportScore = Integer.parseInt( "" + modelAchievementsScores.getSport() );
                        technologyScore = Integer.parseInt( "" + modelAchievementsScores.getTechnology() );
                        socialScore = Integer.parseInt( "" + modelAchievementsScores.getSocial() );
                        englishScore = Integer.parseInt( "" + modelAchievementsScores.getEnglish() );
                        turkishScore = Integer.parseInt( "" + modelAchievementsScores.getTurkish() );
                        studyScore = Integer.parseInt( "" + modelAchievementsScores.getStudy() );
                    }
                }
            }

            @Override
            public void onCancelled( @NonNull DatabaseError error ) {

            }
        } );


    }

    private void increaseOnePoints( String genre ) {
        //TODO create all users with default scores

        title = "";
        description = "";
        point = "";
        nId = "";
        level = "";


        // getting user's scores
        DatabaseReference usersDbRefAchscore = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Users/" + uid );
        usersDbRefAchscore.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange( @NonNull DataSnapshot snapshot ) {
                for ( DataSnapshot ds : snapshot.getChildren() ) {
                    if ( ds.getKey().equals( "AchievementsScores" ) ) {

                        ModelAchievementsScores modelAchievementsScores = ds.getValue( ModelAchievementsScores.class );

                        //Parsing in database user's scores
                        mathScore = Integer.parseInt( "" + modelAchievementsScores.getMath() );
                        careerScore = Integer.parseInt( "" + modelAchievementsScores.getCareer() );
                        sportScore = Integer.parseInt( "" + modelAchievementsScores.getSport() );
                        technologyScore = Integer.parseInt( "" + modelAchievementsScores.getTechnology() );
                        socialScore = Integer.parseInt( "" + modelAchievementsScores.getSocial() );
                        englishScore = Integer.parseInt( "" + modelAchievementsScores.getEnglish() );
                        turkishScore = Integer.parseInt( "" + modelAchievementsScores.getTurkish() );
                        studyScore = Integer.parseInt( "" + modelAchievementsScores.getStudy() );
                    }
                }
            }

            @Override
            public void onCancelled( @NonNull DatabaseError error ) {

            }
        } );
        // increasing the points depending on the genre
        // increase 1 point to mathScore
        if ( genre.equals( "1" ) ) {
            mathScore++;
            if ( mathScore == 10 || mathScore == 50 || mathScore == 100 || mathScore == 500 || mathScore == 100 ) {
                // query pass info
                DatabaseReference DbRefAchs = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Achievements/" );
                DbRefAchs.addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange( @NonNull DataSnapshot snapshot ) {
                        for ( DataSnapshot ds : snapshot.getChildren() ) {
                            ModelAchievements modelAchievements = ds.getValue( ModelAchievements.class );
                            if ( modelAchievements.getGenre().equals( "1" ) && modelAchievements.getPoint().equals( String.valueOf( mathScore ) ) ) {

                                // gets achievement information
                                title = modelAchievements.getTitle();
                                description = modelAchievements.getDescription();
                                point = modelAchievements.getPoint();
                                nId = modelAchievements.getnId();
                                level = modelAchievements.getLevel();

                                //puts this to user
                                HashMap<Object, String> hashMapd = new HashMap<>();
                                hashMapd.put( "title", title );
                                hashMapd.put( "description", description );
                                hashMapd.put( "point", point );
                                hashMapd.put( "genre", genre );
                                hashMapd.put( "nId", nId );
                                hashMapd.put( "level", level );
                                DatabaseReference usersDbRefAchs = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Users/" + uid + "/Achievements/" );

                                usersDbRefAchs.child( nId ).setValue( hashMapd );
                            }
                        }
                    }

                    @Override
                    public void onCancelled( @NonNull DatabaseError error ) {

                    }
                } );
                // add user's Achievements their id
            }
        }
        //increase 1 point to careerScore
        else if ( genre.equals( "2" ) ) {
            careerScore++;

            if ( careerScore == 10 || careerScore == 50 || careerScore == 100 || careerScore == 500 || careerScore == 100 ) {
                // query ile bilgileri getirt
                DatabaseReference DbRefAchs = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Achievements/" );
                DbRefAchs.addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange( @NonNull DataSnapshot snapshot ) {
                        for ( DataSnapshot ds : snapshot.getChildren() ) {
                            ModelAchievements modelAchievements = ds.getValue( ModelAchievements.class );
                            if ( modelAchievements.getGenre().equals( "2" ) && modelAchievements.getPoint().equals( String.valueOf( careerScore ) ) ) {
                                // gets achievement information
                                title = modelAchievements.getTitle();
                                description = modelAchievements.getDescription();
                                point = modelAchievements.getPoint();
                                nId = modelAchievements.getnId();
                                level = modelAchievements.getLevel();
                                //puts this to user
                                HashMap<Object, String> hashMapd = new HashMap<>();
                                hashMapd.put( "title", title );
                                hashMapd.put( "description", description );
                                hashMapd.put( "point", point );
                                hashMapd.put( "genre", genre );
                                hashMapd.put( "nId", nId );
                                hashMapd.put( "level", level );
                                DatabaseReference usersDbRefAchs = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Users/" + uid + "/Achievements/" );

                                usersDbRefAchs.child( nId ).setValue( hashMapd );
                            }
                        }
                    }

                    @Override
                    public void onCancelled( @NonNull DatabaseError error ) {

                    }
                } );
                // add user's Achievements their id
            }
        }
        //increase 1 point to sportScore
        else if ( genre.equals( "3" ) ) {
            sportScore++;
            if ( sportScore == 10 || sportScore == 50 || sportScore == 100 || sportScore == 500 || sportScore == 100 ) {
                // query ile bilgileri getirt
                DatabaseReference DbRefAchs = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Achievements/" );
                DbRefAchs.addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange( @NonNull DataSnapshot snapshot ) {
                        for ( DataSnapshot ds : snapshot.getChildren() ) {
                            ModelAchievements modelAchievements = ds.getValue( ModelAchievements.class );
                            if ( modelAchievements.getGenre().equals( "3" ) && modelAchievements.getPoint().equals( String.valueOf( sportScore ) ) ) {

                                // gets achievement information
                                title = modelAchievements.getTitle();
                                description = modelAchievements.getDescription();
                                point = modelAchievements.getPoint();
                                nId = modelAchievements.getnId();
                                level = modelAchievements.getLevel();

                                //puts this to user
                                HashMap<Object, String> hashMapd = new HashMap<>();
                                hashMapd.put( "title", title );
                                hashMapd.put( "description", description );
                                hashMapd.put( "point", point );
                                hashMapd.put( "genre", genre );
                                hashMapd.put( "nId", nId );
                                hashMapd.put( "level", level );
                                DatabaseReference usersDbRefAchs = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Users/" + uid + "/Achievements/" );

                                usersDbRefAchs.child( nId ).setValue( hashMapd );
                            }
                        }
                    }

                    @Override
                    public void onCancelled( @NonNull DatabaseError error ) {

                    }
                } );
                // add user's Achievements their id
            }
        }
        //increase 1 point to technologyScore
        else if ( genre.equals( "4" ) ) {
            technologyScore++;
            if ( technologyScore == 10 || technologyScore == 50 || technologyScore == 100 || technologyScore == 500 || technologyScore == 100 ) {
                // query ile bilgileri getirt
                DatabaseReference DbRefAchs = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Achievements/" );
                DbRefAchs.addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange( @NonNull DataSnapshot snapshot ) {
                        for ( DataSnapshot ds : snapshot.getChildren() ) {
                            ModelAchievements modelAchievements = ds.getValue( ModelAchievements.class );
                            if ( modelAchievements.getGenre().equals( "4" ) && modelAchievements.getPoint().equals( String.valueOf( technologyScore ) ) ) {

                                // gets achievement information
                                title = modelAchievements.getTitle();
                                description = modelAchievements.getDescription();
                                point = modelAchievements.getPoint();
                                nId = modelAchievements.getnId();
                                level = modelAchievements.getLevel();

                                //puts this to user
                                HashMap<Object, String> hashMapd = new HashMap<>();
                                hashMapd.put( "title", title );
                                hashMapd.put( "description", description );
                                hashMapd.put( "point", point );
                                hashMapd.put( "genre", genre );
                                hashMapd.put( "nId", nId );
                                hashMapd.put( "level", level );
                                DatabaseReference usersDbRefAchs = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Users/" + uid + "/Achievements/" );

                                usersDbRefAchs.child( nId ).setValue( hashMapd );
                            }
                        }
                    }

                    @Override
                    public void onCancelled( @NonNull DatabaseError error ) {

                    }
                } );
                // add user's Achievements their id
            }
        }
        //increase 1 point to socialScore
        else if ( genre.equals( "5" ) ) {
            socialScore++;
            if ( socialScore == 10 || socialScore == 50 || socialScore == 100 || socialScore == 500 || socialScore == 100 ) {
                // query ile bilgileri getirt
                DatabaseReference DbRefAchs = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Achievements/" );
                DbRefAchs.addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange( @NonNull DataSnapshot snapshot ) {
                        for ( DataSnapshot ds : snapshot.getChildren() ) {
                            ModelAchievements modelAchievements = ds.getValue( ModelAchievements.class );
                            if ( modelAchievements.getGenre().equals( "5" ) && modelAchievements.getPoint().equals( String.valueOf( socialScore ) ) ) {

                                // gets achievement information
                                title = modelAchievements.getTitle();
                                description = modelAchievements.getDescription();
                                point = modelAchievements.getPoint();
                                nId = modelAchievements.getnId();
                                level = modelAchievements.getLevel();

                                //puts this to user
                                HashMap<Object, String> hashMapd = new HashMap<>();
                                hashMapd.put( "title", title );
                                hashMapd.put( "description", description );
                                hashMapd.put( "point", point );
                                hashMapd.put( "genre", genre );
                                hashMapd.put( "nId", nId );
                                hashMapd.put( "level", level );
                                DatabaseReference usersDbRefAchs = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Users/" + uid + "/Achievements/" );

                                usersDbRefAchs.child( nId ).setValue( hashMapd );
                            }
                        }
                    }

                    @Override
                    public void onCancelled( @NonNull DatabaseError error ) {

                    }
                } );
                // add user's Achievements their id
            }
        }
        //increase 1 point to englishScore
        else if ( genre.equals( "6" ) ) {
            englishScore++;
            if ( englishScore == 10 || englishScore == 50 || englishScore == 100 || englishScore == 500 || englishScore == 100 ) {
                // query ile bilgileri getirt
                DatabaseReference DbRefAchs = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Achievements/" );
                DbRefAchs.addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange( @NonNull DataSnapshot snapshot ) {
                        for ( DataSnapshot ds : snapshot.getChildren() ) {
                            ModelAchievements modelAchievements = ds.getValue( ModelAchievements.class );
                            if ( modelAchievements.getGenre().equals( "6" ) && modelAchievements.getPoint().equals( String.valueOf( englishScore ) ) ) {

                                // gets achievement information
                                title = modelAchievements.getTitle();
                                description = modelAchievements.getDescription();
                                point = modelAchievements.getPoint();
                                nId = modelAchievements.getnId();
                                level = modelAchievements.getLevel();

                                //puts this to user
                                HashMap<Object, String> hashMapd = new HashMap<>();
                                hashMapd.put( "title", title );
                                hashMapd.put( "description", description );
                                hashMapd.put( "point", point );
                                hashMapd.put( "genre", genre );
                                hashMapd.put( "nId", nId );
                                hashMapd.put( "level", level );
                                DatabaseReference usersDbRefAchs = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Users/" + uid + "/Achievements/" );

                                usersDbRefAchs.child( nId ).setValue( hashMapd );
                            }
                        }
                    }

                    @Override
                    public void onCancelled( @NonNull DatabaseError error ) {

                    }
                } );
                // add user's Achievements their id
            }
        }
        //increase 1 point to turkishScore
        else if ( genre.equals( "7" ) ) {
            turkishScore++;
            if ( turkishScore == 10 || turkishScore == 50 || turkishScore == 100 || turkishScore == 500 || turkishScore == 100 ) {
                // query ile bilgileri getirt
                DatabaseReference DbRefAchs = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Achievements/" );
                DbRefAchs.addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange( @NonNull DataSnapshot snapshot ) {
                        for ( DataSnapshot ds : snapshot.getChildren() ) {
                            ModelAchievements modelAchievements = ds.getValue( ModelAchievements.class );
                            if ( modelAchievements.getGenre().equals( "7" ) && modelAchievements.getPoint().equals( String.valueOf( turkishScore ) ) ) {

                                // gets achievement information
                                title = modelAchievements.getTitle();
                                description = modelAchievements.getDescription();
                                point = modelAchievements.getPoint();
                                nId = modelAchievements.getnId();
                                level = modelAchievements.getLevel();

                                //puts this to user
                                HashMap<Object, String> hashMapd = new HashMap<>();
                                hashMapd.put( "title", title );
                                hashMapd.put( "description", description );
                                hashMapd.put( "point", point );
                                hashMapd.put( "genre", genre );
                                hashMapd.put( "nId", nId );
                                hashMapd.put( "level", level );
                                DatabaseReference usersDbRefAchs = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Users/" + uid + "/Achievements/" );

                                usersDbRefAchs.child( nId ).setValue( hashMapd );
                            }
                        }
                    }

                    @Override
                    public void onCancelled( @NonNull DatabaseError error ) {

                    }
                } );
                // add user's Achievements their id
            }
        }
        //increase 1 point to studyScore
        else if ( genre.equals( "8" ) ) {
            studyScore++;
            if ( studyScore == 10 || studyScore == 50 || studyScore == 100 || studyScore == 500 || studyScore == 100 ) {
                // query ile bilgileri getirt
                DatabaseReference DbRefAchs = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Achievements/" );
                DbRefAchs.addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange( @NonNull DataSnapshot snapshot ) {
                        for ( DataSnapshot ds : snapshot.getChildren() ) {
                            ModelAchievements modelAchievements = ds.getValue( ModelAchievements.class );
                            if ( modelAchievements.getGenre().equals( "8" ) && modelAchievements.getPoint().equals( String.valueOf( studyScore ) ) ) {

                                // gets achievement information
                                title = modelAchievements.getTitle();
                                description = modelAchievements.getDescription();
                                point = modelAchievements.getPoint();
                                nId = modelAchievements.getnId();
                                level = modelAchievements.getLevel();

                                //puts this to user
                                HashMap<Object, String> hashMapd = new HashMap<>();
                                hashMapd.put( "title", title );
                                hashMapd.put( "description", description );
                                hashMapd.put( "point", point );
                                hashMapd.put( "genre", genre );
                                hashMapd.put( "nId", nId );
                                hashMapd.put( "level", level );
                                DatabaseReference usersDbRefAchs = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Users/" + uid + "/Achievements/" );

                                usersDbRefAchs.child( nId ).setValue( hashMapd );
                            }
                        }
                    }

                    @Override
                    public void onCancelled( @NonNull DatabaseError error ) {

                    }
                } );
                // add user's Achievements their id
            }
        }
        // Prapare scores to make it appropraite to send to server
        String SmathScore = String.valueOf( mathScore );
        String ScareerScore = String.valueOf( careerScore );
        String SsportScore = String.valueOf( sportScore );
        String StechnologyScore = String.valueOf( technologyScore );
        String SsocialScore = String.valueOf( socialScore );
        String SenglishScore = String.valueOf( englishScore );
        String SturkishScore = String.valueOf( turkishScore );
        String SstudyScore = String.valueOf( studyScore );

        // Creating Hashes to send information to database
        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put( "math", SmathScore );
        hashMap.put( "career", ScareerScore );
        hashMap.put( "sport", SsportScore );
        hashMap.put( "technology", StechnologyScore );
        hashMap.put( "social", SsocialScore );
        hashMap.put( "english", SenglishScore );
        hashMap.put( "turkish", SturkishScore );
        hashMap.put( "study", SstudyScore );
        DatabaseReference userAchref = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Users/" + uid + "/AchievementsScores/" );

        // Sending hashes to database
        userAchref.setValue( hashMap );
    }

    // Users score will be checked and if he/she wins the achievement achievement's unique id will be added to his database
    private void didIAchieveAny( String usersUid ) {


    }

    private void addToHisLastActivities( String pId, String notification ) {

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put( "pId", pId );
        hashMap.put( "timestamp", timestamp );
        hashMap.put( "notification", notification );
        hashMap.put( "sUid", uid );
        hashMap.put( "sName", username );
        hashMap.put( "sTag", tagsToUpload );
        hashMap.put( "type", "2" );  // 1 buddy 2 club 3 stack

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Users/" + uid + "/LastActivities" ); // uid
        String laUid = ref.push().getKey();
        hashMap.put( "nId", laUid );
        ref.child( laUid ).setValue( hashMap )

                .addOnSuccessListener( new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess( Void aVoid ) {

                    }
                } )
                .addOnFailureListener( new OnFailureListener() {
                    @Override
                    public void onFailure( @NonNull Exception e ) {
                        // failed
                    }
                } );

    }

    public void calendarToString( Calendar calendar ) {
        SimpleDateFormat dateFormat = new SimpleDateFormat( "dd/MM/yyyy" );
        SimpleDateFormat timeFormat = new SimpleDateFormat( "HH:mm" );
        calendar.setTimeInMillis( MainActivity.getServerDate() );
        time = timeFormat.format( calendar.getTime() );
        date = dateFormat.format( calendar.getTime() );
    }

    public boolean tagHasSelectedBefore( AppCompatButton tag1, AppCompatButton tag2, AppCompatButton tag3 ) {

        boolean boo;

        String tag1String = tag1.getText().toString();
        String tag2String = tag2.getText().toString();
        String tag3String = tag3.getText().toString();

        if ( !tag1String.equals( "" ) && !tag2String.equals( "" ) && !tag3String.equals( "" ) ) {

            return ( tag1String.equals( tag2String ) || tag2String.equals( tag3String ) || tag1String.equals( tag3String ) );
        } else {
            return ( tag1String.equals( tag2String ) && tag2String.equals( tag3String ) && tag1String.equals( tag3String ) );
        }
    }

    public static boolean isMatch( String[] arr1, String[] arr2 ) {
        for ( int i = 0; i < arr1.length; i++ ) {
            if ( Arrays.stream( arr2 ).anyMatch( arr1[i]::equals ) ) {

            } else {
                return false;
            }

        }
        return true;
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, @Nullable Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );
        if ( requestCode == CAMERA_REQUEST_CODE ) {
            if ( resultCode == Activity.RESULT_OK ) {
                File f = new File( currentPhotoPath );
                //userPpInDialog.setImageURI(Uri.fromFile(f));
                Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE );
                Uri contentUri = Uri.fromFile( f );
                mediaScanIntent.setData( contentUri );
                getContext().sendBroadcast( mediaScanIntent );


                uploadImageToFirebase( pUid, contentUri );

            }
        }

        if ( requestCode == GALLERY_REQUEST_CODE ) {
            if ( resultCode == Activity.RESULT_OK ) {
                Uri contentUri = data.getData();
                //userPpInDialog.setImageURI(contentUri);

                uploadImageToFirebase( pUid, contentUri );

            }
        }

            /*gelen resmi direkt koymak için
            Bitmap image = (Bitmap) data.getExtras().get("data");
            userPpInDialog.setImageBitmap(image);
             */


    }

    private void uploadImageToFirebase( String pId, Uri contentUri ) {
        FirebaseUser user;
        user = FirebaseAuth.getInstance().getCurrentUser();
        StorageReference image = FirebaseStorage.getInstance().getReference( "BilkentUniversity/ClubPosts/" + pId );
        image.putFile( contentUri );

        image_uri = pId;
        addPhotoIv.setBackground( ContextCompat.getDrawable( getContext(), R.drawable.upload_photo_icon ) );
        clickToSeeImageTv.setVisibility( View.VISIBLE );
    }

    void askCameraPermissions() {
        if ( ContextCompat.checkSelfPermission( getContext(), Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( getActivity(), new String[]{ Manifest.permission.CAMERA }, CAMERA_PERM_CODE );
        } else {
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult( int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults ) {
        if ( requestCode == CAMERA_PERM_CODE ) {
            if ( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                dispatchTakePictureIntent();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
        // Ensure that there's a camera activity to handle the intent
        try {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch ( IOException ex ) {
                // Error occurred while creating the File
                System.out.println( "Error while creating the file" );
            }
            // Continue only if the File was successfully created
            if ( photoFile != null ) {
                Uri photoURI = FileProvider.getUriForFile( getContext(),
                        "com.union.android.fileprovider",
                        photoFile );
                takePictureIntent.putExtra( MediaStore.EXTRA_OUTPUT, photoURI );
                startActivityForResult( takePictureIntent, CAMERA_REQUEST_CODE );
            }
        } catch ( Exception e ) {
            Toast.makeText( getContext(), "No camera application", Toast.LENGTH_SHORT ).show();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat( "yyyyMMdd_HHmmss" ).format( new Date() );
        String imageFileName = "JPEG_" + timeStamp + "_";
        //Private için kod File storageDir = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES );
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


}