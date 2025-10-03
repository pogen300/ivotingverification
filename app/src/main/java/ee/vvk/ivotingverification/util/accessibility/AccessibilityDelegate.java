package ee.vvk.ivotingverification.util.accessibility;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ScrollView;

import androidx.annotation.NonNull;

import java.util.Locale;

import ee.vvk.ivotingverification.util.C;

public class AccessibilityDelegate {
    /*
    Main window and error window both use a scrollView to fit the message. We allow scrollView to
    be focusable only if the text does not fit in the scrollView frame (scrolling is required).
     */
    public void handleScrollViewFocus(Context context, ScrollView scrollView, View view) {
        AccessibilityManager accessibilityManager =
                (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        // Create a green rectangle as scrollView background in focused state
        GradientDrawable focusedScrollViewBackground = new GradientDrawable();
        focusedScrollViewBackground.setShape(GradientDrawable.RECTANGLE);
        focusedScrollViewBackground.setColor(Color.TRANSPARENT);
        focusedScrollViewBackground.setStroke(5, Color.GREEN);
        scrollView.setOnFocusChangeListener((_view, isFocused) -> {
            if (isFocused) {
                scrollView.setBackground(focusedScrollViewBackground);
            } else {
                scrollView.setBackgroundColor(Color.TRANSPARENT);
            }
        });
        view.post(() -> {
            boolean scrollableText = scrollView.getHeight() < view.getHeight();
            if (scrollableText) {
                scrollView.setFocusable(View.FOCUSABLE);
                scrollView.setFocusableInTouchMode(true);
            }  else {
                scrollView.setFocusable(View.NOT_FOCUSABLE);
                scrollView.setFocusableInTouchMode(false);
                if (accessibilityManager.isEnabled()) {
                    view.setFocusable(View.FOCUSABLE);
                    view.setFocusableInTouchMode(true);
                }
            }
        });
    }

    public void informUserToStartScanning(Context context) {
        AccessibilityManager accessibilityManager =
                (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);

        if (accessibilityManager.isEnabled()) { // Send an accessibility event when a user uses an accessibility service
            AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT);
            event.getText().add(C.qrViewOpened);
            accessibilityManager.sendAccessibilityEvent(event);
        }
    }

    // Set accessibility focus to a specific view instead of letting screen reader focus the first view in layout order
    public void setAccessibilityFocusOnStart(View defaultView, View customView) {
        // Temporarily disable accessibility focus on defaultView to prevent the screen reader to focus it by default
        defaultView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        // Delay is used to give time to the system to finish its initial view
        customView.postDelayed(() -> {
            customView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            defaultView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        }, 1000);
    }

    public void informAboutTimeLimit(Context context, String message) {
        AccessibilityManager accessibilityManager =
                (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);

        if (accessibilityManager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT);
            event.getText().add(message);
            accessibilityManager.sendAccessibilityEvent(event);
        }
    }

    public void textToLowercase(View view, String text) {
        // View text is set to lowercase, to prevent the screen reader to read all-uppercase words out letter by letter
        view.setAccessibilityDelegate(accessibilityDelegate(text.toLowerCase(Locale.ROOT)));
    }

    private View.AccessibilityDelegate accessibilityDelegate(final String text) {
        return new View.AccessibilityDelegate() {
            @Override
            public void onInitializeAccessibilityNodeInfo(@NonNull View host, @NonNull AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                if (text != null) {
                    info.setText(text);
                }
            }
        };
    }
}
