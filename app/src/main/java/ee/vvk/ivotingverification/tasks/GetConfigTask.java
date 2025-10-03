package ee.vvk.ivotingverification.tasks;

import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.HttpRequest;
import ee.vvk.ivotingverification.util.Util;

public class GetConfigTask extends BaseTask<ConfigTaskResult> {

    private static final String TAG = GetConfigTask.class.getSimpleName();

    private final AsyncTaskActivity<ConfigTaskResult> activity;
    private final String url;

    public GetConfigTask(AsyncTaskActivity<ConfigTaskResult> activity, String url) {
        this.activity = activity;
        this.url = url;
    }

	@Override
    public void setUiForLoading() {
        activity.onPreExecute();
    }

    @Override
    public ConfigTaskResult call() {
        try {
            String config = new HttpRequest().get(url);
            return new ConfigTaskResult(config, null);
        } catch (Exception e) {
            Util.logException(TAG, e);
            return new ConfigTaskResult(null, C.getConfigMessage);
        }
    }

    @Override
    public void setDataAfterLoading(ConfigTaskResult result) {
        activity.onPostExecute(result);
    }

}

