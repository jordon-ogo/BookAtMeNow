/**
 * Scan in ISBN using phone camera
 */

package ca.ualberta.cmput301f20t04.bookatmenow;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

/**
 * Scan book ISBN for add/borrow/return.
 */
public class ScanBook extends AppCompatActivity {
    private String bookISBN;
    private String bookName;
    private SurfaceView cameraView;
    private CameraSource cameraSource;
    private static final int PERMISSIONS_REQUEST_ACCESS_CAMERA = 1;
    private TextView scanMessage;
    private TextView isbnText;

    Context context;
    Intent main;

    private DBHandler db;

     // If the USE BARCODE button is clicked.
     // If no ISBN has been scanned, tell user to scan one.  Otherwise put the scanned ISBN in an
     // intent object and finish the activity.
    public void setBookISBN(View view) {
        if (bookISBN == null){
            Toast toast = Toast.makeText(this, "Please scan an ISBN barcode.", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            String passedIsbn = main.getStringExtra(ProgramTags.PASSED_ISBN);

            if (passedIsbn != null) {
                // if book barcode matches scanned barcode
                // allow barcode to be used to check out the book
                if (passedIsbn.equals(bookISBN)) {
                    setResult(Activity.RESULT_OK);
                    this.finish();
                } else {
                    Toast toast = Toast.makeText(this, "Please scan a matching ISBN barcode.", Toast.LENGTH_SHORT);
                    toast.show();
                }
            } else {
                // new book
                Intent returnData = new Intent();
                returnData.putExtra("isbn", bookISBN);
                setResult(Activity.RESULT_OK, returnData);
                this.finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_book);
        cameraView = findViewById(R.id.scanbook_camera_view);
        isbnText = findViewById(R.id.scanbook_isbn);
        scanMessage = findViewById(R.id.scanbook_message);
        scanMessage.setVisibility(View.INVISIBLE);
        cameraView.setVisibility(View.INVISIBLE);
        main = getIntent();
        context = this;


        if(main.getStringExtra(ProgramTags.SCAN_MESSAGE) != null) {
            scanMessage.setVisibility(View.VISIBLE);
            if(main.getStringExtra(ProgramTags.SCAN_MESSAGE).equals("ScanExisting")) {
                String scanBook = "Please scan: ";
                bookName = main.getStringExtra(ProgramTags.PASSED_BOOKNAME);
                SpannableString messageString = new SpannableString(scanBook + bookName);
                messageString.setSpan(new StyleSpan(Typeface.BOLD), 0,
                        scanBook.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                messageString.setSpan(new StyleSpan(Typeface.ITALIC), scanBook.length() - 1,
                        scanBook.length() + bookName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                scanMessage.setText(messageString);
            } else if (main.getStringExtra(ProgramTags.SCAN_MESSAGE).equals("ScanNew")) {
                String scanBook = "Please scan ISBN barcode";
                SpannableString messageString = new SpannableString(scanBook);
                messageString.setSpan(new StyleSpan(Typeface.BOLD), 0,
                        scanBook.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                scanMessage.setText(messageString);
            }


        }

        checkPermissions();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        this.finish();
    }

    private void initialize(){

        //Build barcode detector object. Set it to read 13 digit barcodes.
        BarcodeDetector detector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.EAN_13)
                .build();

        //Build camera source object.
        cameraSource = new CameraSource.Builder(this, detector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setAutoFocusEnabled(true)
                .build();

        cameraView.setVisibility(View.VISIBLE);

        //Get camera permissions and add callback to the SurfaceHolder of the camera preview.
        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                try {
                    cameraSource.start(cameraView.getHolder());
                } catch (SecurityException | IOException e) {
                    e.printStackTrace();
                    Log.e(ProgramTags.GENERAL_ERROR, "Camera permissions error.");
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        //Set the processor to scan barcodes as they appear.
        detector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                //If a barcode exists, set TextView and bookISBN string.
                if (barcodes.size() > 0) {
                    isbnText.post(new Runnable() {
                        @Override
                        public void run() {
                            bookISBN = barcodes.valueAt(0).displayValue;
                            isbnText.setText("ISBN: " + bookISBN);
                        }
                    });
                }
            }
        });
    }

    /**
     * Check the camera permissions.   If permission has been granted.  Call initialize() to set up
     * and start the camera.
     */
    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(ScanBook.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initialize();
        } else {
            ActivityCompat.requestPermissions(ScanBook.this, new
                    String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_ACCESS_CAMERA);
        }
    }

    /**
     * If permissions to the camera has been granted call initialize() and re-initialize the camera
     * source.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == PERMISSIONS_REQUEST_ACCESS_CAMERA) {
            if (ActivityCompat.checkSelfPermission(ScanBook.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                initialize();
                try {
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                Toast.makeText(context, "Camera Permission denied", Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
                this.finish();
            }

        }

    }

}