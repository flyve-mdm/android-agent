package org.flyve.mdm.agent.utils;

import android.content.Context;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.flyve.mdm.agent.R;

import java.util.ArrayList;
import java.util.List;

/*
 *   Copyright © 2017 Teclib. All rights reserved.
 *
 *   This file is part of flyve-mdm-android-agent
 *
 * flyve-mdm-android-agent is a subproject of Flyve MDM. Flyve MDM is a mobile
 * device management software.
 *
 * Flyve MDM is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * Flyve MDM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * ------------------------------------------------------------------------------
 * @author    Rafael Hernandez
 * @date      8/8/17
 * @copyright Copyright © 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
public class MultipleEditText {

    private List<EditText> editList = new ArrayList<>();
    private List<Spinner> spinnList = new ArrayList<>();
    private int index = 0;
    private Context context;
    private ViewGroup container;
    private String hint;
    private int inputType;
    private int limit = 0;
    private ArrayAdapter<CharSequence> adapter = null;

    public MultipleEditText(Context context, ViewGroup container, String hint) {
        this.context = context;
        this.container = container;
        this.hint = hint;
        this.inputType = InputType.TYPE_CLASS_TEXT;
    }

    public void setLimit(int size) {
        this.limit = size;
    }

    public void setInputType(int type) {
        inputType = type;
    }

    public void setSpinnerArray(int array) {
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = ArrayAdapter.createFromResource(context,
                array, android.R.layout.simple_spinner_item);
    }

    public List<EditText> getEditList() {
        return editList;
    }

    public List<Spinner> getSpinnList() {
        return spinnList;
    }

    public LinearLayout createEditText() {
        int id = ++index;

        // if limit is mayor of id return null
        if(limit < id && limit > 0) {
            return new LinearLayout(context);
        }

        final LinearLayout llv = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,0,0,0);
        llv.setLayoutParams(params);
        llv.setOrientation(LinearLayout.VERTICAL);

        // -------------------
        // LinearLayout HORIZONTAL
        // -------------------
        LinearLayout ll = new LinearLayout(context);
        ll.setLayoutParams(params);
        ll.setOrientation(LinearLayout.HORIZONTAL);

        // -------------------
        // Email Layout
        // -------------------
        LinearLayout.LayoutParams paramsEdit = new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);

        // -------------------
        // Email EditText
        // -------------------
        final EditText editText = new EditText(context);
        editText.setId(id);
        editText.setLayoutParams(paramsEdit);
        editText.setHint(hint);
        editText.setTag("");
        editText.setInputType(inputType);
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(!"used".equalsIgnoreCase(v.getTag().toString())) {
                    v.setTag("used");
                    container.addView(createEditText());
                }
                return false;
            }
        });
        ll.addView(editText);

        // -------------------
        // Clear Button
        // -------------------
        LinearLayout.LayoutParams paramsImg = new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsImg.gravity= Gravity.CENTER;

        ImageView imgDelete = new ImageView(context);
        imgDelete.setId(id);
        imgDelete.setLayoutParams(paramsImg);
        imgDelete.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_clear));
        imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editList.size()>=2) {
                    container.removeView(llv);
                    editList.remove(editText);
                } else {
                    editText.setText("");
                    editText.setTag("");
                    index = 0;
                }
            }
        });
        ll.addView(imgDelete);

        // -------------------
        // Add email to list
        // -------------------
        editList.add(editText);

        llv.addView(ll);

        // -----------------
        // Add spinner
        // -----------------
        if(adapter != null) {
            Spinner spinner = new Spinner(context);

            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // Apply the adapter to the spinner
            spinner.setAdapter(adapter);
            spinnList.add(spinner);

            llv.addView(spinner);
        }

        return llv;
    }

}
