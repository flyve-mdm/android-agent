package org.flyve.mdm.agent.policies;

import android.content.Context;

import org.flyve.mdm.agent.utils.FlyveLog;
import org.json.JSONObject;

public class RemoveFilePolicy extends BasePolicies {
    public static final String POLICY_NAME = "removeFile";

    public RemoveFilePolicy(Context context) {
        super(context, POLICY_NAME);
    }

    @Override
    protected boolean process() {
        try {
            JSONObject jsonObj = new JSONObject(message);

            if(jsonObj.has(POLICY_NAME)) {
                String removeFile = jsonObj.getString(POLICY_NAME);
                String taskId = jsonObj.getString("taskId");

                // execute the policy
                PoliciesController policiesController = new PoliciesController(context);
                policiesController.removeFile(taskId, removeFile, context);
            }
            return true;
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", process", ex.getMessage());
            return false;
        }
    }
}