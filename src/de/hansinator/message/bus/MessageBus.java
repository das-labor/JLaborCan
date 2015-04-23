package de.hansinator.message.bus;

import java.util.ArrayList;

import de.hansinator.message.MessageObject;

/**
 *
 * @author hansinator
 */
public class MessageBus<T extends MessageObject> {
	
	final ArrayList<MessageNode<T>> nodes;
	
	public MessageBus() {
		this.nodes = new ArrayList<MessageNode<T>>();
	}
	
	public synchronized boolean addMessageNode(MessageNode<T> node) {
		return nodes.add(node);
	}

	public synchronized boolean removeNode(MessageNode<T> node) {
		return nodes.remove(node);
	}
	
    public synchronized void sendMessage(MessageNode<T> sender, T message) {
    	for(MessageNode<T> node : nodes)
    		if(node != sender)
    			node.onMessageReceived(message);
    }
}
