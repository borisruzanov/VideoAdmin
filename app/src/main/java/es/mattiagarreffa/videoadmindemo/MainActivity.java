package es.mattiagarreffa.videoadmindemo;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    VideoView videoView;
    ProgressBar progress_bar;
    Button uploadButton, declineButton;

    List<String> videoIDList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView = findViewById(R.id.videoView);

        progress_bar = findViewById(R.id.progress_bar);

        uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadButtonAction();
            }
        });
        declineButton = findViewById(R.id.declineButton);
        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                declineButtonAction();
            }
        });

        retrieveVideos();

    }

    /**
     * This function retrieve all the new videos from database.
     */
    private void retrieveVideos() {
        FirebaseDatabase.getInstance().getReference().child("videos").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    videoIDList.add(ds.getKey());
                }
                populateVideoView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * This function reproduces the first video of the list in the videoView
     */
    private void populateVideoView() {
        if (!videoIDList.isEmpty()) {
            progress_bar.setAlpha(1f);
            FirebaseDatabase.getInstance().getReference().child("videos").child(videoIDList.get(0)).child("videoURL").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Uri videoURL = Uri.parse(String.valueOf(dataSnapshot.getValue()));

                    MediaController mediaController = new MediaController(MainActivity.this);
                    videoView.setMediaController(mediaController);
                    videoView.setVideoURI(videoURL);

                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.setLooping(true);
                            progress_bar.setAlpha(0f);
                            videoView.start();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "There are no videos", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * This function declines the video.
     * It delete the video from Firebase Storage and the reference in database.
     */
    private void declineButtonAction() {
        if (!videoIDList.isEmpty()) {
            videoView.pause();
            progress_bar.setAlpha(1f);
            FirebaseStorage.getInstance().getReference().child("/videos/" + videoIDList.get(0)).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    FirebaseDatabase.getInstance().getReference().child("videos").child(videoIDList.get(0)).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progress_bar.setAlpha(0f);
                            videoIDList.remove(videoIDList.get(0));
                            populateVideoView();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "There are no videos", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * This function accepts the video.
     * It moves the reference of the video to a new database reference so it doesn't pop up anymore in admin app.
     */
    private void uploadButtonAction() {
        if (!videoIDList.isEmpty()) {
            progress_bar.setAlpha(1f);
            FirebaseDatabase.getInstance().getReference().child("videos").child(videoIDList.get(0)).child("videoURL").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    HashMap<String, String> videoInfo = new HashMap<>();
                    videoInfo.put("videoID", videoIDList.get(0));
                    videoInfo.put("videoURL", String.valueOf(dataSnapshot.getValue()));
                    FirebaseDatabase.getInstance().getReference().child("videosAccepted").child(videoIDList.get(0)).setValue(videoInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            FirebaseDatabase.getInstance().getReference().child("videos").child(videoIDList.get(0)).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    videoIDList.remove(videoIDList.get(0));
                                    progress_bar.setAlpha(0f);
                                    Toast.makeText(getApplicationContext(), "Video Accepted", Toast.LENGTH_LONG).show();
                                    populateVideoView();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progress_bar.setAlpha(0f);
                            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "There are no videos", Toast.LENGTH_LONG).show();
        }
    }
}