package gr.ntua.metal.gaiopepper;

import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;

import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayPosition;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.aldebaran.qi.sdk.object.locale.Language;
import com.aldebaran.qi.sdk.object.locale.Region;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnClickListener {
    private static final String TAG = "Main Activity";

    InputMethodManager imm;


    private QiContext qiContext;

    private FloatingActionButton fabSettings;
    private FloatingActionButton fabEncyclopedia;
    private TextInputLayout textInputLayout;
    private TextInputEditText textInputEditText;

    //private final Locale locale_greek = new Locale(Language.GREEK, Region.GREECE);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);
        this.setContentView(R.layout.activity_main);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);


        /// Set Default Variables
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        findViews();

    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Log.i(TAG, "onRobotFocusGained");
        this.qiContext = qiContext;
        addListeners();

        QiChatbot chatbot =  buildQiChatbot();
        Chat chat = buildChat(chatbot);

        Future<Void> chatFuture = chat.async().run();
        chatFuture.thenConsume(future -> {
            if (future.hasError()){
                Log.e(TAG, "Chat finished with error: " + future.getErrorMessage());
            }
        });







    }

    @Override
    public void onRobotFocusLost() {
        Log.i(TAG, "onRobotFocusLost");
        this.qiContext = null;
        removeListeners();
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.i(TAG, "onRobotFocusRefused: " + reason);

        // The robot focus is refused.
    }

    @Override
    public void onClick(View view) {
        int viewID = view.getId();
        if (viewID == R.id.fab_settings) {
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        } else if (viewID == R.id.fab_encyclopedia) {
            Intent encyclopediaIntent = new Intent(MainActivity.this, EncyclopediaActivity.class);
            startActivity(encyclopediaIntent);
        } else if (viewID == R.id.editTextHumanInput) {
            Log.i(TAG, "TOUCH inside");
            textInputLayout.clearFocus();
            hideSoftKeyboard(textInputLayout);
        }
    }

    private void findViews() {
        try {
            fabSettings = findViewById(R.id.fab_settings);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            fabEncyclopedia = findViewById(R.id.fab_encyclopedia);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            textInputLayout = findViewById(R.id.editTextLayout);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            textInputEditText = (TextInputEditText) textInputLayout.getEditText();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addListeners() {
        fabSettings.setOnClickListener(this);
        fabEncyclopedia.setOnClickListener(this);
        textInputEditText.setOnClickListener(this);
    }

    private void removeListeners() {

    }

    private QiChatbot buildQiChatbot() {
        Topic lexicon = TopicBuilder
                .with(qiContext)
                .withResource(R.raw.lexicon)
                .build();
        Topic topic_minerals = TopicBuilder
                .with(qiContext)
                .withResource(R.raw.topic_minerals)
                .build();


        List<Topic> topicList = new LinkedList<Topic>();
        topicList.add(lexicon);
        topicList.add(topic_minerals);

        QiChatbot chatbot = QiChatbotBuilder
                .with(qiContext)
                .withTopics(topicList)
                //.withLocale(locale_greek)
                .build();

        chatbot.addOnBookmarkReachedListener(bookmark -> {
            Log.i(TAG, "Bookmark " + bookmark.getName() + " reached.");

        });
        chatbot.addOnEndedListener(endReason -> {
            Log.i(TAG, "Chatbot ended for reason: " + endReason);

        });

        return chatbot;
    }

    private Chat buildChat(QiChatbot chatbot) {
        Chat chat = ChatBuilder
                .with(qiContext)
                //.withLocale(locale_greek)
                .withChatbot(chatbot)
                .build();

        chat.addOnStartedListener(() -> {
            Log.i(TAG, "Chat started.");

        });
        chat.addOnListeningChangedListener(listening -> {
            Log.i(TAG, "Listening changed.");

        });
        chat.addOnHeardListener(heardPhrase -> {
            Log.i(TAG, "Heard something.");

        });
        return chat;
    }

    public void showSoftKeyboard(View view){
        if(view.requestFocus()){
            imm.showSoftInput(view,InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void hideSoftKeyboard(View view){
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


}