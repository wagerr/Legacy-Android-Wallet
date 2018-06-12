package com.wagerr.wallet.ui.splash_activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.VideoView;

import com.wagerr.wallet.WagerrApplication;
import com.wagerr.wallet.R;
import com.wagerr.wallet.ui.start_activity.StartActivity;
import com.wagerr.wallet.ui.wallet_activity.WalletActivity;

/**
 * Created by Neoperol on 6/13/17.
 */

public class SplashActivity extends AppCompatActivity {
    VideoView videoView;
    private boolean ispaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        videoView = (VideoView) findViewById(R.id.video_view);
        Uri video;
        if(WagerrApplication.getInstance().getAppConf().isSplashSoundEnabled())
            video = Uri.parse("android.resource://" + getPackageName() + "/"
                + R.raw.splash_video);
        else {
            //video = Uri.parse("android.resource://" + getPackageName() + "/"
            //        + R.raw.splash_video_muted);
            Intent intent = new Intent(this, WalletActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        if (videoView != null) {
            videoView.setVideoURI(video);
            videoView.setZOrderOnTop(true);
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    jump();
                }
            });

            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    jump();
                    return true;
                }
            });

            videoView.start();

        }else{
            jump();
        }
    }


    private void jump() {

        if (WagerrApplication.getInstance().getAppConf().isAppInit()){
            Intent intent = new Intent(this, WalletActivity.class);
            startActivity(intent);
        }else {
            // Jump to your Next Activity or MainActivity
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ispaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ispaused) {
            jump();
        }

    }
}
