package com.jaimegag.demo.springcloud;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;

public class RedisRepository {

	   private RedisTemplate<String, String> template;
	   
	   public RedisRepository(RedisTemplate<String, String> template) {
		   this.template = template;
	   }
	 
	   public void add(String key, String value) {
	      template.opsForValue().set(key, value);
	   }
	 
	   public String getValue(String key) {
	      return template.opsForValue().get(key);
	   }
	 
	   public void delete(String key) {
	      template.opsForValue().getOperations().delete(key);
	   }
	   
	   public Map<String, String> getKeyStore() {
		   Map<String, String> store = new HashMap<String, String>();
		   Set<String> keys = template.keys("*");
		   for(String key : keys) {
			   System.out.println("key found: "+key);
			   System.out.println("value: "+getValue(key));
			   store.put(key, getValue(key));
		   }
		   return store;
	   }
}
