package com.example.cmput301w21t25.customDialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.example.cmput301w21t25.R;

/**
 * @author Eden
 * A dialog fragment that appears when an experiment owner clicks on an item in the HideTrialActivity
 * list view. It prompts the owner as to whether they want to show the selected user's trials that
 * have been uploaded to the owner's experiment.
 */
public class ShowTrialDialogFragment extends DialogFragment {

    public interface OnFragmentInteractionListenerShow {
        void showUser(Integer position);
    }

    private OnFragmentInteractionListenerShow listener;
    private Integer position;

    public ShowTrialDialogFragment(Integer position) {
        this.position = position;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListenerShow){
            listener = (OnFragmentInteractionListenerShow) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.show_user_fragment, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setNegativeButton(Html.fromHtml("<font color=#28527a>CANCEL</font>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                    }
                })
                .setPositiveButton(Html.fromHtml("<font color=#28527a>SHOW</font>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.showUser(position);
                    }}).create();
    }
}
