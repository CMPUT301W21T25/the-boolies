package com.example.cmput301w21t25.managers;

import android.icu.text.UFormat;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.cmput301w21t25.FirestoreCommentCallback;
import com.example.cmput301w21t25.FirestoreStringCallback;
import com.example.cmput301w21t25.experiments.Experiment;
import com.example.cmput301w21t25.forum.Comment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * @author Eden
 * Sorts comments in a nested fashion such that a particular question's responses are listed
 * directly below it by order of date
 */
public class ForumManager {

    public ForumManager() { }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ArrayList<Comment> nestedComments(ArrayList<Comment> comments) {
        ArrayList<Comment> orderedForum = new ArrayList<Comment>();

        //Sort the comments by date to preserve sensical order
        comments.sort(Comparator.comparing(comment -> comment.getCommentDate()));

        //First add new thread comments that have no parents (were passed empty parentID string
        //upon construction)
        for (int i = 0; i < comments.size(); i++) {
            Comment comment = comments.get(i);
            if (comment.getCommentParent().equals("")) {
                orderedForum.add(comments.remove(i));
            }
        }

        //The outer loop runs while the size of the array of comments passed is greater than zero,
        //as in some instances the parents of children comments will also be children, so they would
        //not be present to check against in the orderedForum list on a first pass
        while (comments.size() > 0) {
            for (int i = 0; i < comments.size(); i++) {
                Comment childComment = comments.get(i);
                //The current child is checked against each existing comment in the orderedForum
                //list for a parent
                checkParent:
                for (int j = 0; j < orderedForum.size(); j++) {
                    Comment parentComment = orderedForum.get(j);
                    //If the parent is found, it is added to the orderedForum list and removed from
                    //the comments list. If not, it will be returned to on the next pass of the
                    // outer loop after new potential parents have been added to the orderedForum list
                    if (parentComment.getCommentID().equals(childComment.getCommentParent())) {
                        //The number of children is incremented so that the child is properly
                        //placed in the list (e.g., if the parent is at index 4 and currently has
                        //2 children, its children are at index 5 and 6. When another child is found,
                        //the children count is incremented and the new child is placed at the parent
                        //index + its children count, 4 + 3, which means it is at index 7, the
                        //properly ordered index
                        parentComment.setCommentChildren(parentComment.getCommentChildren() + 1);
                        orderedForum.add((j + parentComment.getCommentChildren()), comments.remove(i));
                        break checkParent;
                    }
                }
            }
        }

        //Log.d("EDEN TEST:", String.valueOf(orderedForum));
        return orderedForum;
    }

    /////
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ExperimentManager expManager = new ExperimentManager();

    /**
     *
     * @param comment
     * @param commenterName
     * @param commenterID
     * @param commentParent
     * @param respondingTo
     */
    public void FB_CreateComment(String experimentID, String comment,String commenterName,String commenterID,String commentParent,String respondingTo){
        // Create a new experiment Hash Map this is the datatype stored in firebase for documents
        Map<String,Object> experimentDoc  = new HashMap<>();
        experimentDoc.put("comment", comment);
        experimentDoc.put("commenterName", commenterName);
        experimentDoc.put("commenterID", commenterID);
        experimentDoc.put("commentParent",commentParent);
        experimentDoc.put("respondingTo",respondingTo);
        experimentDoc.put("commentDate", new Date());

        // Add a new Experiment with a generated ID
        db.collection("Comments")
                .add(experimentDoc)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        db.collection("Experiments").document(experimentID).get()

                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            DocumentSnapshot document = task.getResult();
                                            if(document.exists()){
                                                ArrayList<String> currentComments = (ArrayList<String>)document.getData().get("commentKeys");
                                                currentComments.add(documentReference.getId());
                                                expManager.FB_UpdateCommentKeys(currentComments,experimentID);
                                            }
                                        }
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    //security stuff to make debuging easier
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding experiment", e);
                    }
                });
    }
//    public void FB_FetchComments(Experiment exp,FirestoreCommentCallback fsCallback){
//        ArrayList<Comment>comments= new ArrayList<Comment>();
//        expManager.FB_FetchCommentKeys(exp.getFb_id(), new FirestoreStringCallback() {
//            @Override
//            public void onCallback(ArrayList<String> list) {
//                if(list.size()>0){
//                    Log.d("YA-DB TEST: ", "calling the fetch" );
//                    db.collection("Comments").whereIn(FieldPath.documentId(),list)
//                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                                @Override
//                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
//                                    for (QueryDocumentSnapshot doc: queryDocumentSnapshots) {
//                                        Comment temp =doc.toObject(Comment.class);
//                                        temp.setCommentID(doc.getId());
//                                        comments.add(temp);
//                                    }
//                                    fsCallback.onCallback(comments);
//                                }
//                            });
//                }
//
//            }
//        });
//    }
    public void FB_FetchComments(Experiment exp, ArrayAdapter<Comment> commentAdapter, ArrayList<Comment> comments){
        expManager.FB_FetchCommentKeys(exp.getFb_id(), new FirestoreStringCallback() {
            @Override
            public void onCallback(ArrayList<String> list) {
                Log.d("FORUM_TEST2:", String.valueOf(list));
                if(!list.isEmpty()){
                    Log.d("YA-DB TEST: ", "calling the fetch" );
                    db.collection("Comments").whereIn(FieldPath.documentId(),list)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @RequiresApi(api = Build.VERSION_CODES.N)
                                @Override
                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                                    comments.clear();
                                    for (QueryDocumentSnapshot doc: queryDocumentSnapshots) {
                                        Comment temp =doc.toObject(Comment.class);
                                        temp.setCommentID(doc.getId());
                                        comments.add(temp);
                                        commentAdapter.notifyDataSetChanged();
                                    }
                                    /*ArrayList<Comment> sorted = nestedComments(comments);
                                    Log.d("CATS", String.valueOf(sorted));
                                    comments.clear();
                                    Log.d("CLEARED", "got here");
                                    comments.addAll(sorted);
                                    commentAdapter.notifyDataSetChanged();
                                     */
                                }
                            });
                }

            }
        });
    }
}
