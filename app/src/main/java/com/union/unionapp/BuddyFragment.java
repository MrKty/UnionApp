package com.union.unionapp;



import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class BuddyFragment extends Fragment {

    Dialog buddyDialog;
    Spinner genderSpinner;
    Spinner tagSpinner;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buddy, container, false);

        ImageView filterImageView = (ImageView) view.findViewById(R.id.showBuddyFilterPopup);
        ImageView createPost = (ImageView) view.findViewById(R.id.showPopUpCreate);
        buddyDialog = new Dialog(getActivity());
        // Layoutu transparent yapıo
        buddyDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


        //genderSpinner.setOnItemSelectedListener(this);


        createPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                buddyDialog.setContentView(R.layout.custom_create_post_buddy_popup);

                genderSpinner = buddyDialog.findViewById(R.id.genderSpinner);
                ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(getActivity(),R.array.gender_preferences, android.R.layout.simple_spinner_item);
                genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                genderSpinner.setAdapter(genderAdapter);

                tagSpinner = buddyDialog.findViewById(R.id.tagSpinner);
                ArrayAdapter<CharSequence> tagAdapter = ArrayAdapter.createFromResource(getActivity(),R.array.buddy_tags, android.R.layout.simple_spinner_item);
                tagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                tagSpinner.setAdapter(tagAdapter);

                buddyDialog.show();
            }
        });

        filterImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buddyDialog.setContentView(R.layout.custom_buddy_filter);

                tagSpinner = buddyDialog.findViewById(R.id.tagSpinner);
                ArrayAdapter<CharSequence> tagAdapter = ArrayAdapter.createFromResource(getActivity(),R.array.buddy_tags, android.R.layout.simple_spinner_item);
                tagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                tagSpinner.setAdapter(tagAdapter);

                buddyDialog.show();
            }
        });

        return view;

    }

}