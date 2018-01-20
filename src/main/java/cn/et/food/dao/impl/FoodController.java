package cn.et.food.dao.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.et.food.utils.SolrUtils;

@RestController
public class FoodController {
	
	@Autowired
	FoodDaoImpl dao;
	
	//单条件查询
	@GetMapping("/searchFood")
	public List<Map> getFood(String keyWord) throws IOException, SolrServerException{
		return SolrUtils.search("foodName_ik", keyWord);
	}
	
	//face查询  每个菜系下有多少菜品
	@GetMapping("/foodFacet")
	public List<Map> foodFacet(String keyword) throws SolrServerException, IOException {
		if(keyword!=null&&keyword!="") {
			return SolrUtils.facet("foodName_ik", keyword, "typeName_s");
		}
		return null;
	}
	
	//多条件查询     菜系加菜名（在face查询的基础上）
	@GetMapping("/searchFood1")
	public List<Map> getFood(String keyword,String typeName) throws IOException, SolrServerException{
		return SolrUtils.searchByMulti(new String[] {"foodName_ik","typeName_s"}, new String[] {keyword,typeName});
	}
	
	
	//创建索引库
	@GetMapping("/createIndex")
	public String createIndex() throws SolrServerException{
		try {
			//查询数据库数据  批量查询
			int startIndex=0;
			int rows=2;
			//第一次拉取  0,2
			//第二次拉取  2,2
			while(startIndex<dao.queryFoodCount())//小于等于查询的总数据
			{
				List<Map<String,Object>> queryFood=dao.queryFood(startIndex, rows);
				for(int i=0;i<queryFood.size();i++){
					Map<String,Object> map=queryFood.get(i);
					SolrInputDocument doc=new SolrInputDocument();
					doc.addField("foodid_i", map.get("foodid").toString());
					doc.addField("foodName_ik",map.get("foodName").toString());
					doc.addField("foodDetails_s",map.get("foodDetails").toString());
					doc.addField("typeName_s", map.get("typeName").toString());
					SolrUtils.write(doc);
				}
				startIndex+=rows;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return "索引创建失败";
		}
		
		return "索引创建成功";
	}
}