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
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import static android.nfc.NfcAdapter.FLAG_READER_NFC_A;
import static android.nfc.NfcAdapter.FLAG_READER_NFC_B;
import static android.nfc.NfcAdapter.FLAG_READER_NFC_BARCODE;
import static android.nfc.NfcAdapter.FLAG_READER_NFC_F;
import static android.nfc.NfcAdapter.FLAG_READER_NFC_V;

@SuppressWarnings("SwitchStatementWithTooFewBranches")
public class EnrollActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private static final String TAG = "EnrollActivity";

    private NfcAdapter mNfcAdapter;
    private InputMethodManager mImManager;

    private CardsDbHelper mDbHelper;

    private MenuItem mDoneMenu;

    private View mHelpView;
    private View mShowView;
    private ViewGroup mColorsView;

    private CardView mCardView;
    private EditText mNameView;

    private byte[] mUid = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mImManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        mDbHelper = new CardsDbHelper(this);

        mHelpView = findViewById(R.id.help);
        mShowView = findViewById(R.id.show);
        mShowView.setVisibility(View.GONE);

        mCardView = findViewById(R.id.card);
        mNameView = findViewById(R.id.name);

        mColorsView = findViewById(R.id.colors);
        mColorsView.setVisibility(View.GONE);

        for (int i = 0; i < mColorsView.getChildCount(); i++) {
            final ImageButton button = (ImageButton) mColorsView.getChildAt(i);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.enroll, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mDoneMenu = menu.findItem(R.id.done);
        mDoneMenu.setVisible(mUid != null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.done:
                enroll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mUid == null) {
            mNfcAdapter.enableReaderMode(this, this,
                    FLAG_READER_NFC_A | FLAG_READER_NFC_B | FLAG_READER_NFC_F |
                            FLAG_READER_NFC_V | FLAG_READER_NFC_BARCODE,
                    null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mUid == null) {
            mNfcAdapter.disableReaderMode(this);
        }
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        runOnUiThread(() -> {
            mUid = tag.getId();

            mNfcAdapter.disableReaderMode(this);

            mDoneMenu.setVisible(true);
            mHelpView.setVisibility(View.GONE);
            mShowView.setVisibility(View.VISIBLE);
            mColorsView.setVisibility(View.VISIBLE);

            if (mNameView.requestFocus()) {
                mImManager.showSoftInput(mNameView, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private void enroll() {
        if (mNameView.getText().length() == 0) {
            return;
        }

        final String name = mNameView.getText().toString();
        final int color = mCardView.getCardBackgroundColor().getDefaultColor();

        if (mDbHelper.insert(mUid, name, color, null)) {
            finish();
        }
    }

}
