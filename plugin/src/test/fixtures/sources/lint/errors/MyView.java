import android.content.Context;
import android.view.View;

public class MyView extends View {

    public MyView(Context context) {
        super(context);
    }

    @Override
    protected void onDetachedFromWindow() {
        // missing super call
    }
}
