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

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorInt;

class Card implements Parcelable {

    public static final Creator<Card> CREATOR = new Creator<Card>() {
        @Override
        public Card createFromParcel(Parcel in) {
            return new Card(in);
        }

        @Override
        public Card[] newArray(int size) {
            return new Card[size];
        }
    };

    int id;

    byte[] uid;

    String name;

    @ColorInt
    int color;

    byte[] texture;

    Card() {
    }

    @SuppressWarnings("WeakerAccess")
    protected Card(Parcel in) {
        id = in.readInt();
        uid = in.createByteArray();
        name = in.readString();
        color = in.readInt();
        texture = in.createByteArray();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeByteArray(uid);
        dest.writeString(name);
        dest.writeInt(color);
        dest.writeByteArray(texture);
    }

}
