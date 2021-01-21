package app.splitbit.today.View;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import app.splitbit.today.Model.Todo;
import app.splitbit.today.R;

public class TodoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private ArrayList<Todo> arraylist_todos;
    private Context context;

    //-- Firebase
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    public TodoAdapter(ArrayList<Todo> arraylist_todos, Context context) {
        this.arraylist_todos = arraylist_todos;
        this.context = context;
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        //-- UI
        private CheckBox radioButtonTask;
        private ImageView imageView_task;

        public TaskViewHolder(View v) {
            super(v);
            radioButtonTask = v.findViewById(R.id.radiobutton_taskandstatus);
            imageView_task = v.findViewById(R.id.imageView_task);
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_task, parent, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        final Todo todo = arraylist_todos.get(position);

        ((TaskViewHolder)holder).radioButtonTask.setText(todo.getTaskname());

        if(todo.isDone()){
            ((TaskViewHolder)holder).radioButtonTask.setChecked(true);
            ((TaskViewHolder)holder).radioButtonTask.setTextColor(context.getResources().getColor(R.color.textColorSecondary));
        }else{
            ((TaskViewHolder)holder).radioButtonTask.setChecked(false);
            ((TaskViewHolder)holder).radioButtonTask.setTextColor(context.getResources().getColor(R.color.textColorPrimary));
        }

        if(todo.getImage().equals("notavailable")){

        }else{
            try{
                Picasso.get().load(todo.getImage()).into(((TaskViewHolder)holder).imageView_task);
            }catch (Exception e){
                FirebaseFirestore.getInstance().collection("user").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .collection("tasks").document(todo.getKey()).delete();
            }

        }

        ((TaskViewHolder)holder).radioButtonTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((TaskViewHolder)holder).radioButtonTask.isChecked()){
                    firestore.collection("user").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("tasks").document(todo.getKey())
                            .update("done",true).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });
                }else{
                    firestore.collection("user").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("tasks").document(todo.getKey())
                            .update("done",false).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return arraylist_todos.size();
    }

    public void removeItem(Todo obj,int position) {
        arraylist_todos.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, arraylist_todos.size());
        firestore.collection("user")
                .document(auth.getCurrentUser().getUid())
                .collection("tasks")
                .document(obj.getKey())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    public void restoreItem(Todo obj, int position) {
        arraylist_todos.add(position, obj);
        // notify item added by position
        notifyItemInserted(position);
        firestore.collection("user")
                .document(auth.getCurrentUser().getUid())
                .collection("tasks")
                .document(arraylist_todos.get(position).getKey())
                .set(obj)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

}
