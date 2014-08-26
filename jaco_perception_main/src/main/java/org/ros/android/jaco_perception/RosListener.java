package org.ros.android.jaco_perception;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import std_msgs.*;
import std_msgs.String;

/**
 * Created by robot on 18/06/14.
 */


/**
 * The main listener for the tablet.  It here that the tablet received all its callback from the
 * computer.  The listener look if the object have been selected or not.
 * If objectRecon == -1 no object have been selected.
 * If objectRecon == 0 An object have been selected but not recognise.
 * If objectRecon == 1 The object have been recognise.
 */
public class RosListener extends AbstractNodeMain {

	private boolean allMessageReceived = false;
	private boolean priseListReceived = false;
	private int objectRecon = -2;
	private java.lang.String objectReconOk = "object_recon";
	private java.lang.String objectReconNotOk = "object_not_recon";
    private java.lang.String noObject = "no_object";
	private java.lang.String priseList = "";


	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("/android_ros_listener");
	}

	public void onStart(ConnectedNode connectedNode){
		final Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber("/android_listener", String._TYPE);
		subscriber.addMessageListener(stringMessageListener);
	}

	public MessageListener<String> stringMessageListener = new MessageListener<String>() {
		@Override
		public void onNewMessage(String string) {
			java.lang.String messageReceived = string.getData();

            if(messageReceived.contentEquals(objectReconNotOk)) {
                objectRecon = 0;
            }

			else if(messageReceived.contentEquals(objectReconOk)) {
				objectRecon = 1;
			}

            else if (messageReceived.contentEquals(noObject)){
                objectRecon = -1;
            }

			else if(messageReceived.charAt(0) == 'p'){
				priseListReceived = true;
				priseList = messageReceived;
			}

			if (objectRecon == 1 && priseListReceived){
				allMessageReceived = true;
			}

            else if(objectRecon == 0){
                allMessageReceived = true;
            }

            else if(objectRecon == -1){
                allMessageReceived = true;
            }
		}
	};

	public java.lang.String getPriseList(){
		return priseList;
	}

	public boolean getAllMessagesReceived(){
		return allMessageReceived;
	}

    public int getObjectRecon(){
        return objectRecon;
    }
    public void setDefault(){
        allMessageReceived = false;
        objectRecon = -2;
    }
}
