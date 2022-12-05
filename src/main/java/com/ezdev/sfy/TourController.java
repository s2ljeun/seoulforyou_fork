package com.ezdev.sfy;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ezdev.sfy.dto.TourDTO;
import com.ezdev.sfy.service.TourMapper;

@Controller
public class TourController {
	@Autowired
	private TourMapper tourMapper;
	//여행지
	@RequestMapping("/tourList.do")
	public String tourList(HttpServletRequest req
			, @RequestParam(required = false) String pageNum
			, @RequestParam(required=false) String region
			, @RequestParam(required=false) String tourType) {
		
		HttpSession session = req.getSession();
		
		//여행지 타입
		if (tourType == null) {
			tourType = "0";
		}
		req.setAttribute("tourType", tourType);
		
		//여행지 지역
		if (region == null) {
			region = "0";
		}
		req.setAttribute("region", region);
		
		//페이징
		if (pageNum == null) {
			pageNum = "1";
		}
		int currentPage = Integer.parseInt(pageNum);
		int pageSize = 8;
		int startRow = (currentPage-1) * pageSize + 1;
		int endRow = startRow + pageSize - 1;
		int countRow = 0;
		
		// countRow 구하기
		// 투어타입이 0이고 지역이 존재하면
		if(tourType == "0" && !region.equals("0")) {
			countRow = tourMapper.getTourCountByRegion(region); //지역전부불러오기
			
		//투어타입이 12345고 지역이 존재하면
		}else if(!tourType.equals("0") && !region.equals("0")) {
			countRow = tourMapper.getTourCountByRegionType(tourType, region);//타입+지역불러오기
		
		//투어타입이 12345면
		}else if(!tourType.equals("0")) {
			countRow = tourMapper.getTourCountByType(tourType); //타입별불러오기
		//그외
		}else {
			countRow = tourMapper.getTourCount(); //전부불러오기
		}
		
		if (endRow > countRow) endRow = countRow;
		int pageCount = countRow / pageSize + (countRow%pageSize==0 ? 0 : 1);
		int pageBlock = 5;
		int startPage = (currentPage - 1) / pageBlock * pageBlock + 1;
		int endPage = startPage + pageBlock - 1;
		if (endPage > pageCount) endPage = pageCount;		
		req.setAttribute("pageCount", pageCount);
		req.setAttribute("pageBlock", pageBlock);
		req.setAttribute("startPage", startPage);
		req.setAttribute("endPage", endPage);
		
		//필터 선택시
		if(!tourType.equals("0")) {
			List<TourDTO> list = tourMapper.listTourByType(tourType, startRow, endRow);
			session.setAttribute("tourList", list);
	        return "tour/tourList";
	        
	     //지도 눌러서 들어오면
		}else if(!region.equals("0")) { 
	         List<TourDTO> list = tourMapper.listTourByRegion(region);
	         session.setAttribute("tourList", list);
	         return "tour/tourList";
	    //여행지 메뉴로 들어오면
	    }else { 
	    	List<TourDTO> list = tourMapper.listTour(startRow, endRow);
	  		session.setAttribute("tourList", list);
	  		return "tour/tourList";
	    }
	}
	   
	@RequestMapping(value="/tourFind.do")
	public String tourFind(HttpServletRequest req, @RequestParam Map<String, String> map) {
		HttpSession session = req.getSession();
		String sql=null;
		int tour_type = Integer.parseInt(map.get("searchType"));
		String word= map.get("keyword");
		if(tour_type !=0) {
			if(map.get("keyword")==null) {
				sql = "select*from tour where tour_type='"+tour_type+"'";
			}else{
				sql ="select*from (select*from tour where tour_type='"+tour_type+"')A where lower(tour_name) like lower('%"+word+"%')";
			}
		}else {
			if(map.get("keyword")==null) {
				sql ="select*from tour";
			}else {
				sql = "select*from tour where lower(tour_name) like lower('%"+word+"%')";
				}
		}
		map.put("sql", sql);
		List<TourDTO> find = tourMapper.findTour(map);
			session.setAttribute("findList", find);
			session.setAttribute("searchType", map.get("searchType"));
		return "myroute/myRoute";
	}

}