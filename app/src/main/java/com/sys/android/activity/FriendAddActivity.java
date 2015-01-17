package com.sys.android.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;

import com.sys.android.xmpp.R;
import com.sys.android.xmppmanager.XmppConnection;
import com.sys.android.xmppmanager.XmppService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

@SuppressWarnings("all")
public class FriendAddActivity extends Activity{
	
	private String pUSERID;//��ǰ�û�
	private Button search_button;
	private Button goback_button;
	private String queryResult="";
	private ListView list;
	Roster roster = XmppConnection.getConnection().getRoster();
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// ȥ��������
		setContentView(R.layout.friend_add);
		this.pUSERID = getIntent().getStringExtra("USERID");
		list = (ListView) findViewById(R.id.testlistshow);
		search_button = (Button) findViewById(R.id.search_cancel_button);
		search_button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				  searchFriend();
				}
		});
		//���ذ�ť
		goback_button = (Button) findViewById(R.id.goback_button);
		goback_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();			
			}			
		});
		
	}
	
	
	
	public void searchFriend() {	
		String search_text = ((EditText) findViewById(R.id.search_text)).getText().toString();
		if (search_text.equals("")) {
			Toast.makeText(FriendAddActivity.this, "search fried", Toast.LENGTH_SHORT).show();
		} else {
			try{
				XMPPConnection connection = XmppConnection.getConnection();
				UserSearchManager search = new UserSearchManager(connection);
				//�˴�һ��Ҫ���� search.
				Form searchForm = search.getSearchForm("search."+connection.getServiceName());
				Form answerForm = searchForm.createAnswerForm();
				answerForm.setAnswer("Username", true);
				answerForm.setAnswer("search", search_text.toString().trim());
				ReportedData data = search.getSearchResults(answerForm,"search."+connection.getServiceName());					
				Iterator<Row> it = data.getRows();
				Row row=null;
				while(it.hasNext()){
					row=it.next();
					queryResult=row.getValues("Username").next().toString();
				}
			}catch(Exception e){
				Toast.makeText(FriendAddActivity.this,e.getMessage()+" "+e.getClass().toString(), Toast.LENGTH_SHORT).show();
			}
			if(!queryResult.equals("")){
				// ��ɶ�̬���飬�������
				ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
				    HashMap<String, Object> map = new HashMap<String, Object>();	     
				    map.put("name", queryResult); //��Ա�ǳ�
					listItem.add(map);
				// �����������Item�Ͷ�̬�����Ӧ��Ԫ��
				SimpleAdapter listItemAdapter = new SimpleAdapter(this, listItem,// ���Դ
						R.layout.friend_search_view,// ListItem��XMLʵ��
						// ��̬������ImageItem��Ӧ������
						new String[] { "name", },
						// ImageItem��XML�ļ������һ��ImageView,����TextView ID
						new int[] { R.id.itemtext });
				// ��Ӳ�����ʾ
				list.setAdapter(listItemAdapter);
				// ��Ӷ̵���¼�
				list.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						HashMap<String, String> map = (HashMap<String, String>) list.getItemAtPosition(position);
						final String name = map.get("name");
						AlertDialog.Builder dialog=new AlertDialog.Builder(FriendAddActivity.this);
						dialog.setTitle("��Ӻ���")
						      .setIcon(R.drawable.default_head)
						      .setMessage("��ȷ��Ҫ��ӡ�"+name+"��Ϊ������")
						      .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					                     @Override
					                     public void onClick(DialogInterface dialog, int which) {		 
					                         // TODO Auto-generated method stub	
					                    	 Roster roster = XmppConnection.getConnection().getRoster();
					                    	 String userName = name+"@"+XmppConnection.getConnection().getServiceName();
					                    	 //Ĭ����ӵ����ҵĺ��ѡ�����
					                    	 String groupName = "�ҵĺ���";
					                    	 XmppService.addUsers(roster, userName, name, groupName);
					                    	 Presence subscription = new Presence(Presence.Type.subscribe);
				                             subscription.setTo(userName);
				                    	     dialog.cancel();//ȡ�����
				                    	     finish();
				                    	     Intent intent = new Intent();
				                    		 intent.putExtra("USERID", pUSERID);
				                    		 intent.putExtra("GROUPNAME", groupName);
				                 			 intent.setClass(FriendAddActivity.this, FriendListActivity.class);
				                 			 startActivity(intent);
					                     }
					                   })
						       .setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
						                 public void onClick(DialogInterface dialog, int which) {			 
						                     // TODO Auto-generated method stub
						                     dialog.cancel();//ȡ�����
						                 }
						               }).create().show();
					       }
				     });	
			  }else{
				  Toast.makeText(FriendAddActivity.this, "���û������ڣ���ȷ���������Ϣ��ȷ��", Toast.LENGTH_SHORT).show();
			  }
		}
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
        if(!queryResult.equals("")){
        	menu.clear();
    		menu.add(Menu.NONE, Menu.FIRST + 1, 1,"�½�����").setIcon(R.drawable.addfriends_icon_icon);
    		menu.add(Menu.NONE, Menu.FIRST + 2, 1,"��ʱ����").setIcon(R.drawable.menu_exit);	  		
		}else{
			menu = null;
		}
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST + 1:
			View view = View.inflate(this, R.layout.dialog, null);
			final PopupWindow mPopupWindow = new PopupWindow(view, LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, true);
			mPopupWindow.setWindowLayoutMode(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
			mPopupWindow.showAtLocation(((Activity) this).getWindow().getDecorView(), Gravity.CENTER, 0, 0);
			mPopupWindow.setAnimationStyle(R.style.animationmsg);
			mPopupWindow.setFocusable(true);
			mPopupWindow.setTouchable(true);
			mPopupWindow.setOutsideTouchable(true);
			mPopupWindow.update();
			final EditText addFriend = (EditText) view.findViewById(R.id.addfriend);
			Button sure = (Button) view.findViewById(R.id.sure);
			Button cancle = (Button) view.findViewById(R.id.cancle);
			sure.setOnClickListener(new OnClickListener() {	
				@Override
				public void onClick(View v) {
					String groupName = addFriend.getText().toString().trim();
					if (groupName.equals("") || groupName.equals("")) {
						Toast.makeText(FriendAddActivity.this, "Ⱥ����Ʋ���Ϊ��!", Toast.LENGTH_SHORT).show();
					} else {
						boolean result = false;
						result = XmppService.addGroup(roster, groupName);
						if (result) {
							 Roster roster = XmppConnection.getConnection().getRoster();
	                    	 String userName = queryResult+"@"+XmppConnection.getConnection().getServiceName();
							 XmppService.addUsers(roster, userName, queryResult, groupName);
                    	     Intent intent = new Intent();
                    		 intent.putExtra("USERID", pUSERID);
                 			 intent.setClass(FriendAddActivity.this, FriendListActivity.class);
                 			 startActivity(intent);
						} else {
							Toast.makeText(FriendAddActivity.this, "Ⱥ�����ʧ��!", Toast.LENGTH_SHORT).show();
						}
					}
					mPopupWindow.dismiss();
				}				
			});
			cancle.setOnClickListener(new OnClickListener() {			
				@Override
				public void onClick(View v) {
					mPopupWindow.dismiss();
				}
			});
			break;
		case Menu.FIRST + 2:		
			Intent intent = new Intent(this,ChatActivity.class);
			String pFRIENDID = queryResult+"@"+XmppConnection.getConnection().getServiceName();
			intent.putExtra("FRIENDID", pFRIENDID);
			intent.putExtra("user", pFRIENDID);
			intent.putExtra("USERID", pUSERID);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
