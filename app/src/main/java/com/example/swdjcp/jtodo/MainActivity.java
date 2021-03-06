package com.example.swdjcp.jtodo;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

    public final String LIST = "list";


    Toolbar toolbar;

    Spinner spinner;

    EditText addInput;

    RecyclerView recyclerview;

    TodoAdapter adapter;

    TodoList list;

    TodoListFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //retrieve from Saved or SharedPreferences if we are starting fresh

        if(savedInstanceState != null){

            list = new TodoList(savedInstanceState.getString(LIST));

        }else{

            list = new TodoList(getSharedPreferences("data", Context.MODE_PRIVATE).getString(LIST, null));

        }

        //setup the filter which allows us to get the data we want to display

        filter = new TodoListFilter(list);


        //setup the Adapter , this contains a callback when an item is checked/unchecked


        adapter = new TodoAdapter(this, new TodoCompletedChangeListener() {
            @Override
            public void onTodoCompletedChanged(Todo todo) {
                list.toggle(todo);
                adapter.onTodoListChanged(filter.getFilteredData());
            }
        });

        //setup the list with the adapter

        recyclerview = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recyclerview.setAdapter(adapter);


        //setup adding new items to the list
        addInput = (EditText) findViewById(R.id.add_todo_input);
        //ensures the edittext isnt focused when entering the activity
        findViewById(R.id.add_todo_container).requestFocus();

        findViewById(R.id.btn_add_todo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String item = addInput.getText().toString();


                //ensure we dont add empty item
                if(!TextUtils.isEmpty(item.trim())){


                    //update our list

                    list.add(new Todo(item,false));

                        //update the adapter with the lastest filtered data

                    adapter.onTodoListChanged(filter.getFilteredData());


                    //clear input , remove focus and hide keyboard

                    addInput.setText("");

                    dismissKeyboard();

                }

            }
        });

        //setup the toolbat

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //setup the filter in the toolbar
       spinner = (Spinner) toolbar.findViewById(R.id.spinner);

        spinner.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_expandable_list_item_1,new String[]{"All","Incomplete","completed"}));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filter.setFilterMode(position);
                adapter.onTodoListChanged(filter.getFilteredData());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                filter.setFilterMode(TodoListFilter.ALL);
                adapter.onTodoListChanged(filter.getFilteredData());

            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(LIST,list.toString());
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences.Editor editor = getSharedPreferences("data",Context.MODE_PRIVATE).edit();
        editor.putString(LIST,list.toString());

        editor.apply();
    }


    private void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(addInput.getWindowToken(), 0);
    }


}
