package wang.switchy.hin2n.tool;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by janiszhang on 2018/5/23.
 */

public class N2nTools {
    public static int dp2px(Context context, int dp) {

        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}
