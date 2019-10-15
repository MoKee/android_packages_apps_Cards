/*
 * Copyright (C) 2019 The MoKee Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mokee.cards;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.SharedElementCallback;

import java.util.List;

@SuppressWarnings("SwitchStatementWithTooFewBranches")
public class EditActivity extends AppCompatActivity {

    private static final String TAG = "EditActivity";

    private CardsDbHelper mDbHelper;

    private CardView mCardView;
    private EditText mNameView;

    private Card mCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        final InputMethodManager imManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        mDbHelper = new CardsDbHelper(this);

        mCard = getIntent().getParcelableExtra("card");

        mCardView = findViewById(R.id.card);
        mCardView.setCardBackgroundColor(mCard.color);

        mNameView = findViewById(R.id.name);
        mNameView.setText(mCard.name);

        final ViewGroup colorsView = findViewById(R.id.colors);
        for (int i = 0; i < colorsView.getChildCount(); i++) {
            final ImageButton button = (ImageButton) colorsView.getChildAt(i);
            button.setOnClickListener(v -> {
                final ColorStateList tint = button.getImageTintList();
                if (tint != null) {
                    mCardView.setCardBackgroundColor(tint.getDefaultColor());
                }
            });
        }

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("");

        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements,
                                           List<View> sharedElementSnapshots) {
                mNameView.postDelayed(() -> {
                    if (mNameView.requestFocus() && imManager != null) {
                        imManager.showSoftInput(mNameView, InputMethodManager.SHOW_IMPLICIT);
                        mNameView.selectAll();
                    }
                }, 500);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            case R.id.done:
                save();
                return true;
            case R.id.delete:
                delete();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void save() {
        if (mNameView.getText().length() == 0) {
            return;
        }

        mCard.name = mNameView.getText().toString();
        mCard.color = mCardView.getCardBackgroundColor().getDefaultColor();

        if (mDbHelper.update(mCard)) {
            supportFinishAfterTransition();
        }
    }

    private void delete() {
        new AlertDialog.Builder(this)
                .setTitle(mCard.name)
                .setMessage(R.string.delete_desc)
                .setPositiveButton(R.string.delete_yes, (dialog, which) -> {
                    if (mDbHelper.delete(mCard.id)) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.delete_no, null)
                .show();
    }

}
