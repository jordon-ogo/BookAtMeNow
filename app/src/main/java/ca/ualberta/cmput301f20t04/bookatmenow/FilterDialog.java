package ca.ualberta.cmput301f20t04.bookatmenow;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Filter dialog for books according to status.
 */
public class FilterDialog extends AppCompatDialogFragment {
    private MainActivity main;

    FilterDialog(MainActivity main) {
        this.main = main;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String[] filterItems = {FireStoreMapping.BOOK_STATUS_AVAILABLE,
                FireStoreMapping.BOOK_STATUS_UNAVAILABLE,
                FireStoreMapping.BOOK_STATUS_BORROWED,
                FireStoreMapping.BOOK_STATUS_REQUESTED};
        final boolean[] checkedItems = {false, false, false, false};

        for (int j = 0; j < filterItems.length; j++) {
            checkedItems[j] = main.filterTerms.contains(filterItems[j]);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder
                .setTitle("Filter Books")
                .setMultiChoiceItems(filterItems, checkedItems,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                checkedItems[i] = b;
                            }
                        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        main.filterTerms.clear();
                        for (int k = 0; k < filterItems.length; k++) {
                            if (checkedItems[k]) main.filterTerms.add(filterItems[k]);
                        }
                        main.filterUpdate();
                    }
                })
                .create();
    }
}