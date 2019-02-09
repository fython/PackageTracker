package info.papdt.express.helper.support

import android.app.Activity
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import info.papdt.express.helper.R

object SnackbarUtils {

    fun make(view: View, @StringRes textRes: Int, duration: Int = Snackbar.LENGTH_SHORT): Snackbar {
        return Snackbar.make(view, view.resources.getString(textRes), duration)
    }

    fun make(view: View, text: CharSequence, duration: Int = Snackbar.LENGTH_SHORT): Snackbar {
        return Snackbar.make(view, text, duration)
    }

    fun makeInCoordinator(
            activity: Activity,
            @StringRes textRes: Int,
            duration: Int = Snackbar.LENGTH_SHORT): Snackbar {
        return SnackbarUtils.make(activity.findViewById(R.id.coordinator_layout),
                textRes, duration)
    }

    fun makeInCoordinator(
            fragment: Fragment,
            @StringRes textRes: Int,
            duration: Int = Snackbar.LENGTH_SHORT): Snackbar {
        return SnackbarUtils.make(fragment.view!!.findViewById(R.id.coordinator_layout),
                textRes, duration)
    }

    fun makeInCoordinator(
            activity: Activity,
            text: CharSequence,
            duration: Int = Snackbar.LENGTH_SHORT): Snackbar {
        return SnackbarUtils.make(activity.findViewById(R.id.coordinator_layout), text, duration)
    }

    fun makeInCoordinator(
            fragment: Fragment,
            text: CharSequence,
            duration: Int = Snackbar.LENGTH_SHORT): Snackbar {
        return SnackbarUtils.make(fragment.view!!.findViewById(R.id.coordinator_layout), text, duration)
    }

}