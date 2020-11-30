package ca.ualberta.cmput301f20t04.bookatmenow;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

/**
 * Search books dialog.
 */
public class SortDialog extends AppCompatDialogFragment {
    private final MainActivity main;

    SortDialog(MainActivity main) {
        this.main = main;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.sort_dialog, null);

        final Spinner spinner = view.findViewById(R.id.sort_by);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.sort_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setSelection(main.sortOption.toInt());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder
                .setView(view)
                .setTitle("Sort Books")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BookAdapter.CompareBookBy.SortOption sortOption = BookAdapter.CompareBookBy.SortOption
                                .valueOf(spinner.getSelectedItem().toString().toUpperCase());
                        main.allBooksAdapter.sort(sortOption);
                        main.sortOption = sortOption;
                    }
                })
                .create();
    }
}
