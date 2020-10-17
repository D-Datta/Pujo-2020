package com.applex.utsav.utility;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.VideoView;

public class Video_Progress extends AsyncTask<Integer, Integer, Integer> {

    private final int duration;
    @SuppressLint("StaticFieldLeak")
    private final VideoView videoView;
    @SuppressLint("StaticFieldLeak")
    private final ProgressBar video_progress;

    public Video_Progress(int duration, VideoView videoView, ProgressBar video_progress) {
        this.duration = duration;
        this.videoView = videoView;
        this.video_progress = video_progress;
        Log.i("BAM", "1");
    }

    @Override
    protected Integer doInBackground(Integer... integers) {
        int current = videoView.getCurrentPosition()/1000;
        Log.i("BAM", "2");
        do {
            try {
                int currentPercent = current * 100/duration;
                publishProgress(currentPercent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while(video_progress.getProgress() <= 100);
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        Log.i("BAM", values[0]+"");
        video_progress.setProgress(values[0]);
    }
}
