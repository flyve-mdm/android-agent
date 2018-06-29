/*
 * Copyright Teclib. All rights reserved.
 *
 * Flyve MDM is a mobile device management software.
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
 * @copyright Copyright Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/android-mdm-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.adapter.PoliciesAdapter;
import org.flyve.mdm.agent.room.database.AppDataBase;
import org.flyve.mdm.agent.room.entity.Policies;
import org.flyve.mdm.agent.utils.Helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FragmentPolicies extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_policies, null);

        final ListView lst = v.findViewById(R.id.lst);
        loadData(lst);

        final SwipeRefreshLayout swipeLayout = v.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeLayout.setRefreshing(false);
                loadData(lst);
            }
        });

        return v;
    }

    private void loadData(ListView lst) {

        AppDataBase dataBase = AppDataBase.getAppDatabase(FragmentPolicies.this.getContext());

        List<Policies> arrPolicies = dataBase.PoliciesDao().loadAll();

        ArrayList arr = new ArrayList<HashMap<String, Boolean>>();

        if(arrPolicies.isEmpty()) {
            HashMap<String, String> map = new HashMap<>();
            map.put("description", "0 policies");
            map.put("value", "");
            arr.add(map);
        } else {
            for (int i = 0; i < arrPolicies.size(); i++) {
                HashMap<String, String> map = new HashMap<>();
                map.put("description", Helpers.splitCapitalized(arrPolicies.get(i).policyName));
                map.put("value", String.valueOf(arrPolicies.get(i).value));
                arr.add(map);
            }
        }

        lst.setAdapter( new PoliciesAdapter(FragmentPolicies.this.getActivity(), arr));
    }
}
