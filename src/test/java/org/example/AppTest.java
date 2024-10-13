package org.example;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.ArraySerializer;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

/**
 * Unit test for simple App.
 */
public class AppTest {
    @Test
    public void nihao(){
//        TreeSet<Integer> treeSet=new TreeSet<>();
//        for(int i=1;i<100;i++){
//            treeSet.add(i);
//        }
//        new Integer(10)
//        System.out.println(treeSet.subSet(10,30));
////        System.out.println(treeSet.subSet(30,10));
        print(createTree(30),0,6);
    }
    void print(Node head,int deep,int maxDeep){
        if(head==null){
            return;
        }
        print(head.right,deep+1,maxDeep);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append(' ');
        for(int i=0;i<deep;i++){
            stringBuffer.append("  ");
        }
        stringBuffer.append(head.value);
        for(int i=0;i<maxDeep-deep;i++){
            stringBuffer.append("--");
        }
        System.out.println(stringBuffer);
        print(head.left,deep+1,maxDeep);
    }
    LinkedList<Node> findAll(int start,int end,Node head){
        if(head==null){
            return null;
        }
        if(end<head.value){
            return findAll(start,end,head.left);
        }
        if(start>head.value){
            return findAll(start,end,head.right);
        }
        LinkedList<Node> ret=new LinkedList<>();
        ret.add(head);
        LinkedList<Node> left=findAll(start,end,head.left);
        LinkedList<Node> right=findAll(start,end,head.right);
        if(left!=null){
            left.addAll(ret);
            ret=left;
        }
        if(right!=null){
            ret.addAll(right);
        }
        return ret;
    }
    Node createTree(int count){
        HashMap<Integer,Node> hashMap=new HashMap<Integer, Node>(count);
        Node head=new Node(1,null,null);
        hashMap.put(head.value,head);
        for(int i=2;i<=count;i++){
            Node tmp=new Node(i,null,null);
            hashMap.put(tmp.value,tmp);
            Node parentNode=hashMap.get(i/2);
            if(i%2==0){
                parentNode.left=tmp;
            }else {
                parentNode.right=tmp;
            }
        }
        return head;
    }
}

//1
//2 3
//45 67

@Data
@AllArgsConstructor
@NoArgsConstructor
class Node{
    int value;
    Node left;
    Node right;
}


class Hotel extends Object{
    BigDecimal price;
}