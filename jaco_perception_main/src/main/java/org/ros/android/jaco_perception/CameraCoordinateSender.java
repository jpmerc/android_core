package org.ros.android.jaco_perception;

/**
 * Created by robot on 06/06/14.
 */

import android.util.Log;

import org.ros.internal.message.RawMessage;
import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import std_msgs.String;

/*
    A node that communicate with the computer.  It's with him that the tablet communicate with the
    computer.  It can only publish.
 */

public class CameraCoordinateSender extends AbstractNodeMain {

	private Publisher<std_msgs.String> publisher;
	private std_msgs.String string ;
	private float[] array = new float[2];

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("/android_ros_sender");
	}

	@Override
	public void onStart(ConnectedNode connectedNode){
		publisher = connectedNode.newPublisher("/android_sender", String._TYPE);
		/*
		connectedNode.executeCancellableLoop(new CancellableLoop() {
			@Override
			protected void loop() throws InterruptedException {
				string = publisher.newMessage();
				java.lang.String temps_string = new java.lang.String("aalo");
				string.setData(temps_string);
				Log.d("blablabla", "j'envoie qqch");
				Log.d("blablabla", "j'envoie qqch");
				Log.d("blablabla", "j'envoie qqch");
				publisher.publish(string);
				Thread.sleep(1000);
			}
		});
		*/
	}

	public void send_coordinate(float[] p_array)
	{
        string = publisher.newMessage();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("c_");
		stringBuilder.append(java.lang.String.valueOf(p_array[0]));
		stringBuilder.append('_');
		stringBuilder.append(java.lang.String.valueOf(p_array[1]));
		string.setData(stringBuilder.toString());
		publisher.publish(string);
	}

	public void sendCloseMenu(){
		string = publisher.newMessage();
		string.setData("menu_close");
		publisher.publish(string);
	}

	public void sendMessage(java.lang.String p_string){
		string = publisher.newMessage();
		string.setData(p_string);
		publisher.publish(string);
	}

}
