package com.union.unionapp.controllers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.union.unionapp.R;
import com.union.unionapp.models.ModelNotification;
import com.union.unionapp.views.PostActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * This class binds given notifications into the view
 *
 * @author unionTeam
 * @version 04.05.2021
 */
public class AdapterNotification extends RecyclerView.Adapter<AdapterNotification.HolderNotification> {

    private Context context;
    private ArrayList<ModelNotification> notificationsList;
    private FirebaseAuth firebaseAuth;

    public AdapterNotification( Context context, ArrayList<ModelNotification> notificationsList ) {
        this.context = context;
        this.notificationsList = notificationsList;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderNotification onCreateViewHolder( @NonNull ViewGroup parent, int viewType ) {
        // inflate view row_notification
        View view = LayoutInflater.from( context ).inflate( R.layout.row_notification, parent, false );
        return new HolderNotification( view );
    }

    @Override
    public void onBindViewHolder( @NonNull HolderNotification holder, int position ) {
        // get and set data to views

        // get data
        final ModelNotification modelNotification = notificationsList.get( position );
        String name = modelNotification.getsName();
        String notification = modelNotification.getNotification();
        String image = modelNotification.getsImage();
        String timestamp = modelNotification.getTimestamp();
        String senderUid = modelNotification.getsUid();
        String pId = modelNotification.getpId();
        // conver timestamp to dd//mm/yyyy hh:mm

        Calendar cal = Calendar.getInstance( Locale.ENGLISH );
        cal.setTimeInMillis( Long.parseLong( timestamp ) );
        String dateTime = DateFormat.format( "dd/MM/yyyy hh:mm aa", cal ).toString();
        final String postType;

        //TODO notificationsı comment
        if ( notification.contains( "pro" ) ) {
            postType = "Stack";
            holder.avatarIv.setImageResource( R.drawable.stack_icon );
        } else if ( notification.contains( "announc" ) ) {
            postType = "Club";
            holder.avatarIv.setImageResource( R.drawable.club_icon );
        } else {
            postType = "Buddy";
            holder.avatarIv.setImageResource( R.drawable.buddy_icon );

        }


        // set to views
        holder.nameTv.setText( "@" + name );
        holder.notificationTv.setText( notification );
        holder.timeTv.setText( dateTime );
        if ( notification.contains( "pro" ) ) {
            holder.avatarIv.setImageResource( R.drawable.stack_icon );
        } else if ( notification.contains( "announc" ) ) {

            holder.avatarIv.setImageResource( R.drawable.club_icon );
        } else {

            holder.avatarIv.setImageResource( R.drawable.buddy_icon );

        }

        //TODO Tıklandığında postu acıcak
        holder.itemView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                Intent i = new Intent( context, PostActivity.class );
                i.putExtra( "pType", postType );
                i.putExtra( "source", "outside" );
                i.putExtra( "pId", pId );
                i.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP );
                context.startActivity( i );

            }
        } );
        // long press to delete
        holder.itemView.setOnLongClickListener( new View.OnLongClickListener() {
            @Override
            public boolean onLongClick( View v ) {
                // show conf dialog
                AlertDialog.Builder builder = new AlertDialog.Builder( context );
                builder.setTitle( "Delete" );
                builder.setMessage( "Are you sure to delete this notification?" );
                builder.setPositiveButton( "Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick( DialogInterface dialog, int which ) {
                        // delete notif
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference( "BilkentUniversity/Users" );
                        ref.child( firebaseAuth.getUid() ).child( "Notifications" ).child( senderUid ).removeValue().addOnSuccessListener( new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess( Void aVoid ) {
                                //deleted
                            }
                        } ).addOnFailureListener( new OnFailureListener() {
                            @Override
                            public void onFailure( @NonNull Exception e ) {
                                // failed
                            }
                        } );
                    }
                } ).setNegativeButton( "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick( DialogInterface dialog, int which ) {
                        // cancel
                        dialog.dismiss();
                    }
                } );
                return false;
            }
        } );

    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    // holder class for views of row_notifications.xlm
    class HolderNotification extends RecyclerView.ViewHolder {
        // declare views
        ImageView avatarIv;
        TextView nameTv, notificationTv, timeTv;

        public HolderNotification( @NonNull View itemView ) {
            super( itemView );

            //init views
            avatarIv = itemView.findViewById( R.id.avatarIv );
            nameTv = itemView.findViewById( R.id.usernameTv );
            notificationTv = itemView.findViewById( R.id.notificationTv );
            timeTv = itemView.findViewById( R.id.timeTv );

        }
    }
}
