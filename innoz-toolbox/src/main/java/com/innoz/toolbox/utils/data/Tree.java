package com.innoz.toolbox.utils.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 * This is a basic implementation of a hierarchical tree collection. The tree consists of nodes, each storing data of type
 * {@code T}. There must always be a root element that holds no data but serves as a "top level container" for all other nodes.
 * The nodes are classified via unique identifiers (strings) to make it work for administrative units.<br>
 * For example, the top level container "Germany" could have the id "0". It could have one child with id "01" called 
 * "Schleswig-Holstein" and another child with id "09" called "Bayern". These children have children of their own, e.g.
 * "09" would store ids like "09180", "09178" etc. So, the childrens' ids always "extend" their predecessor's id. If a new node
 * is added and its id doesn't match any existing node's id, it is automatically a child of the root element.<br>
 * 
 * @author dhosse
 *
 * @param <T> The data type this tree applies for.
 */
public class Tree<T> {

	private Node<T> root;
	private int size = 0;
	Set<Node<T>> nodesToSwap = new HashSet<>();
	
	public Tree(T rootData){
		
		this.root = new Node<T>();
		root.data = rootData;
		root.children = new ArrayList<Node<T>>();
		size++;
		
	}
	
	public boolean add(T data){
		
		return this.add(data, this.root);
		
	}
	
	boolean add(T data, Node<T> top){
		
		boolean isAdded = false;
		
		for(Node<T> child : top.children){
			
			if(data.toString().contains(child.data.toString())){
				
				if(!data.toString().equals(child.data.toString())){
					
					isAdded = add(data, child);
					
				} else {
					
					return false;
					
				}
				
			} else if(child.data.toString().contains(data.toString())){
				
				nodesToSwap.add(child);
				
			}
			
		}
		
		if(!isAdded || top.children.isEmpty()){
			
			if(nodesToSwap.size() > 0){
				
				for(Node<T> node : nodesToSwap){
					
					swap(data, node);
					
				}

				nodesToSwap = new HashSet<>();
				return true;
				
			} else {

				Node<T> node = new Node<T>();
				node.data = data;
				node.children = new ArrayList<>();
				node.parent = top;
				top.children.add(node);
				size++;
				return true;
				
			}
			
		}
		
		return false;
		
	}
	
	void swap(T data, Node<T> existingChild){
		
		boolean contained = get(data.toString()) != null;
		
		Node<T> newNode = contained ? get(data.toString()) : new Node<>();
		
		if(!contained){

			newNode.data = data;
			newNode.children = new ArrayList<>();
			newNode.parent = existingChild.parent;
			newNode.parent.children.add(newNode);
		
		}
		
		newNode.children.add(existingChild);
		newNode.parent.children.remove(existingChild);
		existingChild.parent = newNode;
		
		size++;
		
	}
	
	public Node<T> get(String id){
		
		return this.get(id, this.root);
		
	}
	
	Node<T> get(String id, Node<T> top){
		
		if(id.equals(top.data.toString())) return top;
		
		for(Node<T> child : top.children){

			if(id.toString().equals(child.data.toString())){
				
				return child;
				
			}
			
			if(id.toString().contains(child.data.toString())){
				
				return get(id, child);
				
			}
			
		}
		
		return null;
		
	}
	
	public List<Node<T>> getAll(){

		return getAll(this.root);
		
	}
	
	List<Node<T>> getAll(Node<T> top){
		
		List<Node<T>> list = new ArrayList<>();
		
		for(Node<T> node : top.children){
			
			list.add(node);
			List<Node<T>> children = getAll(node);
			
			list.addAll(children);
			
		}
		
		return list;
		
	}
	
	public int getSize(){
		
		return this.size;
		
	}
	
	public static class Node<T>{
		
		private T data;
		private Node<T> parent;
		private List<Node<T>> children;
		
		public T getData(){
			
			return this.data;
			
		}
		
		public Node<T> getParent(){
			
			return this.parent;
			
		}
		public List<Node<T>> getChildren(){
			
			return this.children;
			
		}
		
		@Override
		public String toString(){
			return "[data: " + this.data.toString() + "]";
		}
		
	}
	
}