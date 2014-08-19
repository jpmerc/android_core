/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.android.jaco_perception;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.common.collect.Iterables;
import com.google.common.collect.Table;

import org.ros.address.InetAddressFactory;
import org.ros.android.BitmapFromCompressedImage;
import org.ros.android.RosActivity;
import org.ros.android.view.RosImageView;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import sensor_msgs.CompressedImage;

import org.ros.android.jaco_perception.ImageViewCustom;

/**
 * @author jeanbouch418@hotmail.com (Jean Bouchard)
 */


/*
    This is the main program that will be launch at the startup.
 */

public class MainActivity extends RosActivity {

	private RosListener rosListener;
	public CameraCoordinateSender cameraCoordinateSender;
	public ImageViewCustom<CompressedImage> image;
	private float ratio_touched_x;
	private float ratio_touched_y;
	private int imageWidthProportion;
	private int imageHeightProportion;
	private int border;
	private boolean isMenuUp = false;
	private boolean allMessageReceived = false;
	private String graspSelected = "";
	private List<Button> buttonsList = new ArrayList<Button>();

	private LinearLayout linearLayoutMain;

	public MainActivity() {
	// The RosActivity constructor configures the notification title and ticker
	// messages.
		super("jaco_perception", "jaco_perception");
	}

	@SuppressWarnings("unchecked")
	@Override
    /*
        The create method.  this is where all the variable should be initialize.  Its the first
        method that is call when a application is run at the startup.
     */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		image = (ImageViewCustom<CompressedImage>) findViewById(R.id.image_view);
		image.setTopicName("/rgb_image_square/compressed");
		image.setMessageType(CompressedImage._TYPE);
		image.setMessageToBitmapCallable(new BitmapFromCompressedImage());
		linearLayoutMain = (LinearLayout) findViewById(R.id.linear_layout_main);
	}

	@Override
    /*
        This is where the ros node are connected to the topic.
        Presently you need to hard code the ip of the tablet and the roscore
        hostMaster is the roscore ip address
        nodeConfigure is the tablet ip address on the vpn.
        I haven't find a easy way to do it automaticly.  The host master can be tap at the
        application start.  If you set it this way
            NodeConfiguration nodeConfiguration =
            NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress(),getMasterUri());
     */
	protected void init(NodeMainExecutor nodeMainExecutor) {
		rosListener = new RosListener();
		cameraCoordinateSender = new CameraCoordinateSender();
		String hostLocal = InetAddressFactory.newNonLoopback().getHostAddress();
		String hostMaster = "132.203.241.145";
		URI uri = URI.create("http://" + hostMaster + ":" + "11311");
		NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic("132.203.241.141",uri);
		nodeMainExecutor.execute(image, nodeConfiguration);
		nodeMainExecutor.execute(cameraCoordinateSender, nodeConfiguration);
		nodeMainExecutor.execute(rosListener, nodeConfiguration);
	}


    /*
        Methode to look if the coordinate is in the image.
        Return true if the coordinate are in the image range.
     */

	public boolean isInImage(float p_x, float p_y){
		boolean validation = false;
		int[] coords = new int[2];
		image.getLocationOnScreen(coords);
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		int bottomImage;
		imageWidthProportion = (4 * imageHeight) / 3;
		imageHeightProportion = (3 * imageWidth) / 4;
		if(imageHeight >= imageHeightProportion){
			//image have been resize to fit the width
			//the borders are at the top and the bottom
			border = (imageHeight - imageHeightProportion)/2;
			bottomImage = coords[1] + border + imageHeightProportion;
			if(p_x <= imageWidth && p_x >= coords[0] && p_y <= bottomImage && p_y >= coords[1] + border)
				validation = true;
		}
		else if(imageWidth >= imageWidthProportion){
			//image have been resize to fit the height
			//the borders are at the sides
			border = (imageWidth - imageWidthProportion)/2;
			bottomImage = coords[1] + imageHeight;
			if(p_x <= imageWidth - border && p_x > border + coords[0] && p_y <= bottomImage && p_y >= coords[1])
				validation = true;
		}
		return validation;
    }

    /*

        The callback when the user touch the screen.
        It look if the coordinate are in the image range.
        If they are, the coordinate are send to the computer.
        Then the computer must send the message back to stop the while loop.
     */
	@Override
	public boolean onTouchEvent(MotionEvent event){
		if(event.getAction() == event.ACTION_UP)
		{
			float x = event.getX();
			float y = event.getY();
			int[] coords = new int[2];
			image.getLocationOnScreen(coords);
			int absoluteTop = coords[1];
			if(isInImage(x,y)) {
                //fit for horizontal image
                if (!isMenuUp) {
                    float absoluteY = y - absoluteTop;
                    float absoluteX = x - border;
                    int windows_x_size = image.getWidth();
                    int windows_y_size = image.getHeight();
                    ratio_touched_x = absoluteX / imageWidthProportion;
                    ratio_touched_y = absoluteY / windows_y_size;
                    float[] send_array_temp = new float[2];
                    send_array_temp[0] = ratio_touched_x;
                    send_array_temp[1] = ratio_touched_y;
                    cameraCoordinateSender.send_coordinate(send_array_temp);
                    while (!allMessageReceived) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(allMessageReceived);
                        String message = sb.toString();
                        Log.d("blablabla", message);
                        allMessageReceived = rosListener.getAllMessagesReceived();
                    }
                } else {
                    cameraCoordinateSender.sendCloseMenu();
                }
                if (rosListener.getObjectRecon() != -1){
                    insertMenu();
                }
                allMessageReceived = false;
                rosListener.setDefault();
			}
		}
		return true;
	}

    /*
        Function thats insert the menu in the screen or close it.
        We use the inflater service to generate add a layout that we already create in the xml file.
        We add all the button for all the grasp.
        When we delete the menu, we concidere it's the last the inflate.
     */

	public void insertMenu(){
		if(!isMenuUp) {
			//unable the menu

			isMenuUp = true;
			image.setPauseNeeded(true);

			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View menu = inflater.inflate(R.layout.menu, linearLayoutMain, false);

			Button buttonTrain = (Button) menu.findViewById(R.id.button_train);
			buttonTrain.setOnClickListener(onClickListenerTrain);

			Button buttonGo = (Button) menu.findViewById(R.id.button_go);
			buttonGo.setOnClickListener(onClickListenerGo);

			TextView statusTexTView = (TextView) menu.findViewById(R.id.text_view_status);
            if(rosListener.getObjectRecon() == 1) {
                statusTexTView.setText("Recon OK");
            }
            else if(rosListener.getObjectRecon() == -1){
                statusTexTView.setText("Not Recon");
            }

			TableLayout menuTableLayout = (TableLayout) menu.findViewById(R.id.table_layout_prise);

			String graspList = rosListener.getPriseList();
			graspList = graspList.substring(2, graspList.length());
			String[] grapsItem = graspList.split(";");
			graspSelected = "";
			buttonsList.clear();
			for(int i = 0; i< grapsItem.length; i++){
				View row = inflater.inflate(R.layout.prise_row, null);
				Button rowButton = (Button) row.findViewById(R.id.button_row_menu);
				rowButton.setText(grapsItem[i]);
				rowButton.setOnClickListener(onClickListenerRowButton);
				buttonsList.add(rowButton);
				menuTableLayout.addView(row);
			}
			linearLayoutMain.addView(menu);
		}
		else{
			//disable the menu
			isMenuUp = false;
			image.setPauseNeeded(false);
			linearLayoutMain.removeViewAt(1);
		}
	}

    /*
        The callback for the train button.
        It only send one message.
     */
	public OnClickListener onClickListenerTrain = new OnClickListener() {
		@Override
		public void onClick(View view) {

			String sendMessage = new String("t_train");
			cameraCoordinateSender.sendMessage(sendMessage);

		}
	};

    /*
        The call back for take it button.
        It send the message whith the grasp selected, for now it can send a empty grasp.
     */
	public OnClickListener onClickListenerGo = new OnClickListener() {
		@Override
		public void onClick(View view) {

			String sendMessage = new String("g_take_it");
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("g_");
			stringBuilder.append(graspSelected);
			cameraCoordinateSender.sendMessage(stringBuilder.toString());
		}
	};

    /*
        The callback for all the button in the grasp section.  It update a variable that can be use
        byy the other fonction.  The text show in the button represent the grasp selected.

     */
	public OnClickListener onClickListenerRowButton = new OnClickListener() {
		@Override
		public void onClick(View view) {
			for(int i = 0; i < buttonsList.size(); i++){
				buttonsList.get(i).setBackgroundResource(R.drawable.button_border);
			}
			Button button = (Button)view;
			graspSelected = button.getText().toString();
			button.setBackgroundColor(Color.RED);
		}
	};

}
