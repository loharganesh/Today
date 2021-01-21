package app.splitbit.today;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import app.splitbit.today.Application.TimeStamp;
import app.splitbit.today.Model.Todo;
import app.splitbit.today.View.TodoAdapter;

public class MainActivity extends AppCompatActivity{

    //-- Firebase
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private CollectionReference todoRef;

    //-- UI
    private Dialog dialog;
    private RecyclerView recyclerView_tasks;
    private ArrayList<Todo> arrayList_todos;
    private TodoAdapter todoAdapter;

    //-- Google Sign in client
    private GoogleSignInOptions googleSignInOptions;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private int click;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //--
        //SETTING TOOLBAR
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Tasks");
        //set toolbar appearance
        //for crate home button
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        //-- Firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        todoRef = firestore.collection("user").document(user.getUid()).collection("tasks");

        //-- UI


        recyclerView_tasks = (RecyclerView) findViewById(R.id.recyclerView_tasks);
        arrayList_todos = new ArrayList<>();
        todoAdapter = new TodoAdapter(arrayList_todos,this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,true);
        layoutManager.setStackFromEnd(true);
        recyclerView_tasks.setLayoutManager(layoutManager);
        recyclerView_tasks.setAdapter(todoAdapter);

        loadTasks();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView_tasks);
    }

    public void addTodo(View view){
        startActivity(new Intent(this,CreateTodo.class));
    }
    public void settings(View view){
        startActivity(new Intent(this,Settings.class));
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

    private void loadTasks(){
        todoRef.whereEqualTo("date",TimeStamp.getDate(TimeStamp.getTimestamp())).orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(!queryDocumentSnapshots.isEmpty()){
                    for(DocumentChange doc:queryDocumentSnapshots.getDocumentChanges()){
                        Todo todo = doc.getDocument().toObject(Todo.class);
                        todo.setKey(doc.getDocument().getId());
                        int pos = arrayList_todos.indexOf(todo);
                        switch (doc.getType()){
                            case ADDED:
                                if(!arrayList_todos.contains(todo) && todo.getDate().equals(TimeStamp.getDate(TimeStamp.getTimestamp()))){
                                    arrayList_todos.add(arrayList_todos.size(),todo);
                                    todoAdapter.notifyItemInserted(arrayList_todos.indexOf(todo));
                                    recyclerView_tasks.scrollToPosition(arrayList_todos.indexOf(todo));
                                }
                                Log.d(" ADDED ",todo.getTaskname());
                                break;
                            case MODIFIED:
                                if(arrayList_todos.contains(todo)){
                                    arrayList_todos.get(pos).setDone(todo.isDone());
                                    todoAdapter.notifyItemChanged(pos);
                                }
                                Log.d(" MODIFIED ",todo.getTaskname());
                                break;
                            case REMOVED:
                                Log.d(" REMOVED ",todo.getTaskname());
                                break;
                        }
                    }
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.main_menu_settings:
                startActivity(new Intent(this,Settings.class));
                return true;

            case R.id.main_menu_calender_search:
                startActivity(new Intent(this,TodoCalenderSearch.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
