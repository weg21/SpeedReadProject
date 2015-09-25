package com.andrewgiang.textspritzer.lib;

import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Arrays;


/**
 * Spritzer parses a String into a Queue
 * of words, and displays them one-by-one
 * onto a TextView at a given WPM.
 */
public class Spritzer {
    protected static final String TAG = "Spritzer";
    protected static final boolean VERBOSE = false;

    protected static final int MSG_PRINT_WORD = 1;

    protected static final int MAX_WORD_LENGTH = 13;
    protected static final int CHARS_LEFT_OF_PIVOT = 3;

    protected String[] mWordArray;                  // A parsed list of words parsed from {@link #setText(String input)}
    protected ArrayDeque<String> mWordQueue;        // The queue of words from mWordArray yet to be displayed

    protected TextView mTarget;
    protected int mWPM;

    protected Handler mSpritzHandler;
    protected Object mPlayingSync = new Object();
    protected boolean mPlaying;
    protected boolean mPlayingRequested;
    protected boolean mSpritzThreadStarted;

    protected int mCurWordIdx;
    private ProgressBar mProgressBar;

    public interface OnCompletionListener {

        public void onComplete();

    }

    private DelayStrategy mDelayStrategy;

    public void setOnCompletionListener(OnCompletionListener onCompletionListener) {
        mOnCompletionListener = onCompletionListener;
    }

    private OnCompletionListener mOnCompletionListener;


    public Spritzer(TextView target) {
        init();
        mTarget = target;
        mSpritzHandler = new SpritzHandler(this);
    }


    public void setText(String input) {
        createWordArrayFromString(input);
        setMaxProgress();
        refillWordQueue();
    }

    private void setMaxProgress() {
        if (mWordArray != null && mProgressBar != null) {
            mProgressBar.setMax(mWordArray.length);
        }
    }

    private void createWordArrayFromString(String input) {
        mWordArray = input
                .replaceAll("/\\s+/g", " ")      // condense adjacent spaces
                .split(" ");                    // split on spaces
    }


    protected void init() {

        mDelayStrategy = new DefaultDelayStrategy();
        mWordQueue = new ArrayDeque<String>();
        mWPM = 500;
        mPlaying = false;
        mPlayingRequested = false;
        mSpritzThreadStarted = false;
        mCurWordIdx = 0;
    }

    public int getMinutesRemainingInQueue() {
        if (mWordQueue.size() == 0) {
            return 0;
        }
        return mWordQueue.size() / mWPM;
    }

    public int getWpm() {
        return mWPM;
    }


    public void setWpm(int wpm) {
        mWPM = wpm;
    }


    public void swapTextView(TextView target) {
        mTarget = target;
        if (!mPlaying) {
            printLastWord();
        }

    }


    public void start() {
        if (mPlaying || mWordArray == null) {
            return;
        }
        if (mWordQueue.isEmpty()) {
            refillWordQueue();
        }

        mPlayingRequested = true;
        startTimerThread();
    }

    private int getInterWordDelay() {
        return 60000 / mWPM;
    }

    private void refillWordQueue() {
        updateProgress();
        mCurWordIdx = 0;
        mWordQueue.clear();
        mWordQueue.addAll(Arrays.asList(mWordArray));
    }

    private void updateProgress() {
        if (mProgressBar != null) {
            mProgressBar.setProgress(mCurWordIdx);
        }
    }



    protected void processNextWord() throws InterruptedException {
        if (!mWordQueue.isEmpty()) {
            String word = mWordQueue.remove();
            mCurWordIdx += 1;
            // Split long words, at hyphen if present
            word = splitLongWord(word);

            mSpritzHandler.sendMessage(mSpritzHandler.obtainMessage(MSG_PRINT_WORD, word));

            final int delayMultiplier = mDelayStrategy.delayMultiplier(word);
            //Do not allow multiplier that is less than 1
            final int wordDelay = getInterWordDelay() * (mDelayStrategy != null ? delayMultiplier < 1 ? 1 : delayMultiplier : 1);
            Thread.sleep(wordDelay);

        }
        updateProgress();
    }


    protected String splitLongWord(String word) {
        if (word.length() > MAX_WORD_LENGTH) {
            int splitIndex = findSplitIndex(word);
            String firstSegment;
            if (VERBOSE) {
                Log.i(TAG, "Splitting long word " + word + " into " + word.substring(0, splitIndex) + " and " + word.substring(splitIndex));
            }
            firstSegment = word.substring(0, splitIndex);
            // A word split is always indicated with a hyphen unless ending in a period
            if (!firstSegment.contains("-") && !firstSegment.endsWith(".")) {
                firstSegment = firstSegment + "-";
            }
            mCurWordIdx--; //have to account for the added word in the queue
            mWordQueue.addFirst(word.substring(splitIndex));
            word = firstSegment;

        }
        return word;
    }


