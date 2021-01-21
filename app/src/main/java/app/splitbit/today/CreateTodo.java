package app.splitbit.today;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import app.splitbit.today.Application.TimeStamp;
import app.splitbit.today.Helper.ImageHelper;

public class CreateTodo extends AppCompatActivity {

    private EditText editText_taskname;
    private Button button_addTask;
    private TextView dateTxt;
    private ImageView imageView_taskImage;

    private int mYear,mMonth,mDay;
    private String date = TimeStamp.getDate(TimeStamp.getTimestamp());

    private StorageReference storageReference,ref;

    private Uri imageUri;
    private Bitmap bitmap;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_todo);

        //--
        //SETTING TOOLBAR
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("");
        //set toolbar appearance
        //for crate home button
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        storageReference = FirebaseStorage.getInstance().getReference("images");

        dialog = new ProgressDialog(CreateTodo.this);
        dialog.setMessage("Setting up task");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        editText_taskname = (EditText) findViewById(R.id.editText_taskname);
        button_addTask = (Button) findViewById(R.id.button_addtask);
        dateTxt = (TextView) findViewById(R.id.dateTxt);
        imageView_taskImage = (ImageView) findViewById(R.id.imageview_select_image);

        button_addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeTask();
            }
        });

        imageView_taskImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,1);
            }
        });

    }



    private void writeTask(){

        if(!TextUtils.isEmpty(editText_taskname.getText().toString().trim())){
            editText_taskname.setEnabled(false);
            button_addTask.setEnabled(false);
            dialog.show();
            if(imageUri!=null){
                final String name = System.currentTimeMillis()+"";
                ref = storageReference.child(name);

                ref.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageReference.child(name).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    updateDB(uri.toString());
                                    Log.d("Uri",uri.toString());
                                }
                            });
                        }
                    });
            }else {
                updateDB("notavailable");
            }
        }

    }

    private void updateDB(String image){
        Map<String,Object> task = new HashMap<>();
        task.put("taskname",editText_taskname.getText().toString().trim());
        task.put("timestamp", TimeStamp.getTimestamp());
        task.put("date",date);
        task.put("time","");
        task.put("done",false);
        task.put("image",image);

        FirebaseFirestore.getInstance()
                .collection("user")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("tasks").add(task)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        editText_taskname.setText("");
                        editText_taskname.setEnabled(true);
                        button_addTask.setEnabled(true);
                        onBackPressed();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                editText_taskname.setEnabled(true);
                button_addTask.setEnabled(true);
                dialog.dismiss();
            }
        });
    }

    public void setDate(View view){
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(CreateTodo.this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        String month;
                        if(monthOfYear>9){
                            int month_int = monthOfYear+1;
                            month = month_int+"";
                        }else{
                            int month_int = monthOfYear+1;
                            month = "0"+month_int;
                        }

                        dateTxt.setText(dayOfMonth + "/" + month + "/" + year);
                        date  = dayOfMonth + "/" + month + "/" + year;


                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data!=null && data.getData() != null){
            imageUri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                bitmap = ImageHelper.getResizedBitmap(bitmap,500);
                imageView_taskImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private String getImageExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
