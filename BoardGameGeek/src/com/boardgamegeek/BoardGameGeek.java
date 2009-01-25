package com.boardgamegeek;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class BoardGameGeek extends Activity
{	
	// declare variables
    private EditText textbox;
	private Button go_button;
	private String query = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        
       	// call the xml layout
        this.setContentView(R.layout.main);
        
        // declare the gui variables
        textbox = (EditText) findViewById(R.id.game_text);
        go_button = (Button) findViewById(R.id.game_button);
        
        // when an id is entered, view board game
        go_button.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
        		query = textbox.getText().toString();
        		if (!query.equals(""))
        			viewBoardGameList();
            }
        });
        textbox.setOnClickListener(new OnClickListener()
        {
        	public void onClick(View v)
        	{
        		query = textbox.getText().toString();
        		if (!query.equals(""))
        			viewBoardGameList();
        	}
        });
	}
	
	public void viewBoardGameList()
	{
		Intent myIntent = new Intent();
		myIntent.setClassName("com.boardgamegeek", "com.boardgamegeek.ViewBoardGameList");
		myIntent.putExtra("QUERY", query);
		startActivity(myIntent);
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        
        //inflate the menu from xml
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
       
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        	case R.id.reload:
        		textbox.setText("");
                return true;
        	case R.id.settings:
        		startActivity(new Intent(this, Preferences.class));          
        		return true;
        	case R.id.credits:
                Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Special thanks to:");
                builder.setMessage(R.string.special_thanks); 
                builder.show();
                return true;
        }
        return false;
    }
}