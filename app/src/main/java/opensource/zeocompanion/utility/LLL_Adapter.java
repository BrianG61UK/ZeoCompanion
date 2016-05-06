package opensource.zeocompanion.utility;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import java.util.List;

// Stub adaptor for the ListLinearLayout
public class LLL_Adapter<T> extends ArrayAdapter {

    // constructor
    public LLL_Adapter(Context context, int layoutResourceId, List<T> objects) {
        super(context, layoutResourceId, objects);
    }

    // will be overrided in the implementation class
    public boolean getShouldBeVisible(int position, View view) {
        return true;
    }

    // will be overrided in the implementation class
    public void destroyView(View view) {}
}
