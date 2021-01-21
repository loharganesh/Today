package app.splitbit.today;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;

import app.splitbit.today.Application.TimeStamp;
import app.splitbit.today.Model.Todo;
import app.splitbit.today.View.TodoAdapter;

public class TodoCalenderSearch extends AppCompatActivity {

    private TextView dateTxt;
    private int mYear,mMonth,mDay;
    private RecyclerView recyclerView_tasks;
    private ArrayList<Todo> arrayList_todos;
    private TodoAdapter todoAdapter;

    //-- Firebase
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private CollectionReference todoRef;
    private int click;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_calender_search);

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


        //-- Firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        todoRef = firestore.collection("user").document(user.getUid()).collection("tasks");

        //-- UI


        recyclerView_tasks = (RecyclerView) findViewById(R.id.recyclerview_tasks);
        arrayList_todos = new ArrayList<>();
        todoAdapter = new TodoAdapter(arrayList_todos,this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,true);
        layoutManager.setStackFromEnd(true);
        recyclerView_tasks.setLayoutManager(layoutManager);
        recyclerView_tasks.setAdapter(todoAdapter);

        dateTxt = (TextView) findViewById(R.id.textView_date);

        dateTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Date
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(TodoCalenderSearch.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                dateTxt.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                String month;
                                if(monthOfYear>9){
                                    int month_int = monthOfYear+1;
                                    month = month_int+"";
                                }else{
                                    int month_int = monthOfYear+1;
                                    month = "0"+month_int;
                                }
                                loadTasks(dayOfMonth + "/" + month + "/" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView_tasks);

    }

    ItemTouchHelper.SimpleCallback itemTouchCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT|ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            if(direction == ItemTouchHelper.RIGHT || direction == ItemTouchHelper.LEFT){
                int position = viewHolder.getAdapterPosition();
                final Todo deletedModel = arrayList_todos.get(position);
                final int deletedPosition = position;
                todoAdapter.removeItem(deletedModel,position);
                click = 0;
                // showing snack bar with Undo option
                final Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), "Task Deleted",7000);
                snackbar.setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // undo is selected, restore the deleted item
                        if(click == 1){

                        }else{
                            todoAdapter.restoreItem(deletedModel, deletedPosition);
                            click++;
                        }

                    }
                });

                TextView snackbarActionTextView = (TextView) snackbar.getView().findViewById(R.id.snackbar_action);
                TextView snackbarMessageTextView = (TextView) snackbar.getView().findViewById(R.id.snackbar_text);
                snackbarActionTextView.setAllCaps(false);
                snackbarMessageTextView.setTextColor(Color.YELLOW);

                snackbar.setActionTextColor(Color.WHITE);
                snackbar.show();

            }
        }

    };

    private void loadTasks(String date){
        todoRef.whereEqualTo("date", date).orderBy("timestamp", Query.Direction.ASCENDING).get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    arrayList_todos.clear();
                    for(DocumentSnapshot snap:task.getResult()){
                        Todo todo = snap.toObject(Todo.class);
                        todo.setKey(snap.getId());

                        if(!arrayList_todos.contains(todo)){
                            arrayList_todos.add(todo);
                        }
                    }
                    todoAdapter.notifyDataSetChanged();
                }
            });
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
