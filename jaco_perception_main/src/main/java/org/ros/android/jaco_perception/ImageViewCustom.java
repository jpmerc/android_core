package org.ros.android.jaco_perception;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.ros.android.MessageCallable;
import org.ros.android.view.RosImageView;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

/**
 * Created by robot on 26/06/14.
 */

/**
 * This is a copy from what it have by default in ROS Java.  We made this beacause we wanted to\
 * pause the image.  The modification is at line 73.  If the variable pauseNeeded is not to false,
 * the image is updated.
 */
public class ImageViewCustom<T> extends ImageView implements NodeMain {

	private String topicName;
	private String messageType;
	private boolean pauseNeeded;
	private MessageCallable<Bitmap, T> callable;

	public ImageViewCustom(Context context) {
		super(context);
		pauseNeeded = false;
	}

	public ImageViewCustom(Context context, AttributeSet attrs) {
		super(context, attrs);
		pauseNeeded = false;
	}

	public ImageViewCustom(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		pauseNeeded = false;
	}


	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("ros_image_view");
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public void setMessageToBitmapCallable(MessageCallable<Bitmap, T> callable) {
		this.callable = callable;
	}


	@Override
	public void onStart(ConnectedNode connectedNode) {
		Subscriber<T> subscriber = connectedNode.newSubscriber(topicName, messageType);
		subscriber.addMessageListener(new MessageListener<T>() {
			@Override
			public void onNewMessage(final T message) {
				if(!pauseNeeded){
					post(new Runnable() {
						@Override
						public void run() {
							setImageBitmap(callable.call(message));
						}
					});
				}
				postInvalidate();
			}
		});
	}

	public void setPauseNeeded(boolean p_boolean){
		pauseNeeded = p_boolean;
	}

	@Override
	public void onShutdown(Node node) {
	}

	@Override
	public void onShutdownComplete(Node node) {
	}

	@Override
	public void onError(Node node, Throwable throwable) {
	}
}
