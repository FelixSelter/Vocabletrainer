package com.rubengees.vocables.core.test;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.rubengees.vocables.R;
import com.rubengees.vocables.activity.ExtendedToolbarActivity;
import com.rubengees.vocables.core.test.logic.ClassicTestLogic;
import com.rubengees.vocables.core.test.logic.TestLogic;
import com.rubengees.vocables.core.testsettings.ClassicTestSettings;
import com.rubengees.vocables.core.testsettings.TestSettings;
import com.rubengees.vocables.pojo.Meaning;
import com.rubengees.vocables.utils.AnimationUtils;
import com.rubengees.vocables.utils.Utils;

/**
 * Created by Ruben on 01.06.2015.
 */
public class ClassicTest extends Test implements ExtendedToolbarActivity.OnFabClickListener {

    private ClassicTestLogic logic;

    private EditText input;
    private TextView status;

    private boolean animating = false;

    public ClassicTest(Context context, TestSettings settings, OnTestFinishedListener listener, int color, int darkColor, Bundle savedInstanceState) {
        super(context, settings, listener, color, darkColor, savedInstanceState);
    }

    public ClassicTest(Context context, TestSettings settings, OnTestFinishedListener listener, int color, int darkColor) {
        super(context, settings, listener, color, darkColor);

        logic = new ClassicTestLogic(context, (ClassicTestSettings) settings);
    }

    @Override
    protected TestLogic getLogic() {
        return logic;
    }

    @Override
    public View getLayout() {
        View root = View.inflate(getContext(), R.layout.fragment_test_classic, null);
        View header = View.inflate(getContext(), R.layout.header, null);

        input = (EditText) root.findViewById(R.id.fragment_test_classic_input);
        status = (TextView) header.findViewById(R.id.header_text);

        input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    processInput();
                    return true;
                }
                return false;
            }
        });

        getToolbarActivity().expandToolbar();
        getToolbarActivity().setToolbarView(header);
        getToolbarActivity().enableFab(R.drawable.ic_next, this);

        return root;
    }

    private void processInput() {
        if (!animating) {
            String text = input.getText().toString().trim();

            if (text.length() <= 0) {
                text = null;
            }

            Meaning result = logic.processAnswer(text);

            if (shouldAnimate()) {
                showResult(result);
            } else {
                next();
            }
        }
    }

    private void showResult(Meaning result) {
        Spannable text;
        animating = true;

        if (result == null) {
            text = new SpannableString("Correct!");
        } else {
            String resultText = "Wrong! Correct:" + " ";
            text = new SpannableString(resultText + result.toString());

            text.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), resultText.length() - 1, text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        status.setText(text);
        AnimationUtils.animate(status, Techniques.FlipInX, 300, 0, new AnimationUtils.AnimationEndListener() {
            @Override
            public void onAnimationEnd() {
                Utils.wait(getToolbarActivity(), 800, new Utils.OnWaitFinishedListener() {
                    @Override
                    public void onWaitFinished() {
                        next();
                        AnimationUtils.animate(status, Techniques.FlipInX, 300, 0, new AnimationUtils.AnimationEndListener() {
                            @Override
                            public void onAnimationEnd() {
                                animating = false;
                            }
                        });
                    }
                });
            }
        });

    }

    @Override
    protected void restoreSavedInstanceState(Bundle savedInstanceState) {
        super.restoreSavedInstanceState(savedInstanceState);

        logic = new ClassicTestLogic(getContext(), savedInstanceState);
    }

    @Override
    public void show() {
        super.show();
        status.setText("What means" + " '" + logic.getQuestion().toString() + "'?");
        input.getText().clear();
    }

    @Override
    public void onFabClick() {
        processInput();
    }

    private void next() {
        if (logic.next()) {
            show();
        } else {
            finishTest(logic.getResult(), logic.getVocables());
        }
    }
}