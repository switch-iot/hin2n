package wang.switchy.hin2n.template;

import android.content.Context;
import android.view.View;

/**
 * Created by janiszhang on 2018/4/13.
 */

public abstract class BaseTemplate {
    protected Context mContext;

    public BaseTemplate(Context context) {
        this.mContext = context;
    }

    /**
     *  添加主体视图部分
     * @param contentView
     */
    public abstract void setContentView(View contentView);


    /**
     * 获取模版产生的view
     * @return
     */
    public abstract View getPageView();

}
