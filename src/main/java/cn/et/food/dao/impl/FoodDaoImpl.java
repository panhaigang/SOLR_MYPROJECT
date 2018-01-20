package cn.et.food.dao.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FoodDaoImpl {
	@Autowired
	JdbcTemplate jdbc;
	
	//查询总记录数
	public int queryFoodCount(){
		String sql="select count(*) as foodCount from food";
		return Integer.parseInt(jdbc.queryForList(sql).get(0).get("foodCount").toString());
	}
	
	//
	public List<Map<String, Object>> queryFood(int start,int rows){
		String sql="SELECT f.*,ft.typeName FROM food f INNER JOIN foodtype ft ON f.typeid=ft.typeid limit "+start+","+rows;
		return jdbc.queryForList(sql);
	}
}
	
