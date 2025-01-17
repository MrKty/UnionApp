package com.union.unionapp.views;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.union.unionapp.controllers.AdapterAchievements;
import com.union.unionapp.controllers.AdapterLastActivities;
import com.union.unionapp.R;
import com.union.unionapp.models.ModelAchievements;
import com.union.unionapp.models.ModelLastActivities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * This activity shows other users profiles
 *
 * @author unionTeam
 * @version 04.05.2021
 */
public class OtherProfile extends AppCompatActivity {
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private static int i = 0;


    private boolean lastActsIsActive;
    private boolean achsIsActive;
    private String[] allAchs;
    private String[] userAchs;
    private String[] userActs;
    private String achievementLocationsWComma;
    private TextView lastActsTextView;
    private TextView achsTextView;
    private RecyclerView lastActsRv;
    private RecyclerView achsListRv;
    private ArrayList<ModelLastActivities> LastActList;
    private AdapterLastActivities adapterLastAct;
    private ArrayList<ModelAchievements> AchivementList;
    private AdapterAchievements adapterAchivement;
    private ImageView directMessage;
    private TextView usernameTW;
    private ImageView userPP;
    private String hisUid;

    private ImageView back_bt;
    private AppCompatButton tagButton1;
    private AppCompatButton tagButton2;
    private AppCompatButton tagButton3;
    private AppCompatButton[] tagButtons;
    private String[] tagIndexes;
    private String[] allTags;
    private String tagNums;
    private String date;


    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_other_profile );

        Intent intent = getIntent();
        hisUid = intent.getStringExtra( "Hisuid" );

        firebaseDatabase = firebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference( "BilkentUniversity/Users" );

        lastActsIsActive = true;
        achsIsActive = false;

        //init views
        usernameTW = findViewById( R.id.userNameTextView );
        userPP = findViewById( R.id.userPp );
        tagButton1 = findViewById( R.id.profileTagButton1 );
        tagButton2 = findViewById( R.id.profileTagButton2 );
        tagButton3 = findViewById( R.id.profileTagButton3 );
        back_bt = findViewById( R.id.backButtonn );

        tagButtons = new AppCompatButton[]{ tagButton1, tagButton2, tagButton3 };
        allTags = getResources().getStringArray( R.array.all_tags );

        tagButton1.setVisibility( View.INVISIBLE );
        tagButton2.setVisibility( View.INVISIBLE );
        tagButton3.setVisibility( View.INVISIBLE );


        allAchs = getResources().getStringArray( R.array.user_achievements );
        achievementLocationsWComma = "1,3,5".replace( ",", "" );
        userAchs = new String[achievementLocationsWComma.length()];

        for ( i = 0; i < achievementLocationsWComma.length(); i++ ) {
            userAchs[i] = allAchs[Integer.parseInt( String.valueOf( achievementLocationsWComma.charAt( i ) ) )];
        }

        userActs = new String[]{ "asdasd", "asdadd", "asdadad", "sadasdasdads", "asdadasdasdads", "asdasasd", "asdasads" };

        Query query = databaseReference.orderByChild( "uid" ).equalTo( hisUid );
        query.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange( @NonNull DataSnapshot snapshot ) {
                // check until required data get
                for ( DataSnapshot ds : snapshot.getChildren() ) {
                    //get data
                    String name = "@" + ds.child( "username" ).getValue();
                    String pp = "" + ds.child( "pp" ).getValue();
                    tagNums = "" + ds.child( "tags" ).getValue();

                    if ( tagNums != null ) {
                        tagIndexes = tagNums.split( "," );
                        int[] k = new int[1];
                        k[0] = 0;
                        int temp;
                        for ( String str : tagIndexes ) {
                            temp = Integer.parseInt( str );
                            tagButtons[k[0]].setText( allTags[temp] );
                            tagButtons[k[0]].setVisibility( View.VISIBLE );
                            k[0]++;

                        }
                    }
                    // burada yapılacak

                    //set data
                    usernameTW.setText( name );
                    // set Profile Photo
                    try {
                        //if image received, set
                        StorageReference image = FirebaseStorage.getInstance().getReference( "BilkentUniversity/pp/" + hisUid );
                        image.getDownloadUrl().addOnSuccessListener( new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess( Uri uri ) {
                                Picasso.get().load( uri ).into( userPP );
                            }
                        } );
                    } catch ( Exception e ) {
                        //if there is any exception while getting image then set default
                        Picasso.get().load( R.drawable.user_pp_template ).into( userPP );
                    }
                }
            }

            @Override
            public void onCancelled( @NonNull DatabaseError error ) {

            }
        } );


        // Layoutu transparent yapıo


        directMessage = (ImageView) findViewById( R.id.directMessage );
        lastActsTextView = (TextView) findViewById( R.id.lastActsTextView );
        achsTextView = (TextView) findViewById( R.id.achsTextView );
        achsTextView.setTextColor( Color.parseColor( "#5F5E5D" ) );
        achsTextView.setBackgroundTintList( null );

        /*Achievements için listview kısmı*/
        achsListRv = (RecyclerView) findViewById( R.id.achsListPU );
        lastActsRv = (RecyclerView) findViewById( R.id.lastActsListPU );


        lastActsRv.setVisibility( View.VISIBLE );
        lastActsRv.setEnabled( true );
        achsListRv.setVisibility( View.INVISIBLE );
        achsListRv.setEnabled( false );
        loadLastAct();
        lastActsTextView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                if ( !lastActsIsActive ) {
                    lastActsIsActive = true;
                    lastActsTextView.setTextColor( Color.parseColor( "#FFFFFF" ) );
                    lastActsTextView.getBackground().setTint( Color.parseColor( "#4D4D4D" ) );
                    lastActsRv.setVisibility( View.VISIBLE );
                    lastActsRv.setEnabled( true );

                    achsTextView.setTextColor( Color.parseColor( "#5F5E5D" ) );
                    achsTextView.setBackgroundTintList( null );
                    achsListRv.setVisibility( View.INVISIBLE );
                    achsListRv.setEnabled( false );
                    achsIsActive = false;
                    loadLastAct();

                }
            }

        } );

        achsTextView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                if ( !achsIsActive ) {
                    achsIsActive = true;
                    achsTextView.setTextColor( Color.parseColor( "#FFFFFF" ) );
                    achsTextView.getBackground().setTint( Color.parseColor( "#4D4D4D" ) );
                    achsListRv.setVisibility( View.VISIBLE );
                    achsListRv.setEnabled( true );

                    lastActsTextView.setTextColor( Color.parseColor( "#5F5E5D" ) );
                    lastActsTextView.setBackgroundTintList( null );
                    lastActsRv.setVisibility( View.INVISIBLE );
                    lastActsRv.setEnabled( false );
                    lastActsIsActive = false;
                    loadAchievements();
                }
            }

        } );

        directMessage.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                Intent i = new Intent( getApplicationContext(), ChatActivity.class );
                i.putExtra( "Hisuid", hisUid );
                i.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP );

                startActivity( i );
            }
        } );


        // Back button
        back_bt.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                finish();
            }
        } );
    }

    public void getCurrentDateActivities( Calendar calendar ) {

    }

    public void calendarToString( Calendar calendar ) {
        SimpleDateFormat dateFormat = new SimpleDateFormat( "dd/MM/yyyy" );
        date = dateFormat.format( calendar.getTime() );
    }

    public static String removeALetter( StringBuilder s, char c ) {

        if ( s.charAt( i ) == c ) {
            s.deleteCharAt( i );
        }

        if ( i < s.length() - 1 ) {
            i++;
            removeALetter( s, c );
        }

        return s.toString();
    }

    private void loadLastAct() {
        LastActList = new ArrayList<>();
        DatabaseReference databaseReferenceNotif = firebaseDatabase.getReference( "BilkentUniversity/Users/" + hisUid + "/LastActivities/" );
        databaseReferenceNotif
                .addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange( @NonNull DataSnapshot snapshot ) {
                        LastActList.clear();
                        for ( DataSnapshot ds : snapshot.getChildren() ) {
                            // get data
                            ModelLastActivities model = ds.getValue( ModelLastActivities.class );
                            // add to list
                            LastActList.add( model );
                        }
                        // adapter
                        adapterLastAct = new AdapterLastActivities( getApplicationContext(), LastActList );
                        // set to recycler view
                        lastActsRv.setAdapter( adapterLastAct );
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager( getApplicationContext() );
                        lastActsRv.setLayoutManager( linearLayoutManager );
                        linearLayoutManager.setStackFromEnd( true );
                        linearLayoutManager.setReverseLayout( true );

                    }

                    @Override
                    public void onCancelled( @NonNull DatabaseError error ) {

                    }
                } );
    }

    private void loadAchievements() {
        AchivementList = new ArrayList<>();
        DatabaseReference databaseReferenceNotif = firebaseDatabase.getReference( "BilkentUniversity/Users/" + hisUid + "/Achievements/" );
        databaseReferenceNotif
                .addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange( @NonNull DataSnapshot snapshot ) {
                        AchivementList.clear();
                        for ( DataSnapshot ds : snapshot.getChildren() ) {
                            // get data
                            ModelAchievements model = ds.getValue( ModelAchievements.class );
                            // add to list
                            AchivementList.add( model );
                        }
                        // adapter
                        adapterAchivement = new AdapterAchievements( OtherProfile.this, AchivementList );
                        // set to recycler view
                        achsListRv.setAdapter( adapterAchivement );
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager( getApplicationContext() );
                        achsListRv.setLayoutManager( linearLayoutManager );
                        linearLayoutManager.setStackFromEnd( true );
                        linearLayoutManager.setReverseLayout( true );
                    }

                    @Override
                    public void onCancelled( @NonNull DatabaseError error ) {

                    }
                } );
    }
}