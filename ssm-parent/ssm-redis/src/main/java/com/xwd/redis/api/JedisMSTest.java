package com.xwd.redis.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/*
 * 	将red is比做memcache一样，value用来存储 对象的xu lie hu
 */
public class JedisMSTest {

	public static Jedis redis;   
	   
	@Before
	public void initConn(){
		redis = RedisClient.getRedisClient().getJedis();
	}
  
    /** 
     * Test Data 
     * @return 
     */  
    public static List<User> buildTestData(){  
        User a = new User();  
        a.setName("a");  
        User b = new User();  
        b.setName("b");  
        List<User> list = new ArrayList<User>();  
        list.add(a);  
        list.add(b);  
        return list;  
    }  
  
    /** 
     * Test for 
     */  
    @Test
    public void testSetElements(){  
        User a = new User();  
        a.setName("a");  
        User b = new User();  
        b.setName("b");  
        List<User> testData = new ArrayList<User>();  
        testData.add(a);  
        testData.add(b);  
        Jedis jedis =redis;  
        String key = "testSetElements" + new Random(1000).nextInt();  
        jedis.set(key.getBytes(), ObjectsTranscoder.serialize(testData));  
  
        //验证  
        byte[] in = jedis.get(key.getBytes());  
        List<User> list = ObjectsTranscoder.deserialize(in);  
        for(User user : list){  
            System.out.println("testSetElements user name is:" + user.getName());  
        }  
    }  
  
    public static void testSetEnsemble(){  
        List<User> testData = buildTestData();  
        Jedis jedis = redis;  
        String key = "testSetEnsemble" + new Random(1000).nextInt();  
        jedis.set(key.getBytes(), ListTranscoder.serialize(testData));  
  
        //验证  
        byte[] in = jedis.get(key.getBytes());  
        List<User> list = (List<User>)ListTranscoder.deserialize(in);  
        for(User user : list){  
            System.out.println("testSetEnsemble user name is:" + user.getName());  
        }  
    }  
  
    public static void main(String[] args) {  
//        testSetElements();  
        testSetEnsemble();  
    }  
  
    public static void close(Closeable closeable) {  
        if (closeable != null) {  
            try {  
                closeable.close();  
            } catch (Exception e) {  
            }  
        }  
    }  
  
    static class User implements Serializable{  
        String name;  
  
        public String getName() {  
            return name;  
        }  
  
        public void setName(String name) {  
            this.name = name;  
        }  
    }  
  
    static class ObjectsTranscoder{  
          
        public static byte[] serialize(List<User> value) {  
            if (value == null) {  
                throw new NullPointerException("Can't serialize null");  
            }  
            byte[] rv=null;  
            ByteArrayOutputStream bos = null;  
            ObjectOutputStream os = null;  
            try {  
                bos = new ByteArrayOutputStream();  
                os = new ObjectOutputStream(bos);  
                for(User user : value){  
                    os.writeObject(user);  
                }  
                os.writeObject(null);  
                os.close();  
                bos.close();  
                rv = bos.toByteArray();  
            } catch (IOException e) {  
                throw new IllegalArgumentException("Non-serializable object", e);  
            } finally {  
                close(os);  
                close(bos);  
            }  
            return rv;  
        }  
  
        public static List<User> deserialize(byte[] in) {  
            List<User> list = new ArrayList<User>();  
            ByteArrayInputStream bis = null;  
            ObjectInputStream is = null;  
            try {  
                if(in != null) {  
                    bis=new ByteArrayInputStream(in);  
                    is=new ObjectInputStream(bis);  
                    while (true) {  
                        User user = (User) is.readObject();  
                        if(user == null){  
                            break;  
                        }else{  
                            list.add(user);  
                        }  
                    }  
                    is.close();  
                    bis.close();  
                }  
            } catch (IOException e) {  
            } catch (ClassNotFoundException e) {  
            } finally {  
            }  
            return list;  
        }  
    }  
      
    static class ListTranscoder{  
        public static byte[] serialize(Object value) {  
            if (value == null) {  
                throw new NullPointerException("Can't serialize null");  
            }  
            byte[] rv=null;  
            ByteArrayOutputStream bos = null;  
            ObjectOutputStream os = null;  
            try {  
                bos = new ByteArrayOutputStream();  
                os = new ObjectOutputStream(bos);  
                os.writeObject(value);  
                os.close();  
                bos.close();  
                rv = bos.toByteArray();  
            } catch (IOException e) {  
                throw new IllegalArgumentException("Non-serializable object", e);  
            } finally {  
                close(os);  
                close(bos);  
            }  
            return rv;  
        }  
  
        public static Object deserialize(byte[] in) {  
            Object rv=null;  
            ByteArrayInputStream bis = null;  
            ObjectInputStream is = null;  
            try {  
                if(in != null) {  
                    bis=new ByteArrayInputStream(in);  
                    is=new ObjectInputStream(bis);  
                    rv=is.readObject();  
                    is.close();  
                    bis.close();  
                }  
            } catch (IOException e) {  
            } catch (ClassNotFoundException e) {  
            } finally {  
            }  
            return rv;  
        }  
    }  
}