    private int findSplitIndex(String thisWord) {
        int splitIndex;
        // Split long words, at hyphen or dot if present.
        if (thisWord.contains("-")) {
            splitIndex = thisWord.indexOf("-") + 1;
        } else if (thisWord.contains(".")) {
            splitIndex = thisWord.indexOf(".") + 1;
        } else if (thisWord.length() > MAX_WORD_LENGTH * 2) {
            // if the word is floccinaucinihilipilifcation, for example.
            splitIndex = MAX_WORD_LENGTH - 1;
            // 12 characters plus a "-" == 13.
        } else {
            // otherwise we want to split near the middle.
            splitIndex = Math.round(thisWord.length() / 2F);
        }
        // in case we found a split character that was > MAX_WORD_LENGTH characters in.
        if (splitIndex > MAX_WORD_LENGTH) {
            // If we split the word at a splitting char like "-" or ".", we added one to the splitIndex
            // in order to ensure the splitting char appears at the head of the split. Not accounting
            // for this in the recursive call will cause a StackOverflowException
            return findSplitIndex(thisWord.substring(0,
                    wordContainsSplittingCharacter(thisWord) ? splitIndex - 1 : splitIndex));
        }
        if (VERBOSE) {
            Log.i(TAG, "Splitting long word " + thisWord + " into " + thisWord.substring(0, splitIndex) +
                    " and " + thisWord.substring(splitIndex));
        }
        return splitIndex;
    }

    private boolean wordContainsSplittingCharacter(String word) {
        return (word.contains(".") || word.contains("-"));
    }


    private void printLastWord() {
        if (mWordArray != null) {
            printWord(mWordArray[mWordArray.length - 1]);
        }
    }


    private void printWord(String word) {
        int startSpan = 0;
        int endSpan = 0;
        word = word.trim();
        if (VERBOSE) Log.i(TAG + word.length(), word);
        if (word.length() == 1) {
            StringBuilder builder = new StringBuilder();
            for (int x = 0; x < CHARS_LEFT_OF_PIVOT; x++) {
                builder.append(" ");
            }
            builder.append(word);
            word = builder.toString();
            startSpan = CHARS_LEFT_OF_PIVOT;
            endSpan = startSpan + 1;
        } else if (word.length() <= CHARS_LEFT_OF_PIVOT * 2) {
            StringBuilder builder = new StringBuilder();
            int halfPoint = word.length() / 2;
            int beginPad = CHARS_LEFT_OF_PIVOT - halfPoint;
            for (int x = 0; x <= beginPad; x++) {
                builder.append(" ");
            }
            builder.append(word);
            word = builder.toString();
            startSpan = halfPoint + beginPad;
            endSpan = startSpan + 1;
            if (VERBOSE) Log.i(TAG + word.length(), "pivot: " + word.substring(startSpan, endSpan));
        } else {
            startSpan = CHARS_LEFT_OF_PIVOT;
            endSpan = startSpan + 1;
        }

        Spannable spanRange = new SpannableString(word);
        TextAppearanceSpan tas = new TextAppearanceSpan(mTarget.getContext(), R.style.PivotLetter);
        spanRange.setSpan(tas, startSpan, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTarget.setText(spanRange);
    }

    public void pause() {
        mPlayingRequested = false;
    }

    public boolean isPlaying() {
        return mPlaying;
    }

    /**
     * Begin the background timer thread
     */
    private void startTimerThread() {
        synchronized (mPlayingSync) {
            if (!mSpritzThreadStarted) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (VERBOSE) {
                            Log.i(TAG, "Starting spritzThread with queue length " + mWordQueue.size());
                        }
                        mPlaying = true;
                        mSpritzThreadStarted = true;
                        while (mPlayingRequested) {
                            try {
                                processNextWord();
                                if (mWordQueue.isEmpty()) {
                                    if (VERBOSE) {
                                        Log.i(TAG, "Queue is empty after processNextWord. Pausing");
                                    }
                                    mTarget.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (mOnCompletionListener != null) {
                                                mOnCompletionListener.onComplete();
                                            }
                                        }
                                    });
                                    mPlayingRequested = false;

                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }


                        if (VERBOSE)
                            Log.i(TAG, "Stopping spritzThread");
                        mPlaying = false;
                        mSpritzThreadStarted = false;

                    }
                }).start();
            }
        }
    }


    public String[] getWordArray() {
        return mWordArray;
    }

    public ArrayDeque<String> getWordQueue() {
        return mWordQueue;
    }

    public void attachProgressBar(ProgressBar bar) {
        if (bar != null) {
            mProgressBar = bar;
        }
    }



    public void setDelayStrategy(DelayStrategy strategy) {
        mDelayStrategy = strategy;

    }

    protected static class SpritzHandler extends Handler {
        private WeakReference<Spritzer> mWeakSpritzer;

        public SpritzHandler(Spritzer muxer) {
            mWeakSpritzer = new WeakReference<Spritzer>(muxer);
        }

        @Override
        public void handleMessage(Message inputMessage) {
            int what = inputMessage.what;
            Object obj = inputMessage.obj;

            Spritzer spritzer = mWeakSpritzer.get();
            if (spritzer == null) {
                return;
            }

            switch (what) {
                case MSG_PRINT_WORD:
                    spritzer.printWord((String) obj);
                    break;
                default:
                    throw new RuntimeException("Unexpected msg what=" + what);
            }
        }

    }


}