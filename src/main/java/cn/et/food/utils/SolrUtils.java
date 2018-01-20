package cn.et.food.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

public class SolrUtils {
	
	//用于solr生成索引的主键
	static Integer i=1;
	
	static String urlString="http://192.168.119.128:8080/solr/foodcore";
	static SolrClient solr;
	static {
		solr=new HttpSolrClient(urlString);
	}
	
	//face
	/**
	 * 
	 * @param field   分词字段  foodName_ik
	 * @param value   分词内容   番茄      （foodName_ik:番茄）
	 * @param facetField   类型（菜系）
	 * @return
	 * @throws SolrServerException
	 * @throws IOException
	 */
	public static List<Map> facet(String field,String value,String facetField) throws SolrServerException, IOException {
		List<Map> list=new ArrayList<Map>();
		SolrQuery solrQuery=new SolrQuery(field+":"+value);
		//开启face
		solrQuery.setFacet(true);
		solrQuery.addFacetField(facetField);
		QueryResponse response=solr.query(solrQuery);
		List<FacetField> facetFields=response.getFacetFields();
		for (FacetField facetField2 : facetFields) {
			List<Count> values=facetField2.getValues();
			for (Count count : values) {
				String name=count.getName();
				long coun=count.getCount();
				if(coun!=0) {
					Map map=new HashMap();
					map.put(name, coun);
					list.add(map);
				}
			}
		}
		return list;
	}
	
	
	
	
	//搜索
	public static List<Map> search(String field,String value) throws IOException, SolrServerException{
		SolrQuery solrQuery=new SolrQuery(field+":"+value);
		//设置高亮
		solrQuery.setHighlight(true);
		//设置那个属性需要高亮
		solrQuery.addHighlightField("foodName_ik");
		//前缀
		solrQuery.setHighlightSimplePre("<font color=red>");
		//后缀
		solrQuery.setHighlightSimplePost("</font>");
		//查询
		QueryResponse query=solr.query(solrQuery);
		
		List<Map> listMap=new ArrayList<Map>();
		//返回高亮后的
		Map<String, Map<String, List<String>>> highlighting = query.getHighlighting();
		//获取最终的document
		SolrDocumentList results = query.getResults();
		for(SolrDocument doc:results) {
			String id = doc.getFieldValue("id").toString();
			Map<String, List<String>> msl = highlighting.get(id);
			List<String> list = msl.get("foodName_ik");
			String highStr = list.get(0);
			Map map=new HashMap();
			map.put("foodid", doc.get("foodid_i"));
			map.put("foodDetails",doc.get("foodDetails_s"));
			map.put("foodName", highStr);
			map.put("typeName", doc.get("typeName_s"));
			listMap.add(map);
		}
		
		
		return listMap;
	}
	
	
	
	//多条件搜索
	public static List<Map> searchByMulti(String[] field,String[] value) throws IOException, SolrServerException{
		SolrQuery solrQuery=new SolrQuery();
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<field.length;i++) {
			
			if(i==field.length-1) {
				sb.append(field[i]+":"+value[i]);
			}else {
				sb.append(field[i]+":"+value[i]+" && ");
			}
		}
		solrQuery.setQuery(sb.toString());
		//设置高亮
		solrQuery.setHighlight(true);
		//设置那个属性需要高亮
		solrQuery.addHighlightField("foodName_ik");
		//前缀
		solrQuery.setHighlightSimplePre("<font color=red>");
		//后缀
		solrQuery.setHighlightSimplePost("</font>");
		//查询
		QueryResponse query=solr.query(solrQuery);
		
		List<Map> listMap=new ArrayList<Map>();
		//返回高亮后的
		Map<String, Map<String, List<String>>> highlighting = query.getHighlighting();
		//获取最终的document
		SolrDocumentList results = query.getResults();
		for(SolrDocument doc:results) {
			String id = doc.getFieldValue("id").toString();
			Map<String, List<String>> msl = highlighting.get(id);
			List<String> list = msl.get("foodName_ik");
			String highStr = list.get(0);
			Map map=new HashMap();
			map.put("foodid", doc.get("foodid_i"));
			map.put("foodDetails",doc.get("foodDetails_s"));
			map.put("foodName", highStr);
			map.put("typeName", doc.get("typeName_s"));
			listMap.add(map);
		}
		
		
		return listMap;
	}
	
	
	
	//创建索引库
	public static void write(SolrInputDocument doc) throws IOException, SolrServerException{
		//加锁主键
		synchronized (i) {
			doc.addField("id", i);
			i++;
		}
		solr.add(doc);
		//提交事务
		solr.commit();
	}
}
