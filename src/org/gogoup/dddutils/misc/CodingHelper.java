/*
 * Copyright 2013 Rui Sun (SteveSunCanada@gmail.com)
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.gogoup.dddutils.misc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.gogoup.dddutils.appsession.AppSessionContext;
import org.gogoup.dddutils.objectsegment.Ordering;
import org.gogoup.dddutils.objectsegment.Paging;

final public class CodingHelper {
	
	
	private static final char hexDigits[] = {'0', '1', '2', '3', '4',
		'5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',  'e', 'f'};
	
	public static String md5(String content){
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			
			byte[] thedigest = md.digest(content.getBytes());
			char chars[] = new char[16 * 2];			
			for (int i = 0; i < thedigest.length; i++) {				
				chars[i*2] = hexDigits[thedigest[i] >>> 4 & 0xf];
				chars[i*2+1] = hexDigits[thedigest[i] & 0xf];		
			}
			
			return new String(chars);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return null;
	}
	
	public static String sha2(String content){
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			
			byte[] thedigest = md.digest(content.getBytes());

			char chars[] = new char[32 * 2];			
			for (int i = 0; i < thedigest.length; i++) {				
				chars[i*2] = hexDigits[thedigest[i] >>> 4 & 0xf];
				chars[i*2+1] = hexDigits[thedigest[i] & 0xf];		
			}
			
			return new String(chars);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return null;
	}
	
	//sha2(sha2(content)+salt)
	public static String saltedHash(String content, long salt) {
		StringBuilder saltedContent=new StringBuilder(sha2(content));
		saltedContent.append(salt);
		return sha2(saltedContent.toString());
	}
	
	public static String hmacSHA1(String value, String keySeed){
		try {
            // Get an hmac_sha1 key from the raw key bytes
            byte[] keyBytes = keySeed.getBytes("UTF8");           
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

            // Get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);

            // Compute the hmac on input data bytes
            byte[] signBytes = mac.doFinal(value.getBytes("UTF8"));
          
            return Base64.encodeBytes(signBytes, Base64.DO_BREAK_LINES);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		return null;
	}
			
	public static byte[] toUUIDBinary(UUID uuid) {		 
		ByteBuffer bb = ByteBuffer.allocate(16);
		bb.putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
		return bb.array();
	}
	
	public static byte[] toUUIDBinary(String uuid) {
		byte[] binary=new byte[uuid.length()/2];
		
		for (int i = 0; i < binary.length; i++) {
			int idx=i << 1;			
		    final String code = uuid.substring(idx, idx + 2);
		    binary[i]=(byte)Integer.parseInt(code, 16);
		}
		
		return binary;	
	}
			
	public static UUID toUUID(byte[] binary) {
		ByteBuffer bb = ByteBuffer.wrap(binary);
		return new UUID(bb.getLong(), bb.getLong());
	}
	
	public static String toUUIDString(byte[] binary) {
		if(null == binary) return null;
		StringBuilder hex = new StringBuilder(binary.length * 2);
		for (byte b : binary)
		{
			hex.append(hexDigits[(b >> 4) & 0x0F]);
			hex.append(hexDigits[b & 0x0F]);
		}
		return hex.toString();
	}
	
	public static String nextUUIDString() {
		return toUUIDString(toUUIDBinary(UUID.randomUUID()));
	}
	
	public static String getCVBase64StringFromStringArray(String[] ids) {
		StringBuilder strBuilder =  new StringBuilder();
		try {
			for(int i=0; i<ids.length; i++)
			{			
				if(ids[i].trim().length() == 0)
					continue;
				strBuilder.append(Base64.encodeBytes(CodingHelper.toUUIDBinary(ids[i]), Base64.ENCODE));
				if(i<(ids.length - 1))
					strBuilder.append(",");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strBuilder.toString();
	}
	
	public static String[] toStringArrayFromCVBase64String(String str){
		if(str.trim().length() == 0) return new String[0];
		
		String[] ids = str.split(",");
		String[] paths = new String[ids.length];
		try {
			for(int i=0; i<paths.length; i++)
			{
				paths[i] = CodingHelper.toUUIDString(Base64.decode(ids[i], Base64.DECODE));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return paths;		
	}
	
	public static String getCVStringFromStringArray(String[] ids) {
		StringBuilder strBuilder =  new StringBuilder();
		for(int i=0; i<ids.length; i++)
		{			
			if(ids[i].trim().length() == 0)
				continue;
			strBuilder.append(ids[i]);
			if(i<(ids.length - 1))
				strBuilder.append(",");
		}
		return strBuilder.toString();
	}
	
	public static String generateMySQLPagingSQL(String sql, Paging paging, AppSessionContext ctx) {
		StringBuilder sqlBuilder=new StringBuilder(sql);
		
		if(null==paging)
		{
			sqlBuilder.append(";");
			return sqlBuilder.toString();
		}
		
		/*Order sql assemble*/
		if(paging.getOrderings().length>0)
			sqlBuilder.append(" ORDER BY ");		
		
		Map<String, Ordering> orderings=new LinkedHashMap<String, Ordering>(paging.getOrderings().length);
		//filter out duplicates
		for(Ordering ord:paging.getOrderings())
		{			
			if(!orderings.containsKey(ord.getAttribute()))
				orderings.put(ord.getAttribute(), ord);
		}
		
		int i=0;
		for(Iterator<Ordering> iter=orderings.values().iterator(); iter.hasNext();)
		{			
			Ordering ord=iter.next();
			Object obj=ctx.getCurrentSession().getParameter(ord.getAttribute());
			if(null==obj)
				throw new ProgrammingLogicException("Invalid MySql database column specified for ordering, "+ord.getAttribute());
			String colName = (String) obj;
			sqlBuilder.append(colName);			
			if(ord.getOrder()==Ordering.ORDER_ASC)
				sqlBuilder.append(" ASC");
			else if(ord.getOrder()==Ordering.ORDER_DESC)
				sqlBuilder.append(" DESC");
			if(i<paging.getOrderings().length-1)
				sqlBuilder.append(",");
			i++;
		}		
		
		/*Limit sql assemble*/		
		//no paging limit
		if(paging.getPageSize() == -1)
		{
			sqlBuilder.append(";");
			return sqlBuilder.toString();
		}
		
		sqlBuilder.append(" LIMIT ");
		sqlBuilder.append(paging.getPageStart()-1); //first record is start from 0.
		sqlBuilder.append(", ");
		sqlBuilder.append(paging.getPageSize());
		sqlBuilder.append(";");		
		
		return sqlBuilder.toString();
	}
}
