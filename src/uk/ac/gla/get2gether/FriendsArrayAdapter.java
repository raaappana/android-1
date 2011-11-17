/*
 * FriendsArrayAdapter.java
 * 
 * Author: C. Enrique Ortiz | http://cenriqueortiz.com
 * Copyright 2010 C. Enrique Ortiz.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.gla.get2gether;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * ListView Friends ArrayAdapter
 */
public class FriendsArrayAdapter extends ArrayAdapter<Friend> {
    private final Activity context;
    private final ArrayList<Friend> friends;
    private int resourceId;

    /**
     * Constructor
     * @param context the application content
     * @param resourceId the ID of the resource/view
     * @param friends the bound ArrayList
     */
    public FriendsArrayAdapter(
            Activity context, 
            int resourceId, 
            ArrayList<Friend> friends) {
        super(context, resourceId, friends);
        this.context = context;
        this.friends = friends;
        this.resourceId = resourceId;
    }

    /**
     * Updates the view
     * @param position the ArrayList position to update
     * @param convertView the view to update/inflate if needed
     * @param parent the groups parent view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = vi.inflate(resourceId, null);
        }
        Friend f = friends.get(position);
        TextView rowTxt = (TextView) rowView.findViewById(R.id.rowtext_top);
        rowTxt.setText(f.name);
        return rowView;
    }

}   

